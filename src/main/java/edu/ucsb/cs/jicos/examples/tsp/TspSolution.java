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
 * @version 2.0
 * @author  Peter Cappello
 */

/*
 * TspSolution.java
 */

package edu.ucsb.cs.jicos.examples.tsp;

import edu.ucsb.cs.jicos.applications.branchandbound.*;
import edu.ucsb.cs.jicos.services.*;


public class TspSolution implements edu.ucsb.cs.jicos.applications.branchandbound.Solution
{
    /* COMPLETE_THRESHOLD is used by isComplete.
     * When there is only 1 city not on the tour, add it:
     * 1) add the edge from the last city currently on the tour to this last city; 
     * 2) add this last city to tour; 
     * 3) add the edge from this last city to the 1st city
     */
    final static int COMPLETE_THRESHOLD = 1;
    final static int ATOMIC = 11; // height of subtree searched by host
    
    private int[] nodes;
    private int length;     // length of partial Hamilton path
    private int lowerBound;
    
    /* These attributes pertain to the symmetric TSP: Removal of equivalent
     * Hamilton circuits: those that are the same modulo traversal order
     * E.g., 012340 is equivalent to 043210.
     *
     * Equivalent circuits are eliminated by ensuring that 2 particular nodes
     * are visited in the same order: node 1 is visited before node 2.
     *
     * At some point, I will expose the option of being asymmetric via a 
     * constructor interface.
     */
    private boolean symmetric = true;    
    /* true: it is not yet known whether city 1 is visited before city 2
     * I.e., city 1 is not yet in the partial circuit.
     */
    private boolean equivalenceUnknown = true;
    
    TspSolution( TspSolution parent, int i ) 
    {
        nodes = new int[ parent.nodes.length ];
        System.arraycopy( parent.nodes, 0, nodes, 0, parent.nodes.length );
        length = parent.length;
        lowerBound = parent.lowerBound;
        equivalenceUnknown = parent.equivalenceUnknown;
        
        // is the city to be added city 1?
        if ( equivalenceUnknown && nodes[i] == 1 )
        {
            // In extensions of this partial path, city 1 is visited before city 2.
            equivalenceUnknown = false;
        }

        // put new node into partial path; extend path length
        int temp = nodes[length];        
        nodes[length++] = nodes[i];
        nodes[i] = temp;             
    }
    
    /** Constructor of root solution: root of search tree */
    public TspSolution( int n )
    {
        nodes = new int[n];
        for ( int i = 0; i < n; i++ )
        {
            nodes[i] = i;
        }
        length = 1; // partial path includes just node 0
    }
    
    /** Constructor of root solution: root of search tree for Euclidean graph */
    public TspSolution( int[] nodes )
    {
        this.nodes = nodes;
        length = 1; // partial path includes just node 0
    }
    
    public void computeLowerBound( Object input )
    {
        // add cost of edge between old last node and new last node
        lowerBound += ( (TSP) input).distance[ nodes[ length - 2 ] ][ nodes[length - 1] ];
        if ( isComplete() )
        {
            length++; // toString uses length
            // add cost of edge from new last node to nodes.length
            lowerBound += ( (TSP) input).distance[ nodes[ nodes.length - 2 ] ][ nodes[ nodes.length - 1 ] ];
            
            // add cost of edge from nodes.length to 0: complete cycle.
            lowerBound += ( (TSP) input).distance[ nodes[ nodes.length - 1 ] ][ 0 ];
        }
    }
    
    public Q getChildren( Environment environment )
    {
        Object input = environment.getInput(); 
        Shared shared = environment.getShared(); 
        Q q = new Q(); // queue of children     
        for ( int i = length; i < nodes.length; i++ )
        {
            // symmetric case: skip over "equivalent" children
            if ( symmetric && equivalenceUnknown && nodes[i] == 2 )
            {
                continue; // visits city 2 before city 1: skip it.
            }
            
            // this is the template for the children
            TspSolution child = new TspSolution( this, i );
            
            child.computeLowerBound( input ); // update lower bound            

            int upperBound = ((Integer) shared.get()).intValue();
            if ( child.lowerBound < upperBound )
            {
                 q.add( child );
            }
        }
        return q;
    }
    
    public int getCost() { return lowerBound; }
        
    public int getLowerBound( Object input ) { return lowerBound; }
    
    public int[] getTour() { return nodes; }

    public boolean isAtomic( Task task )
    {
        return ( nodes.length - length < ATOMIC ) ? true : false;
    }
    
    public boolean isComplete()
    {
        // if only COMPLETE_THRESHOLD nodes are not in tour, return true
        return ( nodes.length - length <= COMPLETE_THRESHOLD ) ? true : false;
    }

    public String toString()
    {
        StringBuffer tour = new StringBuffer();
        tour.append("Size: " + nodes.length + ", tour size: " + length );
        tour.append(", cost: " + lowerBound + ", Tour: ");
        for (int i = 0; i < length; i++)
        {
            tour.append( " " ).append( nodes[i] );
        }
        return new String( tour );
    }
}
