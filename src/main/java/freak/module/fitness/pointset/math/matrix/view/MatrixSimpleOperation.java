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
public class MatrixSimpleOperation extends ExtendedView{
	
	public MatrixSimpleOperation(Matrix m) {
		super(new NormalView(m));
	}
	
	public MatrixSimpleOperation(ExtendedView pView) {
		super(pView.view);
	}
	
	public MatrixSimpleOperation(SimpleView pView) {
		super(pView);
	}
		
	/*
	 * 
	 * */
		
	
	public NormalView multMatrix(SimpleView pOther){
		if (this.view.getCol() != pOther.getRow()){
			throw new Error("Bad dimensions in multMatrix");
		}
		
		Matrix newMat = new Matrix(this.view.getRow(),pOther.getCol());
		
		NormalView nvNewMat = new NormalView(newMat);
						
		for (int r = 0; r < nvNewMat.getRow(); r++)
		{
			for (int c = 0; c < nvNewMat.getCol(); c++)
			{
				double temp_scalar = 0;
				try{
					for (int k = 0; k < this.view.getCol(); k++)
					{
						temp_scalar+=view.get(r, k) * pOther.get(k, c);
					}
					nvNewMat.set(r, c, temp_scalar);
				} catch(Exception e){throw new Error("Unsupported Error in multMatrix");}
			}
		}
		return nvNewMat;
	}
	
	/*public VectorView multMatrixV(SimpleView pOther){
		if (this.view.getCol() != pOther.getRow()){
			throw new Error("Bad dimensions in multMatrixV");
		}
		
		if ((this.view.getRow() != 1) || (pOther.getCol() != 1) ){
			throw new Error("Bad dimensions in multMatrixV");
		}
		
		Matrix newMat = new Matrix(this.view.getRow(),pOther.getCol());
		
		NormalView matrix = multMatrix()
		
		
		return nvNewMat;
	}*/
	
	public NormalView multMatrix(ExtendedView pOther){
		return multMatrix(pOther.view);
	}
	
	public void addMatrix(SimpleView pOther) throws Exception {
		if ((view.getCol() != pOther.getCol())||(view.getRow() != pOther.getRow())) {
			throw new Exception("Die Spalten oder Zeilen zahlen stimmen nicht ueberein");
		}
		if (view.sameModel(pOther)) {
			double [][] m = new double [view.getRow()][view.getCol()];  
			for (int r = 0; r < view.getRow(); r++) {
				for (int c = 0; c < view.getCol(); c++) {
					m[r][c] = view.get(r, c) + pOther.get(r, c);
				}
			}
			for (int r = 0; r < view.getRow(); r++) {
				for (int c = 0; c < view.getCol(); c++) {
					view.set(r, c, m[r][c]); 
				}
			}
		} else {
			for (int r = 0; r < view.getRow(); r++) {
				for (int c = 0; c < view.getCol(); c++) {
					view.set(r, c, view.get(r, c) + pOther.get(r, c));
				}
			}
		}
	}
	public void subMatrix(SimpleView pOther) throws Exception {
		if ((view.getCol() != pOther.getCol())||(view.getRow() != pOther.getRow())) {
			throw new Exception("Die Spalten oder Zeilen zahlen stimmen nicht ueberein");
		}
		if (view.sameModel(pOther)) {
			double [][] m = new double [view.getRow()][view.getCol()];  
			for (int r = 0; r < view.getRow(); r++) {
				for (int c = 0; c < view.getCol(); c++) {
					m[r][c] = view.get(r, c) - pOther.get(r, c);
				}
			}
			for (int r = 0; r < view.getRow(); r++) {
				for (int c = 0; c < view.getCol(); c++) {
					view.set(r, c, m[r][c]); 
				}
			}
		} else {
			for (int r = 0; r < view.getRow(); r++) {
				for (int c = 0; c < view.getCol(); c++) {
					view.set(r, c, view.get(r, c) - pOther.get(r, c));
				}
			}
		}
	}
	public void addMatrix(ExtendedView pOther)throws Exception {
		addMatrix(pOther.view);
	}
	public void subMatrix(ExtendedView pOther)throws Exception {
		subMatrix(pOther.view);
	}
	
	public Matrix addMatrixM(SimpleView pOther) throws Exception {
		if ((view.getCol() != pOther.getCol())||(view.getRow() != pOther.getRow())) {
			throw new Exception("Die Spalten oder Zeilen zahlen stimmen nicht ueberein");
		}
		
		Matrix m = view.copyModelFromView();
		
		MatrixSimpleOperation sop = new MatrixSimpleOperation(m);
		
		sop.addMatrix(pOther);
				
		return m;
	}
	public Matrix addMatrixM(ExtendedView pOther) throws Exception {
		return addMatrixM(pOther.view);
	}	
	public MatrixSimpleOperation addMatrixMV(SimpleView pOther) throws Exception {
		return new MatrixSimpleOperation(new NormalView(addMatrixM(pOther)));
	}
	public MatrixSimpleOperation addMatrixMV(ExtendedView pOther) throws Exception {
		return addMatrixMV(pOther.view);
	}
	
	
	public void swapRows(int rowIndex1, int rowIndex2) throws Exception {
		if ((!view.validRowIndex(rowIndex1))||(!view.validRowIndex(rowIndex2))){
			throw new Exception("Bad rowIndex");
		}
		for (int c = 0; c < view.getCol(); c++){
			double temp = view.get(rowIndex1, c);
			view.set(rowIndex1, c, view.get(rowIndex2,c));
			view.set(rowIndex2, c, temp);
		}
	}
	
	public void swapCols(int colIndex1, int colIndex2) throws Exception {
		if ((!view.validColIndex(colIndex1))||(!view.validColIndex(colIndex2))){
			throw new Exception("Bad colIndex");
		}
		for (int r = 0; r < view.getRow(); r++){
			double temp = view.get(r,colIndex1);
			view.set(r,colIndex1, view.get(r,colIndex2));
			view.set(r,colIndex2, temp);
		}
	}
	
}