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
 * Processes a startup configuration XML file and generates a startup
 * configuration that can be passed to Console.startup().
 *
 * @author  pippin
 */

package edu.ucsb.cs.jicos.admin.common;

import edu.ucsb.cs.jicos.services.external.XmlDocument;
import edu.ucsb.cs.jicos.foundation.LogManager;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

public class StartupConfig implements Serializable
{
    private static final String DEFAULT_STARTSTYLE = StartupMachine.DEFAULT_STARTSTYLE;

    private List hspList;
    private List machineList;
    private String desc;
    private String consoleHost;
    private boolean startConsoleIfNecessary;

    private StartupConfig()
    {
        // To prevent this from getting used.
    };

    public StartupConfig(XmlDocument jicosXml) throws InvalidParameterException
    {
        // Initialize variables.
        this.desc = null;
        this.hspList = new LinkedList();
        this.machineList = new LinkedList();
        this.consoleHost = null;
        this.startConsoleIfNecessary = true;

        Map hosts = new HashMap();
        Map groups = new HashMap();
        String jvmOptions = null;


        // Get the description.
        this.desc = jicosXml.getValue( "/Desc" );

        // Get the defaults.
        StartOptions startOptions = new StartOptions();
        startOptions.startStyle = getDefaultStartStyle();
        Element[] defaults = jicosXml.getElement( "/Defaults" );
        if( 1 < defaults.length ) {
            throw new InvalidParameterException( "May only have one /Jicos/Defaults element" );
        } else if( 1 == defaults.length ) {
            startOptions.parse( new XmlDocument( defaults[0] ) );
        }

        // Load the machine maps.
        Element machineArray[] = jicosXml.getElement( "/Machines" );
        if (null != machineArray)
        {
            loadMachines( machineArray, hosts, groups, startOptions );
        }

        /*
         Element consoleArray[] = xml.getElement( "/Jicos/Console" );
         if( null != consoleArray )
         {
         setConsole( consoleArray, hosts, groups );
         }
         */

        // Get any global JVM options.
        jvmOptions = jicosXml.getValue( "/JvmOptions" );

        // Build the service tree.
        Element[] hspArray = jicosXml.getElement( "/Hsp" );
        if (null != hspArray)
        {
            buildTree( hspArray, hosts, groups, startOptions, jvmOptions );
        }

        return;
    }

    private int getDefaultStartStyle() {
        String startStyle = System.getProperty( "jicos.startup.style", DEFAULT_STARTSTYLE );
        int defaultStartStyle = StartupMachine.getStartStyle( startStyle );
        if( (StartupMachine.STARTSTYLE_unknown == defaultStartStyle)
         && (!startStyle.equals( "")) ) {
            LogManager.getLogger().log( LogManager.WARNING, "Unknown log style: \""
                    + startStyle + '"' );
        }

        return( defaultStartStyle );
    }

    //
    //-- Accessors -----------------------------------------------------------

    public List getHspList()
    {
        return (this.hspList);
    }

    public String getDescription()
    {
        return (this.desc);
    }

    public List getMachineList()
    {
        return (this.machineList);
    }

    public String getConsoleHost()
    {
        return (this.consoleHost);
    }

    public boolean getStartConsoleIfNecessary()
    {
        return (this.startConsoleIfNecessary);
    }

    public String toString()
    {
        StringBuffer stringBuffer = new StringBuffer();
        final String CRLF = "\r\n";

        if (null != this.desc)
        {
            stringBuffer.append( '"' + this.desc + '"' + CRLF );
        }

        if (null != this.machineList)
        {
            stringBuffer.append( CRLF );
            stringBuffer.append( "Machines:" + CRLF );

            Iterator machine = this.machineList.iterator();
            while (machine.hasNext())
            {
                String name = (String) machine.next();
                stringBuffer.append( "    " + name + CRLF );
            }
        }

        if (null != this.hspList)
        {
            stringBuffer.append( CRLF );
            stringBuffer.append( "Config:" + CRLF );

            Iterator hsp = this.hspList.iterator();
            while (hsp.hasNext())
            {
                Object[] hspItem = (Object[]) hsp.next();
                StartupMachine hspMachine = (StartupMachine) hspItem[0];
                stringBuffer.append( "  HSP  " + hspMachine.toString() + CRLF );

                Iterator ts = ((List) hspItem[1]).iterator();
                while (ts.hasNext())
                {
                    Object[] tsItem = (Object[]) ts.next();
                    StartupMachine tsMachine = (StartupMachine) tsItem[0];
                    if ((null == tsMachine.machine)
                            && (null == tsMachine.domain)
                            && (null == tsMachine.group))
                    {
                        stringBuffer.append( "    TS (HSP's inner)" + CRLF );

                    }
                    else
                    {
                        stringBuffer.append( "    TS  " + tsMachine.toString()
                                + CRLF );
                    }

                    Iterator h = ((List) tsItem[1]).iterator();
                    while (h.hasNext())
                    {
                        Object[] hItem = (Object[]) h.next();
                        StartupMachine hMachine = (StartupMachine) hItem[0];
                        stringBuffer.append( "      HOST  "
                                + hMachine.toString() + CRLF );
                    }
                }
            }
        }

        stringBuffer.append( CRLF );

        return (new String( stringBuffer ));
    }

