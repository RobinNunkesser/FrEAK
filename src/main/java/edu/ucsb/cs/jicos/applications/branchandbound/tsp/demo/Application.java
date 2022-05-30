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
 * Application.java - A Branch and Bound TSP demonstration
 *
 * @version 1
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.demo;

// Java core packages
import java.awt.*;
import java.awt.event.*;

// Java extension packages
import javax.swing.*;


final class Application extends JFrame 
{
    private Model model;     
    private Controller controller;
    //private ControlToolBar controlToolBar;
    private View view;
    
    private JMenuBar menuBar = new JMenuBar();
    
    // Zoom Menu & Actions
    private JMenu zoomMenu = new JMenu( "Zoom" );
    private Action zoomAction;
    private Action unzoomAction;
    private Action moveAction;
    
    // Resolution menu & Actions    
    private JMenu resolutionMenu = new JMenu( "Resolution" );
    private Action res5Action;
    private Action res6Action;
    private Action res7Action;
    private Action res8Action;
    private Action res9Action;
    
    // Color bits menu & Actions    
    private JMenu colorBitsMenu = new JMenu( "Color bits" );
    private Action bits1Action;
    private Action bits2Action;
    private Action bits3Action;
    private Action bits4Action;
    
    // Janet menu & Actions 
    private JMenu janetMenu = new JMenu( "Janet" );   

    private JPanel viewPanel = new JPanel( new BorderLayout() );
        
    Application( int nodes ) 
    {
        super( "JICOS: Branch And Bound: TSP Demonstration" ); 
        
        model = new Model( nodes );
        controller = new Controller( model );
        //controlToolBar = new ControlToolBar( model );
        view = new View( model, controller );
        
        //menuBar.add( colorBitsMenu );
        //menuBar.add( resolutionMenu );
        //menuBar.add( zoomMenu );        
        menuBar.add( janetMenu );
        setJMenuBar( menuBar );
        
        /*
        // construct colorBits Actions
        initBits1();
        initBits2();
        initBits3();
        initBits4();
        
        // construct resolution Actions
        initRes5();
        initRes6();
        initRes7();
        initRes8();
        initRes9();
        
        // construct zoom Actions
        initZoom();
        initUnzoom();
        initMove();
         */
        
        viewPanel.add( new JScrollPane( view ), BorderLayout.CENTER );    
        
        Container contentPane = getContentPane();
        //contentPane.add( controlToolBar, BorderLayout.NORTH );
        contentPane.add( viewPanel, BorderLayout.CENTER );
        contentPane.add( controller, BorderLayout.SOUTH );
    }
    
    private void initBits1()
    {
        bits1Action = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setColorBits( 1 );
            }
        };
        bits1Action.putValue( Action.NAME, "3" );
        /*
        bits1Action.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        bits1Action.putValue( Action.SHORT_DESCRIPTION, "3-bit color: iteration limit: 8" );
        bits1Action.putValue( Action.MNEMONIC_KEY, new Integer( '3' ) );   
        colorBitsMenu.add( bits1Action );
    }
    
    private void initBits2()
    {
        bits2Action = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setColorBits( 2 );
            }
        };
        bits2Action.putValue( Action.NAME, "6" );
        /*
        bits2Action.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        bits2Action.putValue( Action.SHORT_DESCRIPTION, "6-bit color: iteration limit: 64" );
        bits2Action.putValue( Action.MNEMONIC_KEY, new Integer( '6' ) );   
        colorBitsMenu.add( bits2Action );
    }
    
        
    private void initMove()
    {
        moveAction = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setOperation( Model.MOVE );
            }
        };
        moveAction.putValue( Action.NAME, "Move" );
        /*
        moveAction.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        moveAction.putValue( Action.SHORT_DESCRIPTION, "Move" );
        moveAction.putValue( Action.MNEMONIC_KEY, new Integer( 'M' ) );   
        zoomMenu.add( moveAction );
    }
    
    private void initRes5()
    {
        res5Action = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setResolution( 5 );
            }
        };
        res5Action.putValue( Action.NAME, "5" );
        /*
        res5Action.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        res5Action.putValue( Action.SHORT_DESCRIPTION, "32 X 32 Image" );
        res5Action.putValue( Action.MNEMONIC_KEY, new Integer( '5' ) );   
        resolutionMenu.add( res5Action );
    }
    
    private void initRes6()
    {
        res6Action = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setResolution( 6 );
            }
        };
        res6Action.putValue( Action.NAME, "6" );
        /*
        res6Action.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        res6Action.putValue( Action.SHORT_DESCRIPTION, "64 X 64 Image" );
        res6Action.putValue( Action.MNEMONIC_KEY, new Integer( '6' ) );   
        resolutionMenu.add( res6Action );
    }
    
    private void initUnzoom()
    {
        unzoomAction = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setOperation( Model.UNZOOM );
            }
        };
        unzoomAction.putValue( Action.NAME, "Unzoom" );
        /*
        unzoomAction.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        unzoomAction.putValue( Action.SHORT_DESCRIPTION, "Zoom out" );
        unzoomAction.putValue( Action.MNEMONIC_KEY, new Integer( 'U' ) );   
        zoomMenu.add( unzoomAction );
    }
    
    private void initZoom()
    {
        zoomAction = new AbstractAction()
        {
            public void actionPerformed( ActionEvent event )
            {
                model.setOperation( Model.ZOOM );
            }
        };
        zoomAction.putValue( Action.NAME, "Zoom" );
        /*
        zoomAction.putValue
        ( 
            Action.SMALL_ICON, new ImageIcon( getClass().getResource
            ( "images/zoom.gif")
        );
         */
        zoomAction.putValue( Action.SHORT_DESCRIPTION, "Zoom in" );
        zoomAction.putValue( Action.MNEMONIC_KEY, new Integer( 'Z' ) );   
        zoomMenu.add( zoomAction );
    }

    /**
    * @param args the command line arguments
    */
    public static void main( String[] args )
   {
       int nodes = Integer.parseInt( args[0] );
       
       Application application = new Application( nodes );
       application.setDefaultCloseOperation( EXIT_ON_CLOSE );
       application.pack();
       application.setVisible( true );
   }
}
