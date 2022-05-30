package edu.ucsb.cs.jicos.examples.test;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;

public class Killable extends Task 
{
    public Object execute ( Environment environment )
    {
        System.out.println("Killable:" + this.getTaskId() );
        Shared shared = environment.getShared();
        if ( ((Integer) shared.get()).intValue() == 1 )
        {
            System.out.println("Killing my host." + this.getTaskId() );
            environment.setShared( new IntUpperBound( 0 ) );
            try { Thread.sleep( 5000 ); } catch (InterruptedException ignore ){}           
            System.exit( 1 );
        }
        return new Integer( 0 );
    }
}
