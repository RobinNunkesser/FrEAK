/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import freak.Freak;

///////////////////////////////////////////////////////////////////////Original

public class RDoubleMatrix {

    private double[] values;
    private int[] dim;
    private String[] names;

    /**
     * @param values
     * @param dim
     * @param names
     */
    public RDoubleMatrix(double[] values, int[] dim, String[] names) {
        super();
        this.values = values;
        this.dim = dim;
        this.names = names;
    }

    public RDoubleMatrix(double[] values, int[] dim) {
        super();
        this.values = values;
        this.dim = dim;
        this.names = new String[dim[1]];
        for (int i = 0; i < dim[1]; i++)
            this.names[i] = "" + i;
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
    public double[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Values:\n");
        for (int i = 0; i < values.length; i++)
            sb.append(values[i] + ",");
        sb.append("\nDim:\n");
        for (int i = 0; i < dim.length; i++)
            sb.append(dim[i] + ",");
        sb.append("\nNames:\n");
        for (int i = 0; i < names.length; i++)
            sb.append(names[i] + ",");
        return sb.toString();
    }

}
