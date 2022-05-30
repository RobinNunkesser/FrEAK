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
 *  A thread-safe queue of Objects.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.utilities;

import edu.ucsb.cs.jicos.foundation.*;

import java.util.LinkedList;


public final class Qu extends LinkedList implements Q
{
    /** Add an object to the queue.
     * @param object The object to be added to the queue.
     * @return true (as per the general contract of Collection.add).
     */
    public synchronized boolean add( Object object ) 
    {
        super.add( object );
        if ( object == null )
        {
            throw new IllegalArgumentException();
        }
        
        // Was the queue empty immediately prior to this add?
        if ( size() == 1 )
        {
            notify(); 
        }
        return true;
    }
    
    /** Get the object at the head of the queue.
     * @return the object at the head of the queue.
     */    
    public synchronized Object get() { return getFirst(); }
    
    /** Remove an object according to the FIFO discipline.
     * @return the removed object.
     */
    public synchronized Object remove()
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
        return removeFirst();
    }
    
    /** Is the queue empty?
     * @return true if and only if the queue is empty.
     */    
    public synchronized boolean isEmpty() { return super.isEmpty(); }
    
    /** Returns a String representation of the object.
     * @return a String representation of the object.
     */    
    public String toString() { return getClass() + ": " + super.toString(); }
}
