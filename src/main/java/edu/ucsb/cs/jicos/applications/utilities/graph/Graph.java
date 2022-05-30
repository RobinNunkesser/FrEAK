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
 * The beginning of a simple graph interface.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.utilities.graph;


public interface Graph extends java.io.Serializable
{
    public final static int METRIC_GEO = 1;
    public final static int METRIC_EUC_2D = 2;

    public String costs2String();
    
    /** Get an edge's cost, distance, weight, or capacity.
     * @return The edge's cost, distance, weight, or capacity.
     * @param i One endpoint of an edge.
     * @param j One endpoint of an edge.
     */   
    public int getCost( int i, int j );
    
    /** Get the graph's cost or distance matrix.
     * @return the cost or distance matrix.
     */    
    public int[][] getCosts();
    
    /** Get a minimum cost maximum matching associated with this graph.
     * @return a minimum cost maximum matching associated with this graph.
     */    
    public int[] getMinCostMaxMatch();
    
    /** Get a maximum cost maximum matching associated with this graph.
     * @return a maximum cost maximum matching associated with this graph.
     */    
    public int[] getMaxCostMaxMatch();
    
    /** Get the size of this graph: The number of vertices.
     * @return number of vertices.
     */    
    public int size();
    
    /** Get a string representation of the graph.
     * @return a string representation of the graph.
     */    
    public String toString();
}
