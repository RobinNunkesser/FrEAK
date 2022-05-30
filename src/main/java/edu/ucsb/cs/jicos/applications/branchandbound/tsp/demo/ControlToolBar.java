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
 * ControlToolBar.java
 *
 * @version 1
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.demo;

// Java core packages
import java.awt.event.*;

// Java extension packages
import javax.swing.*;


final class ControlToolBar extends JToolBar 
{
    Model model;
    
    JButton visualize = new JButton("View");
    JButton backButton; 
    JButton forwardButton;
    //JButton reset = new JButton("Reset");
    
    public ControlToolBar( Model model ) 
    {
        super( "View Tool Bar" );
        this.model = model;
        
        visualize.addActionListener(
            new ActionListener() 
            {            
                public void actionPerformed( ActionEvent event )
                {
                    ControlToolBar.this.model.extend();
                    ControlToolBar.this.model.view();        
                }
            }
        );  
        add( visualize );
        
        backButton = new JButton
        ( 
            new ImageIcon( getClass().getResource( "images/back.gif" ) )
        );
        backButton.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed( ActionEvent event )
                {
                    ControlToolBar.this.model.goBack();
                }
            }
        );
        add( backButton );
        
        forwardButton = new JButton
        ( 
            new ImageIcon( getClass().getResource( "images/forward.gif" ) )
        );
        forwardButton.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed( ActionEvent event )
                {
                    ControlToolBar.this.model.goForward();
                }
            }
        );
        add( forwardButton );
        
        /*
        reset.addActionListener(
            new ActionListener() 
            {            
                public void actionPerformed( ActionEvent event )
                {
                    ControlToolBar.this.model.extend();
                    ControlToolBar.this.model.reset();        
                }
            }
        );                        
        add( reset );
         */
    }
}
