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

package edu.ucsb.cs.jicos.admin.chameleon;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Chameleon extends Remote
{
    //
    // -- Constants ----------------------------------------------------------

    /** The Chameleon service name. */
    public static final String SERVICE_NAME = "JicosChameleon";

    /** Default value of the refresh before starting a service. */
    public static final boolean DEFAULT_REFRESH = true;

    /** Verify the command against a signature. */
    public static final boolean VERIFY_COMMAND = false;

    /** Command to shutdown a chameleon. */
    public static final String CMD_Shutdown = "shutdown";

    /** Command to shutdown a chameleon. */
    public static final String CMD_Hsp = "Hsp";

    /** Command to shutdown a chameleon. */
    public static final String CMD_TaskServer = "TaskServer";

    /** Command to shutdown a chameleon. */
    public static final String CMD_Host = "Host";

    /** Command to shutdown a chameleon. */
    public static final String CMD_TaskServerMatlab = "TaskServerMatlab";

    /** Command to shutdown a chameleon. */
    public static final String CMD_HostMatlab = "HostMatlab";

    /** Command to shutdown a chameleon. */
    public static final String CMD_Console = "Console";

    /** Name of the file containing paroperties. */
    public static final String PROPERTY_FILENAME = "edu/ucsb/cs/jicos/admin/chameleon/default.properties";

    // Properties
    public static final String PROP_PropertyFilename = "chameleon.property.filename";

    public static final String PROP_RefreshJarfileBaseurl = "chameleon.refresh.jarfile.baseurl";
    public static final String PROP_RefreshJarfileFilename = "chameleon.refresh.jarfile.filename";
    public static final String PROP_RefreshDirname = "chameleon.refresh.dirname";
    public static final String PROP_SecurityPolicyHsp = "chameleon.security.policy.hsp";
    public static final String PROP_SecurityPolicyTaskserver = "chameleon.security.policy.taskserver";
    public static final String PROP_SecurityPolicyTaskservermatlab = "chameleon.security.policy.taskservermatlab";
    public static final String PROP_SecurityPolicyHost = "chameleon.security.policy.host";
    public static final String PROP_SecurityPolicyHostmatlab = "chameleon.security.policy.hostmatlab";
    public static final String PROP_SecurityPolicy = "chameleon.security.policy";
    public static final String PROP_SecurityAllowable = "chameleon.security.allowable";
    public static final String PROP_StartRefresh = "chameleon.start.refresh";
    public static final String PROP_StartJvmoptions = "chameleon.start.jvmoptions";

    public static final String DEFAULT_PropertyFilename = null;
    public static final String DEFAULT_RefreshJarfileBaseurl = null;
    public static final String DEFAULT_RefreshJarfileFilename = "/jicos-runtime.jar";
    public static final String DEFAULT_RefreshDirname = "/tmp";
    //    public static final String DEFAULT_SecurityPolicyHsp = null;
    //    public static final String DEFAULT_SecurityPolicyTaskserver = null;
    //    public static final String DEFAULT_SecurityPolicyTaskservermatlab = null;
    //    public static final String DEFAULT_SecurityPolicyHost = null;
    //    public static final String DEFAULT_SecurityPolicyHostmatlab = null;
    public static final String DEFAULT_SecurityPolicy = null;
    public static final String DEFAULT_SecurityAllowable = "all";
    public static final String DEFAULT_StartRefresh = String.valueOf( true );
    public static final String DEFAULT_StartJvmoptions = null;

    public static final String[][] knownServices =
        { // label name
                    { CMD_Hsp, "edu.ucsb.cs.jicos.services.Hsp" },
                    { CMD_TaskServer, "edu.ucsb.cs.jicos.services.TaskServer" },
                    { CMD_TaskServerMatlab,
                            "edu.ucsb.cs.jicos.services.external.services.matlab.TaskServerMatlab" },
                    { CMD_Host, "edu.ucsb.cs.jicos.services.Host" },
                    { CMD_HostMatlab,
                            "edu.ucsb.cs.jicos.services.external.services.matlab.HostMatlab" },
                    { CMD_Console, "edu.ucsb.cs.jicos.admin.ConsoleImpl" },

        };

    //
    // -- Remote Methods -----------------------------------------------------

    /**
     * Start a service, possibly after refreshing the Jicos jar file.
     * 
     * @param refresh
     *            Refresh the Jar?
     * @param command
     *            The command to be invoked.
     * @param signature
     *            The signature of the command.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws RemoteException
     */
    public boolean startService( boolean refresh, String command,
            byte[] signature ) throws RemoteException;

    /**
     * Start a service&#x2e;&nbsp;&nbsp;The jarfile refresh will, or will not
     * (see DEFAULT_REFRESH, above) be performed.
     * 
     * @param command
     *            The command to be invoked.
     * @param signature
     *            The signature of the command.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws RemoteException
     */
    public boolean startService( String command, byte[] signature )
            throws RemoteException;
}