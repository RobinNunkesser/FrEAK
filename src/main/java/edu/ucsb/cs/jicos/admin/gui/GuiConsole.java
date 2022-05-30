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

package edu.ucsb.cs.jicos.admin.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;

import edu.ucsb.cs.jicos.services.Property;

public class GuiConsole extends JFrame implements ActionListener {

    //
    //-- Constants -----------------------------------------------------------

    private static final String ACTION_Exit = "ACTION_Exit";

    //
    //-- Variables -----------------------------------------------------------

    JPanel statusPanel;   // Used for non-textual status.
    JTextField statusText;
    
    //
    //-- Constructors --------------------------------------------------------

    /**
     * Default JFrame constructor.
     * 
     * @throws java.awt.HeadlessException
     */
    public GuiConsole() throws HeadlessException
    {
        super();
        this.initVariables();
        this.createGui();
    }

    /**
     * Extends JFrame constructor.
     * 
     * @param graphicsConfiguration the <CODE>GraphicsConfiguration</CODE>
     *		this is used to construct the new <CODE>GuiConsole</CODE>; if
     *       <CODE>gc</CODE> is <CODE>null</CODE>, the system default
     * 		<CODE>GraphicsConfiguration</CODE> is assumed.
     */
    public GuiConsole(GraphicsConfiguration graphicsConfiguration)
    {
        super( graphicsConfiguration );
        this.initVariables();
        this.createGui();
    }

    /**
     * Extends JFrame constructor.
     * 
     * @param title the title for the frame.
     * @throws java.awt.HeadlessException
     */
    public GuiConsole(String title) throws HeadlessException
    {
        super( title );
        this.initVariables();
        this.createGui();
    }

    /**
     * Extends JFrame constructor.
     * 
     * @param title the title for the frame.
     * @param graphicsConfiguration the <CODE>GraphicsConfiguration</CODE>
     *		this is used to construct the new <CODE>GuiConsole</CODE>; if
     *       <CODE>gc</CODE> is <CODE>null</CODE>, the system default
     * 		<CODE>GraphicsConfiguration</CODE> is assumed.
     */
    public GuiConsole(String title, GraphicsConfiguration graphicsConfiguration)
    {
        super( title, graphicsConfiguration );
        this.initVariables();
        this.createGui();
    }

    //
    //-- Initialization ------------------------------------------------------

    /**
     * Initialize any member variables.
     */
    private void initVariables()
    {
        return;
    }

    /**
     * Create the Graphical User Interface.
     */
    private void createGui()
    {
        // Menu bar.
        setJMenuBar( this.createMenuBar() );

        // Create the panel.
        JPanel topPanel = new JPanel( new BorderLayout() );
        setContentPane( topPanel );
        //
        topPanel.add( Box.createRigidArea( new Dimension( 200, 1 ) ), BorderLayout.EAST );
        topPanel.add( Box.createRigidArea( new Dimension( 1, 300 ) ), BorderLayout.NORTH );
        topPanel.setBorder( BorderFactory.createEmptyBorder( 9, 10, 10, 9 ) ); // t l b r

        // Create the split pane.
        JPanel treePanel = new JPanel( new BorderLayout() );
        JTree nodeTree = createNodeTree();
        treePanel.add( nodeTree, BorderLayout.CENTER );
        //
        JPanel infoPanel = createInfoPanel();
        //
        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                treePanel, infoPanel );
        topPanel.add( splitPane, BorderLayout.CENTER );

        // Create the bottom panel for buttons and status.
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 5 ) );
        JButton quitButton = new JButton( "Exit" );
        quitButton.setActionCommand( ACTION_Exit );
        quitButton.addActionListener( this );
        buttonPanel.add( quitButton );
        //
        this.statusPanel = new JPanel( new BorderLayout() );
        this.statusText = new JTextField();
        statusPanel.add( this.statusText, BorderLayout.CENTER );
        //
        JPanel bottomPanel = new JPanel( new BorderLayout() );
        bottomPanel.add( statusPanel, BorderLayout.CENTER );
        bottomPanel.add( buttonPanel, BorderLayout.EAST );
        //
        topPanel.add( bottomPanel, BorderLayout.SOUTH );

        // Don't make it visible yet.
        setVisible( false );

        return;
    }

    /**
     * Start up the program.
     * 
     * @param cmdLine  The command line arguments.
     */
    private void startProgram( String[] cmdLine )
    {

        JFrame.setDefaultLookAndFeelDecorated( true );
        addWindowListener( new WindowAdapter()
        {
            // Redirect the window close event.
            //
            public void windowClosing( WindowEvent windowEvent )
            {
                stopProgram();
            }
        } );

        // Position the window.
        //
        pack();
        JRootPane rootPane = getRootPane();
        Rectangle windowSize = rootPane.getGraphicsConfiguration().getBounds();
        Dimension currSize = rootPane.getSize();
        //
        // In the middle of the screen.
        int xLocation = (windowSize.width - currSize.width) / 2;
        int yLocation = (windowSize.height - currSize.height) / 2;
        setLocation( xLocation, yLocation );

        // Show it.
        setVisible( true );
    }

    /**
     * Shutdown the GUI console.
     */
    private void stopProgram()
    {
        setVisible( false );
        System.exit( 0 );
    }

    /**
     * Create the menu bar.
     */
    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        return (menuBar);
    }

    /**
     *  Create the JTree of nodes in the system.
     * 
     * @return  The nodes in the system.
     */
    private JTree createNodeTree()
    {
        JTree nodeTree = new JTree();
        return (nodeTree);
    }

    /**
     * Create the panel for information.
     * 
     * @return  A JPanel with information.
     */
    private JPanel createInfoPanel()
    {
        JPanel infoPanel = new JPanel( new BorderLayout() );
        return (infoPanel);
    }

    //
    //-- Functions -----------------------------------------------------------
    
    public void clearStatus() {
        this.statusText.setText( "" );
    }
    
    public void setStatus( String text ) {
        this.statusText.setText( (null == text) ? "" : text );
    }

    //
    //-- Event Handlers ------------------------------------------------------

    //
    //-- Entry Point ---------------------------------------------------------

    public static void main( String[] args )
    {
        // Get a final reference to this.
        final String[] cmdLine = args;

        // Load the hsp properties.
        Property.loadProperties( args );
        Property.load( GuiConsole.class );

        // Schedule a job for the event-dispatching thread: creating and
        //   showing this application's GUI.
        //
        javax.swing.SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                GuiConsole guiConsole = new GuiConsole( "Jicos Console" );
                guiConsole.startProgram( cmdLine );
            }
        } );
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent actionEvent )
    {
        String actionCommand = actionEvent.getActionCommand();

        if (ACTION_Exit.equals( actionCommand ))
        {
            stopProgram();
        }
    }
}