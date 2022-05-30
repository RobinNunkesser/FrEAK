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
 * RTSolutionComposer.java
 *
 * Created on December 9, 2003, 7:24 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.raytrace;

import edu.ucsb.cs.jicos.services.Compose;
import edu.ucsb.cs.jicos.services.Environment;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class RTSolutionComposer extends Compose
{
    Rectangle rect;
    /** Creates a new instance of RTSolutionComposer */
    public RTSolutionComposer(Rectangle rect)
    {
        this.rect = rect;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
    public Object execute ( Environment environment )
    {
        RTScene sc = (RTScene)environment.getInput();
        RTSolution firstSolution = (RTSolution) getInput( 0 );
        int x = firstSolution.getRect().width * 2;
        int y = firstSolution.getRect().height * 2;
        RTSolution currentBest = new RTSolution(firstSolution.getParentRect(), rect);
        BufferedImage buf = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buf.createGraphics();
        for ( int i = 0; i < numInputs(); i++ ) 
        {
            RTSolution reportedSolution = (RTSolution) getInput( i );            
            if ( reportedSolution == null ) 
            {
                continue; // subTask: "No better solution found."
            }        
            g.drawImage(reportedSolution.getImage(),-reportedSolution.getParentRect().x+reportedSolution.getRect().x,-reportedSolution.getParentRect().y + reportedSolution.getRect().y,null);            
        }        
        currentBest.setImage(buf);
        return currentBest;
    }
    
    public boolean executeOnServer() { return true; }
    
}
