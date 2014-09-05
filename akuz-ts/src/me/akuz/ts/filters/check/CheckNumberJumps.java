package me.akuz.ts.filters.check;

import java.util.List;

import me.akuz.ts.CurrTime;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqCursor;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;
import me.akuz.ts.log.TLogLevel;

public class CheckNumberJumps<T extends Comparable<T>> extends Filter<T> {
	
	private final String _tag;
	private final double _infoJump;
	private final double _warningJump;
	private final double _errorJump;
	private final boolean _ignore0;
	private TItem<T> _lastItem;
	private T _currTime;
	
	public CheckNumberJumps(
			final String tag,
			final double infoJump,
			final double warningJump,
			final double errorJump,
			final boolean ignore0) {
		
		if (infoJump <= 0.0) {
			throw new IllegalArgumentException("Argument infoJump must be positive");
		}
		if (warningJump <= 0.0) {
			throw new IllegalArgumentException("Argument warningJump must be positive");
		}
		if (errorJump <= 0.0) {
			throw new IllegalArgumentException("Argument errorJump must be positive");
		}
		
		_tag = tag;
		_infoJump = infoJump;
		_warningJump = warningJump;
		_errorJump = errorJump;
		_ignore0 = ignore0;
		_lastItem = null;
	}
	
	@Override
	public TItem<T> getCurrItem() {
		
		CurrTime.checkSet(_currTime);

		// we are only checking for
		// jumps, but we don't
		// derive any state
		return null;
	}
	
	@Override
	public List<TItem<T>> getMovedItems() {

		CurrTime.checkSet(_currTime);
		
		// we are only checking for
		// jumps, but we don't
		// derive any state
		return null;
	}

	@Override
	public void next(
			final T time,
			final SeqCursor<T> cursor, 
			final TLog<T> log) {

		CurrTime.checkNew(_currTime, time);

		if (log == null) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " filter requires a log");
		}
		
		final List<TItem<T>> movedItems = cursor.getMovedItems();
		for (int i=0; i<movedItems.size(); i++) {
			
			final TItem<T> prevItem = _lastItem;
			_lastItem = movedItems.get(i);
			
			if (prevItem != null) {
				checkNumberJump(log, prevItem, _lastItem);
			}
		}
		
		_currTime = time;
	}
	
	private final void checkNumberJump(
			final TLog<T> log,
			final TItem<T> prevItem,
			final TItem<T> currItem) {
		
		double prevNumber = prevItem.getNumber().doubleValue();
		double currNumber = _lastItem.getNumber().doubleValue();
		
		if (Math.abs(prevNumber) < Double.MIN_NORMAL && _ignore0) {
			return;
		}
		if (Math.abs(currNumber) < Double.MIN_NORMAL && _ignore0) {
			return;
		}
		
		final double distance = Math.abs(currNumber - prevNumber);
		if (distance > Double.MIN_NORMAL) {
			
			final double absPrevNumber = Math.abs(prevNumber);
			final double absCurrNumber = Math.abs(currNumber);
			
			final double norm = Math.min(absPrevNumber, absCurrNumber);
			final double jump = distance / norm;

			if (!Double.isNaN(_errorJump) && jump > _errorJump) {
				
				log.add(currItem.getTime(),
						TLogLevel.Error, "[" + _tag + "] Jump in \"" + getFieldName() 
						+ "\" field value: " + prevItem + " >> " + _lastItem);
				
			} else if (!Double.isNaN(_warningJump) && jump > _warningJump) {
				
				log.add(currItem.getTime(),
						TLogLevel.Warning, "[" + _tag + "] Jump in \"" + getFieldName() + 
						"\" field value: " + prevItem + " >> " + _lastItem);
				
			} else if (!Double.isNaN(_infoJump) && jump > _infoJump) {
				
				log.add(currItem.getTime(),
						TLogLevel.Info, "[" + _tag + "] Jump in \"" + getFieldName() + 
						"\" field value: " + prevItem + " >> " + _lastItem);
			}
		}
	}

}
