package me.akuz.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.NIGDist;
import me.akuz.core.math.StatsUtils;
import Jama.Matrix;

import me.akuz.core.math.MatrixVar;
import me.akuz.core.math.MultDist;

public final class LatentHierarchyGibbs {

	private final Random _rnd;

	private final Matrix _data;

	private final MatrixVar  _vVar;
	private final int        _zSize;
	private final MultDist   _zDist;
	private final NIGDist[]  _zVDist;
	private final int[]      _zAlloc;
	private final double[]   _zCache;
	
	private final int           _wSize;
	private final MultDist      _wDist;
	private final MultDist[][]  _wwZDist;
	private final MultDist[]    _wLeftZDist;
	private final MultDist[]    _wRightZDist;
	private final int[]         _wAlloc;
	private final double[]      _wCache;

	public LatentHierarchyGibbs(
			Matrix data, 
			MatrixVar vVar,
			int zSize,
			double zAlpha,
			NIGDist zVDistPrior,
			int wSize,
			double wAlpha) {
		
		_rnd = ThreadLocalRandom.current();
		_data = data;

		// set v var
		_vVar = vVar;
		
		// init unconditional z dist with leg alpha
		_zSize = zSize;
		_zDist = new MultDist(zSize, zAlpha);

		// init z topics vars 
		_zVDist = new NIGDist[zSize];
		for (int z=0; z<_zVDist.length; z++) {
			_zVDist[z] = zVDistPrior.clone();
		}
		
		// init z topics allocations
		_zAlloc = new int[data.getRowDimension()];
		Arrays.fill(_zAlloc, -1);
		_zCache = new double[zSize];
		
		// init w topics distribution
		_wSize = wSize;
		_wDist = new MultDist(wSize, wAlpha);
		_wwZDist = new MultDist[wSize][];
		for (int w1=0; w1<wSize; w1++) {
			MultDist[] wZDist = new MultDist[wSize]; 
			for (int w2=0; w2<wSize; w2++) {
				wZDist[w2] = new MultDist(zSize, zAlpha);
			}
			_wwZDist[w1] = wZDist;
		}
		
		// init w topic legs distributions
		_wLeftZDist  = new MultDist[wSize];
		_wRightZDist = new MultDist[wSize];
		for (int w=0; w<wSize; w++) {
			_wLeftZDist[w]  = new MultDist(zSize, zAlpha);
			_wRightZDist[w] = new MultDist(zSize, zAlpha);
		}
		
		// init w topics allocations
		_wAlloc = new int[data.getRowDimension()];
		Arrays.fill(_wAlloc, -1);
		_wCache = new double[wSize];
	}
	
