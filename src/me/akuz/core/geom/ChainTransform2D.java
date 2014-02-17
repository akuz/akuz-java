package me.akuz.core.geom;

import java.util.ArrayList;
import java.util.List;

public final class ChainTransform2D implements Transform2D {

	private final List<Transform2D> _list;
	
	public ChainTransform2D() {
		_list = new ArrayList<>();
	}
	
	public void addTranform(Transform2D transform) {
		_list.add(transform);
	}

	@Override
	public void toTarget(Point2D point) {
		for (int i=0; i<_list.size(); i++) {
			_list.get(i).toTarget(point);
		}
	}

	@Override
	public void toSource(Point2D point) {
		for (int i=_list.size()-1; i>=0; i--) {
			_list.get(i).toSource(point);
		}
	}
	
}
