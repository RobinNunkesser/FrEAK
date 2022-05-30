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
 *  TaskServer monitor
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services; 

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.util.*;

public class TaskServerMonitor
{
    // constants
    private final static CommandSynchronous GET_TASKS = new GetTaskServerTasks();
     
     /** Not used by Jicos applications.
      * @param args Not used by Jicos applications.
      * @throws Exception Not used by Jicos applications.
      */     
     public static void main(String args[]) throws Exception
    {
         System.setSecurityManager( new RMISecurityManager() );

         // Load the host properties.
        Property.loadProperties( args );
     	Property.load( System.getProperty( "jicos.services.host.config" ) );
        
        String taskServerDomainName = args[0];
        String url = "//" + taskServerDomainName + ":" + RegistrationManager.PORT + "/" + TaskServer.SERVICE_NAME;		
        
        Service taskServer = (Service) Naming.lookup(url);
        
        // get Task objects from TaskServer's Session.Tasks
        List tasks = (List) taskServer.executeCommand( null, GET_TASKS );
        for ( Iterator iterator = tasks.iterator(); iterator.hasNext(); )
        {
            Task task = (Task) iterator.next();
            System.out.println( task );
        }
    }       
}
