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
 * Generator for a minimal spanning one-tree.
 * <br>The minimal spanning one-tree for the set of vertices V is defined as
 * the minimal spanning tree of the set of vertices V' = V - v[0], plus the
 * two shortest edges incident upon v[0]. All tours (and specifically, the
 * optimal tour) of the Graph are one-trees, but not all one-trees are tours.
 * Therefore, finding the MSOneT for a cost matrix yields a lower bound for
 * the Graph.
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides;

import edu.ucsb.cs.jicos.applications.utilities.graph.Graph;
import java.util.HashSet;

public class MSOneTPrim implements java.io.Serializable
{
//    private int distance[][];
    private Graph tsp;
    private int closest[];
    private int U[];
    private int T[];
    private boolean inU[];
    private int firsttov;
    private int secondtov;
    private int iter = 2;
    private double cost = 0.0;
    private boolean die;
    private boolean canchangefirst = true;
    private boolean canchangesecond = true;
    
    private int included[];
    private int includedT[];
    private int excluded[];
    private int excludedT[];
    
    private double weight[];
    private int degree[];
    
    /** Creates a new instance of MSOneT
     *
     * @param cost The cost matrix
     * @param included An array such that (included[i],includedT[i]) is an included edge. Symmetric (iff contains (a,b), contains (b,a))
     * @param degree A destructively modified array containing the degree of each vertex
     */
    public MSOneTPrim(Graph tsp, int[] included, int[] includedT, int[] excluded, int[] excludedT, double weight[], int degree[])
    {
        this.included = included;
        this.includedT = includedT;
        this.excluded = excluded;
        this.excludedT = excludedT;
        this.weight = weight;
        this.degree = degree;
        
        this.tsp = tsp;
        closest = new int[tsp.size()];
        U = new int[tsp.size()];
        T = new int[tsp.size()];
        inU = new boolean[tsp.size()];
        U[1] = 1;
        inU[1] = true;
        for (int i = 0; i < tsp.size(); i++)
        {
            closest[i] = 1;
            if (inExcluded(i, closest[i]))
            {
                // This ensures that when the next non-excluded node is added to U, it will become "closest"
                // There must be at least one possible edge to each node (branching rules must ensure this).
                closest[i] = findWorst(i);
            }
            degree[i] = 0;
        }
    }
    
    /** Creates a Minimal Spanning One Tree.
     * <br>This is a modification of the MST algorithm in Papadimitriou and
     * Steiglitz, page 273.
     * <br><b>Side effects:<\b> cost is computed by calling this method.
     * <br> degree is modified by calling this method
     */
    public void compute()
    {
        addAttachedEdges(1);
        
        while (!isComplete())
        {
            int next = getNext();
            addNext(next);
            addAttachedEdges(next);
        }
        setFirstTwoEdges();
    }
    
    private boolean isComplete()
    {
        // iter is updated after adding an edge, and the index starts at 1
        // There are 1 less nodes than in distance (cull first node for one tree)
        return iter == tsp.size();
    }
    
    /** recursive function that adds mandatory edges to the new vertex */
    private void addAttachedEdges(int next)
    {
        if (next == 0) return;
        for (int i = 0 ; i < included.length ; i++)
        {
            if (included[i] == next)
            {
                //System.out.println("Adding Attached Edge");
                // if 1,2 is included, so is 2,1, this ends the reflexive recursion
                if (inU(includedT[i])) continue;
                addEdge(includedT[i],next);
                addAttachedEdges(includedT[i]);
            }
        }
    }
    
    private void addEdge(int u, int t)
    {
        if (inExcluded(u,t))
        {
            System.out.println("Illegal Add [" + u + ", " + t + "]");
            System.out.println("Included: " + includedString());
            System.out.println("Excluded: " + excludedString());
            System.exit(0);
        }
        // special case -- these deal with the mandatory included edges incident on node 0
        if (u == 0)
        {
            if (firsttov == 0)
            {
                firsttov = t;
                canchangefirst = false;
            }
            else
            {
                secondtov = t;
                canchangesecond = false;
            }
            return;
        }
        if (t == 0)
        {
            if (firsttov == 0)
            {
                firsttov = u;
                canchangefirst = false;
            }
            else
            {
                secondtov = u;
                canchangesecond = false;
            }
            return;
        }
        assert (iter < U.length);
        assert (u < degree.length);
        assert (t < degree.length);
        if (iter >= U.length)
        {
            System.out.println("**ERROR** (u,t,iter): (" + u + ", " + t + ", " + iter + ")");
        }
        inU[u] = true;
        U[iter] = u;
        T[iter] = t;
        iter++;
        degree[u]++;
        degree[t]++;
        cost += tsp.getCost(u, t) + weight[u] + weight[t];
        updateClosest(u);
    }
    
