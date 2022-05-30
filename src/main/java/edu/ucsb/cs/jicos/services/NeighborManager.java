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

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.external.services.TaskExternal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages a TaskServer's neighbor TaskServer TaskServerServiceInfo objects.
 * Currently, these are managed under the assumption that the TaskServer
 * neighborhood is North, East, South, and West: The TaskServer topology is a 2D
 * torous.
 *
 * Every network is connected and has a spanning subtree (used for efficient
 * propagation of Command objects, such as login). The subtree is operationally
 * defined by the methods setParent and setChildren.
 *
 * @author Peter Cappello
 */
final class NeighborManager extends HashSet 
{
    // Constants
    static final int EASTWEST   = 5; // must be > 4. See TopologyManager
    static final int NORTHSOUTH = 6; // must be > 4. See TopologyManager
    
    // Attributes
    private TaskServerServiceInfo myTaskServerServiceInfo;
    private TaskServer myTaskServer;
    private RemoteExceptionHandler remoteExceptionHandler;
    private TaskServerProxy parent;
    private Set children = new HashSet();
    
    private TaskServerProxy root;
    private TaskServerProxy north;
    private TaskServerProxy east;
    private TaskServerProxy south;
    private TaskServerProxy west;
    
    public NeighborManager( TaskServerServiceInfo[] taskServerServiceInfoArray, 
                            TaskServer myTaskServer, 
                            RemoteExceptionHandler remoteExceptionHandler 
                          ) 
    {
        this.myTaskServer = myTaskServer;
        this.remoteExceptionHandler = remoteExceptionHandler;
        myTaskServerServiceInfo = taskServerServiceInfoArray[ TopologyManager.ME ];
        
        TaskServerServiceInfo rootInfo, eastInfo, southInfo, westInfo, northInfo;
        eastInfo  = taskServerServiceInfoArray[ TopologyManager.EAST  ];
        southInfo = taskServerServiceInfoArray[ TopologyManager.SOUTH ];
        westInfo  = taskServerServiceInfoArray[ TopologyManager.WEST  ];
        northInfo = taskServerServiceInfoArray[ TopologyManager.NORTH ];
        
        // if I am a TaskServerExternal, the root TaskServer is my only neighbor
        Class myTaskClass = myTaskServerServiceInfo.taskClass();
        if ( TaskExternal.class.isAssignableFrom( myTaskClass ) )
        {
            rootInfo = taskServerServiceInfoArray[ TopologyManager.ROOT  ];
            root =  new TaskServerProxy( rootInfo, myTaskServer, remoteExceptionHandler );
            add( root );
            parent = root;
            children = new HashSet(); // I have no children.
            return;
        }
        
        //Command command;
        if ( eastInfo != null )
        {
            east =  new TaskServerProxy( eastInfo, myTaskServer, remoteExceptionHandler );
            add( east );
        }
        
        if ( southInfo != null )
        {
            south =  new TaskServerProxy( southInfo, myTaskServer, remoteExceptionHandler );
            add( south );
        }
        
        if ( westInfo != null )
        {            
            if ( eastInfo != westInfo )
            {
                west =  new TaskServerProxy( westInfo, myTaskServer, remoteExceptionHandler );
                add( west );
            }
            else
            {
                west = east;
            }                        
        }
        
        if ( northInfo != null )
        {            
            if ( southInfo != northInfo )
            {
                north =  new TaskServerProxy( northInfo, myTaskServer, remoteExceptionHandler );
                add( north );
            }
            else
            {
                north = south;
            }                        
        }
        
        setParent();
    }
    
    Set getChildren() { return children; }
    
    /* A TaskServer's south neighbor, if any, is a child. 
     * If the TaskServer is in row 0, its east neighbor, if any, is a child.
     */
    synchronized void setChildren()
    {        
        Point point = myTaskServerServiceInfo.point();
        int myRow = point.row();
        int myCol = point.col();
        Logger logger = LogManager.getLogger( this );
        children = new HashSet();
        
        logger.log( LogManager.DEBUG, "me: " + point.toString() );

        if ( myRow == 0 && east != null )
        {
            int eastCol = ((TaskServerServiceInfo) east.serviceName()).point().col();
            if ( eastCol > myCol )
            {
                children.add( east );
            }            
        }
        if ( south != null )
        {
            int southRow = ((TaskServerServiceInfo) south.serviceName()).point().row();
            if ( southRow > myRow )
            {
                children.add( south );
            }
        }
    }
    
    Proxy getParent() { return parent; }      
    
    Point point() { return myTaskServerServiceInfo.point(); }
    
    /* For the torous, the new neighbor can be NORTH, EAST, SOUTH, or WEST.
     */
    synchronized TaskServerProxy setNeighbor
                          ( 
                            TaskServerServiceInfo neighborTaskServerServiceInfo,
                            int direction
                          )
    {                
        // construct neighbor TaskServerProxy
        TaskServerProxy neighborTaskServerProxy = new TaskServerProxy( 
           neighborTaskServerServiceInfo, myTaskServer, remoteExceptionHandler);
        
        switch ( direction )
        {
            case TopologyManager.NORTH:
                remove( north );
                north = neighborTaskServerProxy;
                break;
            case TopologyManager.SOUTH:
                remove( south );
                south = neighborTaskServerProxy;
                break;
            case TopologyManager.EAST:
                remove( east );
                east = neighborTaskServerProxy;
                break;
            case TopologyManager.WEST:
                remove( west );
                west = neighborTaskServerProxy;
                break;
            case EASTWEST:
                remove( east );
                remove( west );
                east = neighborTaskServerProxy;
                west = neighborTaskServerProxy;
                break;
            case NORTHSOUTH:
                remove( north );
                remove( south );
                north = neighborTaskServerProxy;
                south = neighborTaskServerProxy;
                break;
                
            case TopologyManager.EXTERNAL:
                add( neighborTaskServerProxy );
                children.add( neighborTaskServerProxy );
                return neighborTaskServerProxy;
                
            default:
                assert false; // default case should never be executed.
        }       
        add( neighborTaskServerProxy );               
                 
        //setParent(); // !! If TaskServer fails, new neighbor can be parent.   
        setChildren();
        return neighborTaskServerProxy;
    }
    
    synchronized void setParent()
    {
        parent = north; // the most common case
        
        Point point = myTaskServerServiceInfo.point();
        if ( point.row() == 0 )
        {
            parent = west; // west == null, if point.col() == 0, which is right
        }    
    }
}