	public void iterate() {
		
		// iterate z
		for (int i=0; i<_data.getRowDimension(); i++) {
			
			// get underlying value
			double v = _data.get(i, _vVar.getColumnIndex());
			
			// calculate z topic log likelihoods
			for (int z=0; z<_zSize; z++) {
				
				double prob_v_given_z;
				{
					NIGDist dist_v_given_z = _zVDist[z];
					if (z == _zAlloc[i]) {
						dist_v_given_z.removeObservation(v);
						prob_v_given_z = dist_v_given_z.getProb(v);
						dist_v_given_z.addObservation(v);
					} else {
						prob_v_given_z = dist_v_given_z.getProb(v);
					}
				}
				
				MultDist dist_z_given_ww;
				{
					// check if right parent present
					if (i < _data.getRowDimension()-1) {
						
						if (_wAlloc[i] >= 0 && _wAlloc[i+1] >= 0) {
							// both parents set, use joint distribution
							dist_z_given_ww = _wwZDist[_wAlloc[i]][_wAlloc[i+1]];
						} else if (_wAlloc[i] >= 0) {
							// only left parent set, use its right leg
							dist_z_given_ww = _wRightZDist[_wAlloc[i]];
						} else if (_wAlloc[i+1] >= 0) {
							// only right parent set, use its left leg
							dist_z_given_ww = _wLeftZDist[_wAlloc[i+1]];
						} else {
							// no parents set, use unconditional
							dist_z_given_ww = _zDist;
						}
					} else { // right parent not present
						
						if (_wAlloc[i] >= 0) {
							// only left parent set, use its right leg
							dist_z_given_ww = _wRightZDist[_wAlloc[i]];
						} else {
							// no parents set, use unconditional
							dist_z_given_ww = _zDist;
						}
					}
				}
				double prob_z_given_ww;
				if (z == _zAlloc[i]) {
					dist_z_given_ww.removeObservation(_zAlloc[i]);
					prob_z_given_ww = dist_z_given_ww.getUnnormalizedProb(z);
					dist_z_given_ww.addObservation(_zAlloc[i]);
				} else {
					prob_z_given_ww = dist_z_given_ww.getUnnormalizedProb(z);
				}
				
				// set z log likelihood
				_zCache[z] 
						= Math.log(prob_v_given_z)
						+ Math.log(prob_z_given_ww);
			}
			
			// normalize z topic probs
			StatsUtils.logLikesToProbsInPlace(_zCache);

			// calculate CDF
			StatsUtils.calcCDFReplace(_zCache);
			
			// select next z topic by CDF
			int z = StatsUtils.nextDiscrete(_rnd, _zCache);
			
			// update statistics
			if (z != _zAlloc[i]) {
				
				// remove observations
				if (_zAlloc[i] >= 0) {
					_zVDist[_zAlloc[i]].removeObservation(v);
					if (i<_data.getRowDimension()-1) {
						if (_wAlloc[i] >= 0 && _wAlloc[i+1] >= 0) {
							_wwZDist[_wAlloc[i]][_wAlloc[i+1]].removeObservation(_zAlloc[i]);
						}
						if (_wAlloc[i+1] >= 0) {
							_wLeftZDist[_wAlloc[i+1]].removeObservation(_zAlloc[i]);
						}
					}
					if (_wAlloc[i] >= 0) {
						_wRightZDist[_wAlloc[i]].removeObservation(_zAlloc[i]);
					}
					_zDist.removeObservation(_zAlloc[i]);
				}
				
				// set new z
				_zAlloc[i] = z;
				
				// add observations
				_zVDist[_zAlloc[i]].addObservation(v);
				if (i<_data.getRowDimension()-1) {
					if (_wAlloc[i] >= 0 && _wAlloc[i+1] >= 0) {
						_wwZDist[_wAlloc[i]][_wAlloc[i+1]].addObservation(_zAlloc[i]);
					}
					if (_wAlloc[i+1] >= 0) {
						_wLeftZDist[_wAlloc[i+1]].addObservation(_zAlloc[i]);
					}
				}
				if (_wAlloc[i] >= 0) {
					_wRightZDist[_wAlloc[i]].addObservation(_zAlloc[i]);
				}
				_zDist.addObservation(_zAlloc[i]);
			}
		}
		
		// iterate w
		for (int i=0; i<_data.getRowDimension(); i++) {
			
			for (int w=0; w<_wSize; w++) {
				
				double logLike = 0;
				
				if (i > 0) {

					double zLeftProb;
					if (_wAlloc[i] >= 0) {
						MultDist leftZDist = _wLeftZDist[_wAlloc[i]];
						if (w == _wAlloc[i]) {
							leftZDist.removeObservation(_zAlloc[i-1]);
							zLeftProb = leftZDist.getUnnormalizedProb(_zAlloc[i-1]);
							leftZDist.addObservation(_zAlloc[i-1]);
						} else {
							zLeftProb = leftZDist.getUnnormalizedProb(_zAlloc[i-1]);
						}
					} else {
						zLeftProb = _zDist.getUnnormalizedProb(_zAlloc[i-1]);
					}
					logLike += Math.log(zLeftProb);
				}
				
				{ // always
					
					double zRightProb;
					if (_wAlloc[i] >= 0) {
						MultDist rightZDist = _wRightZDist[_wAlloc[i]];
						if (w == _wAlloc[i]) {
							rightZDist.removeObservation(_zAlloc[i]);
							zRightProb = rightZDist.getUnnormalizedProb(_zAlloc[i]);
							rightZDist.addObservation(_zAlloc[i]);
						} else {
							zRightProb = rightZDist.getUnnormalizedProb(_zAlloc[i]);
						}
					} else {
						zRightProb = _zDist.getUnnormalizedProb(_zAlloc[i]);
					}
					logLike += Math.log(zRightProb);
				}
				
				if (w == _wAlloc[i]) {
					_wDist.removeObservation(_wAlloc[i]);
					logLike += Math.log(_wDist.getUnnormalizedProb(w));
					_wDist.addObservation(_wAlloc[i]);
				} else {
					logLike += Math.log(_wDist.getUnnormalizedProb(w));
				}
				
				_wCache[w] = logLike;
			}
			
			// normalize w topic probs
			StatsUtils.logLikesToProbsInPlace(_wCache);

			// calculate CDF
			StatsUtils.calcCDFReplace(_wCache);
			
			// select next w topic by CDF
			int w = StatsUtils.nextDiscrete(_rnd, _wCache);
			
			// update statistics
			if (w != _wAlloc[i]) {
				
				// remove observations
				if (_wAlloc[i] >= 0) {
					if (i > 0 &&_wAlloc[i-1] >= 0) {
						_wwZDist[_wAlloc[i-1]][_wAlloc[i]].removeObservation(_zAlloc[i-1]);
					}
					if (i < _data.getRowDimension()-1 &&_wAlloc[i+1] >= 0) {
						_wwZDist[_wAlloc[i]][_wAlloc[i+1]].removeObservation(_zAlloc[i]);
					}
					if (i > 0) {
						_wLeftZDist[_wAlloc[i]].removeObservation(_zAlloc[i-1]);
					}
					_wRightZDist[_wAlloc[i]].removeObservation(_zAlloc[i]);
					_wDist.removeObservation(_wAlloc[i]);
				}
				
				// set new w
				_wAlloc[i] = w;
				
				// add observations
				if (i > 0 &&_wAlloc[i-1] >= 0) {
					_wwZDist[_wAlloc[i-1]][_wAlloc[i]].addObservation(_zAlloc[i-1]);
				}
				if (i < _data.getRowDimension()-1 &&_wAlloc[i+1] >= 0) {
					_wwZDist[_wAlloc[i]][_wAlloc[i+1]].addObservation(_zAlloc[i]);
				}
				if (i > 0) {
					_wLeftZDist[_wAlloc[i]].addObservation(_zAlloc[i-1]);
				}
				_wRightZDist[_wAlloc[i]].addObservation(_zAlloc[i]);
				_wDist.addObservation(_wAlloc[i]);
			}

		}
	}
	
	public MultDist[] getWLeftZDist() {
		return _wLeftZDist;
	}
	
	public MultDist[] getWRightZDist() {
		return _wRightZDist;
	}
	
	public NIGDist[] getZVDist() {
		return _zVDist;
	}
	
}
