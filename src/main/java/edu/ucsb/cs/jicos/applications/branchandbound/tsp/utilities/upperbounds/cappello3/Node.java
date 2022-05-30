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
 * The method that determines which edge between 2 Path objects is the smallest
 * returns this object, reflecting the cost, and whether Path[k] uses endpoint i
 * (i = true) and whether Path[l] uses endpoint i (j = true).
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello3;


public class Node
{
    private int[] costsIndices;
    private Path myPath;
    private int costsIndex; // ! proposed fix: March 12, 2004

    // DUMMY_NODE constructor
    //public Node() { this.costsIndices = new int[0]; }
  
    public Node( int costsIndex, Path myPath ) // ! proposed fix: March 12, 2004
    //public Node( int[] costsIndices, Path myPath )
    {
        //this.costsIndices = costsIndices;
        this.costsIndex = costsIndex;
        this.myPath = myPath;
    }

    //public int[] getCostsIndices() { return costsIndices; }
    public int getCostsIndex() { return costsIndex; }

    public Path getMyPath() { return myPath; }
    
    public void setPath( Path path ) { myPath = path; }
    
    public String toString() 
    { 
        return "  costsIndex: " + costsIndex + ", myPath: " + myPath; 
    }
}