    /*
     * Load the machines.
     *
     * Go through all the elements in the machine list.  Create iterators
     * out of the group to facillitate round robin.
     *
     * @param  machineArray  The Machines elements in the config XML.
     * @param  hosts         A map of all the hosts & addresses.
     * @param  groups        A map of iterators of all the groups.
     */
    private void loadMachines( Element[] machineArray, Map hosts, Map groups, StartOptions startOptions )
    {
        for (int machine = 0; machine < machineArray.length; ++machine)
        {
            XmlDocument machineXml = new XmlDocument( machineArray[machine] );

            // Add all the machines that aren't in a group.
            Element[] solo = machineXml.getElement( "/Machine" );
            if (null != solo)
            {
                for (int m = 0; m < solo.length; ++m)
                {
                    HostItem hostItem = new HostItem( solo[m], startOptions );
                    hosts.put( hostItem.name, hostItem );
                    this.machineList.add( hostItem.name + "=" + hostItem.addr + " " );
                }
            }

            // Add all the groups.
            Element[] groupArray = machineXml.getElement( "/Group" );
            if (null != groupArray)
            {
                Map allGroups = new HashMap();

                // Foreach group
                for (int group = 0; group < groupArray.length; ++group)
                {
                    XmlDocument groupXml = new XmlDocument( groupArray[group] );
                    String groupName = groupXml.getValue( "@name" );

                    // Get the defaults.
                    StartOptions groupOptions = new StartOptions( startOptions );
                    startOptions.startStyle = getDefaultStartStyle();
                    Element[] defaults = groupXml.getElement( "/Defaults" );
                    if( 1 < defaults.length ) {
                        throw new InvalidParameterException(
                                "May only have one /Defaults element in machine group \""
                                + groupName + '"' );
                    } else if( 1 == defaults.length ) {
                        groupOptions.parse( new XmlDocument( defaults[0] ) );
                    }


                    List memberList = new LinkedList();

                    // Foreach machine in the group.
                    Element[] member = groupXml.getElement( "/Machine" );
                    if (null != member)
                    {
                        for (int m = 0; m < member.length; ++m)
                        {
                            HostItem hostItem = new HostItem( member[m], groupOptions );
                            if (null == hosts.get( hostItem.name ))
                            {
                                hosts.put( hostItem.name, hostItem );
                            }

                            this.machineList.add( hostItem.name + "=" + hostItem.addr + " " );
                            memberList.add( hostItem.name );
                        }
                    }

                    allGroups.put( groupName, memberList );
                }

                // Now create iterators out of all the lists.
                //
                Iterator iterator = allGroups.keySet().iterator();
                while (iterator.hasNext())
                {
                    String groupName = (String) iterator.next();
                    List memberList = (List) allGroups.get( groupName );

                    groups.put( groupName, memberList.iterator() );
                }
            }
        }
    }

