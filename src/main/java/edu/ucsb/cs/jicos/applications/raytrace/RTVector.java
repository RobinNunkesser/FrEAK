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

/*
 * Point.java
 *
 * Created on December 9, 2003, 7:27 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.raytrace;

import java.io.Serializable;


public class RTVector implements Serializable
{
    double x;
    double y;
    double z;
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    
    /** Creates a new instance of RTVector */
    public RTVector()
    {
    }
    
    public RTVector(double x, double y, double z) 
    {
        setX(x);
        setY(y);
        setZ(z);
    }
    
    public double mag() {
        return Math.sqrt(magsq());
    }
    
    public double magsq() {
        return RTMath.sq(getX())+RTMath.sq(getY())+RTMath.sq(getZ());
    }
    
    public double distance(RTVector to) {
        return diff(to).mag();
    }
    
    public RTVector diff(RTVector to) {
        return new RTVector(getX()-to.getX(), getY()-to.getY(), getZ() - to.getZ());
    }
    
    public RTVector mult(double scalar) {
        return new RTVector(getX()*scalar, getY()*scalar, getZ()*scalar);
    }
    
    public RTVector unit()
    {
        return new RTVector(getX()/mag(),getY()/mag(),getZ()/mag());
    }
    
    public double dot(RTVector b) { return getX() * b.getX() + getY() * b.getY() + getZ() * b.getZ(); }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
}
