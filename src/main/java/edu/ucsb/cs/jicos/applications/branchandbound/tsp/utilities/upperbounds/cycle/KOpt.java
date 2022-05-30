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
 * KOpt.java
 *
 * Created on May 13, 2004, 10:12 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cycle;

import edu.ucsb.cs.jicos.applications.utilities.graph.Graph;
import edu.ucsb.cs.jicos.applications.utilities.graph.GraphEuclidean;


public final class KOpt {
    private Graph graph;
    private int[] tour;
    private int k;
        
    // compute cost of tour
    private int getCost()
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
    
    private boolean flipElems(int elems[]) {
        int temp = tour[ elems[0] ];
        for (int i=0; i < elems.length-1; ++i) {
            tour[ elems[i] ] = tour[ elems[i+1] ];
        }
        tour[ elems[elems.length-1] ] = temp;
        return true;
    }    
    
    private void unflipElems(int elems[]) {
        int temp = tour[ elems[elems.length - 1] ];
        for (int i=elems.length-1; i > 0; --i) {
            tour[ elems[i] ] = tour[ elems[i-1] ];
        }        
        tour[ elems[0] ] = temp;
    }
    
    private void kOptimal()
    {
        KOptimal ko = new KOptimal(k, graph.size());
        int elems[];
        long bestCost = getCost();
        System.out.println("Initial cost: " + bestCost);
        parent: while (ko.isNext())
        {
                elems = ko.getNextElemsIWontTouchIPromise();
                do {
//                printelems(elems);
                flipElems(elems);
                if (getCost() < bestCost)
                {
                    bestCost = getCost();
                    ko.reInit();
                    System.out.println("New best cost: " + Long.toString(bestCost));
                    System.out.flush();
                    continue parent;
                }
                else
                {
                    unflipElems(elems);
                }
                elems = ko.getPerm();
            } while (ko.hasPermutations());
        }
    }
    
    private static void printelems(int elems[]) {
        for (int i=0;i<elems.length-1;++i) {
            System.out.print(elems[i] + ", ");
        }
        System.out.println(elems[elems.length-1]);
    }

    /** Creates a new instance of KOpt */
    public KOpt(Graph tsp, int[] tour, int k) {
        graph = tsp;
        this.tour = tour;
        this.k = k;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int n = 500;
        Graph tsp = new GraphEuclidean(n, n, 1000);
        UpperBound ub = new UpperBound(tsp);
        int tour[] = ub.getTour();
        KOpt k; // = new KOpt(tsp, tour, 2);
        //k.kOptimal();
        //k = new KOpt(tsp, tour, 3);
        //k.kOptimal();
        k = new KOpt(tsp, tour, 4);
        k.kOptimal();
        for (int i=0;i<tour.length-1;++i) {
            System.out.print(tour[i] + ", ");
        }
        System.out.println(tour[tour.length-1]);
    }
    
}
