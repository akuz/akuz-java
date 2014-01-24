package me.akuz.core.test.sort;

import java.util.List;

import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.sort.SelectK;

import org.junit.Test;

public final class SelectKTests {

	@Test
	public void testSelectK_Highest() {
		
		SelectK<Integer, Double> selectK = new SelectK<>(SortOrder.Desc, 3);
		selectK.add(new Pair<Integer, Double>(0, .6));
		selectK.add(new Pair<Integer, Double>(1, .7));
		selectK.add(new Pair<Integer, Double>(2, .5));
		selectK.add(new Pair<Integer, Double>(3, .4));
		selectK.add(new Pair<Integer, Double>(4, .3));
		selectK.add(new Pair<Integer, Double>(5, .9));
		selectK.add(new Pair<Integer, Double>(6, .1));
		selectK.add(new Pair<Integer, Double>(7, .8));
		selectK.add(new Pair<Integer, Double>(8, .2));
		selectK.add(new Pair<Integer, Double>(9, .0));
		
		List<Pair<Integer, Double>> list = selectK.get();
		if (list.size() != 3) {
			throw new IllegalStateException("Invalid SelectK");
		}
		if (list.get(0).v1().equals(5) == false) {
			throw new IllegalStateException("Invalid SelectK");
		}
		if (list.get(1).v1().equals(7) == false) {
			throw new IllegalStateException("Invalid SelectK");
		}
		if (list.get(2).v1().equals(1) == false) {
			throw new IllegalStateException("Invalid SelectK");
		}
	}
	
	@Test
	public void testSelectK_Lowest() {
		
		SelectK<Integer, Double> selectK = new SelectK<>(SortOrder.Asc, 3);
		selectK.add(new Pair<Integer, Double>(0, .6));
		selectK.add(new Pair<Integer, Double>(1, .7));
		selectK.add(new Pair<Integer, Double>(2, .5));
		selectK.add(new Pair<Integer, Double>(3, .4));
		selectK.add(new Pair<Integer, Double>(4, .3));
		selectK.add(new Pair<Integer, Double>(5, .9));
		selectK.add(new Pair<Integer, Double>(6, .1));
		selectK.add(new Pair<Integer, Double>(7, .8));
		selectK.add(new Pair<Integer, Double>(8, .2));
		selectK.add(new Pair<Integer, Double>(9, .0));
		
		List<Pair<Integer, Double>> list = selectK.get();
		if (list.size() != 3) {
			throw new IllegalStateException("Invalid SelectK");
		}
		if (list.get(0).v1().equals(9) == false) {
			throw new IllegalStateException("Invalid SelectK");
		}
		if (list.get(1).v1().equals(6) == false) {
			throw new IllegalStateException("Invalid SelectK");
		}
		if (list.get(2).v1().equals(8) == false) {
			throw new IllegalStateException("Invalid SelectK");
		}
	}
}
