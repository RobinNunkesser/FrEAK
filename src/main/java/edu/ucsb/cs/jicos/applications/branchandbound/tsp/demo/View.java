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
 * View.java
 *
 * @version 1
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.demo;

import edu.ucsb.cs.jicos.applications.utilities.graph.*;

import java.awt.*;
import java.util.*;

// Java extension packages
import javax.swing.*;


final class View extends JPanel implements Observer 
{
    private final static int OFFSET = 10;
    private final static int SIZE = 670;
    
    final static int MAXSIZE = 700;
    final static int WORK = 25000;
    
    private Model model;
    
    public View( Model model, Controller controller ) 
    {
        this.model = model;
        setSize( MAXSIZE, MAXSIZE );
        
        // register as an Observer to receive Model updates
        model.addObserver( this ); 
                       
        addMouseListener( controller ); // register controller as MouseListener
    }
    
    public Dimension getPreferredSize() 
    {  
        return new Dimension( MAXSIZE, MAXSIZE );
    }
    
    public Dimension getMinimumSize() { return getPreferredSize(); }
    
    public Dimension getMaximumSize() { return getPreferredSize(); }
    
    // to be added to ViewPane
    public void paintComponent( Graphics g )
    {
        // ensure proper painting sequence
        super.paintComponent( g );
      
        // draw Matching
        //drawMatching( g );
        //drawTour( g );
        drawCappello2Approximation( g );
        drawVertices( g );
        g.setColor( Color.black );
        g.drawRect( OFFSET, OFFSET,OFFSET + SIZE , OFFSET + SIZE );
        
    }
    
    void drawCappello2Approximation( Graphics g )
    {
        GraphEuclidean graph = model.getGraph();
        graph.drawCappello2Approximation( g , OFFSET, OFFSET, SIZE );
    }
    
    void drawMatching( Graphics g )
    {      
       GraphEuclidean graph = model.getGraph();
       graph.drawMatching( g , OFFSET, OFFSET, SIZE );
    }
    
    void drawTour( Graphics g )
    {
        GraphEuclidean graph = model.getGraph();
        graph.drawTour( g , OFFSET, OFFSET, SIZE );
    }
    
   void drawVertices( Graphics g )
   {      
       GraphEuclidean graph = model.getGraph();
       g.drawRect( OFFSET, OFFSET,OFFSET + SIZE , OFFSET + SIZE );
       graph.drawVertices( g , OFFSET, OFFSET, SIZE );
   }
   
   public void update( Observable observable, Object obj) { repaint(); }
}
