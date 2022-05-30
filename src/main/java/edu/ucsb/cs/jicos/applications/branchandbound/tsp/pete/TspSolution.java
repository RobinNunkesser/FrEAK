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
 * TspSolution.java
 *
 * @version 2.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.pete;

import edu.ucsb.cs.jicos.applications.branchandbound.*;
import edu.ucsb.cs.jicos.services.*;

import edu.ucsb.cs.jicos.applications.utilities.graph.*; // for MST lower bound
import edu.ucsb.cs.jicos.applications.utilities.set.*;   // for MST lower bound

import java.util.*;


public class TspSolution implements edu.ucsb.cs.jicos.applications.branchandbound.Solution
{
    /* COMPLETE_THRESHOLD is used by isComplete.
     * When there is only 1 city not on the tour, add it:
     * 1) add the edge from the last city currently on the tour to this last city; 
     * 2) add this last city to tour; 
     * 3) add the edge from this last city to the 1st city
     */
    final static int COMPLETE_THRESHOLD = 1;
    final static int ATOMIC = 13; // height of subtree searched by host
    
    private int[] nodes; // array containing partial tour + unused cities
    private int length;  // number of cities in nodes array in partial tour
    private int partialTourCost; // cost of partial tour
    private int mstCost;         // cost of minimum spanning tree 
    private int lowerBound;      // partialTourCost + mstCost
    
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
        partialTourCost = parent.partialTourCost;
        //lowerBound = partialTourCost;  // subtract parent mst (make new one)
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
        partialTourCost = 0;
    }
    
    public void computeLowerBound( Object input )
    {        
        // add cost of edge between old last node and new last node
        partialTourCost += ( (TSP) input).distance[ nodes[ length - 2 ] ][ nodes[length - 1] ];
        if ( isComplete() )
        {
            length++; // toString uses length
            
            // add cost of edge from new last node to nodes.length
            partialTourCost += ( (TSP) input).distance[ nodes[ nodes.length - 2 ] ][ nodes[ nodes.length - 1 ] ];
            
            // add cost of edge from nodes.length to 0: complete cycle.
            partialTourCost += ( (TSP) input).distance[ nodes[ nodes.length - 1 ] ][ 0 ];
            lowerBound = partialTourCost;
        }
        else
        {
            /* !! This is a quick & inefficient MST-based lower bound. If I 
             * were serious about this approach, I would implement it 
             * efficiently.
             */
            
            // compute MST of cities not in tour + 2 endpoints of partial tour
            int[][] distances = ((TSP) input).distance;
            int[][] costs = getExcludedSubgraph( distances );
            
            /*
            // !! debug
            System.out.println("\ncomputeLowerBound: distance matrix:");
            for ( int i = 0; i < distances.length; i++ )
            {
                System.out.print( i + ":" );
                for ( int j = 0; j < distances.length; j++ )
                {
                    System.out.print( "\t" + distances[i][j] );
                }
                System.out.println("");
            }
            System.out.print("computeLowerBound: nodes in tour:");
            for ( int i = 0; i < length; i++ )
            {
                System.out.print(" " + nodes[i] );
            }
            System.out.println("");
            System.out.print("computeLowerBound: EXCLUDED nodes:");
            for ( int i = length; i < nodes.length; i++ )
            {
                System.out.print(" " + nodes[i] );
            }
            System.out.println("");
            System.out.println("computeLowerBound: cost matrix:");
            for ( int i = 0; i < costs.length; i++ )
            {
                System.out.print( i + ":" );
                for ( int j = 0; j < costs.length; j++ )
                {
                    System.out.print( "\t" + costs[i][j] );
                }
                System.out.println("");
            }
            // !! end debug
             */
            
            ArrayList[] mst = getMinSpanningTree( costs );
            mstCost = getMstCost( mst, costs );
            lowerBound = partialTourCost + mstCost;
            
            /*
            System.out.println("Partial tour: " + this);
            
            System.out.println("lowerBound: " + lowerBound + 
             " = partialTourCost: " + partialTourCost + " mstCost: " + mstCost);
             */
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
            if ( child.lowerBound <= upperBound )
            {
                 q.add( child );
            }
            /*
            // !! DEBUG BEGIN
            else
            {
                System.out.println("TspSolution.getChildren: " +
                        " SKIPPING child with cost: " + child.lowerBound );
            }
            // !! DEBUG END
             */
        }
        return q;
    }
    
    public int getCost() { return lowerBound; }
    
    /* computes subgraph induced by excluded cities + 2 endpoints of partial 
     * tour.
     */
    private int[][] getExcludedSubgraph( int[][] distances )
    {
        int size = nodes.length - length + 1;
        if ( length > 1 )
        {
            size++;
        }
        
        // construct list of cities to include in subgraph
        int[] list = new int[size];
        
        //System.out.println("getExcludedSubgraph: size: " + size);
        
        /*
        //!! DEBUG
        System.out.println("getExcludedSubgraph: nodes array: ");
        for ( int i = 0; i < nodes.length; i++ )
        {
            System.out.println( "node " + i + " = " + nodes[i]);
        }
        System.out.println(" CHECK: " + this);
         */
        
        // insert 0th city in partial tour
        int index = 0;
        list[index++] = 0;       
        if ( length > 1 )
        {
            // insert last city in partial tour
            list[index++] = nodes[ length - 1 ];
        }
        
        // insert excluded cities
        for ( int j = length; j < nodes.length; j++ )
        {
            //System.out.println("getExcludedSubgraph: index: " + index + " " + j);
            list[index++] = nodes[j];
        }
        
        /*
        // !! DEBUG
        System.out.print("getExcludedSubgraph: list: ");
        for ( int i = 0; i < list.length; i++ )
        {
            System.out.print(list[i] + " ");
        }
        System.out.println("");
         */
            
        // construct the subgraph of these cities.
        int[][] costs = new int[ size ][ size ];
        for ( int i = 0; i < size; i ++ )
        {
            costs[i][i] = 0;
            for ( int j = 0; j < i; j++ )
            {                
                costs[i][j] = costs[j][i] = distances[ list[i] ][ list[j] ];
            }
        }
        costs[0][1] = costs[1][0] = 1000; // make max value of distance in TSP constructor.

        //System.out.println("getExcludedSubgraph: END");
        return costs;
    } 
        
    public int getLowerBound( Object input ) { return lowerBound; }    
    
    /** returns array of adjacency lists, one for each node
     */
    private ArrayList[] getMinSpanningTree( int[][] distances )
    {
        int length = distances.length;
        ArrayList[] tree = new ArrayList[ length ];      
        Edge[] edges = new Edge[ length * (length - 1) / 2 ];
        
        // construct MergeFindSet & initialize its components
        MergeFindSet components = new MergeFindSet( distances.length );
        
        // construct set of edges
        for ( int i = 0, k = 0; i < length; i++ )
        {
            for ( int j = 0; j < i; j++ )
            {
                Edge edge = new Edge( i, j, distances[i][j] );
                edges[ k++ ] = edge;
            }
        }
        
        Arrays.sort( edges );  
        
        // initialize tree
        for ( int i = 0; i < length; i++ )
        {
            tree[i] = new ArrayList();
        }
        
        // construct minimum weight spanning tree
        for ( int i = 0, nComponents = length; nComponents > 1; i++ )
        {
            Edge edge = edges[i];
            int u = edge.getU();
            int v = edge.getV();
            int uComponent = components.find( u );
            int vComponent = components.find( v ); 
            if ( uComponent != vComponent )
            {
                components.merge( uComponent, vComponent );
                nComponents--;
                
                // add edge to tree
                tree[ u ].add( new Integer( v ) );
                tree[ v ].add( new Integer( u ) );                
            }
        }
        
        return tree;
    }
    
    private int getMstCost( ArrayList[] mst, int[][] costs )
    {
        int cost = 0;
        for ( int i = 0; i < mst.length; i++ )
        {
            for ( int j = 0; j < mst[i].size(); j ++ )
            {
                int v = ((Integer) mst[i].get( j )).intValue();
                cost += costs[i][v];            
            }
        }
        return cost / 2;
    }

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
