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

/*
 * IncrementalAdd.java
 */

package edu.ucsb.cs.jicos.examples.incrementalevaluation;

import edu.ucsb.cs.jicos.services.Environment;
import edu.ucsb.cs.jicos.services.Task;


public final class IncrementalAdd extends Task 
{    
    public boolean executeOnServer( Environment environment ) { return true; }
    
    public boolean executeIncrementally( Environment environment ) { return true; }
    
    public Object execute( Environment environment ) 
    {
        Integer X = (Integer) getInput( 0 );
        Integer Y = (Integer) getInput( 1 );
        int sum = X.intValue() + Y.intValue();
        
        System.out.println("NORMAL Add.execute" );
        
        return new Integer( sum );
    }
    
    public Object execute( Environment environment, int inputIndex )
    {     

        if ( inputIndex == 0 )
        {
            Integer integer = (Integer) getInput( 0 );
            int i = integer.intValue();
            if ( i % 2 == 0 )
            {
                System.out.println("IncrementalAdd.execute: IGNORE input, inputIndex: " 
                + inputIndex + " i: " + i );
                
                return this; // if 1st arg is even, ignore
            }
            else
            {
                System.out.println("IncrementalAdd.execute: KILL inputIndex: " + 
                inputIndex + " i: " + i + " returning Integer: " + integer );
                
                return integer; // if 1st arg is odd, return it: kill other input
            }
        }
        else
        {    
            System.out.println("IncrementalAdd.execute: IGNORE input, inputIndex: 1" );
            
            return this;
        }
    }
}