    /** finds the next edge to add to the MST */
    private int getNext()
    {
        assert tsp != null;
        assert weight != null;
        double min = (double)Integer.MAX_VALUE;
        int next = 0;

        for (int i = 1; i < tsp.size(); i++)
        {
            if (inU(i)) continue;
            if ( (tsp.getCost(i,closest[i]) + weight[i] + weight[closest[i]]) < min && !inExcluded(i,closest[i]) )
            {
                min = (tsp.getCost(i, closest[i]) + weight[i] + weight[closest[i]]);
                next = i;
            }
        }

        return next;
    }
        
    private void addNext(int next)
    {
        addEdge(next,closest[next]);
    }    

    /** returns true if vertex is already in U */
    private boolean inU(int vertex)
    {
        return inU[vertex];
    }
    
    /** updates closest array after a new vertex has been added to the MST */
    private void updateClosest(int next)
    {
        for (int i = 1; i < tsp.size(); i++)
        {
            if (inU(i)) continue;
            if ( (tsp.getCost(i, closest[i]) + weight[closest[i]] > tsp.getCost(i, next) + weight[next]) && !inExcluded(i,next) )
            {
                closest[i] = next;
            }
        }       
    }
    
    private int findWorst(int i)
    {
        double maxval = (double)Integer.MIN_VALUE;
        int max = -1;
        for(int j = 1; j < tsp.size(); j++)
        {
            if (tsp.getCost(i,j) + weight[i] + weight[j] > maxval && !inExcluded(i,j))
            {
                max = j;
                maxval = tsp.getCost(i,j) + weight[i] + weight[j];
            }
        }
        return max;
    }
    
    /** returns true if an edge is in the excluded list */
    private boolean inExcluded(int from, int to)
    {
        for (int i = 0; i < excluded.length; i++)
        {
            if (excluded[i] == from && excludedT[i] == to)
                return true;
        }
        return false;
    }
    
    private boolean inIncluded(int from, int to)
    {
        for (int i = 0; i < included.length; i++)
        {
            if (included[i] == from && includedT[i] == to)
                return true;
        }
        return false;
        
    }
    
    /** The goal is to find the two closest edges to v[0].
     * <br>I couldn't think of a clever initialization, so I unroll the for loop
     * for two iterations as the initialization block.
     * <br><b>Side effects:<\b> firsttov and secondtov are set, cost is updated.
     */
    private void setFirstTwoEdges()
    {
        if (firsttov == 0)
        {
            for (int i = 1; i < tsp.size(); i++)
            {
                // the check against secondtov should not be necessary
                if (i != secondtov && !inExcluded(0, i))
                {
                    firsttov = i;
                    break;
                }
            }
        }
        if  ( secondtov == 0)
        {
            for (int i = 1; i < tsp.size(); i++)
            {
                if (i != firsttov && !inExcluded(0, i))
                {
                    secondtov = i;
                    break;
                }
            }
        }
        
        // ensure that [0,firsttov] is less cost than [0,secondtov]
        if ((tsp.getCost(0,secondtov) + weight[secondtov]) < (tsp.getCost(0,firsttov) + weight[firsttov]))
        {
            if (!canchangefirst) {
                System.err.println("BUGFIX in MSOneTPrim catching something");
            }
            boolean tempbool = canchangesecond;
            canchangesecond = canchangefirst;
            canchangefirst = tempbool;
            int temp = secondtov;
            secondtov = firsttov;
            firsttov = temp;
        }
        
        for (int i = 3; i < tsp.size(); i++)
        {
            if (canchangefirst && (tsp.getCost(0,i) + weight[i]) < (tsp.getCost(0,firsttov) + weight[firsttov]) && !inExcluded(0, i) )
            {
                if (canchangesecond)
                {
                    secondtov = firsttov;
                }
                firsttov = i;
            }
            else if (canchangesecond && (tsp.getCost(0,i) + weight[i]) < (tsp.getCost(0,secondtov) + weight[secondtov])  && !inExcluded(0, i) )
            {
                secondtov = i;
            }
        }
        
        if (firsttov == 0 || secondtov == 0)
        {
            die = true;
        }
        
        degree[0]++;
        degree[0]++;
        degree[firsttov]++;
        degree[secondtov]++;
        cost += tsp.getCost(0, firsttov) + weight[0] + weight[firsttov];
        cost += tsp.getCost(0, secondtov) + weight[0] + weight[secondtov];
    }


    
    /** Gives the cost minus twice the sum of the weights. This is useful for
     * estimating a lower bound.
     * @return The cost of the MSOneT minus twice the sum of the weights.
     */
    public int getUnweightedCost()
    {
        // Too many excluded edges prevents tour
        if (die)
            return Integer.MAX_VALUE;
        
        double sum = 0;
        for (int i = 1; i < weight.length; i++)
        {
            sum += weight[i];
        }
        // java int cast always rounds down, not desired here
        return (int)Math.round(cost - (2*sum));
    }
    
