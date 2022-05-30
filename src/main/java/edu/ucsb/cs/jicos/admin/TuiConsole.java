/* ********************************************************************** *
 *                                                                        *
 *       Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>        *
 *                                                                        *
 *  Permission is hereby granted, free of charge, to any person obtaining *
 * a copy of this software and associated documentation files (the        *
 * "Software"), to deal in the Software without restriction, including    *
 * without limitation the rights to use, copy, modify, merge, publish,    *
 * distribute, sublicense, and/or sell copies of the Software, and to     *
 * permit persons to whom the Software is furnished to do so, subject to  *
 * the following conditions:                                              *
 *                                                                        *
 *  The above copyright notice and this permission notice shall be        *
 * included in all copies or substantial portions of the Software.        *
 *                                                                        *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       *
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF     *
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY   *
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,   *
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE      *
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                 *
 *                                                                        *
 * ********************************************************************** */

/**
 * description
 * 
 * @author pippin
 */

package edu.ucsb.cs.jicos.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import edu.ucsb.cs.jicos.admin.chameleon.Chameleon;
import edu.ucsb.cs.jicos.admin.chameleon.ChameleonImpl;
import edu.ucsb.cs.jicos.admin.common.Launcher;
import edu.ucsb.cs.jicos.admin.common.StartupConfig;
import edu.ucsb.cs.jicos.admin.common.AdminUtilities;
import edu.ucsb.cs.jicos.foundation.Administrable;
import edu.ucsb.cs.jicos.foundation.RegistrationManager;
import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.Hsp;
import edu.ucsb.cs.jicos.services.Property;
import edu.ucsb.cs.jicos.services.TaskServer;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

public class TuiConsole
{
    //
    //-- Constants -----------------------------------------------------------

    private static final int CMD_Unknown = Command.CMD_Unknown;
    private static final int CMD_Error = Command.CMD_Error;
    private static final int CMD_Okay = Command.CMD_Okay;

    public static final String CONSOLE_POLICY = "tuiconsole.security.policy.console";
    //
    //-- Variables -----------------------------------------------------------

    private static Console remoteConsole = null;

    private StartupConfig  startupConfig;
    
    private cmd_Connect cmdConnect;
    private cmd_Console cmdConsole;
    private cmd_Exit cmdExit;
    private cmd_Help cmdHelp;
    private cmd_Load cmdLoad;
    private cmd_Local cmdLocal;
    private cmd_Log cmdLog;
    private cmd_Ping cmdPing;
    private cmd_Show cmdShow;
    private cmd_Start cmdStart;
    private cmd_Stop cmdStop;

    private CommandLine[] commandList;

    //
    //-- Constructors --------------------------------------------------------

    public TuiConsole()
    {
        initVariables();
    }

    public TuiConsole(Console console)
    {
        initVariables();
        TuiConsole.remoteConsole = console;
    }

    private void initVariables()
    {
        this.cmdConnect = new cmd_Connect();
        this.cmdConsole = new cmd_Console( true );
        this.cmdExit = new cmd_Exit();
        this.cmdHelp = new cmd_Help();
        this.cmdLoad = new cmd_Load();
        this.cmdLocal = new cmd_Local();
        this.cmdLog = new cmd_Log();
        this.cmdPing = new cmd_Ping();
        this.cmdShow = new cmd_Show();
        this.cmdStart = new cmd_Start();
        this.cmdStop = new cmd_Stop();

        commandList = new CommandLine[]
            {
                
//				new CommandLine( "chameleon", this.cmdChameleon, 4 ),
				new CommandLine( "connect", this.cmdConnect, 4 ),
				new CommandLine( "console", this.cmdConsole, 4 ),
				new CommandLine( "exit", this.cmdExit, 2 ),
				new CommandLine( "help", this.cmdHelp ),
				new CommandLine( "kill", this.cmdStop ),
				new CommandLine( "load", this.cmdLoad ),
//				new CommandLine( "local", this.cmdLocal ),
//				new CommandLine( "log", this.cmdLog ),
				new CommandLine( "quit", this.cmdExit, 1 ),
				new CommandLine( "ping", this.cmdPing ),
				new CommandLine( "show", this.cmdShow, 2 ),
				new CommandLine( "start", this.cmdStart ),
				new CommandLine( "stop", this.cmdStop ),
        
            };
    }

    //
    //-- Getters and Setters -------------------------------------------------

    public Console getRemoteConsole()
    {
        return( TuiConsole.remoteConsole );
    }
    
    public CommandLine[] getCommandList()
    {
        return( this.commandList );
    }
    
    public StartupConfig getStartupConfig()
    {
        return( this.startupConfig );
    }
    
    
    public void setRemoteConsole( Console remoteConsole )
    {
        TuiConsole.remoteConsole = remoteConsole;
    }
    
    public void setStartupConfig( StartupConfig startupConfig )
    {
        this.startupConfig = startupConfig;
    }

    //
    //-- Functionality -------------------------------------------------------

    /**
     * Loop endlessly displaying the prompt and performing commands.
     */
    private void mainLoop()
    {
        try
        {
            while (true)
            {
                System.out.print( "\r\nJicos> " );
                System.out.flush();

                byte[] input = new byte[1024];
                int read = -1;
                if (-1 == (read = System.in.read( input )))
                {
                    break;
                }

                try
                {
                    performCommand( new String( input, 0, read ) );
                }
                catch (Exception exception)
                {
                    System.err.println( exception.getMessage() );
                }
            }
        }
        catch (Exception exception)
        {
        }
    }

    /**
     * Find a matching command to cmdLine[0].
     * 
     * @param cmdLine
     *            An array of strings.
     * @return The command handler, or <CODE>null</CODE> if not found.
     */
    private Command findCommand( String commandName )
    {
        Command command = null;
        CommandLine[] commands = commandList;

        for (int cmd = 0; cmd < commandList.length; ++cmd)
        {
            if (commandList[cmd].equals( commandName ))
            {
                command = commandList[cmd].command;
                break;
            }
        }

        return (command);
    }

