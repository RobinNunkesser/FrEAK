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
 * SubMatrixProduct.java
 *
 * Created on November 27, 2004, 3:31 PM
 */

package edu.ucsb.cs.jicos.examples.pieceworkmatrixproduct;

/**
 *
 * @author  Pete Cappello
 */
public final class SubMatrixProduct implements java.io.Serializable
{
    private int rowBlock;
    private int colBlock; 
    private Matrix cMatrix;
    
    public SubMatrixProduct(int rowBlock, int colBlock, Matrix cMatrix) 
    {
        this.rowBlock = rowBlock;
        this.colBlock = colBlock;
        this.cMatrix  = cMatrix;
    }   
    
    int getRowBlock() { return rowBlock; }
    
    int getColBlock() { return colBlock; }
    
    Matrix getMatrix() { return cMatrix; }
}
