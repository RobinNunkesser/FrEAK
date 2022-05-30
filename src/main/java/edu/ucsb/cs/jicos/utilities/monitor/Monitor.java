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
 *  Startup and monitor Jicos (uses Swing).
 *
 * @author  Andy Pippin
 */

package edu.ucsb.cs.jicos.utilities.monitor;

import  java.awt.*;
import  java.awt.event.*;
import  java.io.*;
import  java.util.Enumeration;
import  java.util.Properties;
import  javax.swing.*;
import  javax.swing.tree.*;

import edu.ucsb.cs.jicos.services.Hsp;


public class Monitor extends JFrame implements ActionListener {
    //~~Constants~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private static String versionControl = "$Id: Monitor.java,v 1.3 2004/06/20 03:47:46 pippin Exp $";

    private static final int defaultWidth = 1000;

    private static final int defaultHeight = 800;

    // Action commands.
    //
    private static final String ACTION_Quit = "Quit";

    private static final String ACTION_Start = "Start";

    private static final String ACTION_StartHsp = "StartHsp";

    private static final String ACTION_StartTS = "StartTS";

    private static final String ACTION_StartHost = "StartHost";

    private static final String ACTION_Stop = "Stop";

    private static final String ACTION_StopHsp = "StopHsp";

    private static final String ACTION_StopTS = "StopTS";

    private static final String ACTION_StopHost = "StopHost";

    private static final String ACTION_HelpContents = "HelpContents";

    private static final String ACTION_HelpAbout = "HelpAbout";

    private static final int DEFAULT_HSP = 1;

    private static final int DEFAULT_TS = 1;

    private static final int DEFAULT_Hosts = 1;

    public static final int DEFAULT_LogPort = 17893;

    public static final String CRLF = System.getProperty("line.separator");

    //~~Variables~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Static holder for the command line arguments. */
    private String[] cmdLine;

    /** Static holder for the frame. */
    private  Monitor monitor = null;

    /** Information scroll pane and text area. */
    private JScrollPane scrollPane;

    private JTextArea info;

    /** Information tree. */
    private JTree tree;

    /** HSP process. */
    private Process hspProcess;

    //~~Constructors~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    //----------------------------------------------------------------------
    /**
     * Default, no argument constructor.
     */
    public Monitor() {
        Initialize();
        CreateGUI();
        new LoggingThread(this, null);
    }

    //----------------------------------------------------------------------
    /**
     * Constructor with command line arguments.
     * 
     * @param cmdLine Command line arguments.
     */
    public Monitor(String[] cmdLine) {
        Initialize();
        setCommandLine(cmdLine);

        CreateGUI();
        new LoggingThread(this, null);
    }

    //----------------------------------------------------------------------
    /**
     * Initialize the instance.
     */
    private void Initialize() {
        this.cmdLine = null;
        this.monitor = null;
        this.scrollPane = null;
        this.info = null;
        this.tree = null;
        this.hspProcess = null;

        return;
    }

    //~~Methods~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    //---------------------------------------------------------------------
    /**
     * Create the graphical user interface.
     */
    private void CreateGUI() {
        this.setJMenuBar(CreateMenubar());

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(Box.createRigidArea(new Dimension(defaultWidth, 0)),
                BorderLayout.SOUTH);
        contentPane.add(Box.createRigidArea(new Dimension(0, defaultHeight)),
                BorderLayout.EAST);
        //
        JPanel blankPanel = new JPanel(new BorderLayout());
        blankPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.add(blankPanel, BorderLayout.CENTER);

        // Create the tree panel.
        //
        JPanel treePanel = new JPanel(new BorderLayout());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Jicos");
        this.tree = new JTree(root);
        treePanel.add(new JScrollPane(this.tree), BorderLayout.CENTER);

        // Create the information panel.
        //
        JPanel infoPanel = new JPanel(new BorderLayout());
        this.info = new JTextArea();
        this.info.setEditable(false);
        this.info.setBackground(Color.lightGray);
        this.scrollPane = new JScrollPane(this.info);
        infoPanel.add(this.scrollPane, BorderLayout.CENTER);

        // Create the split pane.
        //
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treePanel, infoPanel);
        blankPanel.add(splitPane, BorderLayout.CENTER);

        // Sizes
        //
        int panelWidth = /* splitPane.getSize().width */defaultWidth - 10
                - splitPane.getInsets().right - splitPane.getInsets().left
                - splitPane.getDividerSize();
        Dimension minimumSize = new Dimension((panelWidth * 2) / 6,
                defaultHeight);
        Dimension maximumSize = new Dimension((panelWidth * 4) / 6,
                defaultHeight);
        //
        treePanel.setMinimumSize(minimumSize);
        treePanel.setMaximumSize(maximumSize);
        treePanel.setPreferredSize(new Dimension((panelWidth * 1) / 8,
                defaultHeight));