    public int getReevaluatedTourCost()
    {
        int tourcost = 0;
        tourcost += tsp.getCost(0,firsttov);
        tourcost += tsp.getCost(0,secondtov);
        for (int i = 2; i < U.length; i++) 
        {
            tourcost += tsp.getCost(T[i],U[i]);
        }
        return tourcost;        
    }
    
    private boolean causesSubCycle(int i, int j) {
        if (included.length < 1) return false;
        int a = 0;
        HashSet s = new HashSet();
        int lastfrom = i;
        int lastto = j;
        s.add(new Integer(lastfrom));
        // treat as while(true) with iteration variable to check for infinite loop
        for (int b=0;b<includedT.length+2;++b) {
            for (a = 0; a < includedT.length; ++a) {                
                if (included[a] == lastto && includedT[a] != lastfrom) {
                    if (s.contains(new Integer(includedT[a]))) {
                        System.err.println("Possible Cycle Mandated");
                        return true;
                    }
                    s.add(new Integer(includedT[a]));
                    lastfrom = lastto;
                    lastto = includedT[a];
                    break;
                }
            }
            if (a == includedT.length) return false;
        }
        System.err.println("Infinite Loop in MSTOneTreePrim::causesSubCycle!");
        System.exit(0);
        return false;
    }

    public int getMaxAttachedFree(int i)
    {
        assert (i != 0);
        assert (degree[i] != 2);
        int max = -1;
        int maxcost = Integer.MIN_VALUE;
        // find me in U
        for (int j = 2; j < U.length; j++)
        {
            
            if (U[j] == i)
            {
                assert (maxcost < tsp.getCost(i,T[j]));
                if (maxcost < tsp.getCost(i,T[j]) && !inIncluded(i,T[j]) && !causesSubCycle(i,T[j]))
                {
                   maxcost = tsp.getCost(i,T[j]);
                   max = T[j];
                }
                break; // each node in U exactly once
            }
        }
        // find me in T
        for (int j = 2; j < T.length; j++)
        {
            if (T[j] == i)
            {
               if (maxcost < tsp.getCost(i,U[j]) && !inIncluded(i,U[j]))
               {
                   maxcost = tsp.getCost(i,U[j]);
                   max = U[j];
               }
            }
        }
        if (firsttov == i)
        {
               if (maxcost < tsp.getCost(i,0) && !inIncluded(i,0))
               {
                   maxcost = tsp.getCost(i,0);
                   max = 0;
               }            
        }
        else if (secondtov == i)
        {
               if (maxcost < tsp.getCost(i,0) && !inIncluded(i,0))
               {
                   maxcost = tsp.getCost(i,0);
                   max = 0;
               }            
        }
        if (inIncluded(i, max))
        {
            // TODO XXX CJC - Rewrite using Jicos exceptions
            System.out.println("*** NOT FREE ***");
            System.exit(0);
        }
        if (inExcluded(i, max))
        {
            // TODO XXX CJC - Rewrite using Jicos exceptions
            System.out.println("*** BEYOND NOT FREE ***");
            System.out.println("Illegal Add [" + i + ", " + max + "]");
            System.out.println(debugDump());
            System.exit(0);
        }
        return max;
    }

    
    /** returns true if all nodes have exactly 2 edges.
     * @return true if all nodes have exactly 2 edges.
     */
    public boolean isTour()
    {
        for (int i = 1; i < degree.length; i++)
        {
            if (degree[i] != 2)
            {
                return false;
            }
        }
        return true;
    }
    
    /** Accessor method for the tour, represented as an array of ints.
     * @return An array of ints (node id's) representing the tour. null if the partial
     * solution is not a tour.
     */    
    public int[] getTour() {
        if (isTour()) {
            return getCycle();
        }        
        return null;
    }
    
    /** Accessor method for the cycle from the one tree, represented as an array of ints.
     * @return An array of ints (node id's) representing the cycle. 
     */    
    public int[] getCycle() {
        // I am going to assume that tour[0] is city 0, and go from there.
        int tour[] = new int[U.length];
        tour[0] = secondtov;
        tour[1] = 0;
        tour[2] = firsttov;
        int lastfound = 0;
        for (int i=3; i<tour.length; i++)
        {
            for(int j = 2; j<U.length; j++) {
                if (j == lastfound) continue;
                if (U[j] == tour[i-1]) {
                    if (T[j] == tour[0]) return tour;
                    tour[i]=T[j];
                    lastfound = j;
                    break;
                }
                if(T[j] == tour[i-1]) {
                    if (U[j] == tour[0]) return tour;
                    tour[i]=U[j];
                    lastfound = j;
                    break;
                }
            }
        }
        return tour;
    }
    
