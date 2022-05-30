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
 * description
 *
 * @author  pippin
 */

package edu.ucsb.cs.jicos.admin.common;

import edu.ucsb.cs.jicos.admin.Console;
import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.foundation.RegistrationManager;
import edu.ucsb.cs.jicos.foundation.Service;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class AdminUtilities
{
    //
    //-- Constants -----------------------------------------------------------
    
    public static final String BASE_POLICY = "security.policy";
        
    private static final String DotBASE_POLICY = "." + BASE_POLICY;
    private static final String DotBASE_POLICYDot = "." + BASE_POLICY + ".";
    
    /**
     * Check if a hostname is the local host.
     * 
     * @param hostname  Name of the host (may be an address).
     * @return Localhost (<CODE>true</CODE>), or not (<CODE>false</CODE>). 
     */
    public static boolean isLocalhost( String hostname )
    {
        assert null != hostname : "hostname to check cannot be null!";

        boolean isLocalhost = false;

        try
        {
            InetAddress remoteHost = InetAddress.getByName( hostname );
            InetAddress localHost = InetAddress.getLocalHost();
            InetAddress loopback = InetAddress.getByAddress( new byte[]
                { 127, 0, 0, 1 } );
            
            if ( remoteHost.equals( localHost )
                    || remoteHost.equals( loopback ) )
            {
                isLocalhost = true;
            }
        }
        catch( Exception anyException )
        {
        }
        
        return( isLocalhost );
    }
    
    
    /**
     * Get the name of the policy file.
     * 
     * In decreasing priority:
     * <OL>
     *   <LI>&lt;<I>launcher</I>&gt;.security.policy.&lt<I>service</I>&gt;</LI>
     *   <LI>&lt;<I>launcher</I>&gt;.security.policy</LI>
     *   <LI>jicos.security.policy.&lt<I>service</I>&gt;</LI>
     *   <LI>jicos.security.policy</LI>
     *   <LI>java.security.policy</LI>
     *   <LI><CODE>policy/policy</CODE></LI>
     * </OL>
     * 
     * @param launcher Name of program launching service.
     * @param service  Name of the service.
     * @return  The name of the policy file.
     * @throws IllegalArgumentException  If the policy file is in a Jar and cannot be fixed.
     */
    public static String getPolicyFilename( String launcher, String service )
            throws IllegalArgumentException
    {
        String policy = null;
        
        String lName = "";
        String sName = "";
        
        if( null != launcher )
        {
            lName = launcher;
        }
        if( null != service )
        {
            sName = service;
        }

        int attempt = 0;
        while( null == policy )
        {
            switch( ++attempt )
            {
                case 1:
                    policy = System.getProperty( lName + DotBASE_POLICYDot + sName );
                    break;
                case 2:
                    policy = System.getProperty( lName + DotBASE_POLICY );
                    break;
                case 3:
                    policy = System.getProperty( "jicos" + DotBASE_POLICYDot + sName );
                    break;
                case 4:
                    policy = System.getProperty( "jicos" + DotBASE_POLICY );
                    break;
                case 5:
                    policy = System.getProperty( "java.security.policy" );
                    break;
                case 6:
                    policy = "policy/policy";
                    break;
            }
        }
            
        // Fix policy filename.
        if( null != policy )
        {
            if( policy.startsWith( "jar:file:" ) )
            {
                int ndx;
                if( -1 != (ndx = policy.indexOf( "/lib/jicos-system.jar!" )) )
                {
                    policy = policy.substring( 9 ).replaceFirst(
                            "/lib/jicos-system.jar!", "" );
                }
                else
                {
                    throw new IllegalArgumentException(
                            "Cannot use a policy inside a jar file ("
                                    + policy + ")" );
                }
            }
            
            else if( policy.startsWith( "file:" ) )
            {
                policy = policy.substring( 5 );
            }
        }
        
        return( policy );
    }
    /**
     *   Helper function.  Look for the Console in the RMI registry in the
     * default places (Console.JICOS_CONSOLE_HOST, or "localhost");
     * 
     * @param service   The service registering.
     * @param myParent  The parent of the registering service.
     * @return  Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     */
    public static boolean registerWithConsole( Service service, Service myParent )
    {
        return( registerWithConsole( service, myParent, null ) );
    }
    
    /**
     * Register with a Console at the specified RMI registry.
     * 
     * @param service   The service registering.
     * @param myParent  The parent of the registering service.
     * @param consoleHost  The machine of the Console. 
     * @return  Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     */
    public static boolean registerWithConsole( Service service, Service myParent, String consoleHost )
    {
        boolean registered = false;

        // Get the property of where the jicos console is located.
        String registryHost = consoleHost;
        if (null == registryHost)
        {
            registryHost = System.getProperty( Console.JICOS_CONSOLE_HOST,
            "localhost" );
        }
        
        /*
        LogManager.getLogger( AdminUtilities.class ).log( LogManager.DEBUG,
                "service=\"" + service + '"' );
        LogManager.getLogger( AdminUtilities.class ).log( LogManager.DEBUG,
                "myParent=\"" + myParent + '"' );
        LogManager.getLogger( AdminUtilities.class ).log( LogManager.DEBUG,
                "registryUrl=\"" + registryUrl + '"' );
        */

        try
        {
            // Get the console, barf if unable to get it.
            Registry registry = LocateRegistry.getRegistry( registryHost, RegistrationManager.PORT );
            Console console = (Console) registry.lookup( Console.SERVICE_NAME );

            // Register.
            registered = console.register( service, myParent );
            
            LogManager.getLogger( AdminUtilities.class ).log( LogManager.INFO,
                    "registered @ " + registryHost );            
        }
        catch (Exception anyException)
        {
            LogManager.getLogger( AdminUtilities.class ).log( LogManager.WARNING,
                    "Couldn't register with console: \"" + registryHost + '"' );
        }

        return (registered);
    }

}
