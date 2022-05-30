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
 * Sphere based on sphere in the raytracer by Paul Graham in ANSI Common
 * Lisp, 1996, p.151
 *
 * Created on December 10, 2003, 12:04 AM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.raytrace;

import java.awt.Color;


public class RTSphere implements RTObject
{
    private RTSurface surf;
    private double radius;
    private RTVector center;
    
    private void setSurface(RTSurface surf) {
        this.surf = surf;
    }
    
    private RTVector getCenter() { return center; }
    private void setCenter(RTVector c) { center = c; }
    private void setRadius(double r) { radius = r; }
    private double getRadius() { return radius; }
    
    /** Creates a new instance of RTSphere */
    public RTSphere(RTVector center, double r, Color color)
    {
        setCenter(center);
        setRadius(r);
        setSurface(new RTSurface(color));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
    public RTSurface getSurface(RTVector at)
    {
        return surf;
    }
    
    public RTVector intersects(RTVector position, RTVector direction)
    {
        double n = RTMath.minroot(direction.magsq(),
            2*position.diff(getCenter()).dot(direction),
            position.diff(getCenter()).magsq()-RTMath.sq(getRadius()));
        if (Double.isNaN(n)) return null;
        return position.diff(direction.mult(-n));
    }
    
    
    public RTVector normal(RTVector pt)
    {
        return getCenter().diff(pt).unit();
    }
    
}