    /**
     * Break up a single string into an array of strings, and pass the array to
     * 
     * @param cmdLine
     * @return @throws
     *         Exception
     */
    public int performCommand( String cmdLine ) throws Exception
    {
        int exitCode = CMD_Error;
        
        if( null != cmdLine )
        {
            int len = cmdLine.length();
            if( '\n' == cmdLine.charAt( len-1 ) )
            {
                cmdLine = cmdLine.substring( 0, len-1 );
                --len;
            }
            if( 0 == len )
            {
                return( CMD_Okay );
            }
            if( '\r' == cmdLine.charAt( len-1 ) )
            {
                cmdLine = cmdLine.substring( 0, len-1 );
                --len;
            }
            if( 0 == len )
            {
                return( CMD_Okay );
            }
            
            String inputLine = cmdLine.replaceAll( "\\s+", " " );
            
            exitCode = performCommand( inputLine.split( " " ) );
        }
        
        return (exitCode);
    }

    /**
     * Perform a command. If the command is found, it will be performed and the
     * exit code of the command (<CODE>CMD_Okay</CODE> or <CODE>CMD_Error
     * </CODE>).
     * 
     * @param cmdLine
     *            The command line array.
     * @return Exit code of commandHandler, or unknown (<CODE>CMD_Unknown
     *         </CODE>).
     * @throws Exception
     */
    public int performCommand( String[] cmdLine ) throws Exception
    {
        int exitCode = CMD_Error;

        if ((null != cmdLine) && (0 < cmdLine.length)
                && (!cmdLine[0].startsWith( "#" )))
        {
            Command commandHandler = null;

            if (null != (commandHandler = findCommand( cmdLine[0] )))
            {
                try
                {
                    exitCode = commandHandler.invoke( cmdLine );
                }
                catch( RemoteException remoteException )
                {
                    // Unwrap the exception.
                    Throwable rootException = remoteException;
                    while( rootException instanceof RemoteException )
                    {
                        rootException = ((RemoteException)rootException).getCause();
                    }
                    
                    // Display the "real" exception.
                    System.out.flush();
                    System.err.println( "** ERROR **  " + rootException.getClass().getName() + ": " + rootException.getMessage() );
                    System.err.flush();
                    System.out.println();
                }
            }
            else
            {
                System.out.flush();
                System.err.println( "Could not find command \"" + cmdLine[0]
                        + '"' );
                exitCode = CMD_Unknown;
            }
        }

        return (exitCode);
    }

    /**
     *  Get the RMI registry on a particular machine.
     * 
     * @param host  Name of the host (null --> localhost)
     * @return  Registry, or <CODE>null</CODE>.
     */
    private Registry getRegistry( String host )
    {
        Registry registry = null;
        
        try
        {
            if( null != host )
            {
                registry = LocateRegistry.getRegistry( host, RegistrationManager.PORT );
            }
            else
            {
                registry = LocateRegistry.getRegistry( RegistrationManager.PORT );
            }
        }
        catch( Exception ignore )
        {
        }

        return( registry );
    }
    
    //
    //-- Commands ------------------------------------------------------------

    /**
     * Start the TuiConsole up.
     * 
     * @param cmdLine
     *            Array of strings from the command line.
     * @return Exit code of TuiConsole.
     */
    public int start( String[] cmdLine )
    {
        int exitCode = 0;
        Console console = null;

        if (null == System.getSecurityManager())
        {
            System.setSecurityManager( new RMISecurityManager() );
        }

        // Load the hsp properties.
        Property.loadProperties( cmdLine );
        Property.load( TuiConsole.class );

        // See if the command line has the console.
        String registryHost = System.getProperty( Console.JICOS_CONSOLE_HOST );
        int registryPort = RegistrationManager.PORT;
        boolean showHelp = false;
        //
        switch (cmdLine.length)
        {
            case 1:
                if ("-help".equals( cmdLine[0] ))
                {
                    showHelp = true;
                }
                else
                {
                    loadStartupConfig( cmdLine[0] );
                }
                break;

            case 0:
                break;

            default:
                showHelp = true;
                exitCode = 1;
                break;
        }

        if (showHelp)
        {
            System.err.println( "Usage: <java> TuiConsole [config.xml]" );
            if( 0 != exitCode )
            {
                System.exit( exitCode );
            }
        }

        
        // Startup files.
        String fileName = null;
        String homePrefix = "/.jicos/";
        String localPrefix = ".";
        String suffix = ".conf";
        
        if( System.getProperty( "os.name" ).toLowerCase().startsWith( "window" ) )
        {
            homePrefix = "Application Data/Jicos/";
            localPrefix = "";
        }
        
        // See if there is a global startupfile.
        if( null != (fileName = System.getProperty( "user.home" )) )
        {
            fileName += homePrefix + "tuiconsole" + suffix;
            doStartup( new File( fileName ) );
        }
        
        // See if there is a start-up file in the local directory
        fileName = "./" + localPrefix + "tuiconsole" + suffix;
        doStartup( new File( fileName ) );
        
        mainLoop();
        return (CMD_Error);
    }

    private void  loadStartupConfig( String fileName )
    {
        try
        {
            File file = new File( fileName );
            XmlDocument xmlDocument = new XmlDocument( file );
            this.startupConfig = new StartupConfig( xmlDocument );
        }
        catch( Exception anyException )
        {
            System.out.flush();
            System.err.println( "Could not understand configuration file: " + anyException.getMessage() );
            System.err.flush();
        }
        
        return;
    }


