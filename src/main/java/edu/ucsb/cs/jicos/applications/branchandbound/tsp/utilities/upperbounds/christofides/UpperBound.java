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
 */

/*
 * UpperBound.java - Christofides's algorithm: Included to run comparisons
 * with Matching-based upper bound
 *
 * Created on July 21, 2003, 5:57 PM
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides;

import edu.ucsb.cs.jicos.applications.utilities.graph.*;
import edu.ucsb.cs.jicos.applications.utilities.set.*;
import java.util.*;


public final class UpperBound 
{
    //
    // ATTRIBUTES
    //
    private int[][] distances; 
    private Graph graph;
    private ArrayList[] mst;
    private int[] tour;
    
    //
    // PUBLIC METHODS
    //    
    public UpperBound( Graph graph ) 
    { 
        this.graph = graph; 
        distances = graph.getCosts(); 
    }
    
    public UpperBound( Graph graph, ArrayList[] mst ) 
    { 
        this.graph = graph; 
        this.distances = graph.getCosts();
        this.mst = mst;
//        System.out.println("UpperBound: constructing Christofides upper bound.");
    }
    
    public int[] getTour()
    {
        if ( tour != null )
        {
            return tour;
        }
        if ( mst == null )
        {
            mst = getMinSpanningTree();
        }
        
        // get Eulerian graph: MST edges UNION matched edges of odd-degree nodes
        List[] eulerGraph = getEulerGraph( mst );
        
        // get Eulerian walk
        List eulerWalk = getEulerWalk( eulerGraph, 0 );
        
        // construct tour from Eulerian walk
        tour = getEulerTour( eulerWalk );
        
        return tour;
    }
    
    // 
    // PRIVATE METHODS
    //
    // get Eulerian graph: MST edges UNION matching edges
    private List[] getEulerGraph( ArrayList[] mst )
    {
        // identify the (even # of) vertices in the MST of odd degree
        ArrayList oddDegreeNodes = getOddDegreeNodes( mst );
        
        // construct subgraph from vertices in MST with odd degree.
        Graph graph = getOddDegreeSubgraph( oddDegreeNodes );
        
        // compute minimum cost maximum matching for this subgaph
        int[] mates = graph.getMinCostMaxMatch();
        
        // construct empty Eulerian graph
        List[] neighbors = new LinkedList[ mst.length ];
        for ( int i = 0; i < neighbors.length; i++ )
        {
            neighbors[i] = new LinkedList();
        }
        
        // add minimum spanning tree edges
        for ( int u = 0; u < mst.length; u++ )
        {
            // add MST edges to node u
            for ( int j = 0; j < mst[u].size(); j++ )
            {
                int v = ((Integer) mst[u].get( j )).intValue();
                neighbors[u].add( new Integer( v ) );
            }
        }
        
        // add matched edges
        for ( int i = 0; i < mates.length; i++ )
        {
            Integer I = (Integer) oddDegreeNodes.get( i );
            Integer J = (Integer) oddDegreeNodes.get( mates[i] );
            neighbors[ I.intValue() ].add( J );
        }
        
        return neighbors;
    }
        
    // construct Eulerian circuit on Eulerian graph
    private int[] getEulerTour( List eulerWalk )
    {        
        int[] tour = new int[ distances.length ];
        boolean[] isInTour = new boolean[ distances.length ];
        
        for ( int i = 0; i < isInTour.length; i++ )
        {
            isInTour[i] = false;
        }
        
        int index = 0;
        for ( ListIterator iterator = eulerWalk.listIterator(); iterator.hasNext(); )
        {
            int v = ( (Integer) iterator.next() ).intValue();
            if ( ! isInTour[v] )
            {
                isInTour[v] = true;
                tour[ index++ ] = v;
            }             
        }        
        assert index == distances.length : index + " != " + distances.length;
        return tour;
    }
    
    /** Based on algorithm given Combinatorial Optimization: Algorithms and 
     * Complexity, by Papadimitriou & Steiglitz
     */
    private List getEulerWalk( List[] eulerGraph, int v1 )
    {
        List skeleton = new LinkedList();        
        skeleton.add( new Integer( v1 ) );
        
        if ( eulerGraph[ v1 ].isEmpty() )
        {
            return skeleton;
        }

        // loop until v1 is revisited
        int u = v1;
        do 
        {
            ListIterator iterator = eulerGraph[u].listIterator(0);
            Integer V = (Integer) iterator.next();
                        
            // remove edge from node u
            iterator.remove();
            
            // remove edge from node v
            int v = V.intValue();
            eulerGraph[v].remove( new Integer( u ) );
            
            skeleton.add( V );
            u = v;
        }
        while ( u != v1 );
        
        skeleton.remove( skeleton.size() - 1 ); // remove trailing v1
            
        /* Let skeleton have Integers whose intValues are v1, ..., vn, v1.
         * return getEulerWalk(v1), ..., getEulerWalk(vn), v1;
         */
        List walk = new LinkedList();
        for ( ListIterator iterator = skeleton.listIterator(); iterator.hasNext(); )
        {
            int v = ((Integer) iterator.next()).intValue();
            List subwalk = getEulerWalk( eulerGraph, v );
            walk.addAll( subwalk );
        }
        walk.add( new Integer( v1 ) );
            
        return walk; // return List of recursive call values     
    }
    
    /** returns array of adjacency lists, one for each node
     */
    private ArrayList[] getMinSpanningTree()
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
                assert components.find( u ) == components.find( v );
                nComponents--;
                
                // add edge to tree
                tree[ u ].add( new Integer( v ) );
                tree[ v ].add( new Integer( u ) );                
            }
        }
        
        return tree;
    }
    
    private ArrayList getOddDegreeNodes( ArrayList[] mst )
    {
        ArrayList oddDegreeNodes = new ArrayList();
        for ( int i = 0; i < mst.length; i++ )
        {
            if ( mst[i].size() % 2 == 1 )
            {
                oddDegreeNodes.add( new Integer( i ) );
            }            
        }
        return oddDegreeNodes;
    }
    
    /** parameter is a representation of a minimum spanning tree. It is an array
     * of neighbor arrays. Each neighbor is represented by its number: 
     * 0, ..., |V| - 1.
     * 
     * The returned value is a subgraph of the MST containing vertices of odd
     * degree.
     */
    private Graph getOddDegreeSubgraph( ArrayList oddDegreeNodes )
    {
        // construct the subgraph of these vertices.
        int length = oddDegreeNodes.size();
        int[][] costs = new int[ length ][ length ];
        for ( int i = 0; i < length; i ++ )
        {
            costs[i][i] = 0;
            Integer I = (Integer) oddDegreeNodes.get( i );
            for ( int j = 0; j < i; j++ )
            {                
                Integer J = (Integer) oddDegreeNodes.get( j );
                costs[i][j] = costs[j][i] = distances[ I.intValue() ][ J.intValue() ];
            }
        }

        return new GraphImpl( costs );
    }
}
