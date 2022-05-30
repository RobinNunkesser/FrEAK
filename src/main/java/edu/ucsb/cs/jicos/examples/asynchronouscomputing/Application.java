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

package edu.ucsb.cs.jicos.examples.asynchronouscomputing;

import edu.ucsb.cs.jicos.services.*;
import java.util.*;


final class Application 
{
    public static void main ( String args[] ) throws Exception
    {        
        // get arguments from the command line
        String hspDomainName = args[0];        
        int start = Integer.parseInt ( args[1] );  
        int amount = Integer.parseInt ( args[2] );                
        
        // get a reference to a Hosting Service Provider
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
            
        // login
        Environment environment = new Environment( null, null );
        hsp.login( environment );        
        
        /* dispatch several asynchronous Fibonacci computations
         * start is the 1st Fibonacci number to compute
         * amount is the number of Fibonacci numbers to compute
         * E.g. start = 5, number = 3 means compute Fibonacci numbers 5, 6, & 7
         */
        Task task;
        Map table = new HashMap();
        
        ResultId holdKey = null; // !! debug
        
        for ( int number = start; number < start + amount; number++ )
        {
            task = new F( number );
            ResultId key = hsp.setComputation ( task );
            Integer value = new Integer( number );
            table.put( key, value );
                       
            holdKey = key; // !! debug
        }
        
        Thread.sleep( 2000 );
        try
        {
            System.out.println("Attempting isComplete");
            boolean x = hsp.isComplete( holdKey ); // should produce an exception
            System.out.println("isComplete: " + x);
        }
        catch ( Exception e ) 
        {
            e.printStackTrace();
        }
                   
        ResultId key = null; // !! debug
        
        // get & print Result objects
        for ( int i = 0; i < amount; i++ )
        {
            
            Result result = hsp.getResult();
            /*ResultId*/ key = result.getId();
            Integer number = (Integer) table.remove( key );
            Integer value = (Integer) result.getValue();
            System.out.println("F( " + number + " ) = " + value );
        }    
            
        hsp.logout();
        
        //hsp.login( environment );
        try
        {
            System.out.println("Attempting isComplete");
            boolean x = hsp.isComplete( key ); // should produce an exception
            System.out.println("isComplete: " + x);
        }
        catch ( Exception e ) 
        {
            e.printStackTrace();
        }
    }
}
