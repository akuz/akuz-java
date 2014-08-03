package me.akuz.qf;

import me.akuz.core.math.SampleVariance;

import Jama.Matrix;

public final class Accounting {
	
	private static final int LOG_PRICE_COLUMN = 0;
	private static final int LOG_VALUE_COLUMN = 1;
	private static final int VALUE_COLUMN = 2;
	private static final int CASH_COLUMN = 3;
	private static final int POSITION_COLUMN = 4;
	private static final int COL_COUNT = 5;
	
	private final Matrix _mLogPrice;
	private final Matrix _mAccounting;
	private boolean _isInitialized;
	
	private final SampleVariance _instrumentReturnVariance;
	private final SampleVariance _strategyReturnVariance;
	
	public Accounting(Matrix mLogPrice) {
		_mLogPrice = mLogPrice;
		_mAccounting = new Matrix(mLogPrice.getRowDimension(), COL_COUNT, Double.NaN);
		_instrumentReturnVariance = new SampleVariance();
		_strategyReturnVariance = new SampleVariance();
	}
	
	public boolean isInitialized() {
		return _isInitialized;
	}
	
	public void init(int index, double cash) {
		double value = cash;
		double logValue = Math.log(value);
		_mAccounting.set(index, LOG_PRICE_COLUMN, _mLogPrice.get(index, 0));
		_mAccounting.set(index, LOG_VALUE_COLUMN, logValue);
		_mAccounting.set(index, VALUE_COLUMN, value);
		_mAccounting.set(index, CASH_COLUMN, cash);
		_mAccounting.set(index, POSITION_COLUMN, 0);
		_isInitialized = true;
	}
	
	public void next(int index) {
		
		final double currLogPrice = _mLogPrice.get(index, 0);
		final double currPrice = Math.exp(currLogPrice);
		final double prevLogValue = _mAccounting.get(index-1, LOG_VALUE_COLUMN);
		final double prevPosition = _mAccounting.get(index-1, POSITION_COLUMN);
		final double prevCash = _mAccounting.get(index-1, CASH_COLUMN);

		final double currValue = prevCash + prevPosition * currPrice;
		final double currLogValue = Math.log(currValue);
		final double currPosition = prevPosition;
		final double currCash = prevCash;
		
		_mAccounting.set(index, LOG_PRICE_COLUMN, currLogPrice);
		_mAccounting.set(index, LOG_VALUE_COLUMN, currLogValue);
		_mAccounting.set(index, VALUE_COLUMN, currValue);
		_mAccounting.set(index, CASH_COLUMN, currCash);
		_mAccounting.set(index, POSITION_COLUMN, currPosition);
		
		final double prevLogPrice = _mLogPrice.get(index-1, 0);
		final double instrumentReturn = currLogPrice - prevLogPrice;
		_instrumentReturnVariance.add(instrumentReturn);
		
		final double strategyReturn = currLogValue - prevLogValue;
		_strategyReturnVariance.add(strategyReturn);
	}
	
	public void tradeToGetDesiredExposure(int index, double desiredExposure) {
		
		final double currLogPrice = _mLogPrice.get(index, 0);
		final double currPrice = Math.exp(currLogPrice);
		final double beforeValue = _mAccounting.get(index, VALUE_COLUMN);
		final double beforePosition = _mAccounting.get(index, POSITION_COLUMN);
		final double beforeCash = _mAccounting.get(index, CASH_COLUMN);
		
		double desiredValue = beforeValue * desiredExposure;
		double desiredPosition = desiredValue / currPrice;
		
		final double deltaCash = (desiredPosition - beforePosition) * currPrice;
		final double afterCash = beforeCash - deltaCash;
		final double afterPosition = desiredPosition;
		final double afterValue = afterCash + afterPosition*currPrice;
		final double afterLogValue = Math.log(afterValue);
		
		_mAccounting.set(index, LOG_PRICE_COLUMN, currLogPrice);
		_mAccounting.set(index, LOG_VALUE_COLUMN, afterLogValue);
		_mAccounting.set(index, VALUE_COLUMN, afterValue);
		_mAccounting.set(index, CASH_COLUMN, afterCash);
		_mAccounting.set(index, POSITION_COLUMN, afterPosition);
	}
	
	public Matrix getMatrix() {
		return _mAccounting;
	}
	
	public double getInstrumentAnnualizedSharpe() {
		return Math.sqrt(250) * _instrumentReturnVariance.getMean() / Math.sqrt(_instrumentReturnVariance.getVariance());
	}
	
	public double getStrategyAnnualizedSharpe() {
		return Math.sqrt(250) * _strategyReturnVariance.getMean() / Math.sqrt(_strategyReturnVariance.getVariance());
	}

}
