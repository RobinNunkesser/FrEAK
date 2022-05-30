/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math.matrix.view;

import freak.module.fitness.pointset.math.matrix.Matrix;
import freak.module.fitness.pointset.math.matrix.Vector;

/**
 * @author Basti LT
 *
 */
public class VectorView extends ExtendedView{
	
	public VectorView(Vector pVec) {
		super(new NormalView(pVec));
	}
	
	public VectorView(SimpleView pView) {
		super(pView);
		if (view.getMatrix().col != 1)
			setView(null);
	}
	
	public void setMatrix(Matrix pMat) {
		if (pMat.col != 1) return;
		view.setMatrix(pMat);
	}
	
	
	public void set(int k, double v) throws Exception{
		view.set(k, 0, v);
	}
	public double get(int k) throws Exception{
		return view.get(k, 0);
	}
	
	public int getDimension() {
		return view.getRow();
	}
}
