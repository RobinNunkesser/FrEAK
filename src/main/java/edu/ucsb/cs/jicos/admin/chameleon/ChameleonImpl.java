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
 * Implements the Chameleon.
 * 
 * Notes:
 * 	On a Mac G5, this process consumes less than 0.1% of a CPU, spawns 14
 *  threads, uses 11.75MB of real memory, and 267MB of virtual memory when
 *  idle.
 * 
 * @author pippin
 */

package edu.ucsb.cs.jicos.admin.chameleon;

import edu.ucsb.cs.jicos.admin.common.Launcher;
import edu.ucsb.cs.jicos.foundation.Administrable;
import edu.ucsb.cs.jicos.foundation.RegistrationManager;
import edu.ucsb.cs.jicos.foundation.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ChameleonImpl extends UnicastRemoteObject implements Chameleon, Administrable
{
    //
    //-- Constants -----------------------------------------------------------

    private static final int LABEL = 0;
    private static final int NAME = 1;
   
    private static final int NDX_RefreshJarfileBaseurl = 0;
    private static final int NDX_RefreshJarfileFilename = 1;
    private static final int NDX_RefreshDirname = 2;
    private static final int NDX_SecurityPolicyHsp = 3;
    private static final int NDX_SecurityPolicyTaskserver = 4;
    private static final int NDX_SecurityPolicyTaskservermatlab = 5;
    private static final int NDX_SecurityPolicyHost = 6;
    private static final int NDX_SecurityPolicyHostmatlab = 7;
    private static final int NDX_SecurityPolicy = 8;
    private static final int NDX_SecurityAllowable = 9;
    private static final int NDX_StartRefresh = 10;
    private static final int NDX_StartJvmoptions = 11;
    private static final int NDX_number = 12;

    //
    // -- Variables ----------------------------------------------------------

    private int  verboseLevel;
    private String  defaultJvmOptions;
    private Registry rmiRegistry;
    private String savedJarfile;
    
    private boolean refresh;

    private String[] propertyValue;
    private String[] allowableServices;
    
    //
    //-- Constructors --------------------------------------------------------

    public ChameleonImpl() throws RemoteException
    {
        super();
        this.initVariables();
        this.setJvmOptions();
    }

    public ChameleonImpl(String[] cmdLine) throws RemoteException
    {
        super();
        this.initVariables();
        this.parseCommandLineArguments( cmdLine );
        this.setJvmOptions();
    }

    private void initVariables()
    {
        this.verboseLevel = 1;
        this.defaultJvmOptions = "";
        this.rmiRegistry = null;
        this.savedJarfile = null;
        URI topDir = null;
        URL url = null;

        // Get the default properties.
        if (null != (url = Administrable.class.getResource( "Administrable.class" )))
        {
            try
            {
                if( url.toString().startsWith( "jar:file:" ) )
                {
                    int bang;
                    String jarFilename = url.toString();
                    if( -1 != (bang = jarFilename.indexOf( '!', 9 )) )
                    {
                        jarFilename = jarFilename.substring( 9, bang );
                        JarFile jarFile = new JarFile( jarFilename );
                        JarEntry jarEntry = null;
                        if( null != (jarEntry = jarFile.getJarEntry( PROPERTY_FILENAME )) )
                        {
                            System.getProperties().load( jarFile.getInputStream( jarEntry ) );
                        }
                    }
                    
                }
            }
            catch (IOException ioException)
            {
                // Ignore this. It would be nice to throw an exception, but
                // if the system can;t determine where the properties file
                // is, then it's jsut the same as if it doesn't exit.
            }
        }
       
        // Try and get a properties file.
        String propFilename = System.getProperty( PROP_PropertyFilename, DEFAULT_PropertyFilename );
        File propFile = null;
        if( (null != propFilename)
                && (null != (propFile = new File( propFilename )))
                && propFile.exists() )
        {
            try
            {
                InputStream inputStream = new FileInputStream( propFile );
                System.getProperties().load( inputStream );
            }
            catch( IOException ioException )
            {
                System.out.flush();
                System.err.println( "Couldn't load properties --" );
                System.err.println( "  " + ioException.getClass().getName()
                        + ": " + ioException.getMessage() );
                System.err.println( "Continuing without it...." );
                System.err.flush();
            }
        }
        
        
        this.propertyValue = new String[ NDX_number ];
        
        this.propertyValue[ NDX_RefreshJarfileBaseurl ] =  // "http://localhost/projects/jicos/"
            System.getProperty( PROP_RefreshJarfileBaseurl,
                    				DEFAULT_RefreshJarfileBaseurl );
        this.propertyValue[ NDX_RefreshJarfileFilename ] =
            System.getProperty( PROP_RefreshJarfileFilename,
                    				DEFAULT_RefreshJarfileFilename );
        this.propertyValue[ NDX_RefreshDirname ] =
            System.getProperty( PROP_RefreshDirname,
                    				DEFAULT_RefreshDirname );
        this.propertyValue[ NDX_SecurityPolicy ] =
            System.getProperty( PROP_SecurityPolicy,
                    				DEFAULT_SecurityPolicy );
        this.propertyValue[ NDX_SecurityAllowable ] =
            System.getProperty( PROP_SecurityAllowable,
                    				DEFAULT_SecurityAllowable );
        this.propertyValue[ NDX_StartRefresh ] =
            System.getProperty( PROP_StartRefresh,
                    				DEFAULT_StartRefresh );
        this.propertyValue[ NDX_StartJvmoptions ] =
            System.getProperty( PROP_StartJvmoptions,
                    				DEFAULT_StartJvmoptions );

        // "java.security.policy" is the default policy file.
        if( null == this.propertyValue[ NDX_SecurityPolicy ] )
        {
            this.propertyValue[ NDX_SecurityPolicy ] = System.getProperty( "java.security.policy" );
        }
        this.propertyValue[ NDX_SecurityPolicyHsp ] =
            System.getProperty( PROP_SecurityPolicyHsp,
                    this.propertyValue[ NDX_SecurityPolicy ] );
        this.propertyValue[ NDX_SecurityPolicyTaskserver ] =
            System.getProperty( PROP_SecurityPolicyTaskserver,
                    this.propertyValue[ NDX_SecurityPolicy ] );
        this.propertyValue[ NDX_SecurityPolicyTaskservermatlab ] =
            System.getProperty( PROP_SecurityPolicyTaskservermatlab,
                    this.propertyValue[ NDX_SecurityPolicy ] );
        this.propertyValue[ NDX_SecurityPolicyHost ] =
            System.getProperty( PROP_SecurityPolicyHost,
                    this.propertyValue[ NDX_SecurityPolicy ] );
        this.propertyValue[ NDX_SecurityPolicyHostmatlab ] =
            System.getProperty( PROP_SecurityPolicyHostmatlab,
                    this.propertyValue[ NDX_SecurityPolicy ] );
        
        this.refresh = Boolean.getBoolean( this.propertyValue[ NDX_StartRefresh ] );

        String propAllow = this.propertyValue[ NDX_SecurityAllowable ];
        if( null == propAllow )
        {
            this.allowableServices = new String[0];
        }
        else if( this.propertyValue[ NDX_SecurityAllowable ].equalsIgnoreCase( "all" ) )
        {
            this.allowableServices = new String[]{
                            CMD_Hsp,
                            CMD_TaskServer,
                            CMD_TaskServerMatlab,
                            CMD_Host,
                            CMD_HostMatlab,
                            CMD_Console,
                        };
        }
        else
        {
            propAllow = propAllow.replace( ',', ' ' ).replaceAll( "\\s+", " " );
            this.allowableServices = propAllow.split( " " );
        }
    }

    private void parseCommandLineArguments( String[] cmdLine )
    {
        if (1 < cmdLine.length)
        {
            int argv;
            for (argv = 0; argv < cmdLine.length; ++argv)
            {
                if ("-help".equals( cmdLine[argv] ))
                {
                    ChameleonImpl.displayHelp( null );
                }
                else if ("-verbose".equals( cmdLine[argv] ))
                {
                    this.verboseLevel++;
                }
            }
        }
        
        return;
    }

    private void setJvmOptions()
    {
        // Get the policy file.
        String policy = null;
        if( null == (policy = System.getProperty( "chameleon.security.policy" )) )
        {
            // Use the same policy.
            policy = System.getProperty( "java.security.policy", "policy/policy" );
        }
        this.defaultJvmOptions = "-Djava.security.policy=" + policy;
        
        String logLevel = System.getProperty( "jicos.log.level", "warning" );
        this.defaultJvmOptions += " -Djicos.log.level=" + logLevel;
        
        return;
    }
    
    //
    //-- Invocation ----------------------------------------------------------

    public boolean startService( String command, byte[] signature )
            throws RemoteException
    {
        return( startService( DEFAULT_REFRESH, command, signature ) );
    }
    
    /*  @see edu.ucsb.cs.jicos.admin.chameleon.Chameleon#startService(boolean,java.lang.String,byte[])  */
    public boolean startService( boolean refresh, String command,
            byte[] signature ) throws RemoteException
    {
        boolean serviceStarted = false;

        boolean isVerified = validateCommand( command, signature );
        String[] array = command.split( ";" );

        String serviceName = null;
        String jvmOptions = null;
        String serviceOptions = null;

        if (0 < array.length)
        {
            serviceName = array[0];
            if (1 < array.length)
            {
                jvmOptions = array[1];
                if (2 < array.length)
                {
                    serviceOptions = array[2];
                }
            }
        }

        if( !isAllowedService( serviceName ) )
        {
            throw new SecurityException( "\"" + serviceName + "\" is not an allowable service.  "
                    + "  Please set the " + PROP_SecurityAllowable + " property appropriately." );
        }

        String classJar;
        try
        {
             classJar = getSystemFile( refresh );
        }
        catch( FileNotFoundException fileNotFoundException )
        {
            throw new RemoteException( "Couldn't find jar file", fileNotFoundException );
        }
        
        serviceStarted = startJvm( serviceName, classJar, jvmOptions, serviceOptions );

        return (serviceStarted);
    }

    public static String createServiceString( String serviceName, String jvmOptions, String serviceOptions )
    {
        String serviceString = new String();
        
        if( null != serviceName )
        {
            serviceString += serviceName;
        }

        serviceString += ';';
        
        if( null != jvmOptions )
        {
            serviceString += jvmOptions;
        }
        
        serviceString += ';';
        
        if( null != serviceOptions )
        {
            serviceString += serviceOptions;
        }
        
        return( serviceString );
    }
    
    
    public static byte[]  createSignature( String serviceString )
    {
        return( (byte[])null );
    }
    
    
    /* @see edu.ucsb.cs.jicos.foundation.Administrable#shutdown() */
    public void shutdown() throws RemoteException
    {
        try
        {
            if (1 < verboseLevel)
            {
                LogManager.getLogger( this ).log( LogManager.INFO,
                        "Chameleon is shutting down." );
            }

            this.rmiRegistry = RegistrationManager.locateRegistry();
            this.rmiRegistry.unbind( SERVICE_NAME );
        }

        catch (Exception anyException) // AccessException + RemoteException
        {
            LogManager.getLogger( this ).log(
                    LogManager.INFO,
                    "Chameleon caught exception while shutting down - "
                            + anyException.getClass().getName() + ": "
                            + anyException.getMessage() );
        }
        
        System.exit( 0 );
    }

    /*  @see edu.ucsb.cs.jicos.admin.Administrable#echo(java.lang.String)  */
    public String echo( String request ) throws RemoteException
    {
        return( request );
    }
   
    
    /**
     * Get the Administrable interface of a Chameleon from a registry.
     * 
     * @param  hostName  Where the RMI registry is located.
     * @return The Administrable interface.
     * @throws NullPointerException  If hostName is null.
     * @throws RemoteException
     * @throws AccessException
     * @throws NotBoundException  There is no Chameleon registered.
     */
    public static Administrable getAdministrable( String hostName )
            throws NullPointerException, RemoteException, AccessException,
            NotBoundException
    {
        Administrable administrable = null;

        Registry rmiRegistry = LocateRegistry.getRegistry( hostName,
                RegistrationManager.PORT );
        if (null == rmiRegistry)
        {
            throw new NullPointerException( "There is no RMI registry on \""
                    + hostName + "\"." );
        }

        administrable = (Administrable) rmiRegistry
                .lookup( Chameleon.SERVICE_NAME );

        return (administrable);
    }
    
    
    /**
     * Get the Administrable interface of a Chameleon from a registry.
     * 
     * @param  hostName  Where the RMI registry is located.
     * @return The Administrable interface.
     * @throws NullPointerException  If hostName is null.
     * @throws RemoteException
     * @throws AccessException
     * @throws NotBoundException  There is no Chameleon registered.
     */
    public static Chameleon getChameleon( String hostName )
            throws NullPointerException, RemoteException, AccessException,
            NotBoundException
    {
        Chameleon chameleon = null;
        
        Registry rmiRegistry = LocateRegistry.getRegistry( hostName, RegistrationManager.PORT );
        if( null == rmiRegistry )
        {
            throw new NullPointerException( "There is no RMI registry on \"" + hostName + "\"." );
        }
        
        chameleon = (Chameleon)rmiRegistry.lookup( Chameleon.SERVICE_NAME );

        return( chameleon );
    }
    
    //
    //-- Private Helper Methods ----------------------------------------------

    /*
     * Verify the signature.
     */
    private boolean validateCommand( String command, byte[] signature )
    {
        boolean isVerified = false;

        if (VERIFY_COMMAND)
        {
            try
            {
                URI publicKeyURI = new URI( "" );
                PublicKey publicKey = Signature.getPublicKey( publicKeyURI );
                byte[] content = command.getBytes( Signature.TEXT_ENCODING );
                isVerified = Signature.verifySignature( publicKey, content,
                        signature );
            }
            catch (Exception anyException)
            {
                LogManager.getLogger( this ).log( LogManager.ERROR,
                        "Couldn't validate command", anyException );
                isVerified = false;
            }
        }
        else
        {
            isVerified = true;
        }

        return (isVerified);
    }
    
    private boolean  isAllowedService( String serviceName )
    {
        boolean  isAllowed = false;
        
        if( null != this.allowableServices )
        {
            for( int s=0; s < this.allowableServices.length; ++s )
            {
                if( this.allowableServices[s].equals( serviceName ) )
                {
                    isAllowed = true;
                    break;
                }
            }
        }
            
        return( isAllowed );
    }
  
    private boolean startJvm( String serviceName, String classJar, String jvmOptions,
            String serviceOptions )
    {
        boolean jvmStarted = false;
        String servName = null;
        String jvmOpts = this.defaultJvmOptions + " -classpath " + classJar;

        // Find the service name.
        if (null == serviceName)
        {
        }
        else if( serviceName.equals( CMD_Shutdown ))
        {
            LogManager.getLogger( this ).log( LogManager.INFO, "Shutting down" );
            System.exit( 0 );
        }
        else
        {
            for (int s = 0; s < knownServices.length; ++s)
            {
                if (serviceName.equals( knownServices[s][LABEL] ))
                {
                    servName = knownServices[s][NAME];
                    break;
                }
            }
        }

        if (null != servName)
        {
            // Append the passed JVM options.
            if (null != jvmOptions)
            {
                jvmOpts += ' ' + jvmOptions;
            }

            jvmStarted = Launcher.start( jvmOpts, servName,
                    serviceOptions );
        }
        
        return (jvmStarted);
    }

    private String getSystemFile( boolean refresh ) throws FileNotFoundException
    {
        FileOutputStream outputStream = null; // In finally clause

        if (!refresh)
        {
            if (null == this.savedJarfile)
            {
                throw new FileNotFoundException( "There is no jarfile saved." );
                //
                //  *** DOES NOT CONTINUE ***
                //
            }
            else
            {
                File jar = new File( this.savedJarfile );
                if (!jar.exists())
                {
                    LogManager.getLogger().log( LogManager.FINE,
                            "Reusing \"" + this.savedJarfile + "\"" );
                    return (this.savedJarfile);
                    //
                    //  *** DOES NOT CONTINUE ***
                    //
                }
            }
        }
        
        // baseUrl/libName --> tmpDir
        String tmpDirname = this.propertyValue[ NDX_RefreshDirname ];
        String baseUrl = this.propertyValue[ NDX_RefreshJarfileBaseurl ];
        String libName = this.propertyValue[ NDX_RefreshJarfileFilename ];
        
        File tmpDir = new File( tmpDirname );
        if( !tmpDir.exists() )
		{
            throw new FileNotFoundException( "\"" + tmpDirname + "\" does not exist." );
		}
        else if( !tmpDir.isDirectory() )
        {
            throw new FileNotFoundException( "\"" + tmpDirname + "\" is not a directory." );
        }
        
        
        try
        {
            URL url = new URL( baseUrl + libName );
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            Object o = urlConnection.getContent();
            int contentLength = urlConnection.getContentLength();

            if (o instanceof InputStream)
            {
                InputStream inputStream = (InputStream) o;
                
                int ndx;
                if( -1 == (ndx = libName.lastIndexOf( '/' )) )
                {
                    ndx = 0;
                }
                this.savedJarfile = tmpDirname + libName.substring( ndx );

                File jarFile = new File( this.savedJarfile );
                outputStream = new FileOutputStream( jarFile, false );
                int totalRead = 0;
                int bytesRead;
                byte[] data = new byte[1024];

                while (totalRead < contentLength)
                {
                    bytesRead = inputStream.read( data );
                    if (0 > bytesRead)
                        break;
                    totalRead += bytesRead;
                    outputStream.write( data, 0, bytesRead );
                }
                outputStream.close();

                LogManager.getLogger().log(
                        LogManager.FINE,
                        "Retrieved new jarfile: \"" + baseUrl + libName
                                + "\" --> \"" + this.savedJarfile + "\"" );
            }
        }
        catch (FileNotFoundException fileNotFoundException)
        {
            LogManager.getLogger().log(
                    LogManager.ERROR,
                    "FileNotFoundException: "
                            + fileNotFoundException.getMessage() );
        }
        catch (IOException ioException)
        {
            LogManager.getLogger().log( LogManager.ERROR,
                    "IOException: " + ioException.getMessage() );
        }
        
        finally
        {
            try
            {
                outputStream.close();
            }
            catch (IOException anyIoException)
            {
            }
        }
        
        return( this.savedJarfile  );
        
    }
    
    //
    //-- Main Loop -----------------------------------------------------------

    public void run()
    {
        try
        {
            this.rmiRegistry = RegistrationManager.locateRegistry();
            this.rmiRegistry.bind( SERVICE_NAME, this );
            if (1 < verboseLevel)
            {
                java.util.logging.Logger logger = LogManager.getLogger( this );
                logger.log( LogManager.INFO, "Chameleon is ready." );
            }
        }
        
        catch (Exception anyException) // AccessException + RemoteException
        {
            LogManager.getLogger( this ).log(
                    LogManager.ERROR,
                    anyException.getClass().getName() + ": "
                            + anyException.getMessage() );

            System.out.flush();
            System.err.println( "Chameleon could not bind to RMI registry.... Exiting." );
            System.err.flush();

            System.exit( 1 );
        }
        
        return;
    }

    //
    //-- Static methods ------------------------------------------------------

    public static void main( String[] cmdLine ) throws RemoteException
    {
        if (null == System.getSecurityManager())
        {
            System.setSecurityManager( new RMISecurityManager() );
        }
        ChameleonImpl chameleon = new ChameleonImpl( cmdLine );
        //
        chameleon.run();
    }

    public static void displayHelp( String topic )
    {
        System.out.println( "Usage: <java> <Chameleon> [-help | -verbose]" );
    }
}

