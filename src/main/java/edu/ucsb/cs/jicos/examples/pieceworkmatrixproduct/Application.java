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

package edu.ucsb.cs.jicos.examples.pieceworkmatrixproduct;

import edu.ucsb.cs.jicos.services.*;
import java.util.HashMap;
import java.util.Map;

final class Application 
{    
    public static void main ( String args[] ) throws Exception
    {        
        String hspDomainName = args[ 0 ];
        int blockSize = Integer.parseInt( args[1] );
        int numBlocks = Integer.parseInt( args[2] );
        
        // square matrix: # rows equals # cols equals blockSize * numBlocks
        
        // get a remote reference to an Hsp
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
            
        // construct input: 2 identical matrices      
        Matrix matrix = Matrix.oneMatrix( blockSize * numBlocks );
        Object[] matrices = { matrix, matrix };
        
        // login
        Environment environment = new Environment( matrices, null );
        hsp.login( environment );

        // construct, dispatch submatrix product tasks
        int numTasksToComplete = 0;
        Map resultId2Task = new HashMap();
        
        for ( int i = 0; i < numBlocks; i++ )
        for ( int j = 0; j < numBlocks; j++ )
        for ( int k = 0; k < numBlocks; k++ )
        {
            Task task = new SubMatrixProductTask( i, k, j, blockSize );            
            ResultId resultId = hsp.setComputation( task );
            
            // cache mapping between resultId and submatrix product task id
            resultId2Task.put( resultId, new I2( i, k ) );
            
            numTasksToComplete++;
        }
        
        // initialize product matrix: 0
        Matrix productMatrix = new Matrix( blockSize * numBlocks );
        
        // compose submatrix products into matrix product
        for ( ; numTasksToComplete > 0; numTasksToComplete--)
        {
            Result result =  hsp.getResult();
            Matrix subMatrix = (Matrix) result.getValue();                        
            ResultId resultId = result.getId();
            
            // get submatrix product task id from resultId
            I2 taskId = (I2) resultId2Task.get( resultId );
            
            // get block coordinates of block
            int rowBlock = taskId.getX();
            int colBlock = taskId.getY();
            
            // get element coordinates of block
            int startRow = rowBlock*blockSize;
            int startCol = colBlock*blockSize;
                                
            productMatrix.addSubMatrix( subMatrix, startRow, startCol, blockSize, blockSize );
        }
        
        // logout
        hsp.logout();
        
        System.out.println( productMatrix );
    }
}
