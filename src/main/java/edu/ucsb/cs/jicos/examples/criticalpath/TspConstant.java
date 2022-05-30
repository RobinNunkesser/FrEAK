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
 * An instance of the TSP read from a file.
 * UNDER CONSTRUCTION
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.examples.criticalpath;


public final class TspConstant implements java.io.Serializable 
{
    int[][] distance =
    {
        { 0, 1, 2, 3, 4, 5 },
        { 0, 0, 2, 3, 4, 5 },
        { 0, 0, 0, 3, 4, 5 },
        { 0, 0, 0, 0, 4, 5 },
        { 0, 0, 0, 0, 0, 5 },
        { 0, 0, 0, 0, 0, 0 }
    };
    
    public TspConstant() 
    {
        for ( int i = 0; i < distance.length; i++ )
        {
            System.out.print(" Row " + i + ": ");
            for ( int j = 0; j < i; j++ )
            {
                distance[j][i] = distance[i][j];
                System.out.print(" " + distance[i][j]);
            }
            System.out.println(" ");
        }
    }
}
