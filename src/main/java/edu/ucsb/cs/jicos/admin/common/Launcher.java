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
 * @author pippin
 */

package edu.ucsb.cs.jicos.admin.common;

import edu.ucsb.cs.jicos.admin.chameleon.Chameleon;
import edu.ucsb.cs.jicos.admin.chameleon.ChameleonImpl;
import edu.ucsb.cs.jicos.admin.Console;
import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.foundation.RegistrationManager;
import edu.ucsb.cs.jicos.services.Host;
import edu.ucsb.cs.jicos.services.Hsp;
import edu.ucsb.cs.jicos.services.TaskServer;
import edu.ucsb.cs.jicos.services.external.services.matlab.TaskServerMatlab;
import edu.ucsb.cs.jicos.services.external.services.matlab.HostMatlab;

import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

public final class Launcher
{
    //
    //-- Constants -----------------------------------------------------------

    public static final String LAUNCHER_CMD_JAVA = "launcher.command.java";
    public static final String LAUNCHER_CMD_SSH  = "launcher.command.ssh";

    public static final boolean ALLOW_JVM_OPTIONS = true;
    public static final boolean ALLOW_SERVICE_OPTIONS = true;

    private static Logger logger = LogManager.getLogger( Launcher.class );

    public static final int EXTTYPE_None = 0;
    public static final int EXTTYPE_Matlab = 1;

    private static final String HSP_CLASS_NAME = Hsp.class.getName();
    private static final String TASKSERVER_CLASS_NAME = TaskServer.class.getName();
    private static final String TASKSERVERMATLAB_CLASS_NAME = TaskServerMatlab.class.getName();
    private static final String HOST_CLASS_NAME = Host.class.getName();
    private static final String HOSTMATLAB_CLASS_NAME = HostMatlab.class.getName();
    private static final String CONSOLE_CLASS_NAME = Console.class.getName();

    //
    //-- Helper Methods ------------------------------------------------------

    /**
     * Start an HSP.
     * 
     * @param jvmOptions
     *            Options to the JVM.
     * @param numHosts
     *            Number of hosts to start up.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     */
    public static boolean startHsp(String jvmOptions, String numHosts)
    {
        boolean result = false;

        result = start( jvmOptions, HSP_CLASS_NAME, numHosts );

        return (result);
    }

    /**
     * Start a TaskServer.
     * 
     * @param jvmOptions
     *            Options to the JVM.
     * @param domainName
     *            Which HSP domain to which to bind.
     * @param hosts
     *            How many hosts to start (default 1)
     * @param extType
     *            Type of external task server.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     */
    public static boolean startTaskServer( String jvmOptions,
                                           String domainName, String hosts, int extType )
    {
        boolean result = false;
        String serviceName = null;
        String args = "";

        if (null == domainName)
        {
            serviceName = TaskServer.class.getName();
            if (EXTTYPE_Matlab == extType)
            {
                serviceName = TaskServerMatlab.class.getName();
            }

            args = domainName;
            if (null != hosts)
            {
                args += " " + hosts;
            }

            result = start( jvmOptions, serviceName, args );
        }

        return (result);
    }

    /**
     * Start up a Host.
     * 
     * @param jvmOptions
     *            Options to the JVM.
     * @param domainName
     *            Which HSP domain to which to bind.
     * @param extType
     *            Type of external host.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     */
    public static boolean startHost( String jvmOptions, String domainName,
                                     int extType )
    {
        boolean result = false;
        String serviceName = null;

        if (null != domainName)
        {
            serviceName = Host.class.getName();
            if (EXTTYPE_Matlab == extType)
            {
                serviceName = TaskServerMatlab.class.getName();
            }

            result = start( jvmOptions, serviceName, domainName );
        }
        return (result);
    }

