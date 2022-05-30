/*
 * UnregisterHost.java
 *
 * Created on February 8, 2005, 1:19 PM
 */

package edu.ucsb.cs.jicos.services.commands;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.foundation.*;

/**
 *
 * @author  cappello
 */
public final class UnregisterHost implements CommandSynchronous
{
    private Service host;
    
    public UnregisterHost( Service host )
    {
        this.host = host;
    }
    
    public Object execute( Proxy proxy, RemoteExceptionHandler remoteExceptionHandler ) 
    { 
        Service remoteService = proxy.remoteService();
        Service sender = proxy.serviceName().service();
        try
        {
            remoteService.executeCommand( sender, this );
        }
        catch ( Exception exception )
        {
            remoteExceptionHandler.handle( exception, remoteService, sender );
        }
        return null;
    }
    
    public Object execute( ServiceImpl myService ) 
    {
        TaskServer taskserver = (TaskServer) myService;
        taskserver.unregisterHost( host );
        return null;
    }   
}
