package me.akuz.fb.loc;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.math.NIGDist;
import me.akuz.core.sort.SelectK;

public final class Tree {
	
	private Iterable<Place> _places;
	
	private double _x1;
	private double _x2;
	private double _y1;
	private double _y2;
	
	private Tree _child1;
	private Tree _child2;
	private Tree _child3;
	private Tree _child4;
	
	private static final double distance(
			double fromX,
			double fromY,
			double toX,
			double toY) {
		
		return Math.sqrt(Math.pow(fromX - toX, 2) + Math.pow(fromY - toY, 2));
	}
	
	public List<Pair<Place, Double>> findTop(double x, double y, double accuracy, int k) {
		
		if (_places != null) {
			SelectK<Place, Double> top = new SelectK<>(SortOrder.Desc, k);
			for (Place place : _places) {
				top.add(place, place.logLike(x, y, accuracy));
			}
			return top.get();
		} else {
			SelectK<Tree, Double> top = new SelectK<>(SortOrder.Asc, 1);
			top.add(_child1, distance(x, _x1, y, _y1));
			top.add(_child2, distance(x, _x2, y, _y1));
			top.add(_child3, distance(x, _x1, y, _y2));
			top.add(_child4, distance(x, _x2, y, _y2));
			return top.get().get(0).v1().findTop(x, y, accuracy, k);
		}
	}
	
	public Tree(Iterable<Place> places, int size) {
		
		System.out.println(size);
		
		if (size <= 400) {
			
			_places = places;
			
		} else {
			
			NIGDist xDist = new NIGDist(0.0, 1.0, 1.0, 1.0);
			NIGDist yDist = new NIGDist(0.0, 1.0, 1.0, 1.0);
			for (Place place : places) {
				xDist.addObservation(place.getXDist().getMeanMode());
				yDist.addObservation(place.getYDist().getMeanMode());
			}
			
			_x1 = xDist.getMeanMode() - Math.sqrt(xDist.getVarianceMode());
			_x2 = xDist.getMeanMode() + Math.sqrt(xDist.getVarianceMode());
			
			_y1 = yDist.getMeanMode() - Math.sqrt(yDist.getVarianceMode());
			_y2 = yDist.getMeanMode() + Math.sqrt(yDist.getVarianceMode());
			
			SelectK<Place, Double> top1 = new SelectK<Place, Double>(SortOrder.Asc, size/2);
			SelectK<Place, Double> top2 = new SelectK<Place, Double>(SortOrder.Asc, size/2);
			SelectK<Place, Double> top3 = new SelectK<Place, Double>(SortOrder.Asc, size/2);
			SelectK<Place, Double> top4 = new SelectK<Place, Double>(SortOrder.Asc, size/2);
			
			for (Place place : places) {
				
				final double x = place.getXDist().getMeanMode();
				final double y = place.getYDist().getMeanMode();
				
				top1.add(place, distance(_x1, x, _y1, y));
				top2.add(place, distance(_x2, x, _y1, y));
				top3.add(place, distance(_x1, x, _y2, y));
				top4.add(place, distance(_x2, x, _y2, y));
			}
			
			List<Place> subPlaces;
			
			subPlaces = new ArrayList<>();
			for (Pair<Place, Double> pair : top1.get()) {
				subPlaces.add(pair.v1());
			}
			_child1 = new Tree(subPlaces, subPlaces.size());
			
			subPlaces = new ArrayList<>();
			for (Pair<Place, Double> pair : top2.get()) {
				subPlaces.add(pair.v1());
			}
			_child2 = new Tree(subPlaces, subPlaces.size());
			
			subPlaces = new ArrayList<>();
			for (Pair<Place, Double> pair : top3.get()) {
				subPlaces.add(pair.v1());
			}
			_child3 = new Tree(subPlaces, subPlaces.size());
			
			subPlaces = new ArrayList<>();
			for (Pair<Place, Double> pair : top4.get()) {
				subPlaces.add(pair.v1());
			}
			_child4 = new Tree(subPlaces, subPlaces.size());
		}
	}

}
