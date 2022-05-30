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
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import java.util.*;

/**
 * !! Should make this a Set of some kind: No need for a Map.
 *
 * A threqd-safe HashMap of Result values.
 * @author  Peter Cappello
 */
public final class ResultTable extends HashMap 
{
    
     /** put an object into the table.
     * @param object The object to be added to the table.
     * @return true (as per the general contract of Collection.add).
     */
    public synchronized void put( ResultId key, Result result ) 
    {
        super.put( key, result );
        notify();
    }
    
    /** Remove an object according to the FIFO discipline.
     * @return the removed object.
     */
    public synchronized Result remove()
    { 
        while ( isEmpty() )
        {                                 
            try 
            {
                wait(); 
            } 
            catch ( InterruptedException ignore ){}                     
        }
        assert ! isEmpty();
        Iterator keySetIterator = keySet().iterator();
        ResultId key = (ResultId) keySetIterator.next();
        return (Result) remove( key );
    }
    
    public synchronized boolean isEmpty() { return super.isEmpty(); }
    
    /** Returns a String representation of the ResultTable as a HashSet.
     * @return a String representation of the ResultTable as a HashSet.
     */    
    public String toString() { return getClass() + ": " + super.toString(); }   
}