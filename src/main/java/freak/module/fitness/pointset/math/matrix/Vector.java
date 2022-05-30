/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math.matrix;

import freak.module.fitness.pointset.math.matrix.view.SimpleView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;

/**
 * @author Basti LT
 *
 */
public class Vector extends Matrix {
	public Vector(int d) {
		super(d,1);
	}
	
	public Vector(SimpleView sv){
		super(sv.getRow(),sv.getCol()); 
		if (sv.getCol() != 1) throw new Error("Unsupported Vector");
		VectorView vv = new VectorView(this);	
		try{
			for (int i = 0; i < sv.getRow(); i++){
				vv.set(i, sv.get(i, 0));
			}
		}catch(Exception e){
			throw new Error(e+"Unsupported Vector");
		}
	}
	
	public int getDimension() {
		return row;
	}
	
	public boolean resize(int row, int col){
		if (col != 1) return false;
		return super.resize(row, col);
	}
}