    /**
     * Start up the specified service.
     * 
     * @param jvmOptions
     *            Options to JVM.
     * @param serviceName
     *            Entry class (main)
     * @param serviceOptions
     *            Command line arguments.
     * @return
     */
    public static boolean start( String jvmOptions, String serviceName,
                                 String serviceOptions )
    {
        boolean serviceStarted = false;

        try
        {
            if (null != serviceName)
            {
                String execCommand = null;

                // Find the JVM command.
                String javaCmd = System.getProperty( LAUNCHER_CMD_JAVA, "java" );
                String sysType = System.getProperty( "os.name", "unknown" )
                        .toLowerCase();
                if (sysType.startsWith( "window" ))
                {
                    javaCmd = "javaw.exe";
                }
                execCommand = javaCmd + ' ';

                // Add the JVM options
                if (null != jvmOptions)
                {
                    execCommand += jvmOptions;
                }
                execCommand += ' ';

                // Add the service name.
                execCommand += serviceName + ' ';

                // Add the service options
                if (null != serviceOptions)
                {
                    execCommand += serviceOptions;
                }

                logger.log( LogManager.FINE, "execCommand=\"" + execCommand + "\"" );

                // I'm having problems with this blocking and not releasing.
                // Probably want to re-visit this with non-blocking I/O.
                //
                boolean getStdOut =false, getStdErr =false;
                String getStdOutString = System.getProperty( "launcher.getstdout", "false" );
                getStdOutString = getStdOutString.toLowerCase();
                if( getStdOutString.startsWith( "tr" ) )
                {
                    getStdOut = true;
                }
                //
                String getStdErrString = System.getProperty( "launcher.getstderr", "false" );
                getStdErrString = getStdErrString.toLowerCase();
                if( getStdErrString.startsWith( "tr" ) )
                {
                    getStdErr = true;
                }

                // Start the process.
                Process jvmProcess = Runtime.getRuntime().exec( execCommand );
                InputStream stderr = null;
                InputStream stdout = null;
                if (getStdErr)
                {
                    stderr = jvmProcess.getErrorStream();
                }
                if (getStdOut)
                {
                    stdout = jvmProcess.getInputStream();
                }

                // Wait for something to happen.
                if( getStdErr || getStdOut )
                {
                    Thread.sleep( 1000 );
                }

                /*  Get the output from stdout, stdin.  */

                // TODO Implement with non-blocking I/O.
                byte[] data = new byte[1016];
                int bytes;
                if (getStdErr)
                {
                    if (0 < (bytes = stderr.read( data )))
                    {
                        System.out.flush();
                        System.err.println();
                        System.err.println( "** ERROR (stderr) **" );
                        System.err.println( new String( data, 0, bytes ) );
                        System.err.println();
                        System.err.flush();
                    }
                }

                if (getStdOut)
                {
                    if (0 < (bytes = stdout.read( data )))
                    {
                        System.out.flush();
                        System.err.println();
                        System.err.println( "** ERROR (stdout) **" );
                        System.err.println( new String( data, 0, bytes ) );
                        System.err.println();
                        System.err.flush();
                    }
                }

                serviceStarted = true;
            }
        }
        catch (Exception exception)
        {
            logger
                    .log( LogManager.WARNING, "Couldn't start new JVM",
                            exception );
            serviceStarted = false;
        }

        return (serviceStarted);
    }

    /**
     * Start up an HSP via Chameleon.
     * 
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @param numHosts
     *            How many hosts to start up with the Hsp.
     * @param jvmOptions
     * 			Options to pass to the JVM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws RemoteException
     *             This calls a remote method (Chameleon).
     */
    public static boolean startChameleonHsp( String remoteHost, String numHosts, String jvmOptions )
            throws RemoteException
    {
        boolean hspStarted = false;

        Chameleon chameleon = getChameleon( remoteHost );

        String $numHosts = "1";
        if( null != numHosts )
        {
            $numHosts = numHosts;
        }

        String serviceString = ChameleonImpl.createServiceString( Chameleon.CMD_Hsp, jvmOptions, $numHosts );
        byte[] signature = ChameleonImpl.createSignature( serviceString );

        hspStarted = chameleon.startService( serviceString, signature );
        if( hspStarted )
        {
            LogManager.getLogger( Launcher.class ).log( LogManager.CONFIG, "Started HSP on " + remoteHost );
        }
        else
        {
            LogManager.getLogger( Launcher.class ).log(
                    LogManager.ERROR,
                    "Unsuccessful starting HSP on " + remoteHost + ": \""
                    + serviceString + "\"" );
        }

        return (hspStarted);
    }

