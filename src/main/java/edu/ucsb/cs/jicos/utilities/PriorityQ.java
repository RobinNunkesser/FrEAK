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
 * A thread-safe priority queue
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.utilities;

import edu.ucsb.cs.jicos.foundation.*;
import java.util.*;


public final class PriorityQ implements Q 
{
    private LinkedList[] qq;
    private Class2Int class2Level;
    
    public PriorityQ( int levels, Class[][] class2IntArray ) 
    {
        qq = new LinkedList[ levels ];
        for ( int i = 0; i < levels; i++ )
        {
            qq[i] = new LinkedList();
        }
        class2Level = new Class2Int ( class2IntArray );
    }
    
    public synchronized boolean add ( Object object ) //throws Exception
    {
        if ( object == null )
        {
            throw new IllegalArgumentException("Cannot add null Command.");
        }
        int level = class2Level.map ( object );
        qq[ level ].add ( object );
        notify();
        return true;
    }
    
    public synchronized boolean isEmpty() 
    {
        for ( int i = 0; i < qq.length; i++ )
        {
            if ( ! qq[i].isEmpty() )
            {
                return false;
            }
        }
        return true;
    }
    
    public synchronized Object remove ()
    {
        while ( true )
        {
            for ( int i = 0; i < qq.length; i++ )
            {
                if ( ! qq[i].isEmpty() )
                {
                    return qq[i].removeFirst();
                }
            }
            try
            {
                wait();
            }
            catch ( InterruptedException ignore ) {}
        }
    }
  
    public int size ( int level ) 
    {
        if ( level < 0 || level > qq.length - 1 )
        {
            throw new IllegalArgumentException(
                                           "Priority Q has no level " + level );
        }
        return qq[ level ].size(); 
    }
}
