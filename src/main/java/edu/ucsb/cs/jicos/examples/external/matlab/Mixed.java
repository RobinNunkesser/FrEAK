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
 * This creates a mixed task - with Matlab and non-Matlab children.
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.examples.external.matlab;

import java.util.Random;

import edu.ucsb.cs.jicos.examples.fibonacci.F;
import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.Environment;
import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.tasks.MatrixProduct;

public final class Mixed extends Task {
    //
    //-- Constructors --------------------------------------------------------
    
    public Mixed()
    {
    }

    //
    //-- Methods -------------------------------------------------------------

    public Object execute ( Environment environment )
    {
        double[][] matrix = new double[][]
	    {
		{ 2.0, 0.0, 0.0 },
		{ 0.0, 4.0, 0.0 },
		{ 0.0, 0.0, 8.0 }
	    };
        
        compute( new F( 2 ) );
        compute( new MatrixInverse( matrix ) );

        return( new MatrixProduct() );
    }
}
