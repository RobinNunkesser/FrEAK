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

package edu.ucsb.cs.jicos.admin;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import edu.ucsb.cs.jicos.admin.chameleon.Chameleon;
import edu.ucsb.cs.jicos.admin.chameleon.ChameleonImpl;
import edu.ucsb.cs.jicos.admin.common.Launcher;
import edu.ucsb.cs.jicos.admin.common.StartupConfig;
import edu.ucsb.cs.jicos.admin.common.StartupMachine;
import edu.ucsb.cs.jicos.foundation.Administrable;
import edu.ucsb.cs.jicos.foundation.RegistrationManager;
import edu.ucsb.cs.jicos.foundation.Service;
import edu.ucsb.cs.jicos.services.Host;
import edu.ucsb.cs.jicos.services.Hsp;
import edu.ucsb.cs.jicos.services.Property;
import edu.ucsb.cs.jicos.services.ServiceTaskStats;
import edu.ucsb.cs.jicos.services.TaskServer;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

public class ConsoleImpl extends UnicastRemoteObject implements Console,
        Administrable
{
    //
    //-- Constants -------------------------------------------------------

    public static final String POLICY_FILE = "policy/policy";

    private static final int SERVICE_Hsp = 1;
    private static final int SERVICE_TaskServer = 2;
    private static final int SERVICE_Host = 3;

    //
    //-- Variables -------------------------------------------------------

    private cmd_Start cmdStart;
    private cmd_Stop cmdStop;
    private cmd_Chameleon cmdChameleon;

    private CommandLine[] commandList;
    private Map registeredHsps;
    private List otherServices;
    private Map pendingTaskServers;

    //
    //-- Constructors ----------------------------------------------------

    public ConsoleImpl() throws RemoteException
    {
        this.cmdStart = new cmd_Start();
        this.cmdStop = new cmd_Stop();
        this.cmdChameleon = new cmd_Chameleon();

        commandList = new CommandLine[]
            {

            new CommandLine( "start", cmdStart ),
                    new CommandLine( "stop", cmdStop ),
                    new CommandLine( "chameleon", cmdChameleon, 4 ),

            };

        this.registeredHsps = new HashMap();
        this.otherServices = new LinkedList();
        this.pendingTaskServers = new HashMap();

        startLogListener();
    }

    //
    //-- Public Methods --------------------------------------------------

    /* @see edu.ucsb.cs.jicos.admin.Administrable#command(Object) */
    public Object command( Object commandObject ) throws RemoteException
    {
        Object result = null;
        Command command = null;

        if (commandObject instanceof String[])
        {
            String[] array = (String[]) commandObject;
            if (null == (command = findCommand( array[0] )))
            {
                throw new RemoteException( "Command \"" + array[0]
                        + "\" was not found" );
            }
            else
            {
                try
                {
                    int commandResult = command.invoke( array );
                    result = new Integer( commandResult );
                }
                catch (Exception anyException)
                {
                    throw new RemoteException( "Invoking command", anyException );
                }
            }
        }

        return (result);
    }

    /* @see edu.ucsb.cs.jicos.admin.Administrable#getHsps() */
    public AdministrableHsp[] getHsps() throws RemoteException
    {
        AdministrableHsp[] services = null;
        return (services);
    }

    /* @see edu.ucsb.cs.jicos.admin.Administrable#shutdown() */
    public void shutdown() throws RemoteException
    {
        boolean success = true;

        Registry registry = RegistrationManager.locateRegistry();
        try
        {
            registry.unbind( SERVICE_NAME );
        }
        catch (NotBoundException notBoundException)
        {
            throw new RemoteException( "ConsoleImpl is not bound.",
                    notBoundException );
        }

        System.exit( 0 );

        return;
    }

    /* @see edu.ucsb.cs.jicos.admin.Console#startup(edu.ucsb.cs.jicos.admin.common.LaunchConfig) */
    public StartupConfig startup( StartupConfig startupConfig )
            throws RemoteException
    {
        if (null != startupConfig.getHspList())
        {
            Iterator hsp = startupConfig.getHspList().iterator();
            while (hsp.hasNext())
            {
                Object[] hspItem = (Object[]) hsp.next();
                StartupMachine hspMachine = (StartupMachine) hspItem[0];
                if (!startupService( SERVICE_Hsp, hspMachine ))
                {
                    continue; // Couldn't start HSP.
                }

                Iterator ts = ((List) hspItem[1]).iterator();
                while (ts.hasNext())
                {
                    Object[] tsItem = (Object[]) ts.next();
                    StartupMachine tsMachine = (StartupMachine) tsItem[0];
                    if (!startupService( SERVICE_TaskServer, tsMachine ))
                    {
                        continue; // Couldn't start TaskServer.
                    }

                    Iterator h = ((List) tsItem[1]).iterator();
                    while (h.hasNext())
                    {
                        Object[] hItem = (Object[]) h.next();
                        StartupMachine hMachine = (StartupMachine) hItem[0];
                        startupService( SERVICE_Host, hMachine );
                    }
                    int i = 0;
                }
            }
        }

        return (startupConfig);
    }

    private boolean startupService( int serviceCode,
            StartupMachine startupMachine ) throws RemoteException
    {
        boolean success = false;

        if (null == startupMachine)
        {
            success = false;
        }
        else
        {
            String consoleProps = "";
            String jvmOptions = null;

            try
            {
                consoleProps = "-D" + JICOS_CONSOLE_HOST + "="
                        + InetAddress.getLocalHost().getCanonicalHostName();
            }
            catch (UnknownHostException unknownHostException)
            {
                throw new RemoteException(
                        "Couldn't detemine name of local host",
                        unknownHostException );
            }

            if (null == startupMachine.jvmOptions)
            {
                jvmOptions = consoleProps;
            }
            else
            {
                jvmOptions = startupMachine.jvmOptions + " " + consoleProps;
            }

            switch (serviceCode)
            {
                case SERVICE_Hsp:
                    success = startupMachine.started = Launcher
                            .startChameleonHsp( startupMachine.machine,
                                    startupMachine.hosts, jvmOptions );
                    break;

                case SERVICE_TaskServer:
                    if ((null == startupMachine.machine)
                            && (null == startupMachine.domain)
                            && (null == startupMachine.group))
                    {
                        success = true;
                    }
                    else
                    {
                        success = startupMachine.started = Launcher
                                .startChameleonTaskserver(
                                        startupMachine.machine,
                                        startupMachine.domain,
                                        startupMachine.hosts,
                                        startupMachine.extType, jvmOptions );
                    }
                    break;

                case SERVICE_Host:
                    success = startupMachine.started = Launcher
                            .startChameleonHost( startupMachine.machine,
                                    startupMachine.domain,
                                    startupMachine.extType, jvmOptions );
                    break;
            }
        }

        return (success);
    }

    /* @see edu.ucsb.cs.jicos.admin.Administrable#echo(String) */
    public String echo( String request ) throws RemoteException
    {
        return (request);
    }

    /* @see edu.ucsb.cs.jicos.admin.Administrable#register(Service,Service) */
    public boolean register( Service service, Service myParent )
            throws RemoteException
    {
        boolean success = false;
        RegisteredService taskServer = null;
        RegisteredService hsp = null;

        // If this is an Hsp, check if it's already registered, then add
        // it to the map of Hsps.
        if (service instanceof Hsp)
        {
            if (null == this.registeredHsps.get( service ))
            {
                hsp = new RegisteredService( service, null );
                this.registeredHsps.put( service, hsp );
            }

            // A duplicate registration is also a success....
            success = true;
        }

        else if (service instanceof TaskServer)
        {
            if ((null == myParent) || !(service instanceof Hsp))
            {
                throw new RemoteException(
                        "Must specify parent Hsp for this TaskServer" );
            }

            if (null != (hsp = (RegisteredService) this.registeredHsps
                    .get( myParent )))
            {
                hsp.addChild( service );
            }
            // Hsp is not registered yet; register it.
            else
            {
                hsp = new RegisteredService( myParent, null );
                this.registeredHsps.put( myParent, hsp );
                hsp.addChild( service );
            }
            success = true;
        }

        else if (service instanceof Host)
        {
            if ((null == myParent) || !(myParent instanceof TaskServer))
            {
                throw new RemoteException(
                        "Must specify parent TaskServer for this Host" );
            }

            // Find task server.
            boolean foundTaskServer = false;
            Iterator hspIterator = this.registeredHsps.keySet().iterator();
            while (!foundTaskServer && hspIterator.hasNext())
            {
                Object hspKey = hspIterator.next();
                hsp = (RegisteredService) this.registeredHsps.get( hspKey );
                {
                    Iterator taskServerIterator = hsp.getChildren().keySet()
                            .iterator();
                    while (!foundTaskServer && taskServerIterator.hasNext())
                    {
                        Object tsKey = taskServerIterator.next();
                        taskServer = (RegisteredService) hsp.getChildren().get(
                                tsKey );

                        foundTaskServer = myParent.equals( taskServer );
                    }
                }
            }

            // Did we find the task server?
            if (foundTaskServer)
            {
                taskServer.addChild( service );
            }
            else
            {

            }
        }
        else
        {

        }

        return (success);
    }

    /* @see edu.ucsb.cs.jicos.admin.Administrable#getServices() */
    public List getServices() throws RemoteException
    {
        return (null);
    }

    /* @see edu.ucsb.cs.jicos.admin.Administrable#getServiceTaskStats() */
    public List getServiceTaskStats() throws RemoteException
    {
        return (null);
    }

    //
    //-- Private Methods -----------------------------------------------------

    private boolean saveState()
    {
        boolean success = false;
        return (success);
    }

    private boolean loadState()
    {
        boolean success = false;
        return (success);
    }

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

    private void startLogListener()
    {
    }

    private RegisteredService findHsp( Hsp hsp )
    {
        RegisteredService registeredService = null;
        return (registeredService);
    }

    // taskServer is allowed to be null.
    private RegisteredService findTaskServer( TaskServer taskServer, Hsp hsp )
    {
        RegisteredService registeredService = null;
        return (registeredService);
    }

    // hsp and taskServer are allowed to be null.
    private RegisteredService findHost( Host host, TaskServer taskServer,
            Hsp hsp )
    {
        RegisteredService registeredHost = null;

        // If we have an Hsp, just look on it.
        if (0 != this.registeredHsps.size())
        {
            RegisteredService regServ = null;
            List allHsps = new LinkedList();
            List allTaskServers = new LinkedList();

            if ((null != hsp) && (null != (regServ = findHsp( hsp ))))
            {
                allHsps.add( regServ );
            }
            else
            {
                Iterator iterator = registeredHsps.entrySet().iterator();
                while (iterator.hasNext())
                {

                }
            }
        }

        return (registeredHost);
    }

    /**
     * Get the URL of the console.
     * 
     * @return //addr:port/service of console.
     */
    public String getJicosConsoleHost()
    {
        String jicosConsoleHome = null;
        String consoleIpAddress = "127.0.0.1";

        try
        {
            InetAddress localHost = InetAddress.getLocalHost();
            consoleIpAddress = localHost.getHostAddress();
        }
        catch (Exception ignore)
        {
        }

        jicosConsoleHome = "//" + consoleIpAddress + ":"
                + RegistrationManager.PORT + "/" + Console.SERVICE_NAME;

        return (jicosConsoleHome);
    }

    //
    //-- Static Methods ------------------------------------------------------

    public static int start( String[] cmdLine ) throws RemoteException,
            AlreadyBoundException
    {
        int exitCode = 0;
        StartupConfig startupConfig = null;

        if (null == System.getSecurityManager())
        {
            System.setSecurityManager( new RMISecurityManager() );
        }

        // Load the hsp properties.
        Property.loadProperties( cmdLine );
        Property.load( Console.class );

        if ((0 < cmdLine.length) && ("-start".equals( cmdLine[0] )))
        {
            try
            {
                XmlDocument startupXml = new XmlDocument( new File( cmdLine[1] ) );
                startupConfig = new StartupConfig( startupXml );
            }
            catch (IOException ioException)
            {
                System.out.println( "Couldn't parse \"" + cmdLine[1] + "\": "
                        + ioException.getMessage() );
                return (2);
            }
            catch (SAXException saxException)
            {
                System.out.println( "Couldn't parse \"" + cmdLine[1] + "\": "
                        + saxException.getMessage() );
                return (2);
            }
        }

        Console console = new ConsoleImpl();
        Registry registry = RegistrationManager.locateRegistry();
        registry.rebind( SERVICE_NAME, (Remote) console );

        if (null != startupConfig)
        {
            console.startup( startupConfig );
        }

        exitCode = 0;
        return (exitCode);
    }

    public static void main( String[] cmdLine ) throws RemoteException,
            AlreadyBoundException
    {
        // Note to C/C++ programmers: arguments start at cmdLine[0], not
        // at cmdLine[1]. :)
        //
        if ((1 == cmdLine.length) && ("-help".equals( cmdLine[0] )))
        {
            System.out.println( "Usage: <java> " + Console.class.getName()
                    + " [-start config.xml]" );
            System.exit( 0 );
        }
        else if ((1 == cmdLine.length) && ("-start".equals( cmdLine[0] )))
        {
            System.err.println( "You must specify configuration file." );
            System.exit( 1 );
        }
        else if ((2 < cmdLine.length) && ("-start".equals( cmdLine[0] )))
        {
            System.err.println( "You may only specify one configuration file." );
            System.exit( 1 );
        }

        System.setSecurityManager( new RMISecurityManager() );

        int exitCode = start( cmdLine );

        if (0 == exitCode)
        {
            System.out.println( "There is now a console running..." );
        }
        else
        {
            System.out.println( "There was a problem starting the console..." );
        }
    }

    //
    //-- Inner classes -------------------------------------------------------

    /**
     * Wrapper around a registered service.
     */
    private class RegisteredService
    {
        private Map children;
        private Service service;
        private Service parent;
        private ServiceTaskStats serviceTaskStats;

        private RegisteredService()
        {
            this.children = new HashMap();
            this.service = null;
            this.serviceTaskStats = null;
        }

        public RegisteredService(Service service, Service parent)
        {
            this.children = new HashMap();
            this.service = service;
            this.parent = parent;
            this.serviceTaskStats = null;
        }

        public Map getChildren()
        {
            return (this.children);
        }

        public Service getService()
        {
            return (this.service);
        }

        public void setService( Service service )
        {
            this.service = service;
        }

        public Service getParent()
        {
            return (this.parent);
        }

        public void setParent( Service parent )
        {
            this.parent = parent;
        }

        public ServiceTaskStats getServiceTaskStats()
        {
            return (this.serviceTaskStats);
        }

        public void setServiceTaskStats( ServiceTaskStats serviceTaskStats )
        {
            this.serviceTaskStats = serviceTaskStats;
        }

        public RegisteredService getChild( Service service )
        {
            return ((RegisteredService) children.get( service ));
        }

        public void addChild( Service service )
        {
            children.put( service, new RegisteredService( service, this
                    .getService() ) );
        }
    }

    //
    //-- Commands ------------------------------------------------------------

    private abstract class cmd_Base implements Command
    {
        public String getDescription()
        {
            return ("No description available for " + getClass().getName());
        }

        public String getHelp()
        {
            return ("No help available for " + getClass().getName());
        }

        public abstract int invoke( String[] cmdLine ) throws Exception;
    }

    private class cmd_Start extends cmd_Base
    {
        private final String CMD_Hsp = "hsp";
        private final String CMD_TaskServer = "taskserver";
        private final String CMD_Host = "host";
        private final String CMD_TaskServerMatlab = "tsmatlab";
        private final String CMD_HostMatlab = "hmatlab";

        /* @see edu.ucsb.cs.jicos.admin.Command#invoke(java.lang.String[]) */
        public int invoke( String[] cmdLine ) throws Exception
        {
            int success = CMD_Error;
            String numHosts = "1";
            String remoteHost = "localhost";
            String hspDomain = "localhost";
            
            String jvmOpts = "-D" + JICOS_CONSOLE_HOST + "="
                    + getJicosConsoleHost();

            
            if (1 < cmdLine.length)
            {
                //~~ Hsp ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //              " start hsp [machine [#hosts]\r\n"
                if (cmdLine[1].equals( CMD_Hsp ))
                {
                    if (2 > cmdLine.length)
                    {
                        throw new IllegalArgumentException( "Usage: start "
                                + CMD_Hsp + " [machine [#hosts]]" );
                    }
                    else
                    {
                        if (2 < cmdLine.length)
                        {
                            remoteHost = cmdLine[2];
                            if (3 < cmdLine.length)
                            {
                                numHosts = cmdLine[3];
                            }
                        }

                        if (Launcher.startChameleonHsp( remoteHost, numHosts,
                                jvmOpts ))
                        {
                            success = CMD_Okay;
                        }
                    }
                }

                //~~ TaskServer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //              " start taskserver [machine [hspdomain [#hosts]]]\r\n"
                else if (cmdLine[1].equals( CMD_TaskServer ))
                {
                    if ((2 > cmdLine.length) || (5 < cmdLine.length))
                    {
                        throw new IllegalArgumentException( "Usage: start "
                                + CMD_TaskServer
                                + " [machine [hspdomain [#hosts]]]" );
                    }
                    else
                    {
                        if (2 < cmdLine.length)
                        {
                            remoteHost = cmdLine[2];
                            if (3 < cmdLine.length)
                            {
                                hspDomain = cmdLine[3];
                                if (4 < cmdLine.length)
                                {
                                    numHosts = cmdLine[4];
                                }
                            }
                        }

                        if (Launcher.startChameleonTaskserver( remoteHost,
                                hspDomain, numHosts, Launcher.EXTTYPE_None,
                                jvmOpts ))
                        {
                            success = CMD_Okay;
                        }
                    }
                }

                //~~ Host ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //              " start host [machine [ts-machine]]\r\n"
                else if (cmdLine[1].equals( CMD_Host ))
                {
                    if ((2 > cmdLine.length) || (4 < cmdLine.length))
                    {
                        throw new IllegalArgumentException( "Usage: start "
                                + CMD_Host + " [machine [ts-machine]]" );
                    }
                    else
                    {
                        if (2 < cmdLine.length)
                        {
                            remoteHost = cmdLine[2];
                            if (3 < cmdLine.length)
                            {
                                hspDomain = cmdLine[3];
                            }
                        }

                        if (Launcher.startChameleonHost( remoteHost, hspDomain,
                                Launcher.EXTTYPE_None, jvmOpts ))
                        {
                            success = CMD_Okay;
                        }
                    }
                }

                //~~ TaskServerMatlab ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //              " start tsmatlab [machine [hspdomain [#hmatlabs]]]\r\n"
                else if (cmdLine[1].equals( CMD_TaskServerMatlab ))
                {
                    if ((2 > cmdLine.length) || (5 < cmdLine.length))
                    {
                        throw new IllegalArgumentException( "Usage: start "
                                + CMD_TaskServerMatlab
                                + " [machine [hspdomain [#hosts]]]" );
                    }
                    else
                    {
                        if (2 < cmdLine.length)
                        {
                            remoteHost = cmdLine[2];
                            if (3 < cmdLine.length)
                            {
                                hspDomain = cmdLine[3];
                                if (4 < cmdLine.length)
                                {
                                    numHosts = cmdLine[4];
                                }
                            }
                        }

                        if (Launcher.startChameleonTaskserver( remoteHost,
                                hspDomain, numHosts, Launcher.EXTTYPE_Matlab,
                                jvmOpts ))
                        {
                            success = CMD_Okay;
                        }
                    }
                }

                //~~ HostMatlab ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //              " start hmatlab [machine [ts-machine]]\r\n"
                else if (cmdLine[1].equals( CMD_HostMatlab ))
                {
                    if ((2 > cmdLine.length) || (4 < cmdLine.length))
                    {
                        throw new IllegalArgumentException( "Usage: start "
                                + CMD_HostMatlab + " [machine [ts-machine]]" );
                    }
                    else
                    {
                        if (2 < cmdLine.length)
                        {
                            remoteHost = cmdLine[2];
                            if (3 < cmdLine.length)
                            {
                                hspDomain = cmdLine[3];
                            }
                        }

                        if (Launcher.startChameleonHost( remoteHost, hspDomain,
                                Launcher.EXTTYPE_Matlab, jvmOpts ))
                        {
                            success = CMD_Okay;
                        }
                    }
                }
            }

            return (success);
        }
    }

    private class cmd_Stop extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;

            if ((2 > cmdLine.length) && (3 < cmdLine.length))
            {
                throw new IllegalArgumentException(
                        "Usage: stop <service> [host]" );
            }
            else
            {
                String remoteHost = "localhost";
                if (2 < cmdLine.length)
                {
                    remoteHost = cmdLine[2];
                }

                String arg1 = cmdLine[1].toLowerCase();

                if (arg1.equals( "hsp" ))
                {
                    result = stop( remoteHost, Hsp.SERVICE_NAME );
                }
                else if (arg1.startsWith( "task" ))
                {
                    result = stop( remoteHost, TaskServer.SERVICE_NAME );
                }
                else if (arg1.startsWith( "cham" ))
                {
                    result = stop( remoteHost, Chameleon.SERVICE_NAME );
                }

                else
                {
                    throw new IllegalArgumentException(
                            "Don't know how to stop \"" + cmdLine[1] + "\"" );
                }
            }
            return (result);
        }

        private int stop( String host, String service ) throws Exception
        {
            int success = CMD_Error;

            Registry rmiRegistry = LocateRegistry.getRegistry( host,
                    RegistrationManager.PORT );
            Administrable admin = (Administrable) rmiRegistry.lookup( service );

            if (null != admin)
            {
                try
                {
                    admin.shutdown();
                    success = CMD_Okay;
                }
                catch (UnmarshalException unmarshalException)
                {
                    // this is expected
                    success = CMD_Okay;
                }
            }

            return (success);
        }
    }

    private class cmd_Chameleon extends cmd_Base
    {
        public int invoke( String[] cmdLine ) throws Exception
        {
            int result = CMD_Okay;

            if (2 > cmdLine.length)
            {
                throw new IllegalArgumentException(
                        "Usage: cham[eleon] command [arg ...]" );
            }
            else
            {
                String command = cmdLine[1].toLowerCase();

                // What to do?
                if (command.startsWith( "shut" ) || command.startsWith( "stop" ))
                {
                    if (3 > cmdLine.length)
                    {
                        throw new NullPointerException(
                                "remoteHost (cmdLine[2]) cannot be null." );
                    }

                    Administrable administrable = ChameleonImpl
                            .getAdministrable( cmdLine[2] );
                    result = stop( administrable );
                }
            }

            return (result);
        }

        private int stop( Administrable administrable ) throws Exception
        {
            int result = CMD_Error;

            try
            {
                administrable.shutdown();
                result = CMD_Okay;
            }
            catch (UnmarshalException unmarshalException)
            {
                // Consume this error.
                result = CMD_Okay;
            }

            return (result);
        }
    }

}