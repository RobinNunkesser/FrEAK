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

import edu.ucsb.cs.jicos.services.external.services.TaskServerExternal;


import java.util.*;
/**
 * Priority queue of LinkedList objects.
 * A LinkedList has unassigned Task objects at the same level in the dag.
*/
final class PQTasks extends TreeMap
{
    private final static int THRESHOLD = 200;
    
    private int size; // # tasks at all levels
    private TaskServer taskServer;
    
    // DEBUG
    private int waiters;
    int getWaiters() 
    {
        System.out.println("PQTasks.getWaiters: size: " + size );
        return waiters; 
    }
    // END DEBUG
    
    PQTasks( TaskServer taskServer ) { this.taskServer = taskServer; }
    
    public synchronized void clear() { super.clear(); }
    
    public synchronized void dump()
    {
        Collection values = values();
        for ( Iterator iterator = values.iterator(); iterator.hasNext(); )
        {
            LinkedList list = (LinkedList) iterator.next();
            for ( ListIterator listIterator = list.listIterator(); listIterator.hasNext(); )
            {
                Task task = (Task) listIterator.next();
                System.out.println( "PQTasks.dump: " + task.getTaskId() );
            }
        }
    }
    
    synchronized void put ( Task task )
    {
        Integer key = new Integer ( task.getLevel() );
        LinkedList list = (LinkedList) get ( key );
        if ( list == null )
        {
            list = new LinkedList();
            put ( key, list );
        }
        assert list != null;
        assert get( key ) == list;       
        list.add( task );
        assert list.contains( task );
        size++;
        notify();
    }

    /**
     * return unassigned task.
     *
     * if size > highThreshold()
     *     take from lowest level: reduce new task arrival rate;
     * else
     *     take from highest level: increase new task arrival rate.
     */
    synchronized Task remove() //throws Exception
    {
        while ( true )
        {
            /* iteration invariant: remove an empty List of complete tasks
             * OR return a ready task OR both.
            */
            while ( ! isEmpty() )
            {
                Task task;            
                /* Get the firstKey (high level in DAG), if want rate of Task arrivals to 
                * increase; else get the lastKey (low level in DAG).
                */
                Integer key = ( reduceTaskSpawnRate() ) ? (Integer) lastKey() : (Integer) firstKey();
                LinkedList list = (LinkedList) get( key );
            
                // remove most recently added Task at this level
                task = (Task) list.removeLast();
                assert null != task;

                if ( list.isEmpty() ) 
                {
                    remove( key );
                }
                size--;
                
                // DEBUG
//                task.numAssignments++;
//                if (task.numAssignments > 1 )
//                {
//                    System.out.println("PQTasks: " + task.getTaskId() + " numAssignments: " + task.numAssignments );
//                }
                
                return task;
            }
            assert isEmpty();            
            if ( ! (taskServer instanceof TaskServerExternal) )
            {
                // ask neighbor taskServers for unassigned tasks
                taskServer.issueRequestTasks();
            }
            try 
            {
                waiters++; // DEBUG
                wait(); // notifier: put
                waiters--; // DEBUG
            }
            catch ( InterruptedException ignore ) {}
        }
    }
    
    synchronized Task[] removeTasks( int factor ) //throws Exception
    {        
        // return a fraction of all Task objects, but at least 1.
        int $tasks = size / factor;
        if ( $tasks < 1 )
        {
            $tasks = 1;
        }
        Task[] tasks = new Task[ $tasks ];
        for ( int i = 0; i < $tasks; i++ )
        {
            tasks[ i ] = remove();
        }
        //System.out.println("PQTasks.removeTasks: returning." );
        return tasks;
    }
    
    private boolean reduceTaskSpawnRate() 
    { 
        return ( size > THRESHOLD ) ? true : false;
    }
}