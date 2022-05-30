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

import edu.ucsb.cs.jicos.services.*;


final class Decompose extends Task 
{
    int ar, ac, bc, n;
    
    public Decompose( int ar, int ac, int bc, int n )
    {
        this.ar = ar;
        this.ac = ac;
        this.bc = bc;
        this.n = n;
    }
    
    public Object execute( Environment environment )
    {
        Object[] matrices = ( Object[] ) environment.getInput();
        Matrix a = (Matrix) matrices[0];
        Matrix b = (Matrix) matrices[1];
        
        if ( n < 5 )
        {
            int[][] c = new int[n][n];
        
            for( int i = 0; i < n; i++ )
            for( int j = 0; j < n; j++ )
            {
                c[i][j] = 0;
                for( int k = 0; k < n; k++ )
                {
                    c[i][j] += a.m[ar + i][ac + k] * b.m[ac + k][bc + j];
                }
            }
            return new Matrix( c );
        }
        else
        {
            int m = n / 2;
            compute( new Decompose( 0, 0, 0, m) );
            compute( new Decompose( 0, m, 0, m) );
            compute( new Decompose( 0, 0, m, m) );
            compute( new Decompose( 0, m, m, m) );
            compute( new Decompose( m, 0, 0, m) );
            compute( new Decompose( m, m, 0, m) );
            compute( new Decompose( m, 0, m, m) );
            compute( new Decompose( m, m, m, m) );
            return new Compose();
        }
    }
}
