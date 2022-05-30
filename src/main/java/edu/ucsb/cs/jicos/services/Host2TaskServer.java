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
 * A TreeMap that enables assignment of a new Host to a TaskServer.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
//
import java.util.*;


final class Host2TaskServer extends TreeMap 
{    
    // constants
    private final static Integer ZERO = new Integer( 0 );
    
    // add a new TaskServer to data structure with 0 Hosts assigned initially.
    synchronized void add( ServiceName serviceName )
    {
        List list = (List) get( ZERO );
        if ( list == null )
        {
            list = new LinkedList();
        }
        list.add( serviceName );
        super.put( ZERO, list );
        //System.out.println("Host2TaskServer.add: size of ZERO: " + list.size() );
    }
    
    /* Get the ServiceName of a TaskServer with fewest Hosts assigned:
     *   Get the List of TaskServer ServiceNames assigned the fewest Hosts.
     *   Put that TaskServer on the List at next level. 
     *   Return the ServiceName.
     */
    synchronized ServiceName get()
    {
        // pre-conditions
        assert ! isEmpty();
        
        Integer firstKey = (Integer) firstKey(); 
        /*
        System.out.println("Host2TaskServer.get: returning TaskServer with " + 
                                               firstKey.intValue() + " hosts.");
         */
        List list = (List) get( firstKey );
        
        assert list.get( 0 ) != null;
        ServiceName serviceName = (ServiceName) list.remove( 0 );
        if ( list.isEmpty() )
        {
            remove( firstKey );
        }
        Integer nextKey = new Integer( firstKey.intValue() + 1 );
        list = (List) get( nextKey );
        if ( list == null )
        {
            list = new LinkedList();
        }
        list.add( serviceName );
        put( nextKey, list );
        
        return serviceName;
    }
    
    synchronized ServiceName get( String taskServerDomainName )
    {
        // pre-conditions
        assert ! isEmpty();
        
        Integer firstKey = (Integer) firstKey(); 
        /*
        System.out.println("Host2TaskServer.get: returning TaskServer with " + 
                                               firstKey.intValue() + " hosts.");
         */
        List list = (List) get( firstKey );
        
        assert list.get( 0 ) != null;
        ServiceName serviceName = (ServiceName) list.remove( 0 );
        if ( list.isEmpty() )
        {
            remove( firstKey );
        }
        Integer nextKey = new Integer( firstKey.intValue() + 1 );
        list = (List) get( nextKey );
        if ( list == null )
        {
            list = new LinkedList();
        }
        list.add( serviceName );
        put( nextKey, list );
        
        return serviceName;
    }   
}
