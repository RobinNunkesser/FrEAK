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
 * A raytracer based on the raytracer written by Paul Graham in ANSI Common
 * Lisp, 1996, p.151
 *
 * Created on December 9, 2003, 9:01 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.raytrace;

import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.Environment;

import java.awt.Color;
import java.util.Vector;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;


public class Tracer extends Task
{
    private RTScene scene;
    private Rectangle zone;
    private Rectangle parentRect;
    
    /** Creates a new instance of Tracer */
    public Tracer(Rectangle zone, Rectangle parentRect )
    {
        this.zone = zone;
        this.parentRect = parentRect;
    }
    
    private Color sendray(RTVector from, RTVector dir) 
    {
        RTObject obj = null;
        RTVector inter = null;
        double dist = -1.0;
        Vector obs = scene.getObjects();
        
        for (int i=0, m=obs.size(); i<m; i++) {
            RTObject ob = (RTObject)obs.get(i);
            RTVector hit = ob.intersects(from , dir);
            if (hit != null) {
//            System.out.println("Checking intersection");
                double distance = hit.distance(from);
                if (dist < 0.0 || distance < dist) {
                    obj = ob;
                    inter = hit;
                    dist = distance;
                }
            }
        }
        
        if (obj != null) {
//            Color col = colormult(obj.getSurface(inter).getColor(), lambert(obj,inter,dir));
//            System.out.println("seting color: " + col.toString());
            
            return colormult(obj.getSurface(inter).getColor(), lambert(obj,inter,dir));
        }
                
        return Color.BLACK; 
    }
    
    private static Color colormult(Color a, double scale)
    {
        return new Color( (float)(a.getRed() * scale/255), (float)(a.getGreen() * scale/255),(float)(a.getBlue() * scale/255));
    }
    
    private static Color colormult(Color a, Color b) { 
        return new Color( a.getRed() * b.getRed() / (float)(255*255),
            a.getGreen() * b.getGreen() / (float)(255*255),
            a.getBlue() * b.getBlue() / (float)(255*255)); }
    
    private Color colorAt(double x, double y)
    {
        RTVector eye = scene.getEye();
        return colormult(Color.WHITE, sendray( eye, new RTVector(x-eye.getX(), y-eye.getY(), -eye.getZ()).unit()));     
    }
    
    private double lambert(RTObject o, RTVector intersection, RTVector v) {        
        return Math.max(0.0, Math.abs(o.normal(intersection).dot(v)));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
    public Object execute(Environment environment)
    {
// If I want to do more dynamic subdivision I need to edit RTSolutionComposer
        if (zone.width > 50)
        {
            compute(new Tracer(new Rectangle(zone.x,zone.y,zone.width/2,zone.height/2),zone));
            compute(new Tracer(new Rectangle(zone.x+zone.width/2,zone.y,zone.width/2,zone.height/2),zone));
            compute(new Tracer(new Rectangle(zone.x,zone.y+zone.height/2,zone.width/2,zone.height/2),zone));
            compute(new Tracer(new Rectangle(zone.x+zone.width/2,zone.y+zone.height/2,zone.width/2,zone.height/2),zone));
            return new RTSolutionComposer(parentRect);            
        }
        scene = (RTScene)environment.getInput();
        RTSolution sol = new RTSolution(zone,parentRect);
        BufferedImage buf = new BufferedImage(zone.width, zone.height, BufferedImage.TYPE_INT_RGB);
        int res = scene.getRes();
        for (int i = zone.x; i < zone.x + zone.width; i++)
            for (int j = zone.y; j < zone.y + zone.height; j++) {                
                buf.setRGB(i-zone.x,j-zone.y,colorAt((i-scene.getX()/2.0)/(double)res, (j-scene.getY()/2.0)/(double)res).getRGB());
            }
//        sol.setPixels(buf.getRGB(0,0,zone.width,zone.height,null,0,zone.width));
        sol.setImage(buf);
        return sol;
    }
    
}
