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
public class NormalView extends SimpleView{
	public NormalView(Matrix pM) {
		m_kMat = pM;
	}
							
	public int getCol() {return m_kMat.col;};
	public int getRow() {return m_kMat.row;};
	public void set(int r , int c, double v) throws Exception{
		if (!((r < getRow()) && (c < getCol()) && (c > -1) && (r>-1)))
			throw new Exception("Es wurde auf ein Index zugegriffen der nicht existiert");
		m_kMat.m[r*getCol() + c] = v;
	}
	
	public double get(int r, int c) throws Exception{
		if (!((r < getRow()) && (c < getCol()) && (c > -1) && (r>-1)))
			throw new Exception("Es wurde auf ein Index zugegriffen der nicht existiert");
		return m_kMat.m[r*getCol() + c];
	}
}
