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
 * Model.java
 *
 * @version 1
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.demo;

// !! Move as application develops
import edu.ucsb.cs.jicos.applications.utilities.graph.*;

// Java core packages
import java.util.Observable;

// Jicos package
import edu.ucsb.cs.jicos.services.*;


final class Model extends Observable 
{    
    GraphEuclidean graph;
    
    /////// old stuff
    final static int MOVE = 0;
    final static int ZOOM = 1;
    final static int UNZOOM = 2;    
    int operation = MOVE;
    
    boolean sequential = true;
    
    MandelbrotSet mandelbrotSet;
    
    // Jicos Hosting Service Provider
    Client2Hsp hsp;
    
    Model( int nodes ) { graph = new GraphEuclidean( nodes, nodes, 1000 ); }
    
    GraphEuclidean getGraph() { return graph; }
    
    void extend() {}
    
    MandelbrotSet getMandelbrotSet() { return mandelbrotSet; }
    
    void goBack()
    {
        /*
        if ( mandelbrotSet.previous != null )
        {
            mandelbrotSet = mandelbrotSet.previous;
        }
         */
        // indicate model has changed
        setChanged();
      
        // notify Observers that model has changed
        notifyObservers();
        clearChanged();
    }
    
    void goForward()
    {
        /*
        if ( mandelbrotSet.next != null )
        {
            mandelbrotSet = mandelbrotSet.next;
        }
         */
            view();
    }
    
    // Janet utility
    void janetLogin( String hspDomainName )
    {
        // set reference to a Hosting Service Provider
        HspAgent agent = new HspAgent( hspDomainName );
        hsp = agent.getClient2Hsp();
            
        // login with HSP
        Environment environment = new Environment( null, null );
        try
        {
            hsp.login( environment );
        }
        catch ( Exception e ) { System.exit(1); }
    }
    
    void janetLogout()
    {
        // logout from HSP
        try
        {
            hsp.logout();
        }
        catch ( Exception e ) { System.exit(1); }
    }
    
    void notifyView()
    {
        // indicate model has changed
        setChanged();
      
        // notify Observers that model has changed
        notifyObservers();
        clearChanged();
    }
    
    void recenter( int x, int y )
    {        
        mandelbrotSet.recenter( x, y );
     
        view();
    }
    
    void reset()
    {
        mandelbrotSet.reset();
        operation = MOVE;
        view();
    }
    
    void setColorBits( int colorBits )
    {
        mandelbrotSet.setColorBits( colorBits );
        notifyView();
    }
    
    void setOperation( int operation ) { this.operation = operation; }
    
    void setResolution( int resolution )
    {
        mandelbrotSet.setResolution( resolution );
        notifyView();
    }
        
    void view() { notifyView(); }
}
