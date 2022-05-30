/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;
///////////////////////////////////////////////////////////////////////Original

public class RData {
	
	private int[] values;
	private int[] dim;
	private String[] names;
	/**
	 * @param values
	 * @param dim
	 * @param names
	 */
	public RData(double[] values, int[] dim, String[] names) {
		super();
		int[] castValues = new int[values.length];
		for (int i=0;i<values.length;i++) castValues[i]=(int)values[i];
		this.values = castValues;
		this.dim = dim;
		this.names = names;
	}
	
	public RData(int[] values, int[] dim, String[] names) {
		super();
		this.values = values;
		this.dim = dim;
		this.names = names;
	}
	
	public RData(int[] values, int[] dim) {
		super(); 
		this.values = values;
		this.dim = dim;
		this.names  = new String [dim[1]];
		for(int i=0; i<dim[1]; i++ )
		this.names[i] = ""+i;
	}
	/**
	 * @return the dim
	 */
	public int[] getDim() {
		return dim;
	}
	/**
	 * @return the names
	 */
	public String[] getNames() {
		return names;
	}
	/**
	 * @return the values
	 */
	public int[] getValues() {
		return values;
	}
	
	
	
}
