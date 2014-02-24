package me.akuz.qf;

import java.util.List;

import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.sort.SelectK;
import Jama.Matrix;

/**
 * Factor model EM inference, with genetic selection.
 *
 */
public final class FactorGeneticEM {
	
	private final FactorEM _best;
	
	public FactorGeneticEM(
			final Matrix mX, 
			final int factorCount,
			final int firstCandidateCount,
			final int firstIterationCount,
			final int secondCandidateCount,
			final int secondIterationCount,
			final int finalIterationCount) {
		
		this(
			mX,
			0,
			mX.getRowDimension(),
			factorCount,
			firstCandidateCount,
			firstIterationCount,
			secondCandidateCount,
			secondIterationCount,
			finalIterationCount);
	}
	
	public FactorGeneticEM(
			final Matrix mX, 
			final int startRow, 
			final int endRow, 
			final int factorCount,
			final int firstCandidateCount,
			final int firstIterationCount,
			final int secondCandidateCount,
			final int secondIterationCount,
			final int finalIterationCount) {
		
		SelectK<FactorEM, Double> selectSecond = new SelectK<>(SortOrder.Desc, secondCandidateCount);
		
		for (int i=0; i<firstCandidateCount; i++) {
			
			FactorEM secondCandidate = new FactorEM(mX, startRow, endRow, factorCount);
			secondCandidate.execute(firstIterationCount);
			
			selectSecond.add(new Pair<FactorEM, Double>(secondCandidate, secondCandidate.getLogLike()));
		}
		List<Pair<FactorEM, Double>> secondList = selectSecond.get();

		SelectK<FactorEM, Double> selectFinal = new SelectK<>(SortOrder.Desc, 1);
		for (int i=0; i<secondList.size(); i++) {
			
			FactorEM finalCandidate = secondList.get(i).v1();
			finalCandidate.execute(secondIterationCount);
			
			selectFinal.add(new Pair<FactorEM, Double>(finalCandidate, finalCandidate.getLogLike()));
		}
		
		FactorEM best = selectFinal.get().get(0).v1();
		best.execute(finalIterationCount);
		
		_best = best;
	}
	
	public FactorEM getBest() {
		return _best;
	}

}
