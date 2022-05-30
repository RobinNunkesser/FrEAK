/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math.matrix.view;

import freak.module.fitness.pointset.math.matrix.Matrix;

/**
 * @author Basti LT
 *
 */
public class ExtendedView {
	protected SimpleView view;
	
	public ExtendedView(SimpleView pView) {
		view = pView;
	}
	
	public void setView (SimpleView pView) {
		view = pView;
	}
	public SimpleView getView () {
		return view;
	}
	public Matrix getMatrix() {return view.getMatrix();}
	public void setMatrix(Matrix pMat) {view.setMatrix(pMat);}
	
	public String toString(){
		return view.toString();
	}
} 