        infoPanel.setMinimumSize(minimumSize);
        infoPanel.setMaximumSize(maximumSize);
        infoPanel.setPreferredSize(new Dimension((panelWidth * 7) / 8,
                defaultHeight));

        // Button panel.
        //
        JPanel innerButtonPanel = new JPanel(new FlowLayout());
        innerButtonPanel
                .setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        innerButtonPanel.add(Box.createHorizontalGlue());
        //
        JButton exitButton = new JButton("Quit");
        exitButton.setActionCommand(ACTION_Quit);
        exitButton.addActionListener(this);
        innerButtonPanel.add(exitButton);
        //
        JPanel outerButtonPanel = new JPanel(new BorderLayout());
        outerButtonPanel.add(innerButtonPanel, BorderLayout.EAST);
        blankPanel.add(outerButtonPanel, BorderLayout.SOUTH);

        // Position on screen.
        //
        this.pack();
        JRootPane rootPane = this.getRootPane();
        Rectangle rootSize = rootPane.getGraphicsConfiguration().getBounds();
        Dimension thisSize = rootPane.getSize();

        double width = (double) (rootSize.width - thisSize.width);
        double height = (double) (rootSize.height - thisSize.height);

        this.setLocation(((int) (width * 0.5)), ((int) (height * 0.5)));

