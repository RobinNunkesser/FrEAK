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
 * The interface of an object that represents a solution to a TSP 
 * branch &amp; bound search.
 *
 * @version 1.0 
 * @author  Peter Cappello
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp;

import edu.ucsb.cs.jicos.services.*;


public interface Solution extends java.io.Serializable
{           
    /** returns a Q of Solution objects that are the children of this Solution.
     * @return a Q of [partial] Solution objects that represent the children
     * of the node in the search tree represented by this [partial] Solution.
     */    
    public Q getChildren( Environment environment );
    
    /** returns the lower bound on the cost of any complete Solution that is an
     * extension of this partial Solution.
     * <p>
     * <b>Contract:</b> Multiple calls to get lower bound may occur (it is
     * called again by MinSolution). Value caching on this method should be
     * implemented. </p>
     * @return the lower bound on the cost of any complete Solution that is an
     * extension of this partial Solution.
     */    
    public int getLowerBound( Environment environment );
    
    //public Object getValue();
    
    public int getUpperBound( Environment environment );
    
    /** returns true if and only if the partial Solution is, in fact, complete.
     * @return true if and only if the partial Solution is, in fact, complete.
     */    
    public boolean isComplete( Environment environment );
    
    public void reduce( Environment environment );

    /** returns a String representation of this partial solution.
     * @return a String representation of this partial solution.
     */    
    public String toString();
}
