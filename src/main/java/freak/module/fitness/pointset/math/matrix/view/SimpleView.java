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
abstract public class SimpleView {
	Matrix m_kMat;
	
	abstract public int getCol();
	abstract public int getRow();
	abstract public void set(int r , int c, double v)throws Exception;
	abstract public double get(int r, int c) throws Exception;
	
	public boolean sameModel(SimpleView pView) {
		return m_kMat == pView.m_kMat;
	}
	
	public Matrix getMatrix() {return m_kMat;}
	public void setMatrix(Matrix pMat) {m_kMat = pMat;}
	
	public Matrix copyModelFromView(){
		Matrix cpy = new Matrix(getRow(),getCol());
		NormalView nv = new NormalView(cpy);
		for (int r = 0; r < getRow(); r++){
			for (int c = 0; c < getCol(); c++ ){
				try{
					nv.set(r, c, get(r,c));
				} catch (Exception e){throw new Error("Unespected Error in SimpleView.copyModelFromView()");}
			}
		}
		return cpy;
	}
	
	public boolean validRowIndex(int rowIndex){
		return (-1 < rowIndex) && (rowIndex < getRow());
	}
	
	public boolean validColIndex(int colIndex){
		return (-1 < colIndex) && (colIndex < getCol());
	}
	
	public boolean isSquared(){
		return m_kMat.isSquared();
	}
	
	public String toString() {
		String result = "";
		try {
			for (int r = 0; r < getRow(); r++) {
				result += "(";
				for (int c = 0; c < getCol()-1; c++) {
					result+= get(r,c) + ", "; 
				}
				result += get(r,getCol()-1) + ")\n";
			}
		} catch (Exception e) {
			throw new Error("Ein unerwarteter Fehler in RowView to String");
		}
		return result;
	}
}