    /**
     * Start up a TaskServer via Chameleon.
     * 
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @param hspDomain
     *            The host where the HSP is running.
     * @param numHosts
     *            How many hosts to start up with the Hsp.
     * @param extType
     *            The type of external TaskServer.
     * @param jvmOptions
     * 			Options to pass to the JVM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws RemoteException
     *             This calls a remote method (Chameleon).
     */
    public static boolean startChameleonTaskserver( String remoteHost,
                                                    String hspDomain, String numHosts, int extType, String jvmOptions )
            throws RemoteException
    {
        boolean tsStarted = false;

        if( null == hspDomain )
        {
            throw new RemoteException( "Couldn't start task server", new NullPointerException( "hspDomain is null" ) );
        }

        Chameleon chameleon = getChameleon( remoteHost );

        String $numHosts = "1";
        if( null != numHosts )
        {
            $numHosts = numHosts;
        }

        String service = null;
        switch( extType )
        {
            case  EXTTYPE_None:
                service = Chameleon.CMD_TaskServer;
                break;

            case  EXTTYPE_Matlab:
                service = Chameleon.CMD_TaskServerMatlab;
                break;

            default:
                throw new RemoteException( "Unknown External TaskServer type",
                        new IllegalArgumentException( "unknown external type ("
                        + extType + ")" ) );
        }

        String serviceString = ChameleonImpl.createServiceString( service, jvmOptions, hspDomain +' '+ $numHosts );
        byte[] signature = ChameleonImpl.createSignature( serviceString );

        tsStarted = chameleon.startService( serviceString, signature );
        if( tsStarted )
        {
            LogManager.getLogger( Launcher.class ).log( LogManager.CONFIG, "Started " + service + " on " + remoteHost );
        }
        else
        {
            LogManager.getLogger( Launcher.class ).log(
                    LogManager.ERROR,
                    "Unsuccessful starting " + service + " on " + remoteHost + ": \""
                    + serviceString + "\"" );
        }

        return (tsStarted);
    }

    /**
     * Start up a Host via Chameleon.
     * 
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @param hspDomain
     *            The host where the HSP is running.
     * @param extType
     *            The type of external TaskServer.
     * @param jvmOptions
     * 			Options to pass to the JVM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws RemoteException
     *             This calls a remote method (Chameleon).
     */
    public static boolean startChameleonHost( String remoteHost,
                                              String hspDomain, int extType, String jvmOptions ) throws RemoteException
    {
        boolean hostStarted = false;

        if( null == hspDomain )
        {
            throw new RemoteException( "Couldn't start task server", new NullPointerException( "hspDomain is null" ) );
        }

        Chameleon chameleon = getChameleon( remoteHost );

        String service = null;
        switch( extType )
        {
            case  EXTTYPE_None:
                service = Chameleon.CMD_Host;
                break;

            case  EXTTYPE_Matlab:
                service = Chameleon.CMD_HostMatlab;
                break;

            default:
                throw new RemoteException( "Unknown External Host type",
                        new IllegalArgumentException( "unknown external type ("
                        + extType + ")" ) );
        }

        String serviceString = ChameleonImpl.createServiceString( service, jvmOptions, hspDomain );
        byte[] signature = ChameleonImpl.createSignature( serviceString );

        hostStarted = chameleon.startService( serviceString, signature );
        if( hostStarted )
        {
            LogManager.getLogger( Launcher.class ).log( LogManager.CONFIG, "Started " + service + " on " + remoteHost );
        }
        else
        {
            LogManager.getLogger( Launcher.class ).log(
                    LogManager.ERROR,
                    "Unsuccessful starting " + service + " on " + remoteHost + ": \""
                    + serviceString + "\"" );
        }

        return (hostStarted);
    }


