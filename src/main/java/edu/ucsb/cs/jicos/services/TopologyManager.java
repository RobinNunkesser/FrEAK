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
 * Manages the topology of the TaskServer network. Currently, it maintains the
 * TaskServers as a 2D torous (as square as possible).
 *
 * TaskServerExternal objects are children only of the root TaskServer. They
 * have no other neighbors. References to them are held in a Set
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;
import edu.ucsb.cs.jicos.services.external.services.TaskExternal;
import java.util.*;


final class TopologyManager 
{
    // constants
    static final int ROOT     = 0;
    static final int NORTH    = 0;
    static final int SOUTH    = 1;
    static final int EAST     = 2;
    static final int WEST     = 3;
    static final int ME       = 4;  // neighbors[ ME ] == The new TaskServer.
    static final int EXTERNAL = -1; // I am a TaskServerExternal
    
    // attributes
    private ProxyManager proxyManager;
    private Set taskServerExternalSet = new HashSet();
    private List mesh = new ArrayList();
    private int next;
    private TaskServerServiceInfo rootServerServiceInfo;
    
    TopologyManager( ProxyManager proxyManager ) 
    { 
        this.proxyManager = proxyManager;
    }
    
    TaskServerServiceInfo[] add( TaskServerServiceInfo taskServerServiceInfo )
    {
        TaskServerServiceInfo[] neighbors = new TaskServerServiceInfo[ 5 ];
        neighbors[ ME ] = taskServerServiceInfo;
        
        Class taskClass = taskServerServiceInfo.taskClass();
        if ( TaskExternal.class.isAssignableFrom( taskClass ) )
        {
            // an instance of TaskServerExternal: Do not put in mesh.
            taskServerExternalSet.add( taskServerServiceInfo );
            
            // registration of this taskserver with the root taskserver
            Point point =  new Point( -1, - 1 ); // ==> Outside mesh
            taskServerServiceInfo.point( point );
            neighbors[ ROOT ] = rootServerServiceInfo;
            Service rootService = rootServerServiceInfo.service(); 
            Proxy rootProxy = proxyManager.getProxy( rootService );
            Command command = new RegisterNeighborTaskServer( taskServerServiceInfo, EXTERNAL );
            rootProxy.sendCommand( command );           
            return neighbors;
        }
        
        List row;
        Point point;
        int rows = mesh.size();
        List firstRow;
        List lastRow;   
                                      
        // if empty, add 1st row
        if ( mesh.isEmpty() )
        {
            neighbors[ EAST  ] = null; 
            neighbors[ SOUTH ] = null; 
            neighbors[ WEST  ] = null;
            neighbors[ NORTH ] = null;
            mesh.add( new ArrayList() );
            row = (List) mesh.get( 0 );
            row.add( taskServerServiceInfo );
            point =  new Point( 0, 0 );
            rootServerServiceInfo = taskServerServiceInfo;
        }        
        else 
        {
            firstRow = (List) mesh.get( 0 );
            lastRow = (List) mesh.get( rows - 1 );            
            if ( rows == lastRow.size() && rows == firstRow.size() ) // square?
            {
                // start a new column
                neighbors[ EAST  ] = (TaskServerServiceInfo) firstRow.get( 0 );
                neighbors[ SOUTH ] = null;                              
                neighbors[ WEST  ] = (TaskServerServiceInfo) firstRow.get( firstRow.size() - 1 );
                neighbors[ NORTH ] = null;
                firstRow.add( taskServerServiceInfo );
                next = 1;
                point =  new Point( 0, firstRow.size() - 1 );
                
            }
            // add to last column?
            else if ( firstRow.size() > rows && firstRow.size() > lastRow.size()
                      && rows > 1 )
            {                
                List northRow = (List) mesh.get( next - 1 );
                List westRow  = (List) mesh.get( next );
                neighbors[ EAST  ] = (TaskServerServiceInfo) westRow.get( 0 );
                neighbors[ SOUTH ] = (TaskServerServiceInfo) firstRow.get( firstRow.size() - 1 );
                neighbors[ WEST  ] = (TaskServerServiceInfo) westRow.get( westRow.size() - 1 );
                neighbors[ NORTH ] = (TaskServerServiceInfo) northRow.get( northRow.size() - 1 );
                ((List) mesh.get( next )).add( taskServerServiceInfo );
                point =  new Point( next, ((List) mesh.get( next++ )).size() - 1 );
            }
            // add a new row?
            else if ( firstRow.size() == lastRow.size() )
            {
                neighbors[ EAST  ] = null;
                neighbors[ SOUTH ] = (TaskServerServiceInfo) firstRow.get( 0 );
                neighbors[ WEST  ] = null;
                neighbors[ NORTH ] = (TaskServerServiceInfo) lastRow.get( 0 );
                List list = new ArrayList();
                list.add( taskServerServiceInfo );
                mesh.add( list );
                point =  new Point( rows, 0 );
            }
            else // add new entry to last row
            {
                List next2LastRow = (List) mesh.get( mesh.size() - 2 );
                neighbors[ EAST  ] = (TaskServerServiceInfo) lastRow.get( 0 );
                neighbors[ SOUTH ] = (TaskServerServiceInfo) firstRow.get( lastRow.size() );
                neighbors[ WEST  ] = (TaskServerServiceInfo) lastRow.get( lastRow.size() - 1 );
                neighbors[ NORTH ] = (TaskServerServiceInfo) next2LastRow.get( lastRow.size() );
                lastRow.add( taskServerServiceInfo );
                point = new Point( rows - 1, lastRow.size() - 1 );
            }
        }
        //System.out.println("TopologyManager.add: point: " + point);
        taskServerServiceInfo.point( point );
        
        // Tell appropriate existing TaskServers that they have a new neighbor.
        Service neighborService;
        Proxy neighborProxy;
        Command command;
        if ( neighbors[ EAST ] != null )
        {
            // inform east neighbor that it has a new WEST neighbor.            
            neighborService = neighbors[ EAST ].service();            
            neighborProxy = proxyManager.getProxy( neighborService );
            command = new RegisterNeighborTaskServer( taskServerServiceInfo, WEST );
            neighborProxy.sendCommand( command );
        }
        if ( neighbors[ SOUTH ] != null )
        {
            // inform SOUTH neighbor that it has a new NORTH neighbor.            
            neighborService = neighbors[ SOUTH ].service();            
            neighborProxy = proxyManager.getProxy( neighborService );
            command = new RegisterNeighborTaskServer( taskServerServiceInfo, NORTH );
            neighborProxy.sendCommand( command );
        }
         if ( neighbors[ WEST ] != null )
        {
            // inform WEST neighbor that it has a new EAST neighbor.            
            neighborService = neighbors[ WEST ].service();            
            neighborProxy = proxyManager.getProxy( neighborService );
            command = new RegisterNeighborTaskServer( taskServerServiceInfo, EAST );
            neighborProxy.sendCommand( command );
        }
         if ( neighbors[ NORTH ] != null )
        {
            // inform NORTH neighbor that it has a new SOUTH neighbor.            
            neighborService = neighbors[ NORTH ].service();            
            neighborProxy = proxyManager.getProxy( neighborService );
            command = new RegisterNeighborTaskServer( taskServerServiceInfo, SOUTH );
            neighborProxy.sendCommand( command );
        }
        
        return neighbors;
    }
}
