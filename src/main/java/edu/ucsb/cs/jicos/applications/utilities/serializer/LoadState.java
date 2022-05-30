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
 * The LoadState and SaveState commands utilize a JDBC database to store
 * the current state of tasks and the environment for a future restore.
 *
 * Created on April 2, 2003, 5:15 PM
 *
 * @author  Peter Cappello
 */

/*
 * The PostgreSQL database and PostgreSQL JDBC driver were used for
 * development and testing. The test setup was on a Windows 2000 Pro PC
 * running Cygwin and PostgreSQL. After installation, the authorization 
 * file $PGDATA/pg_hba.conf was edited to allow appropriate access to 
 * the server. Then the postgres server was started by:
 * $ ipc-daemon2.exe &
 * $ pg_ctl start
 * Afterwards, the user specified in the DSN was created:
 * $ createuser -P username
 * and the database itself was created:
 * $ createdb JICOS
 * Then the database tables were created:
 * $ psql JICOS
 * JICOS=# CREATE TABLE application_state (id text PRIMARY KEY, data bytea );
 * JICOS=# CREATE TABLE hsp_state (id text PRIMARY KEY, data bytea );
 * JICOS=# CREATE TABLE taskserver_state (id text PRIMARY KEY, servername text, data bytea );
 * And verified:
 * JICOS=# \dt
 *                  List of relations
 * Schema |       Name        | Type  |      Owner
 *--------+-------------------+-------+-----------------
 * public | application_state | table | Jonathan Siegel
 * public | hsp_state         | table | Jonathan Siegel
 * public | taskserver_state  | table | Jonathan Siegel
 *(3 rows)
 *
 * JICOS=# \d application_state
 *Table "public.application_state"
 * Column | Type  | Modifiers
 *--------+-------+-----------
 * id     | text  | not null
 * data   | bytea |
 *Indexes:
 *    "application_state_pkey" primary key, btree (id)
 *
 * JICOS=# \d hsp_state
 *  Table "public.hsp_state"
 * Column | Type  | Modifiers
 *--------+-------+-----------
 * id     | text  | not null
 * data   | bytea |
 *Indexes:
 *    "hsp_state_pkey" primary key, btree (id)
 *
 * JICOS=# \d taskserver_state
 *Table "public.taskserver_state"
 *   Column   | Type  | Modifiers
 *------------+-------+-----------
 * id         | text  | not null
 * servername | text  |
 * data       | bytea |
 *Indexes:
 *    "taskserver_state_pkey" primary key, btree (id)
 *
 * Once the database is configured, add the PostgreSQL JDBC driver
 * (i.e. pg74_1jdbc3.jar) explicitly to the CLASSPATH for both your
 * regular AND RMI execution enviroments. At this point, you are ready
 * to use the SaveState and LoadState commands during JICOS operation.
 */

package edu.ucsb.cs.jicos.applications.utilities.serializer;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.*;
import java.sql.*;
import java.io.*;
import java.util.*;


final class LoadState
{
    public static void main ( String args[] ) throws Exception
    {
        // get a reference to a Hosting Service Provider
        // This needs to match the configuration you are restoring.
        HspAgent agent = new HspAgent( "127.0.0.1" ); 
        Client2Hsp hsp = agent.getClient2Hsp();
        StateConfig sconf = new StateConfig();
        sconf.dsn = "jdbc:postgresql://192.168.2.101/JICOS";
        sconf.driver = "org.postgresql.Driver";
        sconf.user = "username";
        sconf.pass = "";
        sconf.id = "001"; // Unique ID for this state session.

        // Reconstruct the computation's Environment
        Vector dataToLoad=null;
        try {
            Class.forName(sconf.driver);
            Connection con = DriverManager.getConnection(sconf.dsn, sconf.user, sconf.pass);            
            PreparedStatement ps = 
                con.prepareStatement("SELECT data FROM application_state WHERE id=?");
            ps.setString(1, sconf.id );
            ResultSet rs = ps.executeQuery();
            if( rs != null && rs.next() ) {
                byte[] bytes = rs.getBytes(1);
                dataToLoad = (Vector)((java.rmi.MarshalledObject)bytesToObject( bytes )).get();                
                rs.close();
            } else {
                // Handle inability to load session here...
                System.err.println( "LoadState: Could not load state from DB with ID="
                    + sconf.id );
                System.exit(-1);
            }
            ps.close();
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
        }

        if( dataToLoad.isEmpty() ) {
            // The session was not stored... probably no computation was active.
            System.err.println( "Attempt to load invalid session." );
            return;
        }
        
        // login
        hsp.login( (Environment)dataToLoad.firstElement() );

        // Load state in leui of compute
        hsp.loadState( sconf );

        System.out.println( "LoadState::Sent load state." );      
     
        // continue computation
        Result result = hsp.getResult();
        
        Solution solution = (Solution) result.getValue();

        // logout
        Invoice invoice = hsp.logout();

        // print the solution & invoice
        System.out.println( solution );
        System.out.println( invoice );
        
        System.exit(0);
    
    }
    
    protected static byte[] objectToBytes( Object obj ) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(baos);
        oout.writeObject(obj);
        oout.close();
        return baos.toByteArray();
    }
    
    protected static Object bytesToObject( byte[] bytes ) throws java.io.IOException, ClassNotFoundException {        
        if (bytes != null) {
          ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
          return objectIn.readObject();
        }
        return null;
    }
}
