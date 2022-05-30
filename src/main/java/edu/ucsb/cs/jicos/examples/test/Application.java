package edu.ucsb.cs.jicos.examples.test;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;

final class Application 
{
    public static void main ( String args[] ) throws Exception
    {
        String hspDomainName = args[0];
        
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
            
        Shared shared = new IntUpperBound( 1 );
        Environment environment = new Environment( null, shared );
        hsp.login( environment );

        Task task = new Root();
        hsp.compute( task );

        Invoice invoice = hsp.logout();
        System.out.println ( invoice );
    }
}
