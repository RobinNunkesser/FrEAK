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
 * A Path is a sequence of vertices, with an associated cost.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello3;

import java.util.*;


final class Path 
{    
    private int[][] costs; // costs matrix
    private int i;     // first vertex in path
    private int j;     // last vertex in path
    private Node nodeI;
    private Node nodeJ;
    private int cost;

    // !! should implement List with data structure in which reverse is O(1) &
    // !! append is O(1).
    private List list = new LinkedList();       

    public Path( int index, int[][] costs) 
    {
        this.costs = costs;
        this.i = index;        
        j = index;
        nodeI = new Node( index, this );
        nodeJ = new Node( index, this );
        list.add( new Integer( index ) );
    }  

    /**
     * Assumes that the paths to be added are distinct.
     *
     * Connect this path's i() endpoint that matches arg i to path's j() 
     * endpoint that matches arg j via costs[i][j].
     */
    void add( int i, Path path, int j )
    {
        assert ! this.equals( path);
        
        cost += path.cost() + costs[i][j];
        if ( i == this.i )
        {            
            reverse(); // this Path endpoint is i
        }
        if ( j == path.j() )
        {            
            path.reverse(); // path endpoing is j
        }
        list.addAll( path.list() );
        this.j = path.j();
        nodeJ = path.getNodeJ();
        nodeJ.setPath( this );   
    }

    int cost() { return cost; }
    
    /**
     * This path is a cycle. Delete the largest edge.
     */
    void cycle()
    {            
        cost += costs[i][j];
        j = i;

//        // find max edge; reset endpoints to its endpoints: Delete it
//        //    start with edge that connects last node to 1st node
//        int v = j(), w, maxEdgeCost = -1;
//        for ( Iterator iterator = list.iterator(); iterator.hasNext(); v = w )
//        {
//            Integer W = (Integer) iterator.next();
//            w = W.intValue();
//            if ( costs[v][w] > maxEdgeCost )
//            {
//                // set this Path endpoints to current max edge endpoints
//                this.i = v;
//                this.j = w;
//                maxEdgeCost = costs[v][w];
//            }                    
//        }
//        cost -= maxEdgeCost; // subtract cost of max edge
//
//        // make list endpoints correspond to path endpoints
//        LinkedList l = (LinkedList) list;
//        for ( int k = ((Integer) l.getFirst()).intValue(); k != this.j; k = ((Integer) l.getFirst()).intValue())
//        {                
//            // cyclic shift left
//            l.addLast( l.removeFirst() );
//        }
//        this.i = ((Integer) l.getFirst()).intValue();
//        this.j = ((Integer) l.getLast()).intValue();
//        nodeI = new Node( this.i, this );
//        nodeJ = new Node( this.j, this );
    }
    
    List getList() { return list; }
    
    Node getNodeJ() { return nodeJ; }
    
    int  i()   { return i; }
    
    int  j()    { return j; }

    List list() { return list; }

    Node nodeI() { return nodeI; }
    Node nodeJ() { return nodeJ; }

    void nodeI( Node nodeI ) { this.nodeI = nodeI; }
    void nodeJ( Node nodeJ ) { this.nodeJ = nodeJ; }

    // return the reverse of this.list
    List reverse()
    {
        Stack stack = new Stack();
        Iterator iterator = list.listIterator( 0 );
        while ( iterator.hasNext() )
        {            
             Integer node = (Integer) iterator.next();
             stack.push( node );
        }
        List reversedList = new LinkedList();
        while ( ! stack.isEmpty() )
        {
            Integer node = (Integer) stack.pop();
            reversedList.add( node );
        }
        
        // swap endpoints i & j and nodeI & nodeJ
        int temp = i;        
        i = j;
        j = temp;
        
        Node tempNode = nodeI;
        nodeI = nodeJ;
        nodeJ = tempNode;
        
        list = reversedList;                        
        return list;
    }

    public String toString() 
    { 
        String listString = "";
        for ( Iterator iterator = list.listIterator(); iterator.hasNext(); )
        {
            listString += " " + (Integer) iterator.next();
        }
        return " PATH: i: " + i + ", j: " + j + ", cost: " + cost + 
               ", list: " + listString;
    }
    
    public String listToString() 
    { 
        String listString = "";
        for ( Iterator iterator = list.listIterator(); iterator.hasNext(); )
        {
            listString += " " + (Integer) iterator.next();
        }
        return listString;
    }
}
