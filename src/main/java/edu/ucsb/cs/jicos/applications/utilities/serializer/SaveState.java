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
 * Ensure the stateConfig is specified correctly below for your server
 * then run application to save state.
 *
 * Created on April 2, 2003, 5:15 PM
 *
 * @author  Peter Cappello
 *
 * See LoadState.java for information on configuring PostgreSQL JDBC
 * connection.
 *
 */

package edu.ucsb.cs.jicos.applications.utilities.serializer;

import edu.ucsb.cs.jicos.services.*;


final class SaveState 
{
    public static void main ( String args[] ) throws Exception
    {
        // get a reference to the Hosting Service Provider
        HspAgent agent = new HspAgent( "127.0.0.1" );
        Client2Hsp hsp = agent.getClient2Hsp();
        StateConfig sconf = new StateConfig();
        sconf.dsn = "jdbc:postgresql://192.168.2.101/JICOS";
        sconf.driver = "org.postgresql.Driver";
        sconf.user = "username";
        sconf.pass = "";
        sconf.id = "001";
        
        // Database needs the following schema:
        hsp.saveState( sconf );

        System.out.println( "SaveState::Sent save state." );      
    }
}