    public StartupConfig runStartupConfig( StartupConfig startupConfig ) throws Exception
    {
        StartupConfig  resultConfig = null;
        Console console = null;
        Registry registry = null;
        String consoleHost = null;

        assert null != startupConfig : "Startup configuration cannot be null";
        consoleHost = startupConfig.getConsoleHost();
        assert null != consoleHost : "Console host cannot be null";
        
        
        // Get the console (throws exception if no console).
        try
        {
            registry = LocateRegistry.getRegistry( consoleHost,
                    RegistrationManager.PORT );
            console = (Console) registry.lookup( Console.SERVICE_NAME );
        }
        catch (Exception anyException)
        {
        }

        if ((null == console)
                && startupConfig.getStartConsoleIfNecessary() )
        {
            console = startConsole( consoleHost );
        }

        if( null == console )
        {
            throw new NullPointerException( "Couldn't get a console at " + consoleHost );
        }
        
        // Pass everything to the console.
        resultConfig = console.startup( this.startupConfig );
        
        return( resultConfig );
    }

    // Start the console and wait for it to come up.
    private Console startConsole( String consoleHost ) throws RemoteException
    {
        Console  console = null;
        Registry registry = null;
        int attempt = 0;
        final int numAttempts = 5;
        final int delay = 750; // 0.75 seconds.
        
        if( Launcher.startChameleonConsole( consoleHost ) )
        {
            for( ; attempt < numAttempts; ++attempt )
            {
                try
                {
                    registry = LocateRegistry.getRegistry( consoleHost,
                            RegistrationManager.PORT );
                    console = (Console) registry.lookup( Console.SERVICE_NAME );
                }
                catch( Exception anyException )
                {
                }
                
                if( null == console )
                {
                    try
                    {
                        Thread.sleep( delay );
                    }
                    catch( InterruptedException interruptedException )
                    {
                    }
                }
            }
        }
        
        return( console );
    }
    
    private void doStartup( File file )
    {
        if( (null != file)
                && file.exists()
                && file.canRead() )
        {
            BufferedReader fileReader = null;
            
            try
            {
                fileReader = new BufferedReader( new FileReader( file ) );
                String inputLine = null;
                
                while( null != (inputLine = fileReader.readLine()) ){
                    performCommand( inputLine );
                }
                    
            }
            catch( Exception exception )
            {
                System.out.flush();
                System.err.println( exception.getClass().getName() + ": " + exception.getMessage() );
            }
            finally
            {
                try
                {
                    fileReader.close();
                }
                catch( Exception ignore )
                {
                }
            }
        }
    }

    //
    //-- Entry Point ---------------------------------------------------------

    /**
     * Entry point of TuiConsole.
     * 
     * @param cmdLine
     *            Array of strings from the command line.
     * @return Exit code of TuiConsole.
     */
    public static void main( String[] cmdLine ) throws MalformedURLException,
            NotBoundException, RemoteException
    {
        TuiConsole tuiConsole = new TuiConsole();
        System.exit( tuiConsole.start( cmdLine ) );
    }

    //
    //-- Inner Classes -------------------------------------------------------
     
    //
    //-- Commands ------------------------------------------------------------

    private abstract class cmd_Base implements Command
    {
        public abstract int invoke( String[] cmdLine ) throws Exception;
        public abstract String getDescription();
        public abstract String getHelp();
        
        public int objectToResult( Object resultObject )
        {
            int result = CMD_Error;
            
            if( resultObject instanceof Boolean )
            {
                if( ((Boolean)resultObject).booleanValue() )
                {
                    result = CMD_Okay;
                }
                else
                {
                    result = CMD_Error;
                }
            }
            else if( resultObject instanceof Number )
            {
                result = ((Number)resultObject).intValue();
            }

            return( result );
        }

    }
    
    
    private class cmd_Help extends cmd_Base
    {

        public int invoke( String[] cmdLine )
        {
            int result = 0;

            if ((null == cmdLine) || (1 == cmdLine.length)
                    || "all".equalsIgnoreCase( cmdLine[1] )
                    || "list".equalsIgnoreCase( cmdLine[1] ))
            {
                result = display( null );
            }
            else
            {
                result = display( cmdLine[1] );
            }

            return (result);
        }

        public String getDescription()
        {
            return ("Display help on a topic.");
        }

        public String getHelp()
        {
            return ("" + "    Get help on a command.\r\n" + "\r\n"
                    + "    Usage: help [topic]\r\n" + "\r\n"
                    + "    Examples:\r\n" + "        Jicos> help\r\n"
                    + "            ... displays all available commands ...\r\n"
                    + "\r\n" + "        Jicos> help help\r\n"
                    + "            ... displays this message ...\r\n");
        }

        private int display( String topic )
        {
            int exitCode = CMD_Error;

            if (null == topic)
            {
                CommandLine commandLine = null;
                final String space = "                          ";
                for (int cmd = 0; cmd < commandList.length; ++cmd)
                {
                    commandLine = commandList[cmd];
                    String text = "    " + commandLine.commandName;
                    text += space.substring( 0, 14 - text.length() );
                    text += commandLine.command.getDescription();

                    System.out.println( text );
                }
                System.out.println();
            }
            else
            {
                Command command = findCommand( topic );
                if (null == command)
                {
                    System.out.flush();
                    System.err
                            .println( "Cannot find command \"" + topic + "\"" );
                    System.err.flush();
                }
                else
                {
                    System.out.println();
                    String header = "----[ " + topic + " ]-----------"
                            + "-----------------------------------------"
                            + "-----------------------------------------";
                    header = header.substring( 0, 80 );
                    System.out.println( header );
                    System.out.println();

                    System.out.println( command.getHelp() );

                    //System.out.println();
                    System.out.println( ""
                            + "----------------------------------------"
                            + "----------------------------------------" );
                    System.out.println();
                    System.out.println();
                }

            }

            return (exitCode);
        }
    }

