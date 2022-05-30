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

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.matching;

import java.util.*;


final class Path 
{    
    private Matching upperBound;
    private int index; // this Path object's number.
    private int i;     // first vertex in path
    private int j;     // last vertex in path
    private int cost;
    
    // !! should implement List with data structure in which reverse is O(1) &
    // !! append is O(1).
    private List list = new LinkedList();       
        
    public Path( Matching upperBound, int index ) 
    {
        this.upperBound = upperBound;
        this.index = index;
        this.i = index;
        j = index;
        list.add( new Integer( index ) );
    }  
    
    /**
     * Connect this path to Path path, via costs[this.index][path.index].
     * There are 8 cases: 
     *    this.i connects to path.i
     *        this.i is new i; path.i is new j
     *        path.i is new i; this.i is new j
     *
     *    this.i connects to path.j
     *        this.i is new i; path.j is new j
     *        path.j is new i; this.i is new j
     *
     *    this.j connects to path.i
     *        this.j is new i; path.i is new j
     *        path.i is new i; this.j is new j
     *
     *    this.j connects to path.j
     *        this.j is new i; path.j is new j
     *        path.j is new i; this.j is new j
     * 
     */
    void add( Path path )
    {
        cost += path.cost();
        
        // modify path
        boolean[][][] endpointIsI = upperBound.endpointIsI();
        boolean thisEndpoint = endpointIsI[index][ path.index() ][0];
        boolean pathEndpoint = endpointIsI[index][ path.index() ][1];  
        
        if ( thisEndpoint )
        {
            // this.i is one of the connection endpoints
            if ( pathEndpoint )
            {
                // path.i is one of the connection endpoints
                // new Path endpoints are this.j & path.j
                // set new endpoints
                if ( j < path.j() )
                {
                    i = j;
                    j = path.j();
                    reverse();
                    list.addAll( path.list() );
                }
                else
                {
                    i = path.j();
                    //j = j;
                    path.reverse().addAll( list );
                    list = path.list();
                }
            }
            else
            {
                // path.j is one of the connection endpoints
                // new Path endpoints are this.j & path.i
                if ( j < path.i() )
                {
                    i = j;
                    j = path.i();
                    reverse();
                    list.addAll( path.reverse() );
                }
                else
                {
                    i = path.i();
                    //j = j;
                    path.list().addAll( list );
                    list = path.list();
                }
            }
        }
        else
        {
            // this.j is one of the connection endpoints
            if ( pathEndpoint )
            {
                // path.i is one of the connection endpoints
                // new Path endpoints are this.i & path.j
                if ( i < path.j() )
                {
                    //i = i;
                    j = path.j();
                    list.addAll( path.list() );
                }
                else
                {
                    i = path.j();
                    j = i;
                    path.reverse();
                    path.list().addAll( reverse() );
                    list = path.list();
                }
            }
            else
            {
                // path.j is one of the connection endpoints
                // new Path endpoints are this.i & path.i
                if ( i < path.i() )
                {
                    //i = i;
                    j = path.i();
                    list.addAll( path.reverse() );
                }
                else
                {
                    i = path.i();
                    j = i;
                    path.list().addAll( reverse() );
                    list = path.list();
                }
            }
        }
    }
    
    void addCost( int connectingCost ) { cost += connectingCost; }
    
    int cost() { return cost; }
    
    int  i()    { return i; }
    
    int  index() { return index; }
    
    void index( int index ) { this.index = index; }
    
    int  j()    { return j; }
    
    List list() { return list; }
    
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
        list = reversedList;
        return reversedList;
    }
    
    public String toString() 
    { 
        String listString = "";
        for ( Iterator iterator = list.listIterator(); iterator.hasNext(); )
        {
            listString += " " + (Integer) iterator.next();
        }
        return "index: " + index + ", i: " + i + ", j: " + j + 
               ", cost: " + cost + "\n list: " + listString;
    }
}
