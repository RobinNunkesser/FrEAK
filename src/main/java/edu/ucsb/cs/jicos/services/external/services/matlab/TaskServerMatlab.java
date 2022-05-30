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

package edu.ucsb.cs.jicos.services.external.services.matlab;

import edu.ucsb.cs.jicos.admin.AdministrableTaskServerMatlab;
import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.external.services.TaskServerExternal;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

/**
 * 
 * @author Pete Cappello
 */
public final class TaskServerMatlab extends TaskServerExternal implements AdministrableTaskServerMatlab {
    //
    //-- Constants -----------------------------------------------------------

    private static String SERVICE_NAME = "TaskServerMatlab";

    //
    //-- Constructors --------------------------------------------------------

    /**
     * Create a task server for the TaskMatlab service class.
     * 
     * @param hsp
     *            The HSP to associate with.
     * @throws RemoteException
     *             TaskServer implements the Remote interface.
     * @see edu.ucsb.cs.jicos.services.TaskServer
     */
    public TaskServerMatlab(Service hsp) throws RemoteException
    {
        super( hsp, TaskMatlab.class );
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * @param args
     *            the command line arguments
     */
    public static void main( String[] args ) throws Exception
    {

        if ((args.length == 0)
                || ((1 == args.length) && (args[0].equalsIgnoreCase( "-help" ))))
        {
            System.out
                    .println( "Usage: <java> TaskServerMatlab Hsp-domain-name [#hosts]" );
            System.exit( 1 );
        }

        System.setSecurityManager( new RMISecurityManager() );

        // Load the taskserver properties.
        Property.loadProperties( args );
        Property
                .load( System.getProperty( "jicos.services.taskserver.config" ) );

        // get remote reference to Hsp
        String hspMachineDomainName = args[0];
        HspAgent agent = new HspAgent( hspMachineDomainName );
        Service hsp = agent.getHsp();

        TaskServer taskServer = new TaskServerMatlab( hsp );

        // register task server
        Registry registry = RegistrationManager.locateRegistry();
        //listRegistry( registry );
        registry.bind( SERVICE_NAME, taskServer );

        int $Hosts = 0;
        if (args.length > 1)
        {
            $Hosts = Integer.parseInt( args[1] );
        }

        // construct hosts
        for (int i = 0; i < $Hosts; i++)
        {
            new HostMatlab( taskServer );
        }

        ServiceName serviceName = new ServiceName( taskServer );
	LogManager.getLogger().log( LogManager.CONFIG, 
		"There is now a TaskServerMatlab at: "
			+ serviceName.toStringWithSpace() );
    }

    /**
     * Method for debugging: Dumps the contents of the RMI registry.
     * 
     * @param registry
     *            The RMI registry.
     */
    private static void listRegistry( Registry registry )
    {
        if (null != registry)
        {
            try
            {
                String[] name = registry.list();
                System.out.println( "The names in the registry are:" );

                System.out.println( "------------------------------" );
                for (int n = 0; n < name.length; ++n)
                {
                    System.out.println( "  " + name[n] );
                }
                System.out.println( "------------------------------" );
            } catch (RemoteException remoteException)
            {
            }
        }
    }
}