        return;
    }

    //----------------------------------------------------------------------
    /**
     * Create the graphical user interface.
     */
    private JMenuBar CreateMenubar() {
        JMenu menu, menu2;
        JMenuItem item, item2;

        // Create the container.
        //
        JMenuBar menuBar = new JMenuBar();

        // File menu.
        //
        menu = new JMenu("File");
        menuBar.add(menu);

        menu2 = new JMenu("Start");
        menu.add(menu2);

        item = new JMenuItem("Start...");
        item.setActionCommand(ACTION_Start);
        item.addActionListener(this);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_MASK));
        menu2.add(item);

        menu2.add(new JSeparator());

        item = new JMenuItem("HSP");
        item.setActionCommand(ACTION_StartHsp);
        item.addActionListener(this);
        menu2.add(item);

        item = new JMenuItem("TS");
        item.setActionCommand(ACTION_StartTS);
        item.addActionListener(this);
        menu2.add(item);

        item = new JMenuItem("Host");
        item.setActionCommand(ACTION_StartHost);
        item.addActionListener(this);
        menu2.add(item);

        menu2 = new JMenu("Stop");
        menu.add(menu2);

        item = new JMenuItem("Stop...");
        item.setActionCommand(ACTION_Stop);
        item.addActionListener(this);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        menu2.add(item);

        menu2.add(new JSeparator());

        item = new JMenuItem("HSP");
        item.setActionCommand(ACTION_StopHsp);
        item.addActionListener(this);
        menu2.add(item);

        item = new JMenuItem("TS");
        item.setActionCommand(ACTION_StopHsp);
        item.addActionListener(this);
        menu2.add(item);

        item = new JMenuItem("Host");
        item.setActionCommand(ACTION_StopHsp);
        item.addActionListener(this);
        menu2.add(item);

        menu.add(new JSeparator());

        item = new JMenuItem("Quit");
        item.setActionCommand(ACTION_Quit);
        item.addActionListener(this);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                InputEvent.CTRL_MASK));
        menu.add(item);

        // Help menu.
        //
        menu = new JMenu("Help");
        menuBar.add(menu);

        item = new JMenuItem("Contents");
        item.setActionCommand(ACTION_HelpContents);
        item.addActionListener(this);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menu.add(item);

        item = new JMenuItem("About...");
        item.setActionCommand(ACTION_HelpAbout);
        item.addActionListener(this);
        menu.add(item);

        return (menuBar);
    }

    //----------------------------------------------------------------------
    /**
     * Add text to the info panel.
     * 
     * @param text Text to add.
     */
    public void addInfo(String text) {
        if ((null != this.info) && (null != text)) {
            this.info.append(text);

            // Reset scrollbar
            //
            if (this.info.getHeight() > this.scrollPane.getHeight()) {
                JScrollBar scrollBar = this.scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            }
        }
    }

    //~~ Event handlers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //----------------------------------------------------------------------
    /**
     * Handle named actions (JButtons, etc.)
     * 
     * @param actionEvent The event that is being handled.
     */
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();

        if (ACTION_Quit.equals(actionCommand)) {
            StopProgram();
        } else if (ACTION_Start.equals(actionCommand)) {
            startJicos(null, null);
        } else if (ACTION_StartHsp.equals(actionCommand)) {
        } else if (ACTION_StartTS.equals(actionCommand)) {
        } else if (ACTION_StartHost.equals(actionCommand)) {
        } else if (ACTION_Stop.equals(actionCommand)) {
            stopJicos();
        } else if (ACTION_StopHsp.equals(actionCommand)) {
        } else if (ACTION_StopTS.equals(actionCommand)) {
        } else if (ACTION_StopHost.equals(actionCommand)) {
        } else if (ACTION_HelpContents.equals(actionCommand)) {
        } else if (ACTION_HelpAbout.equals(actionCommand)) {
        }
    }

    //----------------------------------------------------------------------
    /**
     * Do any initialization necessary, and start the program.
     */
    private void StartProgram() {
        String[] properties = { "jicos", };

        java.net.URI veryTopDir = null;
        java.net.URL getURL = null;
        Class monitorClass = Monitor.class;

        if (null != (getURL = monitorClass.getResource("."))) {
            String thisClass = "/monitor/"
                    + monitorClass.getName().replace('.', '/') + ".class";
            String stringVeryTopDir = getURL.toString().replaceFirst(thisClass,
                    "");

            try {
                veryTopDir = new java.net.URI(stringVeryTopDir);
            } catch (java.net.URISyntaxException uriSyntaxException) {
            }
        }

        if (null != (this.monitor = new Monitor())) {
            LoadProperties(properties);
            this.monitor.setVisible( true );
        } else {
            System.out.flush();
            System.err.println("Couldn't start monitor.");
            System.err.flush();
            System.exit(0);
        }
    }

    //----------------------------------------------------------------------
    /**
     * Do any shutdown necessary, and stop the program.
     */
    private void StopProgram() {
        if (null != this.monitor) {
            this.monitor.setVisible(false);
        }

        System.exit(0);
    }

    //----------------------------------------------------------------------
    /**
     * Import properties.
     * 
     * @param propFileList List of property files to import.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     */
    private static void LoadProperties(String[] propFileList) {
        // Find the appropriate directory.
        //
        String baseName = System.getProperty("user.home");
        String osName = System.getProperty("os.name");
        FileInputStream inputStream = null;

        if (osName.toLowerCase().startsWith("windows")) {
            baseName += File.separator + "Application Data" + File.separator
                    + "Jicos" + File.separator + "jicos";
        } else {
            baseName += File.separator + ".jicos" + File.separator;
        }

        // If the properties file exists, import the properties.
        //
        if (null == propFileList)
            return;

        for (int pFile = 0; pFile < propFileList.length; ++pFile) {
            inputStream = null;
            File propFile = null;
            // String propFilename = baseName + propFileList[ pFile ];

            try {
                Properties newProperties = null;
                String key = null;
                String value = null;

                propFile = new File(baseName + propFileList[pFile]
                        + ".properties");

                if ((propFile.exists())
                        && (null != (inputStream = new FileInputStream(propFile)))
                        && (null != (newProperties = new Properties()))) {
                    newProperties.load(inputStream);
                    Enumeration enumeration = newProperties.propertyNames();

                    while (enumeration.hasMoreElements()) {
                        if (null != (key = (String) enumeration.nextElement())) {
                            value = (String) newProperties.getProperty(key);
                            System.setProperty(key, value);
                        }
                    }
                    dprint(false, "DEBUG Monitor.LoadProperties()'Loaded \""
                            + propFile.getAbsolutePath() + "\"");
                }
            }

            // Oops!
            //
            catch (Exception exception) {
                System.err.println("Couldn't load properties file \""
                        + propFile.getAbsolutePath() + "\": "
                        + exception.getMessage());

            }

            // Close the file, if it is open.
            //
            finally {
                if (null != inputStream)
                    try {
                        inputStream.close();
                    } catch (IOException ignore) {
                    }
            }

        } // End for.

        return;
    }

    //----------------------------------------------------------------------
    /**
     * Copy the command line arguments.
     * 
     * @param cmdLine Command line arguments.
     */
    public void setCommandLine(String[] cmdLine) {
        if (null == cmdLine) {
            for (int argc = 0; argc < cmdLine.length; ++argc) {
                this.cmdLine[argc] = new String(cmdLine[argc]);
            }
        }
    }

    //----------------------------------------------------------------------
    /**
     * Start up Jicos.
     * 
     * @param ts Number of Task Servers.
     * @param host Number of Hosts.
     */
    private void startJicos(Integer ts, Integer host) {
	    HspThread hspThread = new HspThread( "localhost", ts, host );
		new Thread( hspThread ).start();
    }
			