    private class cmd_Exit extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            if ((1 < cmdLine.length) && (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "exit" );
            }
            else
            {
                System.out.println();

                if ((null != cmdLine) && (1 < cmdLine.length))
                {
                    result = CMD_Error;
                    try
                    {
                        result = Integer.parseInt( cmdLine[1] );
                    }
                    catch (NumberFormatException numberFormatException)
                    {
                    }
                }
            }

            System.exit( result );
            return (CMD_Error); // function needs to return an int.
        }

        public String getDescription()
        {
            return ("Exit the console.");
        }

        public String getHelp()
        {
            return ("    Help not currently available");
        }
    }

    private class cmd_Local extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "local" );
            }
            else
            {
                result = CMD_Error;
                System.out
                        .println( "Cannot perform \"local\" commands at this time." );
            }
            return (result);
        }

        public String getDescription()
        {
            return ("Perform \"local\" command.");
        }

        public String getHelp()
        {
            return ("    Help not currently available");
        }
    }

    private class cmd_Show extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "show" );
            }
            else
            {
                String arg1 = cmdLine[1].toLowerCase();
                
                if( arg1.startsWith( "rmi" ) )
	            {
	                String hostName = "localhost";
	                if( 2 < cmdLine.length )
	                {
	                    hostName = cmdLine[2];
	                }
	                result = showRmiRegistry( hostName );
	            }
                else if( arg1.startsWith( "serv" ) )
                {
                    result = showServices();
                }
                else if (arg1.startsWith( "cons" ) )
                {
                    System.out.println( "Remote console: " + TuiConsole.remoteConsole );
                    System.out.println();
                }
                else if (arg1.startsWith( "prop" ) )
                {
                    if( 3 > cmdLine.length )
                    {
                        System.out.flush();
                        System.err.println( "**ERROR**  Usage: show property <name>" );
                        System.err.println();
                        System.err.flush();
                    }
                    else
                    {
                        String propName = cmdLine[2];
                        
                        if( cmdLine[2].equals( "policy" ) )
                        {
                            propName = "java.security.policy";
                        }
                        
                        else if( cmdLine[2].equals( "classpath" ) )
                        {
                            propName = "java.class.path";
                        }
                        
                        String propValue = System.getProperty( propName );
                        
                        if( null != propValue )
                        {
                            System.out.println( "System.getProperty( \"" + propName + "\" ) = \"" + propValue + '"' );
                        }
                        else
                        {
                            System.out.println( "System.getProperty( \"" + propName + "\" ) is not defined." );
                        }
                    }
                }
                   
                else if( arg1.startsWith( "start" ) )
                {
                    if( null == startupConfig )
                    {
                        System.out.println( "There is no configuration to show." );
                    }
                    else
                    {
                        System.out.print( startupConfig.toString() );
                    }
                }
	            else 
	            {
	                result = CMD_Error;
	                
	                System.out.flush();
	                System.err.println( "Don't know how to show \"" + cmdLine[1] + '"' );
	                System.err.flush();
	            }
            }
            return (result);
        }

        public String getDescription()
        {
            return ("Show a value.");
        }

        public String getHelp()
        {
            return (""
                    + "    Display a value.\r\n"
                    + "\r\n"
                    + "    Usage: show <object>\r\n"
                    + "\r\n"
                    + "    Where object is:\r\n"
                    + "        rmi [host]    - the RMI registry on a host.\r\n"
                    + "        services      - the services that the Console knows about.\r\n"
                    + "        console       - the reference to the remote console.\r\n"
                    + "        property prop - a system property.\r\n"
                    + "        startup       - the startup configuration.\r\n"
                    + "\r\n"
                    + "\r\n"
                    + "    Examples:"
                    + "        Jicos> show registry\r\n"
                    + "            ... displays all registered objects ...\r\n"
                    + "\r\n"
                    + "        Jicos> show property java.security.policy\r\n"
                    + "            ... displays the policy file name ...\r\n"
                    );
        }
        
        public int showRmiRegistry( String hostName )
        {
            int result = CMD_Okay;
            
            // If there is a host, and it isn't localhost...
            String remoteHost = null;
            if( (null != hostName) && !AdminUtilities.isLocalhost( hostName ) )
            {
                remoteHost = hostName;
            }
            
            // Get the registry.
            Registry registry = null;
            try
            {
                if( null != remoteHost )
                {
                    registry = LocateRegistry.getRegistry( hostName, RegistrationManager.PORT );
                }
                else
                {
                    registry = RegistrationManager.locateRegistry();
                }
            }
            catch( RemoteException remoteException )
            {
                registry = null;
            }
            catch( Exception anyException )
            {
                int i = 0;
            }
            
            
            if( null == registry )
            {
                System.out.print( "There is no registry on \"" + hostName + '"' );
                result = CMD_Error;
            }
            else
            {
                String[] registered = null;
                try
                {
                    registered = registry.list();
                    if (0 == registered.length)
                    {
                        System.out.println( "There is nothing registered on \""
                                + hostName + '"' );
                    }
                    else
                    {
                        String is = " are ";
                        String objs = " objects ";
                        if (1 == registered.length)
                        {
                            is = " is ";
                            objs = " object ";
                        }

                        System.out.println( "There" + is + registered.length
                                + " registed" + objs + "on \"" + hostName
                                + "\":" );
                        for (int r = 0; r < registered.length; ++r)
                        {
                            System.out.println( "  " + registered[r] );
                        }
                    }
                }
                catch (Exception exception)
                {
                    System.out
                            .println( "There was an error getting the registry from \""
                                    + hostName
                                    + "\": "
                                    + exception.getClass().getName()
                                    + " - "
                                    + exception.getMessage() );
                }
            }
            
            System.out.flush();

            return( result );
        }
  
        public int showServices()
        {
            int result = CMD_Okay;
            
            if( null == TuiConsole.remoteConsole )
            {
                System.out.println( "Must be connected to a console." );
                System.out.println();
                result = CMD_Error;
            }
            else
            {
                try
                {
                    List services = TuiConsole.remoteConsole.getServices();
                    if( null == services )
                    {
                        System.out.println( "There are no services listed." );
                        System.out.println();
                    }
                    else
                    {
                        int count =  services.size();
                        String is = " are ";
                        String objs = " items ";
                        String num = String.valueOf( count );
                        
                        if( 0 == count )
                        {
                            num = "no";
                        }
                        else if (1 == count )
                        {
                            is = " is ";
                            objs = " item ";
                        }

                        System.out.println( "There" + is + count
                                 + objs + ":" );
                        Iterator iterator = services.iterator();
                        while( iterator.hasNext() )
                        {
                            String item = (String)iterator.next();
                            System.out.println( "  " + item );
                        }
                    }
                }
                catch( Exception ignore )
                {
                    result = CMD_Error;
                }
                System.out.flush();
            }
            
            return( result );
        }
    }

    private class cmd_Connect extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            
            String registryHost = "localhost";
            boolean connectIfNotFound = true;
            int registryPort = RegistrationManager.PORT;
            
            if ((1 < cmdLine.length) && (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "connect" );
            }
            else
            {
                if( 1 < cmdLine.length )
                {
                    registryHost = cmdLine[1];
                    
                    if( 2 < cmdLine.length )
                    {
                        if( cmdLine[2].toLowerCase().startsWith( "no" ) )
                        {
                            connectIfNotFound = false;
                        }
                        
                        if( 3 < cmdLine.length )
                        {
	                        try
	                        {
	                            registryPort = Integer.parseInt( cmdLine[2] );
	                        }
	                        catch (NumberFormatException numberFormatException)
	                        {
	                        }
                        }
                    }
                }

                return (connect( registryHost, connectIfNotFound, registryPort ));
            }
            return (result);
        }

        public String getDescription()
        {
            return ("Connect to a remote Console.");
        }

        public String getHelp()
        {
            return (""
                    + "    Connect to a console.\r\n"
                    + "\r\n"
                    + "    Usage:  connect [host [nostart [port]]]\r\n"
                    + "\r\n"
                    + "        Where\r\n"
                    + "            host    - Hostname of console.\r\n"
                    + "            nostart - Don't start a Console if one doesn't exist.\r\n"
                    + "            port    - Registry port on host.\r\n"
                    + "\r\n"
                    + "    Examples:"
                    + "        Jicos> connect\r\n"
                    + "            ... connects to ConsoleImpl running on localhost ...\r\n"
                    );
        }

        public int connect( String hostName, boolean start, int portNumber ) throws Exception
        {
            int result = CMD_Error;
            Exception exception = null;
            
            try
            {
                result = connectToConsole( hostName, portNumber, 1 );
            }
            catch( Exception anyException )
            {
                exception = anyException;
            }

            // Connected?
            if( CMD_Okay == result )
            {
                System.out.println( "Connected to console at \"" + hostName
                        + ':' + portNumber + '"' );

            						//
                return( result );	//  ***  DOES NOT CONTINUE  ***
            }					//
            
            // Nope, but don't try to start it.
            else if( !start )
            {
                if( null != exception )
                {
                    throw exception;
                }
                else
                {
                    throw new RemoteException( "Could not connect to console." );
                }
            }
            
            // Try and start it.
            else if( CMD_Okay != ( result = cmdStart.startConsole( new String[]
                                                  { "start", "console", "localhost", "noconn" } )) )
            {
                throw new Exception( "Could not start a Console." );
            }
            
            // Now try and connect.
            else if (CMD_Okay == (result = connectToConsole( hostName,
                    portNumber, 5 )))
            {
                System.out.println( "Connected to console at \"" + hostName
                        + ':' + portNumber + '"' );
            }
            else
            {
                System.out.println( "Couldn't connect to console at \""
                        + hostName + ':' + portNumber + '"' );
            }

            return (result);
        }
        
        
        private int connectToConsole( String host, int port, int numAttempts )
        {
            int  result = CMD_Error;
            boolean isConnected = false;
            
            for( int attempt=0; (attempt<numAttempts) && !isConnected; ++attempt )
            {
	            try
	            {
		            Registry registry = LocateRegistry.getRegistry( host, port );
		
		            TuiConsole.remoteConsole = (Console) registry
		                    .lookup( Console.SERVICE_NAME );
		            
		            isConnected = true;
	            }
	            catch( Exception exception )
	            {
	                // ignored
	            }
	            
	            /*
	            catch (NotBoundException notBoundException)
	            {
	                throw (new NotBoundException(
	                        "No console found in registry at: \"" + host + ":" + port
	                                + "\"" ));
	            }
	            catch (RemoteException remoteException)
	            {
	                throw new RemoteException( "No registry found at: \""
	                        + host + ":" + port + "\"" );
	            }
	            */
	            
	            if( !isConnected && (attempt < numAttempts-1) )
	            {
	                try
	                {
	                    Thread.sleep( 750 );
	                }
	                catch( InterruptedException interruptedException )
	                {
	                    // ignore
	                }
	            }
            }
            
            if( isConnected )
            {
                result = CMD_Okay;
            }

            return( result );
        }

    }

    private class cmd_Start extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int exitCode = CMD_Okay;
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "start" );
            }
            else
            {
                String cmd1 = cmdLine[1].toLowerCase();
                
                // We can start a console on our own.
                if ( cmd1.startsWith( "cons" ) )
                {
                    exitCode = startConsole( cmdLine );
                }
                
                // We can start a chameleon on our own.
                else if ( cmd1.startsWith( "cham" ) )
                {
                    exitCode = startChameleon( cmdLine );
                }
                
                
                // Everything else needs a console.
                else if (null == TuiConsole.remoteConsole)
                {
                    throw new NullPointerException(
                            "There must be a remote console to start a \""
                                    + cmdLine[1] + "\"\r\n" );
                }

                // Launch configuration.
                else if( cmd1.startsWith( "start" ) )
                {
                    if( null == startupConfig )
                    {
                        throw new NullPointerException( "There must be a startup configuration loaded!\r\n" );
                    }
                    else
                    {
                        assert null != startupConfig : "Startup configuration cannot be null";
                        String consoleHost = startupConfig.getConsoleHost();
                        assert null != consoleHost : "Console host cannot be null";

                        // Start everything up.
                        StartupConfig resultConfig = TuiConsole.remoteConsole.startup( startupConfig );
                        if( null != resultConfig )
                        {
                            startupConfig = resultConfig;
                        }
                    }
                }

                // Pass command to the console.
                else
                {
                    Object commandResult = TuiConsole.remoteConsole.command( cmdLine );
                    exitCode = objectToResult( commandResult );
                }
            }
            
            return (exitCode);
        }

        public String getDescription()
        {
            return ("Start a service.");
        }

        public String getHelp()
        {
            return (""
                    + "    Start a service or startup configuration.\r\n"
                    + "\r\n"
                    + "    Usage: start <service> [options]    or\r\n"
                    + "           start startup\r\n"
                    + "\r\n"
                    + "    Known services:\r\n"
                    + "        console     [machine [NOCONNect]]\r\n"
                    + "        hsp         [machine [#hosts]\r\n"
                    + "        taskserver  [machine [hspdomain [#hosts]]]\r\n"
                    + "        tsmatlab    [machine [hspdomain [#hmatlabs]]]\r\n"
                    + "        host        [machine [ts-machine]]\r\n"
                    + "        hmatlab     [machine [ts-machine]]\r\n"
                    + "\r\n"
                    + "      For anything other than a console, there must be  a connection to\r\n"
                    + "    an active console.  The default machine, hspdomain and ts-machine\r\n"
                    + "    (TaskServer machine) is localhost, and the default number of hosts\r\n"
                    + "    (or matlabhosts) is 1.\r\n"
                    + "\r\n"
                    + "      See \"load\" for directions on how to load a startup configuration\r\n"
                    + "\r\n"
                    + "    Examples:\r\n"
                    + "        Jicos> start console\r\n"
                    + "            ... starts a ConsoleImpl on the localhost ...\r\n"
                    + "\r\n"
                    + "        Jicos> start hsp 192.168.5.5 42\r\n"
                    + "            ... starts a Host Service provider on 192.168.5.5 with 42 hosts ...\r\n"
                    + "\r\n"
                    + "        Jicos> start startup\r\n"
                    + "            ... starts the previously loaded startup configuration ...\r\n"
                    );
        }

        public int startConsole( String cmdLine[] ) throws Exception
        {
            int result = CMD_Error;
            String  hostName = "localhost";
            boolean  connect = true;
            boolean  wasStarted = false;
            

            // Is it really a remote host name?
            if( 2 < cmdLine.length )
            {
                hostName = cmdLine[2];
	            // Don't connect?
	            if( 3 < cmdLine.length )
	            {
	                String cmd3 = cmdLine[3].toLowerCase();
	                if( cmd3.startsWith( "noconn" ) )
	                {
	                    connect = false;
	                }
	            }
            }
            
            // Start the console.
            if( !AdminUtilities.isLocalhost( hostName ) )
            {
                wasStarted = Launcher.startChameleonConsole( hostName );
            }
            else
            {
                String policyFilename = null;
                try
                {
                    policyFilename = AdminUtilities.getPolicyFilename( "tuiconsole", "console" );
                }
                catch( IllegalArgumentException illegalArgumentException )
                {
                    System.out.flush();
                    System.err.println( "Please specify the tuiconsole." + AdminUtilities.BASE_POLICY + ".console property." );
                    System.err.flush();
                }
	                
                String jvmOpts = "-Djava.security.policy=" + policyFilename;
                jvmOpts += " -classpath " + System.getProperty( "java.class.path" );

                wasStarted = Launcher.start( jvmOpts, ConsoleImpl.class.getName(), null );
            }
            
            // Done?
            if( !connect )
            {
                result = (wasStarted ) ? CMD_Okay : CMD_Error;
            }
            else
            {
                if( null == hostName )
                {
                    hostName = "localhost";
                }
                
                int attempts = 3;
                int timeout = 2;
                result = CMD_Error;
                while( (attempts > 0) && (CMD_Error == result) )
                {
                    try
                    {
                        result = cmdConnect.connect( hostName, false, RegistrationManager.PORT );
                    }
                    catch( Exception anyException )
                    {
                        result = CMD_Error;
                    }
                    
                    if( CMD_Okay == result )
                    {
                        break;
                    }
                    else
                    {
                        Thread.sleep( timeout * 1000 );
                        timeout += timeout;
                    }
                }
            }
            
            if( CMD_Okay == result )
            {
                System.out.println( "Started a console on \"" + hostName + ":"
                        + RegistrationManager.PORT + "\"" );
                System.out.flush();
            }
            return (result);
        }
        
        public int startChameleon( String[] cmdLine ) throws Exception
        {
            int result = CMD_Error;
            boolean wasStarted = false;
            String policyFilename = null;
            
            try
            {
                policyFilename = AdminUtilities.getPolicyFilename( "tuiconsole", "chameleon" );
            }
            catch( IllegalArgumentException illegalArgumentException )
            {
                System.out.flush();
                System.err.println( "Please specify the tuiconsole." + AdminUtilities.BASE_POLICY + ".chameleon property." );
                System.err.flush();
            }
                
            String jvmOpts = "-Djava.security.policy=" + policyFilename;
            jvmOpts += " -classpath " + System.getProperty( "java.class.path" );

            if( Launcher.start( jvmOpts, ChameleonImpl.class.getName(), null ) )
            {
                System.out.println( "Started a Chameleon." );
                result = CMD_Okay;
            }

            return( result );
        }
    }

    private class cmd_Stop extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "stop" );
            }
            else
            {
                String arg1 = cmdLine[1].toLowerCase();
                
                if( null == TuiConsole.remoteConsole )
                {
                    System.out.flush();
                    System.err.println( "ERROR: You must first be connected to a Console!" );
                    System.err.println();
                    System.err.flush();
                }

                else if (arg1.startsWith( "cons" ))
                {
                    try
                    {
                        TuiConsole.remoteConsole.shutdown();
                    }
                    catch( UnmarshalException unmarshalException )
                    {
                        System.out.println( "Console was shutdown." );
                        System.out.flush();
                        result = CMD_Okay;
                    }
                    
                    TuiConsole.remoteConsole = null;
                }
                
                else
                {
                    Object commandResult = TuiConsole.remoteConsole.command( cmdLine );
                    if( commandResult instanceof Number )
                    {
                        result = ((Number)commandResult).intValue();
                    }
                    else if( commandResult instanceof Boolean )
                    {
                        result = CMD_Okay;
                        if( !((Boolean)commandResult).booleanValue() )
                        {
                            result = CMD_Error;
                        }
                    }

                    if( CMD_Okay == result )
                    {
                        System.out.println( "Stopped." );
                    }
                }
            }
            
            return (result);
        }

        public String getDescription()
        {
            return ("Stop a service.");
        }

        public String getHelp()
        {
            return (""
                    + "    Stop a service.\r\n"
                    + "\r\n"
                    + "    Usage: stop service [host]\r\n"
                    + "\r\n"
                    + "    Known services:\r\n"
                    + "        console\r\n"
                    + "        hsp\r\n"
                    + "        taskserver\r\n"
                    + "        chameleon\r\n"
                    + "\r\n"
                    + "    For anything other than a console, there must be "
                    + "a connection to\r\n    an active console\r\n"
                    + "\r\n"
                    + "    Examples:\r\n"
                    + "        Jicos> stop console\r\n"
                    + "            ... stops a ConsoleImpl on the localhost ...\r\n"
                    + "\r\n"
                    + "        Jicos> stop hsp 192.168.5.5\r\n"
                    + "            ...  stops a Host Service Provider on 192.168.5.5 ...\r\n"
                    );
        }
    }

    private class cmd_Load extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Error;
            
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "load" );
            }
            else
            {
                XmlDocument configXml = new XmlDocument( new File( cmdLine[1] ) );
                //  SAXException, IOException
                startupConfig = new StartupConfig( configXml );
                // InvalidParameterException

                result = CMD_Okay;
            }

            return (result);
        }

        public String getDescription()
        {
            return ("Load startup configuration file.");
        }

        public String getHelp()
        {
            return (""
                    + "    " + getDescription() + "\r\n"
                    + "\r\n"
                    + "    Usage: load filename\r\n"
                    + "\r\n"
                    );
        }
    }

    private class cmd_Log extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "log" );
            }
            else if ("on".equals( cmdLine[1] ))
            {
                Logger logger = LogManager.getLogger( null );
                logger.setLevel( LogManager.FINEST );
                
                System.out.println( "Default logger (" + logger.getName() +") is now at level " + logger.getLevel() );
            }
            else if ("off".equals( cmdLine[1] ))
            {
                Logger logger = LogManager.getLogger( null );
                logger.setLevel( LogManager.WARNING );

                System.out.println( "Default logger (" + logger.getName() +") is now at level " + logger.getLevel() );
            }

            return (result);
        }

        public String getDescription()
        {
            return ("Modify logging values.");
        }

        public String getHelp()
        {
            return (""
                    + "    " + getDescription() + "\r\n"
                    + "\r\n"
                    + "    Usage: log {on,off}\r\n"
                    + "\r\n"
                    + "\r\n"
                    + "    Examples:\r\n"
                    + "        Jicos> log on\r\n"
                    + "            ... sets the log level to FINEST ...\r\n"
                    + "\r\n"
                    + "        Jicos> log off\r\n"
                    + "            ... sets the log level to WARNING ...\r\n"
                    + "\r\n"
                    + "    This currently only effects the default ("
                    		+ LogManager.getLogger().getName() + ") logger.\r\n"
                    );
        }
    }

    private class cmd_Set extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            boolean  isSet = false;
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "set" );
            }
            else if( "set".equals( cmdLine[0] ) )
            {
                isSet = true;
            }
            
            return (result);
        }

        public String getDescription()
        {
            return ("Set/clear variables.");
        }

        public String getHelp()
        {
            return ("    Help not currently available");
        }
    }

    private class cmd_Console extends cmd_Base
    {
        private boolean stripArgv0;
        
        public cmd_Console()
        {
            this( false );
        }
        
        public cmd_Console( boolean stripArgv0 )
        {
            this.stripArgv0 = stripArgv0;
        }
        
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            
            if (null == TuiConsole.remoteConsole)
            {
                throw new NullPointerException( "There is no remote console!" );
            }
            else if( this.stripArgv0 )
            {
                String[] argv = new String[ cmdLine.length - 1];
                for( int argc=1; argc < cmdLine.length; ++argc )
                {
                    argv[argc-1] = cmdLine[argc];
                }
                
                TuiConsole.remoteConsole.command( argv );
                result = CMD_Okay;
            }
            else
            {
                TuiConsole.remoteConsole.command( cmdLine );
                result = CMD_Okay;
            }

            return (result);
        }

        public String getDescription()
        {
            return ("Pass a command to the Console to process.");
        }

        public String getHelp()
        {
            return ("    Help not currently available\r\n");
        }
    }

    private class cmd_Chameleon extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Error;
            
            if (null == TuiConsole.remoteConsole)
            {
                throw new NullPointerException( "There is no remote console!" );
            }
            else
            {
                cmdLine[0] = "chameleon";
                try
                {
                    TuiConsole.remoteConsole.command( cmdLine );
                    result = CMD_Okay;
                }
                catch( Exception anyException )
                {
                    String cmd1 = "???";
                    String cmd2 = "???";
                    
                    if( 2 <= cmdLine.length)
                    {
                        cmd1 = cmdLine[1];
	                    if( 3 <= cmdLine.length)
	                    {
	                        cmd2 = cmdLine[2];
	                    }
                    }
                    
                    
                    if( (anyException instanceof NotBoundException)
                            || ( (anyException instanceof ServerException)
                                    && (anyException.getCause() instanceof RemoteException)
                                    && (anyException.getCause().getCause() instanceof NotBoundException) ) )
                    {
	                    System.out.flush();
	                    System.err.println( "** There is no Chameleon registered at \"" + cmd2 + "\"" );
	                    System.err.println();
	                    System.err.flush();

	                    result = CMD_Error;
                    }
                    
                    else if( anyException instanceof ServerException )
                    {
                        throw (Exception)anyException.getCause();
                    }
                }
            }

            return (result);
        }

        public String getDescription()
        {
            return ("Invoke a command on the chameleon (via the Console).");
        }

        public String getHelp()
        {
            return ("    Help not currently available");
        }
    }

    private class cmd_Ping extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;
            boolean verbose = false;
            String service = null;

            
            if ((1 == cmdLine.length) || (cmdLine[1].endsWith( "help" )))
            {
                cmdHelp.display( "ping" );
            }
            else if (3 > cmdLine.length)
            {
                throw new IllegalArgumentException( "Usage: ping host service [verbose]" );
            }
            else
            {
                if( 3 < cmdLine.length )
                {
                    if( cmdLine[3].toLowerCase().startsWith( "verb" ) )
                    {
                        verbose = true;
                    }
                }
                try
                {
                    if( verbose )
                    {
                        System.out.println( "- Finding registry at \"" + cmdLine[1] + ":" + RegistrationManager.PORT + '"' );
                        System.out.flush();
                    }
                    Registry registry = LocateRegistry.getRegistry( cmdLine[1], RegistrationManager.PORT );
                    if( verbose )
                    {
                        System.out.println( "  + Got registry." );
                        System.out.flush();
                    }
                    
                    if( verbose )
                    {
                        System.out.println( "- Finding service name of \"" + cmdLine[2] + '"' );
                        System.out.flush();
                    }

                    if( null == (service = getServiceName( cmdLine[2] )) )
                    {
                        System.out.flush();
                        System.err.println( "Unknown service \"" + cmdLine[2] + '"' );
                        System.err.flush();
                    }
                    else
                    {
                        if( verbose )
                        {
                            System.out.println( "  + Got \"" + service + '"' );
                            System.out.println( "- Retrieving Administrable interface..." );
                            System.out.flush();
                        }
                        Administrable ping = (Administrable)registry.lookup( service );
                        if( verbose )
                        {
                            System.out.println( "  + Got interface." );
                            System.out.flush();
                        }
                        
                        if( verbose )
                        {
                            System.out.println( "- Retrieving Administrable interface..." );
                            System.out.flush();
                        }
                        if( "ping".equals( ping.echo( "ping" )) )
                        {
                            if( verbose )
                            {
                                System.out.println( "  + Got correct response." );
                                System.out.println();
                            }
                            else
                            {
                            System.out.println( "  \"" + service + "\" is running on " + cmdLine[1] );
                            }
                            System.out.flush();
                        }
                    }
                    
                    result = CMD_Okay;
                }
                catch( ConnectException connectException )
                {
                    System.out.flush();
                    System.err.println( "(TuiConsole) ConnectException: " + connectException.getMessage() );
                    System.err.println();
                    System.err.flush();
                }
                catch( Exception anyException )
                {
                    System.out.flush();
                    System.err.println( "(TuiConsole) "
                            + anyException.getClass().getName() + ": "
                            + anyException.getMessage() );
                    System.err.println();
                    System.err.flush();
                }
            }

            return (result);
        }

        public String getDescription()
        {
            return ("Try to get an echo from a service.");
        }

        public String getHelp()
        {
            return (""
                    + "    Ping a service.\r\n"
                    + "\r\n"
                    + "    Usage: ping host service [verbose]\r\n"
                    + "\r\n"
                    + "    Known services:\r\n"
                    + "        chameleon\r\n"
                    + "        console\r\n"
                    + "        hsp\r\n"
                    + "        taskserver\r\n"
                    + "\r\n"
                    + "    Examples:\r\n"
                    + "        Jicos> ping localhost console\r\n"
                    + "            ... tries to invoke echo() on the ConsoleImpl\r\n"
                    + "                  running on localhost ...\r\n"
                    + "\r\n"
                    + "        Jicos> ping localhost console verbose\r\n"
                    + "            ... tries to invoke echo() on the ConsoleImpl, Hsp,\r\n"
                    + "                  and TaskServer running on localhost, showing"
                    + "                  all intermediate steps ...\r\n"
                    );
        }
        
        public String getServiceName( String string )
        {
            String serviceName = null;
            
            if( null != string )
            {
	            String nickName = string.toLowerCase();
	            
	            if( nickName.equals( "hsp" ) )
	            {
	                serviceName = Hsp.SERVICE_NAME;
	            }
	            else if ( nickName.startsWith( "tasks" ) )
	            {
	                serviceName = TaskServer.SERVICE_NAME;
	            }
	            else if( nickName.startsWith( "host" ) )
	            {
	                // serviceName = Host.SERVICE_NAME;  A Host is not a service
	            }
	            else if( nickName.startsWith( "cons" ) )
	            {
	                serviceName = Console.SERVICE_NAME;
	            }
	            else if( nickName.startsWith( "cham" ) )
	            {
	                serviceName = Chameleon.SERVICE_NAME;
	            }
            }
            
            return( serviceName );
        }
    }

}
