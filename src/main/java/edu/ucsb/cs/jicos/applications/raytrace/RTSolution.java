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
 * RTSolution.java
 *
 * Created on December 9, 2003, 7:08 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.raytrace;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;


public class RTSolution implements Serializable
{
    private Rectangle rect;
    private Rectangle parentRect;
    
    // Images are not Serializable!!!!
//    private Image image;
    private int[] pixels;
    
    public Rectangle getRect() { return rect; }
    public Rectangle getParentRect() { return parentRect; }
    public BufferedImage getImage() { 
        BufferedImage image = new BufferedImage(rect.width,rect.height,BufferedImage.TYPE_INT_RGB);
        image.setRGB(0,0,rect.width,rect.height,pixels,0, rect.width);
        // idiot check
//        System.out.println("center pixel: " + new java.awt.Color(image.getRGB(50,50)).toString());
        return image; 
    }
    public void setImage( BufferedImage image ) {
        setPixels(image.getRGB(0,0,rect.width,rect.height,null,0,rect.width));
        // idiot check
//        boolean damn = true;
//        for (int i = 0; i < pixels.length; i++) {
//            if (pixels[i] != java.awt.Color.black.getRGB()) {
//                System.out.println(new java.awt.Color(pixels[i]).toString());
//                damn = false;
//                break;
//            }
//        }
//        if (damn)
//            System.out.println("No image data saved");
//        else 
//            System.out.println("Something saved");
    }
    public int[] getPixels() { return pixels; }
    public void setPixels(int[] pixels) { this.pixels = pixels; }
    
    /** Creates a new instance of RTSolution */
    public RTSolution(Rectangle rect, Rectangle parentRect)
    {
        this.rect = rect;
        this.parentRect = parentRect;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
}
