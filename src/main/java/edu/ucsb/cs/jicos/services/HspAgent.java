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

package edu.ucsb.cs.jicos.services;
import edu.ucsb.cs.jicos.foundation.*;
import java.rmi.*;


/** Responsible for discovering an appropriate <CODE>Hsp</CODE>.
 *
 * @author Peter Cappello
 * @version 1.0
 */
public final class HspAgent 
{
    private Service hsp;
    private Service taskServer;
    private Service host;
    private Client2Hsp client2Hsp;
    private String hspDomainName;
    //private HspProxy hspProxy;    
    
    /** The constructor discovers a new Hosting Service Provider (Hsp).
     * @param hspDomainName The domain name of a machine that is running a Hosting
     * Service Provider (<CODE>Hsp</CODE>).
     *
     * <B>Stand alone environment:</B>
     * When the value is "test", the HspAgent constructs
     * an <I>internal</I> hosting service provider consisting of
     * 1 Hsp, 1 TaskServer, & 1 Host. These components are
     * constructed within the same JVM as the HspAgent.
     */    
    public HspAgent( String hspDomainName ) 
    {	
        System.setSecurityManager( new RMISecurityManager() );
        this.hspDomainName = hspDomainName;
        try
        {
            if ( hspDomainName.equals( "test" ) )
            {
                hsp = new Hsp();
                Service taskServer = ((Hsp) hsp).rootTaskServer();
                //taskServer = new TaskServer( hsp, Task.class );
                host = new Host( taskServer, false, 1 );
            }
            else
            {
                hsp = (Service) Naming.lookup( "//" + hspDomainName + ":" + 
                            RegistrationManager.PORT + "/" + Hsp.SERVICE_NAME );            
            }
            client2Hsp = new Client2Hsp( (ClientToHsp) hsp, this );
        }
        catch (Exception e) 
        {
            System.out.println( e.getMessage() );
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public HspAgent( ClientToHsp hsp ) throws RemoteException // not possible9
    {	
        client2Hsp = new Client2Hsp( hsp, this );
    }
    
    public Administrable getAdministrableHsp()
    {
        return (Administrable) hsp;
    }
    
    public Service getServicableHsp()
    {
        return (Service) hsp;
    }
    
    /** Returns a <CODE>Remote</CODE> reference to an <CODE>Hsp</CODE>.
     * @return a <CODE>Remote</CODE> reference to an <CODE>Hsp</CODE>.
     */  
    public Client2Hsp getClient2Hsp() { return client2Hsp; }
    
    /** Returns a <CODE>Remote</CODE> reference to the HSP.
     * @return a <CODE>Remote</CODE> reference to the HSP.
     */    
    
    public String  getHspDomainName() { return hspDomainName; }
    
    public Service getHost() { return host; }
    public Service getHsp() { return hsp; }    
    public Service getTaskServer() { return taskServer; }
}