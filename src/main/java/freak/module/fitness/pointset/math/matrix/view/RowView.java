/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math.matrix.view;

import freak.module.fitness.pointset.math.matrix.Vector;

/**
 * @author Basti LT
 *
 */
public class RowView extends ExtendedView{
	
	public RowView(SimpleView pView) {
		super(pView);
	}
	
	public void setRow(int r , Vector row) throws Exception {
		VectorView vV = new VectorView(row); 
		if (vV.getDimension() != view.getCol())
			throw new Error("Die Spaltenanzahl stimmt nicht mit der Anzahl der Spalten der Matrix überein");
		for (int c = 0; c < view.getCol(); c++)
			view.set(r, c, vV.get(c));
	}
	
	public Vector getRow(int r){
		Vector row = new Vector(view.getCol());
		VectorView vV = new VectorView(row);
		
		try {
		for (int c = 0; c < view.getCol(); c++)
			vV.set(c,view.get(r,c));
		} catch(Exception e) {
			throw new Error(e);
		}
		return row;
	}
	
	public String toString() {
		String result = "";
		try {
			for (int r = 0; r < view.getRow(); r++) {
				result += "Row " + r + ":(";
				for (int c = 0; c < view.getCol()-1; c++) {
					result+= view.get(r,c) + ", "; 
				}
				result += view.get(r,view.getCol()-1) + ")\n";
			}
		} catch (Exception e) {
			throw new Error("Ein unerwarteter Fehler in RowView to String");
		}
		return result;
	}
	
}
