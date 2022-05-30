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
 * A class of Mandelbrot Set objects
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.demo;


class MandelbrotSet
{   
     double x,          // lower left corner of region
            y,        
            edge;       // edge length of region
    int     resolution, // discretize region into resolution X resolution grid of squares
            limit,      // iteration limit
            colorBits;  // limit = 2^( 3 * colorBits)
    MandelbrotSet previous;
    MandelbrotSet next;

    public MandelbrotSet()
    {
        this.x = -2.0;
        this.y = -1.25;
        this.edge = 2.5;
        this.resolution = 128;
        this.colorBits = 1;
        limit = 1 << (3 * colorBits);
    }
    
    public MandelbrotSet( MandelbrotSet mandelbrotSet )
    {
        this.x = mandelbrotSet.x;
        this.y = mandelbrotSet.y;
        this.edge = mandelbrotSet.edge;
        this.resolution = mandelbrotSet.resolution;
        this.colorBits = mandelbrotSet.colorBits;
        limit = 1 << (3 * colorBits);
    }
    
    void move( int operation )
    {
        // if requested, adjust region size before viewing
        switch ( operation )
        {
            case Model.ZOOM:
                edge /= 2.0;
                x += ( edge / 2.0 );
                y += ( edge / 2.0 );  
                break;
            case Model.UNZOOM:
                x -= ( edge / 2.0 );
                y -= ( edge / 2.0 );
                edge *= 2.0;
                break;
        }
    }
    
    void recenter( int x, int y )
    {        
        this.x += edge * ( ((double) x ) / resolution );
        this.x -= edge / 2.0;
        this.y += edge * ( ((double) y ) / resolution );
        this.y -= edge / 2.0;
    }
    
    void reset()
    {
        x = -2.0;        
        y = -1.25; 
        edge = 2.5;
    }
    
    void setResolution( int resolution )
    {
        this.resolution =  1 << resolution;       
    }
    
    void setColorBits( int colorBits )
    {
        this.colorBits = colorBits;
        this.limit = 1 << (3 * colorBits); // limit = 2^(3*colorBits)
    }
    
    public String toString()
    {
        return "x: " + x + ", y: " + y + ", edge: " + edge + ", res: " + resolution
               +  ", bits: " + colorBits;
    }
}
