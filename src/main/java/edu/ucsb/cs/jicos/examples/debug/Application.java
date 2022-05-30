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

package edu.ucsb.cs.jicos.examples.debug;

import edu.ucsb.cs.jicos.services.*;
import java.util.Vector;


final class Application
{
    public static void main ( String args[] ) throws Exception
    {

        // command-line argument is domain name of machine running an Hsp
        String hspDomainName = args[0];
        int taskCount = 1;
        if (args.length>1) taskCount = new Integer(args[1]).intValue();

        // get a remote reference to an Hsp
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();

        //try { // after crash recovery
            hsp.logout();
        //} catch (RemoteException e) {
        //} catch (InterruptedException e) {
        //} catch (IllegalStateException e){
        //}

        // login
        Environment environment = new Environment( null, null );
        hsp.login( environment );

        // compute
        Task task = new HelloTask();
        Vector taskIDs = new Vector();
        //System.out.println("setting");
        for (int i=0; i<taskCount;i++) {
            taskIDs.add(hsp.setComputation(task));
        }
        //System.out.println("getting");
        while (taskIDs.size()>0) {
            System.out.println("r0");
            Result result = hsp.getResult();
            System.out.println("r1");
            Object val = result.getValue();
            System.out.println(val);
            taskIDs.remove(result.getId());
        }
        System.out.println("Done.");

        // logout
        hsp.logout();
    }
}
