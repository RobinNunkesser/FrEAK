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
public class ColView extends ExtendedView{
		
	public ColView(SimpleView pView) {
		super(pView);
	}
	public void setCol(int c , Vector col) throws Exception {
		VectorView vV = new VectorView(col);
		if (vV.getDimension() != view.getRow())
			throw new Error("Die Zeilenanzahl stimmt nicht mit der Anzahl der Spalten der Matrix überein");
		for (int r = 0; r < view.getRow(); r++)
			view.set(r, c, vV.get(r));
	}
	
	public Vector getCol(int c){
		Vector col = new Vector(view.getRow());
		VectorView vV = new VectorView(col); 
		try {
		for (int r = 0; r < view.getRow(); r++)
			vV.set(r, view.get(r,c));
		} catch(Exception e) {
			
		}
		return col;
	}
	
	public String toString() {
		String result = "";
		try {
			for (int c = 0; c < view.getCol(); c++) {
				result += "Col " + c + ":(";
				for (int r = 0; r < view.getRow()-1; r++) {
					result+= view.get(r,c) + ", "; 
				}
				result += view.get(view.getRow()-1,c) + ")\n";
			}
		} catch (Exception e) {
			throw new Error("Ein unerwarteter Fehler in RowView to String");
		}
		return result;
	}
}