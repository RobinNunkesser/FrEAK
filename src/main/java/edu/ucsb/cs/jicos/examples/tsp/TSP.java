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
 * @version 1.0
 * @author  Peter Cappello
 */

/*
 * TSP.java
 */

package edu.ucsb.cs.jicos.examples.tsp;

import edu.ucsb.cs.jicos.applications.utilities.graph.*;
import java.util.Random;


public class TSP implements java.io.Serializable 
{
    /*
     * I think it is faster overall to have a complete matrix, even in the 
     * symmetric case: I believe that it takes more time to compute the min & 
     * max of computing a distance element index (e.g., 
     * distance[min(i,j)][max(i,j)] ), which needs to be done many many times, 
     * than it does to double the time to serialize/send the distance matrix 
     * to all hosts initially.
     */
    protected int[][] distance;
    
    public final static int MAX_EDGE = 10;
    
    public TSP ( Graph graph )
    {
    	    if( null != graph )
    	    {
    			distance = graph.getCosts();
    	    }
    	    else
    	    {
    	    	    distance = null;
    	    }
	}
    
    public TSP( int nodes, long seed, int maxEdgeWeight ) 
    {
        Random random = new Random( seed );
        distance = new int[ nodes ][ nodes ];
        for ( int i = 0; i < nodes; i++ )
        {
            System.out.print(" Row " + i + ": ");
            for ( int j = 0; j < i; j++ )
            {
                /* make a pseudo-random, uniformly distributed distance in 
                 * [0, maxEdgeWeight)
                 */                
                distance[i][j] = (int) ( maxEdgeWeight * random.nextFloat() );
                distance[j][i] = distance[i][j];
                System.out.print(" " + distance[i][j]);
            }
            System.out.println(" ");
        }
    }
    
    public int[][]  getDistance()
    {
    		return( (int[][])distance.clone() );
    }
}
