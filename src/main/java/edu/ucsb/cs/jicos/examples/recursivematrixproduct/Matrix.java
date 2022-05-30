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
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.examples.recursivematrixproduct;


final class Matrix implements java.io.Serializable
{
    int[][] m;

    public Matrix( int[][] m ) 
    {
        this.m = m;
    }
    
    public Matrix( int n )
    {
        int[][] m = new int[n][n];
        for( int i = 0; i < n; i++ )
        for( int j = 0; j < n; j++ )
        {
            m[i][j] = 0;
        }
        this.m = m;
    }
    
    void addSubMatrix( Matrix s, int row, int col, int size)
    {
        for( int i = 0; i < size; i++ )
        for( int j = 0; j < size; j++ )
        {
            m[i + row][j + col] += s.m[i][j];
        }
    }
    
    static Matrix makeMatrix( int n )
    {
        int[][] matrix = new int[n][n];
        for ( int i = 0; i < n; i++)
        for ( int j = 0; j < n; j++)
        {
            matrix[i][j] = 1;
        }
        return new Matrix( matrix );
    }
    
    void print()
    {
        for( int i = 0; i < m.length; i++ )
        {
            System.out.print(" row " + i + ":");
            for( int j = 0; j < m[0].length; j++ )
            {
                System.out.print(" " + m[i][j]);
            }
            System.out.println(" ");
        }
    }
}