    /**
     * Start up an Console via Chameleon.
     * 
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws RemoteException
     *             This calls a remote method (Chameleon).
     */
    public static boolean startChameleonConsole( String remoteHost ) throws RemoteException
    {
        boolean consoleStarted = false;

        Chameleon chameleon = getChameleon( remoteHost );

        String startCommand = ChameleonImpl.createServiceString( Chameleon.CMD_Console, null, null );
        byte[] signature = ChameleonImpl.createSignature(startCommand);

        consoleStarted = chameleon.startService( startCommand, signature );

        return (consoleStarted);
    }


    /**
     * Get a reference to the Chameleon program on a remote host.
     * 
     * @param remoteHost
     *            What RMI registry to look in.
     * @return A Remote reference to a Chameleon instance.
     * @throws RemoteException
     *             If the remoteHost is null, if the remote registry doesn't
     *             exist, or a lookup doesn't find the Chameleon reference on
     *             the remote host.
     */
    private static Chameleon getChameleon( String remoteHost )
            throws RemoteException
    {
        Chameleon chameleon = null;

        if (null == remoteHost)
        {
            throw new RemoteException( "Can't get Chameleon",
                    new NullPointerException( "remoteHost is null" ) );
        }

        try
        {
            InetAddress.getByName( remoteHost );
        }
        catch( UnknownHostException unknownHostException )
        {
            throw new RemoteException( "Unknown remote host", unknownHostException );
        }

        Registry rmiRegistry = LocateRegistry.getRegistry( remoteHost,
                RegistrationManager.PORT );
        if (null == rmiRegistry)
        {
            throw new RemoteException( "Can't get Chameleon",
                    new NullPointerException( "No registry on \"" + remoteHost
                    + "\"" ) );
        }

        try
        {
            chameleon = (Chameleon) rmiRegistry.lookup( Chameleon.SERVICE_NAME );
        }
        catch (Exception anyException)
        {
            throw new RemoteException( "Can't get Chameleon", anyException );
        }

        return (chameleon);
    }


    /**
     * Start up a Host via secure shell.
     *
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @param hspDomain
     *            The host where the HSP is running.
     * @param extType
     *            The type of external TaskServer.
     * @param jvmOptions
     * 			Options to pass to the JVM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws IOException
     *             Because of call to Runtime.exec().
     */
    public static boolean startSSHHost( String remoteHost,
                                              String hspDomain, int extType, String jvmOptions ) throws IOException
    {
        StringBuffer service = new StringBuffer();

        switch( extType )
        {
            case  EXTTYPE_None:
                service.append( HOST_CLASS_NAME );
                break;
            case  EXTTYPE_Matlab:
                service.append( HOSTMATLAB_CLASS_NAME );
                break;
        }

        service.append( ' ' + hspDomain );

        return( startSSHProcess( remoteHost, jvmOptions, service.toString() ) );
    }

    /**
     * Start up an TaskServer via secure shell.
     *
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @param hspDomain
     *            The host where the HSP is running.
     * @param numHosts
     *            How many hosts to start up with the Hsp.
     * @param extType
     *            The type of external TaskServer.
     * @param jvmOptions
     * 			Options to pass to the JVM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws IOException
     *             Because of call to Runtime.exec().
     */
    public static boolean startSSHTaskserver( String remoteHost,
                                                    String hspDomain, String numHosts, int extType, String jvmOptions )
            throws IOException
    {
        StringBuffer service = new StringBuffer();

        switch( extType )
        {
            case  EXTTYPE_None:
                service.append( TASKSERVER_CLASS_NAME );
                break;
            case  EXTTYPE_Matlab:
                service.append( TASKSERVERMATLAB_CLASS_NAME );
                break;
        }

        service.append( ' ' + hspDomain );
        service.append( ' ' + String.valueOf( numHosts ) );

        return( startSSHProcess( remoteHost, jvmOptions, service.toString() ) );
    }

