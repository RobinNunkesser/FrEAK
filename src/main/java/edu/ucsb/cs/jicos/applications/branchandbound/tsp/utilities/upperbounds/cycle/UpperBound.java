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
/**
 * UpperBound.java - Rewrite as a Primm's workalike
 *
 * Created on December 7, 2003, 7:41 PM
 *
 * @author  Chris Coakley
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cycle;

import java.util.Vector;
import edu.ucsb.cs.jicos.applications.utilities.graph.Graph;
import edu.ucsb.cs.jicos.applications.utilities.graph.GraphImpl;
import edu.ucsb.cs.jicos.applications.utilities.graph.GraphEuclidean;


public final class UpperBound {
    Vector tour;
    Vector remaining;
    //int[][] distance;
    Graph graph;
    
    /** Creates a new instance of UpperBound */
    public UpperBound(Graph graph) {
        //this.distance = graph.getCosts();
        this.graph = graph;
        tour = new Vector(graph.size()+1);
        tour.add(new Integer(0));
        tour.add(new Integer(0));
        remaining = new Vector(graph.size() - 1);
        for (int i=0,m=remaining.capacity();i<m;i++) {
            remaining.add(new Integer(i+1));
        }
    }
    
    public UpperBound(Graph graph, Vector cycle) {
        //this.distance = graph.getCosts();
        this.graph = graph;
        this.tour = cycle;
        this.remaining = new Vector(graph.size());
        for (int i=0, m=remaining.capacity();i <m;i++) {
            remaining.add(new Integer(i));
        }
        for (int i=0, m=tour.size(); i<m;i++) {
            remaining.remove(tour.get(i));
        }
    }
    
    public int[] getTour() {
        // incrementally add the closest point to the tour
        for (int i=tour.size(), m=graph.size()+1; i<m;i++) {
            // for each point left
            int bestpos = 1;
            int best = ((Integer)remaining.get(0)).intValue();
            int bestdelta = Integer.MAX_VALUE;
            for (int j=0, max=remaining.size(); j < max; j++) {
                int trial = ((Integer)remaining.get(j)).intValue();
                for (int k=1, maxk=tour.size(); k<maxk; k++) {
                    int from = ((Integer)tour.get(k-1)).intValue();
                    int to = ((Integer)tour.get(k)).intValue();
                    int delta = graph.getCost(from,trial) + graph.getCost(trial,to) - graph.getCost(from,to);
                    if (delta < bestdelta) {
                        best = trial;
                        bestdelta = delta;
                        bestpos = k;
                    }
                }
            }
            remaining.remove(new Integer(best));
            tour.add(bestpos,new Integer(best));
        }
        int[] inttour = new int[tour.size()-1];
        for (int i=0; i<inttour.length; i++) {
            inttour[i] = ((Integer)tour.get(i)).intValue();
        }
        return inttour;
    }
        
    // compute cost of tour
    static private int getCost( int[] tour, Graph graph )
    {
        int cost = 0;
        int j = tour[0];
        //System.out.print(" Tour: \n   " + j );
        for ( int i = 1; i < tour.length; i++ )
        {
            //System.out.print(" " + tour[i] );
            cost += graph.getCost( j , tour[i] );
            j = tour[i];
        }        
        cost += graph.getCost( j , tour[0] );
        //System.out.println("\n Cost: " + cost );  
        return cost;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("\tNodes\tcycCost\tTime\tcCost\tTime");        
        float maxRatio = 0;
        int start = 50;
        int stop = 500;
        int increment = 50;
        boolean isGeneral = false;
        for ( int i = start; i <= stop; i += increment )
        {
            // construct graph
            int $nodes = i;
            int seed = $nodes;
            int magnification = 1000;
            Graph graph;
            if ( isGeneral )
            {
                graph = new GraphImpl( $nodes, seed, magnification);
            }
            else
            {
                graph = new GraphEuclidean( $nodes, seed, magnification);
            }

            //int[][] distances = graph.getCosts();
            
            System.out.print("\t" + i);
            
            // compute TSP for Christofides's algorithm
            UpperBound cycTsp = new UpperBound( graph );
            long startTime = System.currentTimeMillis();
            int[] cyctour = cycTsp.getTour();
            long stopTime = System.currentTimeMillis();
            int cycCost = getCost( cyctour, graph );
            System.out.print("\t" + cycCost + ",\t" + (stopTime - startTime) );

            // compute TSP for Christofides's algorithm
            edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound cTsp 
                = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound( graph );
            startTime = System.currentTimeMillis();
            int[] tour = cTsp.getTour();
            stopTime = System.currentTimeMillis();
            int cCost = getCost( tour, graph );
            float ratio = ((float) cCost/cycCost);
            System.out.println(",\t" + cCost + ",\t" + (stopTime - startTime) + ",\t" + ratio );
            if ( ratio > maxRatio )
            {
                maxRatio = ratio;
            }           
        }
        System.out.println("Maximum ratio: " + maxRatio);
    }
    
}