    /*
     * Build the startupMachine tree.
     *
     * @param  hspArray    The Hsp elements in the config XML.
     * @param  hosts       A map of all the hosts & addresses.
     * @param  groups      A map of iterators of all the groups.
     * @param  jvmOptions  Global JVM options.
     */
    private void buildTree( Element[] hspArray, Map hosts, Map groups, StartOptions startOptions,
            String jvmOptions ) throws InvalidParameterException
    {
        this.hspList = new LinkedList();
        String globalOptions = "";

        if (null != jvmOptions)
        {
            globalOptions = jvmOptions;
        }

        for (int hs = 0; hs < hspArray.length; hs++)
        {
            // <Hsp host="csil" name="Instructional Labs">
            XmlDocument hspXml = new XmlDocument( hspArray[hs] );

            // Get the defaults.
            StartOptions hspOptions = new StartOptions( startOptions );
            Element[] hspDefaults = hspXml.getElement( "/Defaults" );
            if( 1 < hspDefaults.length ) {
                String error = "May only have one <Default> element in Hsp";
                String hspName = hspXml.getValue( "@name" );
                if( null != hspName ) {
                    error += " \"" + hspName + '"';
                }
                throw new InvalidParameterException( error );
            } else if( 1 == hspDefaults.length ) {
                hspOptions.parse( new XmlDocument( hspDefaults[0] ) );
            }


            StartupMachine hsp = new StartupMachine( hspXml, hspOptions );
            assignMachine( hsp, hosts, groups );
            assignJvmOptions( hsp, globalOptions, hspXml );
            String hspDomain = hsp.machine;

            Object[] hspItem = new Object[2];
            hspItem[0] = hsp;
            hspItem[1] = new LinkedList();
            hspList.add( hspItem );

            Element[] tsArray = hspXml.getElement( "/TaskServer" );
            for (int ts = 0; ts < tsArray.length; ++ts)
            {
                // <TaskServer group="gsl" name="TS-GSL" hosts="1">
                XmlDocument tsXml = new XmlDocument( tsArray[ts] );

                // Get the defaults.
                StartOptions tsOptions = new StartOptions( hspOptions );
                Element[] tsDefaults = hspXml.getElement( "/Defaults" );
                if( 1 < tsDefaults.length ) {
                    String error = "May only have one <Default> element in TaskServer";
                    String tsName = tsXml.getValue( "@name" );
                    if( null != tsName ) {
                        error += " \"" + tsName + '"';
                    }
                    throw new InvalidParameterException( error );
                } else if( 1 == tsDefaults.length ) {
                    tsOptions.parse( new XmlDocument( tsDefaults[0] ) );
                }


                StartupMachine taskServer = new StartupMachine( tsXml, tsOptions );
                assignMachine( taskServer, hosts, groups );
                assignJvmOptions( taskServer, globalOptions, tsXml );
                String tsDomain = taskServer.machine;
                if (null == taskServer.domain)
                {
                    taskServer.domain = hspDomain;
                }

                Object[] tsItem = new Object[2];
                tsItem[0] = taskServer;
                tsItem[1] = new LinkedList();
                ((List) hspItem[1]).add( tsItem );

                Element[] hostArray = tsXml.getElement( "/Host" );
                for (int ho = 0; ho < hostArray.length; ++ho)
                {
                    // <Host group="gsl"/>
                    XmlDocument hostXml = new XmlDocument( hostArray[ho] );
                    StartupMachine host = new StartupMachine( hostXml, tsOptions );
                    assignMachine( host, hosts, groups );
                    assignJvmOptions( host, globalOptions, hostXml );
                    if (null == host.domain)
                    {
                        host.domain = tsDomain;
                    }

                    Object[] hostItem = new Object[2];
                    hostItem[0] = host;
                    hostItem[1] = null;
                    ((List) tsItem[1]).add( hostItem );
                }
            }

            // Hosts assigned to a particular Hsp.
            Element[] hostArray = hspXml.getElement( "/Host" );
            if ((null != hostArray) && (0 < hostArray.length))
            {
                Object[] mtTaskServer = new Object[2];
                mtTaskServer[0] = new StartupMachine();
                mtTaskServer[1] = new LinkedList();
                ((List) hspItem[1]).add( mtTaskServer );

                for (int ho = 0; ho < hostArray.length; ++ho)
                {
                    // <Host group="gsl"/>
                    XmlDocument hostXml = new XmlDocument( hostArray[ho] );
                    StartupMachine host = new StartupMachine( hostXml, hspOptions );
                    assignMachine( host, hosts, groups );
                    if (null == host.domain)
                    {
                        host.domain = hspDomain;
                    }

                    Object[] hostItem = new Object[2];
                    hostItem[0] = host;
                    hostItem[1] = null;
                    ((List) mtTaskServer[1]).add( hostItem );
                }
            }
        }

    }

    /*
     * Get the console machine.
     *
     * If there is more than one element in the consoleArray, that is an
     * error.
     * 
     * @param  consoleArray  The Machines elements in the config XML.
     * @param  hosts         A map of all the hosts & addresses.
     * @param  groups        A map of iterators of all the groups.
     * @param  consoleHost   The name of the console machine, if specified (in/out).
     * @param  startConsole  Start the console, if necessary (in/out).
     * @throws IllegalArgumentException  More than one console is specified.
     */
    private void setConsole( Element[] consoleArray, Map hosts, Map groups )
            throws IllegalArgumentException
    {
        if ((null == consoleArray) || (0 == consoleArray.length))
        {
        }
        else if (1 < consoleArray.length)
        {
            throw new IllegalArgumentException(
                    "Cannot specify more than 1 console." );
        }
        else
        {
            XmlDocument consoleXml = new XmlDocument( consoleArray[0] );
            String hostname = null;

            if (null == (hostname = consoleXml.getValue( "@machine" )))
            {
                hostname = "localhost";
            }
            else
            {
                this.consoleHost = (String) hosts.get( hostname );
            }

            String ifNull = consoleXml.getValue( "@ifNull" );
            if (null != ifNull)
            {
                String lower = ifNull.toLowerCase();

                if (lower.equals( "start" ))
                {
                    this.startConsoleIfNecessary = true;
                }
                else if (lower.equals( "abort" ) || lower.equals( "nostart" ))
                {
                    this.startConsoleIfNecessary = false;
                }
            }
        }

        return;
    }

