/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math.matrix;


import freak.module.fitness.pointset.math.matrix.view.ColView;
import freak.module.fitness.pointset.math.matrix.view.MatrixSimpleOperation;
import freak.module.fitness.pointset.math.matrix.view.NormalView;
import freak.module.fitness.pointset.math.matrix.view.RowView;
import freak.module.fitness.pointset.math.matrix.view.TransposeView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;

/**
 * @author Basti LT
 *
 */
public class Matrix {
	
	public double m[]; 
	
	public int col;
	public int row;
	
	public Matrix(int square_d){
		if (!resize(square_d, square_d))
			resize(0, 0);
	}
	
	public Matrix(int row, int col) {
		if (!resize(row, col))
			resize(0,0);
	}
	
	
	public boolean resize(int row, int col){
		if ((row < 0)||(col < 0)) {
			System.out.println("Bad row or col index in Matrix.resize(row,col)");
			return false;
		}
		this.col = col;
		this.row = row;
		m = new double [col * row];
		return true;
	}
	
	public boolean isSquared() {
		return col == row;
	}
	
	public static void main (String [] args) {
		Matrix m = new Matrix(3,3);
		
		NormalView nV = new NormalView(m);
		
		TransposeView tV = new TransposeView(m);
		
		try {
			nV.set(0, 0, 4);
			nV.set(1, 0, 2);
			nV.set(2, 0, 1);
			nV.set(0, 1, 5);
			nV.set(1, 1, 3);
			nV.set(2, 1, 2);
			nV.set(0, 2, 6);
			nV.set(1, 2, 4);
			nV.set(2, 2, 3);
			
			RowView cV = new RowView(nV);
			
			System.out.println(cV.toString());
			
			
			
			MatrixSimpleOperation mV = new MatrixSimpleOperation(nV);
			
			MatrixSimpleOperation mVT = new MatrixSimpleOperation(tV);
			
			Matrix result = mV.addMatrixM(tV);
			
			cV.setMatrix(result);
			
			System.out.println(cV.toString());
			
			Vector row2 = cV.getRow(1);
			
			VectorView vV = new VectorView(new TransposeView(row2));
			System.out.println(vV.toString());
			
		} catch(Exception e) {
			
		}
			
	} 
	
	
}


