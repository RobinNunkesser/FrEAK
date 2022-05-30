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
 * Listens in on a port for SOAP messages.
 * 
 * Created on: July 13, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.ClientToHsp;

public class CollectorSoap extends Collector {
    //
    //-- Constructors --------------------------------------------------------

    /**
     * Default, no argument constructor.
     */
    public CollectorSoap()
    {
        super();
        this.initialize();

        return;
    }

    /**
     * Construct a CollectorHttp associated with a particular HSP.
     * 
     * @param hsp
     *            The HSP
     */
    public CollectorSoap(ClientToHsp hsp)
    {
        super( hsp );
        this.initialize();

        return;
    }

    /**
     * Construct a CollectorHttp associated with a particular HSP on a
     * particular port.
     * 
     * @param hsp
     *            The HSP
     * @param port
     *            The port to bind to.
     */
    public CollectorSoap(ClientToHsp hsp, int port)
    {
        super( hsp, port );
        this.initialize();

        return;
    }

    //  Initialize the SOAP collector.
    private void initialize()
    {
        return;
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Start up this thread.
     */
    public void run()
    {
        // TODO aBp: implement CollectorSoap.run()
        LogManager.getLogger().log( LogManager.WARNING,
                "CollectorSoap is not yet implemented.  Exiting!" );
        return;
    }

}