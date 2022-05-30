/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

/**
 * Immutable
 *
 * Created on August 3, 2003, 11:19 AM
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.utilities.graph;


public final class Edge implements Comparable
{
    private int u;
    private int v;
    private int label;
    
    public Edge( int u, int v, int label ) 
    {
        this.u = u;
        this.v = v;
        this.label = label;
    }
    
    public int compareTo( Object object ) 
    {
        if ( ! ( object instanceof Edge ) )
        {
            throw new IllegalArgumentException();
        }
        Edge edge = (Edge) object;
        if ( label < edge.label )
        {
            return -1;
        }
        else if ( label > edge.label )
        {
            return +1;
        }
        else
        {
            return 0;
        }
    }
    
    public int getU() { return u; }
    public int getV() { return v; }
    public int getLabel() { return label; }
    
    public String toString() { return "" + u + " " + v + " label: " + label; }
    
}