/*    	
        if (null == ts)
            ts = new Integer(DEFAULT_TS);
        if (null == host)
            host = new Integer(DEFAULT_Hosts);

        // Both get and set the properties.
        //
        //edu.ucsb.cs.jicos.utilities.Properties.load( "monitor" );
        String JicosHome = System.getProperty("jicos.home");
        String ClassPath = System.getProperty("java.class.path");

        String[] cmdArray = new String[] {
                "/usr/bin/java",
                "-classpath",
                "/Users/pippin/Classes/596-Jicos/framework/build/classes",
                "-Djava.security.policy=/Users/pippin/Classes/596-Jicos/framework/policy/policy",
                "edu.ucsb.cs.jicos.services.Hsp", ts.toString(),
                host.toString(), };

        //***********************************************************************
        // for( int c=0; c < cmdArray.length; ++c ) System.out.print(
        // cmdArray[c] + " \n" ); System.out.println(); //
        // edu.ucsb.cs.jicos.utilities.Properties.list(); if( true ) return; /
        //***********************************************************************

        Runtime runtime = Runtime.getRuntime();

        try {
            this.hspProcess = runtime.exec(cmdArray, null);
            // Start monitoring.
            //
            ProcessListener processListenerObject = new ProcessListener(
                    this.monitor, this.hspProcess);
            Thread processListener = processListenerObject.createThread();
            processListener.start();

            // Try and sleep for 0.5 seconds, and then check the status of
            // the process.
            try {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
            }
            int exitValue = -1;

            try {
                exitValue = this.hspProcess.exitValue();

                // This is actually a bad thing, since this can only happen
                // when the process died.
                //
                JOptionPane.showMessageDialog(this.monitor,
                        "Couldn't start Jicos: exit value = "
                                + String.valueOf(exitValue), "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalThreadStateException illegalThreadStateException) {
                // This is actually a good thing, since this can only happen
                // when the process is still running.
                //
                JOptionPane.showMessageDialog(this.monitor,
                        "Started Jicos with " + ts + " TS, and " + host
                                + " Host.", "Started",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        catch (IOException ioException) {
            JOptionPane.showMessageDialog(this.monitor,
                    "Couldn't start Jicos: " + ioException.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return;
    }
*/
		
		
    //----------------------------------------------------------------------
    /**
     *  Start up Jicos.
     *
     * @param   ts    Number of Task Servers.
     * @param   host  Number of Hosts.
     */
    private void stopJicos() {
        if (null == this.hspProcess)
            this.hspProcess.destroy();
    }

    //----------------------------------------------------------------------
    /**
     *  Entry point into this class.
     *
     * @param  cmdLine  Command line arguments.
     */
    public static void main(String[] cmdLine) {
        final Monitor monitor = new Monitor(cmdLine);

        // Schedule a job for the event dispatching thread.
        //
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public final void run() {
                monitor.StartProgram();
            }
        });
    }
    
    
    private class  HspThread  implements  Runnable
	{
		public final String  DEFAULT_hspHost = "localhost";
		public final int     DEFAULT_numTaskServers = 1;
		public final int     DEFAULT_numHosts = 1;
    	    
    	    private  String   hspHost;
    	    private  Integer  numTaskServers;
    	    private  Integer  numHosts;
    	    
	    public HspThread( String hspHost, Integer numTaskServers, Integer numHosts )
	    {
	        this.hspHost = hspHost;
	        this.numTaskServers = numTaskServers;
	        this.numHosts = numHosts;
	    }
	    
	    public void run() {
			String hspHost = (null != this.hspHost) ? this.hspHost
					: DEFAULT_hspHost;
			
			int numTaskServers = DEFAULT_numTaskServers;
			if( null != this.numTaskServers)
				numTaskServers = this.numTaskServers.intValue();
				
			int numHosts = DEFAULT_numHosts;
			if( null != this.numHosts)
				numHosts = this.numHosts.intValue();
			
			// The values necessary to start a HSP are not visible to anyone
			// other than the HSP.  So, the only interface to it is Hsp.main().
			//
			// Yes, I could change that but, no, I'm not going to.
			//
			String[]  cmdLine = new String[ 3 ];
			
			cmdLine[ 0 ] = hspHost;
			cmdLine[ 1 ] = String.valueOf( numTaskServers );
			cmdLine[ 2 ] = String.valueOf( numHosts );

System.out.println( "Hostname: " + cmdLine[0] );			
System.out.println( "      TS: " + cmdLine[1] );			
System.out.println( "   Hosts: " + cmdLine[2] );

			try
			{
				Hsp.main( cmdLine );
			}
			catch( Exception exception )
			{
				JLabel errorMsg =
					new JLabel( "<HTML><CENTER>Couldn't start HSP.  Error was:<BR>"
							+ exception.getMessage() + "</CENTER></HTML>" );
				
				JOptionPane.showMessageDialog( monitor, errorMsg, "Error",
						JOptionPane.ERROR_MESSAGE );
			}
		}
	}

    public static void dprint( Object object )
        { System.out.println( "DEBUG  " + ((null==object)?"null":object.toString()) ); }
    public static void dprint( boolean print, Object object )
        { if(print) System.out.println( "DEBUG  " + ((null==object)?"null":object.toString()) ); }
    
}
