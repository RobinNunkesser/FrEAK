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
 * Sphere scene based on that in the raytracer written by Paul Graham in ANSI
 * Common Lisp, 1996, p.151
 *
 * Created on December 9, 2003, 7:07 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.raytrace;
import java.util.Vector;
import java.awt.Color;
import java.io.Serializable;
import java.util.Random;


public class RTScene implements Serializable
{
    private int x;
    private int y;
    private int res;
    public int getRes() { return res; }
    public int getX() { return x; }
    public int getY() { return y; }
    
    private RTVector eye;
    public RTVector getEye() { return eye; }
    public void setEye(RTVector eye) { this.eye = eye; }

    private Vector objects;
    public Vector getObjects() { return objects; }
    
    /** Creates a new instance of Scene */
    public RTScene(int x, int y, int res)
    {
        Random rand = new Random(0);
        this.x = x;
        this.y = y;
        this.res = res;
        setEye(new RTVector(0.0, 0.0, 200));
        objects = new Vector();
        objects.add(new RTSphere(new RTVector(0, -300, -1200), 200, Color.red));
        objects.add(new RTSphere(new RTVector(-80,-150, -1200), 200, Color.blue));
        objects.add(new RTSphere(new RTVector(70,-100, -1200), 200, Color.green));
        for (int i = -10; i < 11; i++)
            for (int k = 2; k < 20; k++)
                objects.add(new RTSphere(new RTVector(200*i,700, -400*k), 40 , new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat())));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
}
