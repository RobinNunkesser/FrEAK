/*
 * Root.java
 *
 * Created on April 19, 2005, 2:39 PM
 */

package edu.ucsb.cs.jicos.examples.test;

import edu.ucsb.cs.jicos.examples.fibonacci.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.tasks.*;

/**
 *
 * @author cappello
 */
public class Root extends Task 
{
    public Object execute ( Environment environment )
    {
        compute( new F( 0 ) );
        compute( new Killable() );
        return new AddInteger();
    }
}
