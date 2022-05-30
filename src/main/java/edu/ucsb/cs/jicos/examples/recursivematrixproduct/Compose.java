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


final class Compose extends Task 
{
    public Object execute( Environment environment )
    {
        // construct matrix from submatrices
        int n = ((Matrix) getInput( 0 )).m.length;
        Matrix product = new Matrix( 2 * n );
        product.addSubMatrix( (Matrix) getInput( 0 ), 0, 0, n );
        product.addSubMatrix( (Matrix) getInput( 1 ), 0, 0, n );
        product.addSubMatrix( (Matrix) getInput( 2 ), 0, n, n );
        product.addSubMatrix( (Matrix) getInput( 3 ), 0, n, n );
        product.addSubMatrix( (Matrix) getInput( 4 ), n, 0, n );
        product.addSubMatrix( (Matrix) getInput( 5 ), n, 0, n );
        product.addSubMatrix( (Matrix) getInput( 6 ), n, n, n );
        product.addSubMatrix( (Matrix) getInput( 7 ), n, n, n );
        return product;
    }
}
