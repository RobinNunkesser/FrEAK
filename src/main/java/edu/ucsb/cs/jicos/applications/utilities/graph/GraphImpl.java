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
 *  An implementation of the Graph interface for an general graph.
 *
 * Created on August 1, 2003, 3:07 PM
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.utilities.graph;

import java.util.Random;


public class GraphImpl implements Graph 
{
    private int[][] costs;
    
    /** Constructs a random graph, using seed as the seed for the random number
     * generator. The graph has n vertices in the unit square, which are
     * magnified by magnification. For example, if the x coordinate of a vertex
     * is 0.76543 and the magnification is 1000, then the magnified x coordinate
     * is 765 and the unit square maps to a 1000 X 1000 int grid.
     * @param nodes Number of nodes in the graph.
     * @param maxEdgeWeight The maximum edge weight: edge weights are in the
     * interval [0, maxEdgeWeight).
     * @param seed The seed to use for the random number generator.
     */
    public GraphImpl( int nodes, int seed, int maxEdgeWeight )
    {
        Random random = new Random( seed );
        costs = new int[ nodes ][ nodes ];
        for ( int i = 0; i < nodes; i++ )
        for ( int j = 0; j < i; j++ )
        {
            // a pseudo-random, uniformly distributed int in [0, maxEdgeWeight)           
            costs[i][j] = random.nextInt( maxEdgeWeight );
            costs[j][i] = costs[i][j];
        }
    }
    
    /** Constructs the graph according to the argument cost matrix.
     * @param costs The cost matrix that is used to construct this graph.
     */
    public GraphImpl( int[][] costs ) { this.costs = costs; }
    
    public String costs2String()
    {
        StringBuffer s = new StringBuffer();
        s.append( "Graph costs: \n" );        
        for ( int i = 0; i < costs.length; i++)
        {
            s.append ( "Row " );
            s.append ( i );
            s.append ( ": " );
            for ( int j = 0; j < costs.length; j++ )
            {
                s.append ( " " );
                s.append ( costs[i][j] );
            }
            s.append ( "\n" );
        }
        return new String ( s );
    }
    
    /** Return the distance between nodes whose node numbers are the arguments.
     * @param i the 1st node number.
     * @param j the other node number.
     * @return The Euclidean distance between these nodes.
     */ 
    public int getCost( int i, int j ) { return costs[i][j]; }
    
    /** Returns a symmetric cost matrix. Entry [i,j] is the distance between node i and
     * node j.
     * @return Returns a symmetric cost matrix. Entry [i,j] is the distance between node i and
     * node j.
     */  
    public int[][] getCosts() { return costs; }
    
    /** Get the minimum cost maximum matching in the graph.
     * @return Returns an array of matched nodes. The ith entry is the node number of the node
     * that is matched to the node numbered i. Node numbers are 0, ..., n-1, where n
     * is the number of nodes in the graph.
     */ 
    public int[] getMinCostMaxMatch() 
    {
        WeightedMatch weightedMatch = new WeightedMatch( costs );
        int[] mates = weightedMatch.weightedMatch( WeightedMatch.MINIMIZE);
        
        /* WeightedMatch.weightedMatch returns mates, indexed and valued
         * 1, ..., V. Shift the index to 0, ... , V-1 and put the values in
         * this range too (i.e., decrement them). 
         */
        return weightedMatch.getMatched( mates );
    }
    
    /** Get the maximum cost maximum matching in the graph.
     * @return Returns an array of matched nodes. The ith entry is the node number of the node
     * that is matched to the node numbered i.
     */    
    public int[] getMaxCostMaxMatch() 
    {
        WeightedMatch weightedMatch = new WeightedMatch( costs );
        int[] mates = weightedMatch.weightedMatch( WeightedMatch.MAXIMIZE);
        
        /* WeightedMatch.weightedMatch returns mates, indexed and valued
         * 1, ..., V. Shift the index to 0, ... , V-1 and put the values in
         * this range too (i.e., decrement them). 
         */
        return weightedMatch.getMatched( mates );
    }
    
    /** Get the number of nodes in the graph.
     * @return the number of nodes in the graph.
     */
    public int size() { return costs.length; }  
    
    /** A string representation of the graph.
     * @return A string representation of the graph.
     */    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        for ( int i = 0; i < costs.length; i++ )
        {
            s.append( "\n row " );
            s.append( i );
            s.append( ": " );
            for ( int j = 0; j < costs.length; j++ )
            {
                s.append( "\t" );
                s.append( costs[i][j] );
            }
        }
        s.append( "\n" );
        return new String ( s );
    }
}