    /** debug method */
    private void describeEdge(int u, int t)
    {
        return;
//        System.out.print("  weight " + Integer.toString(u) + ": ");
//        System.out.print(weight[u]);
//        System.out.print("  weight " + Integer.toString(t) + ": ");
//        System.out.print(weight[t]);
//        System.out.print("  distance " + Integer.toString(u) + ", " + Integer.toString(t) + ": ");
//        System.out.println(distance[u][t]);
    }
    
    /** Debug method */
    public void whyNotTour()
    {
        System.out.println(whyNotTourString());
    }
    
    public String whyNotTourString()
    {
        boolean tour = true;
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < degree.length; i++)
        {
            if (degree[i] != 2)
            {
                tour = false;
                output.append("\n    Node " + i + " has degree " + degree[i]);
            }
        }
        if (tour)
        {
            return new String("Should have been tour=true");
        }
        else
        {
            return new String(output);
        }
        
    }
    
    public String uString()
    {
        StringBuffer output = new StringBuffer();
        for (int i = 1; i < U.length - 1; i++) 
        {
            output.append(U[i] + ", ");
        }
        output.append(U[U.length-1]);        
        return new String( output );    
    }
    
    public java.util.ArrayList[] getArrayListRepresentation()
    {
        int length = tsp.size();
        java.util.ArrayList[] tree = new java.util.ArrayList[length];
        // initialize tree
        for ( int i = 0; i < length; i++ )
        {
            tree[i] = new java.util.ArrayList();
        }
        
        // I only add one of the two special edges to avoid a cycle
        if (firsttov != 0)
        {
            tree[ 0 ].add( new Integer(firsttov) );
            tree[ firsttov ].add( new Integer(0) );
        }
        else if (secondtov != 0)
        {
            tree[ 0 ].add( new Integer(secondtov) );
            tree[ secondtov ].add( new Integer(0) );        
        }
        else
        {
            return null;
        }
        
        // Add the MST edges
        for ( int j = 2; j < U.length; j++ )
        {
            // add edge to tree
            tree[ T[j] ].add( new Integer( U[j] ) );
            tree[ U[j] ].add( new Integer( T[j] ) );                
        }        
        return tree;
    }
    
    public String treeString()
    {
        StringBuffer output = new StringBuffer();
        output.append("*[0, " + firsttov + "], ");
        output.append("*[0, " + secondtov + "], ");
        for (int i = 2; i < U.length - 1; i++) 
        {
            output.append("[" + T[i] + ", " + U[i] + "], ");
        }
        output.append("[" + T[U.length-1] + ", " + U[U.length-1] + "] ");
        return new String( output );            
    }

    public String includedString()
    {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < included.length; i++) 
        {
            output.append("[" + included[i] + ", " + includedT[i] + "], ");
        }
        return new String( output );            
    }

    public String excludedString()
    {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < excluded.length; i++) 
        {
            output.append("[" + excluded[i] + ", " + excludedT[i] + "], ");
        }
        return new String( output );            
    }

    
    public String weightString()
    {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < weight.length - 1; i++) 
        {
            output.append(weight[i] + ", ");
        }
        output.append(weight[weight.length-1]);        
        return new String( output );    
    }
    
    public String distanceString()
    {
        StringBuffer output = new StringBuffer();        
        for ( int i = 0; i < tsp.size(); i++ )
        {
            output.append(" Row " + i + ": ");
            for ( int j = 0; j < i; j++ )
            {
                output.append(" " + tsp.getCost(i,j));
            }
            output.append(" \n");
        }     
        return new String( output );
    }
    
    public String debugDump()
    {
        StringBuffer output = new StringBuffer();
        //output.append("U: " + uString());
        output.append("BEGIN DUMP");
        output.append("\n Tree: " + treeString());
        output.append("\n Included: " + includedString());
        output.append("\n Excluded: " + excludedString());
        output.append("\n Augmentation: " + weightString());
        output.append("\n Tour: " + whyNotTourString());
        output.append("\n Cost (bound): " + getUnweightedCost());
        output.append("\nEND DUMP");
        //output.append("\nDistances: \n" + distanceString());
        return new String( output );            
    }
    
//    
//    public int getTourDeviation()
//    {
//        int sum = 0;
//        for (int i=1; i<degree.length; i++)
//        {
//            sum += java.lang.Math.abs( degree[i] - 2 );
//        }
//        return sum;
//    }
    
}
