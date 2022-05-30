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
 * A compositional Task, whose inputs & output are scalar or matrix values.
 * 
 * @author Andy Pippin
 */
package edu.ucsb.cs.jicos.services.tasks;

import edu.ucsb.cs.jicos.services.ComputeException;
import edu.ucsb.cs.jicos.services.Environment;
import edu.ucsb.cs.jicos.services.Task;

public final class MatrixProduct extends Task {
    /**
     * This method's returned value is a matrix that is the product of the
     * inputs.
     * 
     * If the product cannot be computed, this should toss a ComputeException,
     * but instead, the answer is not defined.
     * 
     * @return the Integer whose intValue is the sum of the intValues of its
     *         Integer inputs.
     * @param environment
     *            Not used by this Task.
     */
    public Object execute( Environment environment )
    {
        Object result = null;
        int sum = 0;
        int numObjects = numInputs();

        Object[] inputObject = new Object[numObjects];

        // Collect all the results.
        for (int io = 0; io < numObjects; io++)
        {
            inputObject[io] = getInput( io );
        }

        if (1 == numObjects)
        {
            result = inputObject[0];
        }
        else
        {
            try
            {
                result = product( inputObject[0], inputObject[1] );
                for (int o = 2; o < numObjects; ++o)
                {
                    result = product( result, inputObject[o] );
                }
            } catch (ComputeException computeException)
            {
                // ignored
            }
        }

        return (result);
    }

    /**
     * This task does not execute on the task server.
     * 
     * @param environment
     *            Session environment (ignored).
     * @return false: This task does not execute on the task server.
     */
/*
    public boolean executeOnServer( Environment environment )
    {
        return false;
    }
*/

    /**
     * Perform actual matrix product.
     * 
     * @param lhs
     *            Left-hand side.
     * @param rhs
     *            Right-hand side.
     * @return Result.
     * @throws ComputeException
     *             If product is not possible to compute.
     */
    public Object product( Object lhs, Object rhs ) throws ComputeException
    {
        Object product = null;

        final int LHS = 0, RHS = 1;
        final int ROW = 0, COL = 1;

        double[][] lMatrix = null, rMatrix = null;
        int lRows, lCols, rRows, rCols;

        lMatrix = getMatrix( lhs, "lhs" );
        rMatrix = getMatrix( rhs, "rhs" );

        lRows = lMatrix.length;
        lCols = lMatrix[0].length;
        rRows = rMatrix.length;
        rCols = rMatrix[0].length;

        // scalar?
        if ((1 == lRows) && (1 == lCols))
        {
            product = scalarMatrix( lMatrix, rMatrix, rRows, rCols );
        }
        else if ((1 == rRows) && (1 == rCols))
        {
            product = scalarMatrix( rMatrix, lMatrix, lRows, lCols );
        }
        else
        {
            product = matrixMatrix( lMatrix, lRows, lCols, rMatrix, rRows,
                    rCols );
        }

        return (product);
    }

    /**
     * Convert an object to a matrix.
     * 
     * @param object
     *            The Number, double[], or double[][] object.
     * @param label
     *            Label for the left- or right-hand side.
     * @return A matrix.
     * @throws ComputeException
     */
    public double[][] getMatrix( Object object, String label )
            throws ComputeException
    {
        double[][] matrix = null;

        // scalar
        if (object instanceof Number)
        {
            matrix = new double[1][1];
            matrix[0][0] = ((Number) object).doubleValue();
        }
        // vector
        else if (object instanceof double[])
        {
            matrix = new double[1][];
            matrix[0] = (double[]) object;
        }
        //matrix
        else if (object instanceof double[][])
        {
            matrix = (double[][]) object;
            if (0 == matrix.length)
            {
                throw new ComputeException( label + " matrix has 0 rows" );
            }
        }
        // oops
        else
        {
            throw new ComputeException( label
                    + " is not a Number, double[], or double[][]" );
        }

        return (matrix);
    }

    /**
     * Multiply a scalar times a matrix.
     * 
     * @param scalarMatrix
     *            A double[1][1] containing the scalar value.
     * @param matrix
     *            A double[][] array.
     * @param rows
     *            The number of rows in <CODE>matrix</CODE>.
     * @param cols
     *            The number of columns in <CODE>matrix</CODE>.
     * @return A double[][] typecast to an object.
     * @throws ComputeException
     *             On any Exception.
     */
    public Object scalarMatrix( double[][] scalarMatrix, double[][] matrix,
            int rows, int cols ) throws ComputeException
    {
        double[][] result = null;

        try
        {
            result = new double[rows][cols];
            double scalar = scalarMatrix[0][0];
            for (int r = 0; r < rows; ++r)
            {
                for (int c = 0; c < cols; ++c)
                {
                    result[r][c] = scalar * matrix[r][c];
                }
            }
        } catch (Exception anyException)
        {
            throw new ComputeException( anyException.getClass().getName()
                    + ": " + anyException.getMessage() );
        }

        return ((Object) result);
    }

    /**
     * Multiply a matrix by a matrix.
     * 
     * @param lhs
     *            Left-hand side matrix.
     * @param lRows
     *            Number of rows in the left-hand side matrix.
     * @param lCols
     *            Number of columns in the left-hand side matrix.
     * @param rhs
     *            Right-hand side matrix.
     * @param rRows
     *            Number of rows in the right-hand side matrix.
     * @param rCols
     *            Number of columns in the right-hand side matrix.
     * @return The resulting double[][] typecast as an Object.
     * @throws ComputeException
     */
    public Object matrixMatrix( double[][] lhs, int lRows, int lCols,
            double[][] rhs, int rRows, int rCols ) throws ComputeException
    {
        double[][] result = null;

        if (lRows != rCols)
        {
            throw new ComputeException( "Cannot multiply " + lRows + 'x'
                    + lCols + " and " + rRows + 'x' + rCols + " matrices!" );
        }

        result = new double[rRows][lCols];

        for (int r = 0; r < rRows; ++r)
        {
            for (int c = 0; c < lCols; ++c)
            {
                double sum = 0.0;
                for (int x = 0; x < lRows; ++x)
                {
                    sum += lhs[x][c] * rhs[r][x];
                }
                result[r][c] = sum;
            }
        }

        return (result);
    }
}
