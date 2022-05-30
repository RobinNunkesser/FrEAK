/*
 * Shutdown.java
 *
 * Created on February 11, 2005, 6:54 PM
 */

package edu.ucsb.cs.jicos.examples.shutdown;

import java.rmi.Naming;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.*;

/**
 *
 * @author  Pete Cappello
 */
final class Shutdown 
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        String hspDomainName = args[0];
        String url = "//" + hspDomainName + ":" + RegistrationManager.PORT + "/" + Hsp.SERVICE_NAME;       
        Administrable administrableHsp = (Administrable) Naming.lookup( url );        
        try
        {
            administrableHsp.shutdown();
        }
        catch ( Exception ignore ) {}
    }
}