    /**
     * Pick a machine from a group.
     * 
     * This is currently a round robin selection.
     * 
     * @param startupMachine  The name of the machine on which to start.
     * @param hosts  All the known hosts.
     * @param groups  All the known groups.
     * @throws InvalidParameterException If the group has no more machines.
     */
    private void assignMachine( StartupMachine startupMachine, Map hosts,
            Map groups ) throws InvalidParameterException
    {
        Iterator iterator = null;
        HostItem hostItem = null;

        if (null != startupMachine.machine)
        {
            if (null != (hostItem = (HostItem) hosts.get( startupMachine.machine )))
            {
                startupMachine.machine = hostItem.addr;
                startupMachine.startStyle = hostItem.startStyle;
            }
        }
        else if (null == startupMachine.group)
        {
            throw new InvalidParameterException(
                    "You must specify a host or group" );
        }
        else if (null == (iterator = (Iterator) groups
                .get( startupMachine.group )))
        {
            throw new InvalidParameterException( "Unknown group \""
                    + startupMachine.group + "\"" );
        }
        else if (!iterator.hasNext())
        {
            throw new InvalidParameterException( "All hosts in group \""
                    + startupMachine.group + "\" have been used" );
        }
        else
        {
            startupMachine.machine = (String) iterator.next();
            if (null != (hostItem = (HostItem) hosts.get( startupMachine.machine )))
            {
                startupMachine.machine = hostItem.addr;
                startupMachine.startStyle = hostItem.startStyle;
            }
        }


        return;
    }

    /*
     * Assign the JVM options.
     * 
     * @param  startupMachine  The startup machine to assign options.
     * @param  globalOptions   The global JVM options.
     * @param  xml             The XML associated with this machine.
     */
    private void assignJvmOptions( StartupMachine startupMachine,
            String globalOptions, XmlDocument xml )
    {
        String addtlOptions = xml.getValue( "/JvmOptions" );

        if (null == addtlOptions)
        {
            startupMachine.jvmOptions = globalOptions;
        }
        else
        {
            String optAction = xml.getValue( "/JvmOptions/@action" );

            if ((null != optAction) || (optAction.equals( "append" )))
            {
                startupMachine.jvmOptions = globalOptions + " " + addtlOptions;
            }
            else if (optAction.equals( "prepend" ))
            {
                startupMachine.jvmOptions = addtlOptions + " " + globalOptions;
            }
            if (optAction.equals( "replace" ))
            {
                startupMachine.jvmOptions = addtlOptions;
            }
            if (optAction.equals( "remove" ))
            {
                startupMachine.jvmOptions = null;
            }
        }

        return;
    }


    public class StartOptions {
        public int     startStyle;

        public StartOptions() {
            startStyle = StartupMachine.getStartStyle( DEFAULT_STARTSTYLE );
        }

        public StartOptions( int startStyle ) {
            this.startStyle = startStyle;
        }

        public StartOptions( StartOptions startOptions ) {
            this.startStyle = startOptions.startStyle;
        }

        public void parse( XmlDocument xml ) {
            int startCode = StartupMachine.getStartStyle( xml.getValue( "/StartStyle" ) );
            if( StartupMachine.STARTSTYLE_unknown != startCode ) {
                startStyle = startCode;
            }
        }
    }

    private class HostItem {
        public String name;
        public String addr;
        public int startStyle;

        private HostItem(){}

        public HostItem( String name, String addr, int startStyle ) {
            this.name = name;
            this.addr = addr;
            this.startStyle = startStyle;
        }

        public HostItem( Element element, StartOptions startOptions ) {
            XmlDocument xml = new XmlDocument( element );

            // Assign defaults.
            startStyle = startOptions.startStyle;

            String nameValue = xml.getValue( "@name" );
            String addrValue = xml.getValue( "@addr" );
            String startValue = xml.getValue( "@startStyle" );

            name = nameValue;
            addr = addrValue;
            if( null != startValue ) {
                startStyle = StartupMachine.getStartStyle( startValue );
            }
        }
    }

}