    /**
     * Start up an HSP via secure shell.
     *
     * @param remoteHost
     *            The host that the chameleon is running on.
     * @param numHosts
     *            How many hosts to start up with the Hsp.
     * @param jvmOptions
     * 			Options to pass to the JVM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws IOException
     *             Because of call to Runtime.exec().
     */
    public static boolean startSSHHsp( String remoteHost, String numHosts, String jvmOptions )
            throws IOException
    {
        String service = HSP_CLASS_NAME + ' ' + String.valueOf( numHosts );

        return( startSSHProcess( remoteHost, jvmOptions, service ) );
    }




    /**
     * Start a process remotely using ssh.
     *
     * @param host  Where to start the service
     * @param jvmOptions  JVM options to the service.
     * @param service  Class name and options to service.
     * @return  Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     * @throws IOException Runtime.exec()
     */
    public static boolean startSSHProcess( String host, String jvmOptions, String service ) throws IOException
    {
        String sshCmd = System.getProperty( LAUNCHER_CMD_SSH, "ssh" );
        StringBuffer execCommand = new StringBuffer( sshCmd );
        execCommand.append( ' ' + host );

        String javaCmd = System.getProperty( LAUNCHER_CMD_JAVA, "java" );
        execCommand.append( ' ' + javaCmd );

        // Add the JVM options
        if (null != jvmOptions)
        {
            execCommand.append( ' ' + jvmOptions );
        }

        // Add the service name.
        execCommand.append( ' ' + service );


        logger.log( LogManager.FINE, "execCommand=\"" + execCommand + "\"" );

        // I'm having problems with this blocking and not releasing.
        // Probably want to re-visit this with non-blocking I/O.
        //
        boolean getStdOut =false, getStdErr =false;
        String getStdOutString = System.getProperty( "launcher.getstdout", "false" );
        getStdOutString = getStdOutString.toLowerCase();
        if( getStdOutString.startsWith( "tr" ) )
        {
            getStdOut = true;
        }
        //
        String getStdErrString = System.getProperty( "launcher.getstderr", "false" );
        getStdErrString = getStdErrString.toLowerCase();
        if( getStdErrString.startsWith( "tr" ) )
        {
            getStdErr = true;
        }

        // Start the process.
        Process jvmProcess = Runtime.getRuntime().exec( execCommand.toString() );
        InputStream stderr = null;
        InputStream stdout = null;
        if (getStdErr)
        {
            stderr = jvmProcess.getErrorStream();
        }
        if (getStdOut)
        {
            stdout = jvmProcess.getInputStream();
        }


        // Wait for something to happen.
        if( getStdErr || getStdOut )
        {
            try {
                Thread.sleep( 1000 );
            } catch (InterruptedException interruptedException) {
                System.out.flush();
                System.err.println( interruptedException.getClass().getName() + ": " + interruptedException.getMessage() );
                System.err.flush();
            }
        }

        /*  Get the output from stdout, stdin.  */

        // TODO Implement with non-blocking I/O.
        byte[] data = new byte[1016];
        int bytes;
        if (getStdErr)
        {
            if (0 < (bytes = stderr.read( data )))
            {
                System.out.flush();
                System.err.println();
                System.err.println( "** ERROR (stderr) **" );
                System.err.println( new String( data, 0, bytes ) );
                System.err.println();
                System.err.flush();
            }
        }

        if (getStdOut)
        {
            if (0 < (bytes = stdout.read( data )))
            {
                System.out.flush();
                System.err.println();
                System.err.println( "** ERROR (stdout) **" );
                System.err.println( new String( data, 0, bytes ) );
                System.err.println();
                System.err.flush();
            }
        }

        return( true );
    }

}
