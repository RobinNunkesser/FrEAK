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

package edu.ucsb.cs.jicos.examples.hspstate;

import edu.ucsb.cs.jicos.examples.helloworld.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.utilities.*;
import java.util.*;


final class Application
{
    public static void main ( String args[] ) throws Exception
    {
        // command-line argument is domain name of machine running an Hsp
        String hspDomainName = args[ 0 ];
        
        // get a remote reference to an Hsp
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
        
        // print Hsp state
        System.out.println("Before login ...");
        HspState hspState = hsp.getState();
        System.out.println( " Hosting Service Provider: " + hspState.getHspServiceName() );
        System.out.println( "There are " + hspState.numHosts() + " hosts.");
        System.out.println( "There are " + hspState.numTaskServers() + 
                                                              " task servers.");
        Qu clientQ = hspState.getClientQ();
        System.out.println("The client queue:");
        for ( Iterator iterator = clientQ.iterator(); iterator.hasNext(); )
        {
            ClientProfile clientProfile = (ClientProfile) iterator.next();
            System.out.println( clientProfile );
        }
        
        if ( clientQ.size() > 4 )
        {            
            return; // I don't feel like getting in the queue
        }
        
        // login
        Environment environment = new Environment( null, null );
        hsp.login( environment );
        
        // print Hsp state after login
        System.out.println("After login ...");
        hspState = hsp.getState();
        System.out.println( " Hosting Service Provider: " + hspState.getHspServiceName() );
        System.out.println( "There are " + hspState.numHosts() + " hosts.");
        System.out.println( "There are " + hspState.numTaskServers() + 
                                                              " task servers.");
        clientQ = hspState.getClientQ();
        System.out.println("The client queue:");
        for ( Iterator iterator = clientQ.iterator(); iterator.hasNext(); )
        {
            ClientProfile clientProfile = (ClientProfile) iterator.next();
            System.out.println( clientProfile );
        }
        
        // compute
        Task task = new HelloTask();
        String value = (String) hsp.compute( task );
        System.out.println( value );
        
        hsp.logout();
        
        // print Hsp state after logout
        System.out.println("After logout ...");
        hspState = hsp.getState();
        System.out.println( " Hosting Service Provider: " + hspState.getHspServiceName() );
        System.out.println( "There are " + hspState.numHosts() + " hosts.");
        System.out.println( "There are " + hspState.numTaskServers() + 
                                                              " task servers.");
        clientQ = hspState.getClientQ();
        System.out.println("The client queue:");
        for ( Iterator iterator = clientQ.iterator(); iterator.hasNext(); )
        {
            ClientProfile clientProfile = (ClientProfile) iterator.next();
            System.out.println( clientProfile );
        }
    }
}
