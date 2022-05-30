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

import edu.ucsb.cs.jicos.admin.AdministrableHostMatlab;
import edu.ucsb.cs.jicos.foundation.Service;
import edu.ucsb.cs.jicos.services.Property;
import edu.ucsb.cs.jicos.services.external.services.HostExternal;

import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.util.List;
import java.util.LinkedList;

/**
 * Extends a Jicos Host for Matlab tasks (TaskMatlab).
 * 
 * @see edu.ucsb.cs.jicos.services.external.matlab.TaskMatlab
 * @author Andy Pippin
 */
public final class HostMatlab extends HostExternal implements AdministrableHostMatlab
{
    //
    //-- Constructors --------------------------------------------------------

    /**
     * Create a Host associated with TaskMatlab tasks, start up a Matlab engine,
     * and notify a TaskServerMatlab that it exists.
     * 
     * @param taskServer
     *            A TaskServerMatlab instance.
     * @see edu.ucsb.cs.jicos.services.external.matlab.TaskserverMatlab
     */
    public HostMatlab(Service taskServer) throws RemoteException,
            MatlabProxyNotFoundException, MatlabException
    {
        super( taskServer, false, 1, TaskMatlab.class, new MatlabImpl() );
    }

    /**
     * Not used by Jicos applications.
     * 
     * @param args
     *            Not used by Jicos applications.
     * @throws Exception
     *             Not used by Jicos applications.
     */
    public static void main( String args[] ) throws Exception
    {

        System.setSecurityManager( new RMISecurityManager() );

        // Load the host properties.
        Property.loadProperties( args );
        Property.load( System.getProperty( "jicos.services.host.config" ) );

        // process command-line arguments: Put machine domain names in a List
        List taskServerDomainNameList = new LinkedList();
        for (int i = 0; i < args.length; i++)
        {
            taskServerDomainNameList.add( args[i] );
        }

        // discover my TaskServer
        Service taskServer = discoverTaskServer( taskServerDomainNameList );

        int $processors = Runtime.getRuntime().availableProcessors();

        try
        {
            new HostMatlab( taskServer ); // why is this not garbage
                                          // immediately?
        } catch (MatlabProxyNotFoundException matlabProxyNotFoundException)
        {
            System.out.flush();
            System.err.println( "Matlab engine was not found: "
                    + matlabProxyNotFoundException.getMessage() );
            System.err.flush();
        } catch (MatlabException matlabException)
        {
            System.out.flush();
            System.err.println( "Error starting Matlab engine: "
                    + matlabException.getMessage() );
            System.err.flush();
        }
    }
}
