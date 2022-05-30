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
 * The external value to associate with a Jicos task.
 * 
 * The external id is of the form &lt;ipaddr&lt;-&gt;time&lt;, where: <BR>
 * &nbsp; &nbsp; <I>ipaddr </I> is the IP address of this machine in 8
 * hexadecimal digits. <BR>
 * &nbsp; &nbsp; <I>time </I> is the number of milliseconds since January 1,
 * 1970, 00:00:00 GMT <BR>
 * 
 * Created on Sep 26, 2004
 * 
 * @author Andy Pippin
 */
package edu.ucsb.cs.jicos.services.external;

import edu.ucsb.cs.jicos.services.ResultId;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

public class ExternalRequestId implements Comparable {

    //
    //-- Variables -----------------------------------------------------------

    private String id; //  The external id.

    private ResultId resultId; // The Jicos resultId.

    //
    //-- Constructors --------------------------------------------------------

    /**
     * The default, no argument constructor.
     */
    public ExternalRequestId()
    {
        this.id = makeId();
        this.resultId = null;
    }

    /**
     * Create an external id and associate it with the given Jicos resultId.
     * 
     * @param resultId
     *            The Jicos resultId of a Task.
     */
    public ExternalRequestId(ResultId resultId)
    {
        this.id = makeId();
        this.resultId = resultId;
    }

    /**
     * Create a external request id object with no associated Jicos resultId.
     * 
     * @param externalId
     *            The external id.
     */
    public ExternalRequestId(String externalId)
    {
        id = externalId;
        resultId = null;
    }

    //
    //-- Accessors -----------------------------------------------------------

    /**
     * Return the identifier.
     */
    public String getId()
    {
        return (this.id);
    }

    /**
     * Return the result identifier.
     */
    public ResultId getResultId()
    {
        return (this.resultId);
    }

    /**
     * Override the default toString() method.
     */
    public String toString()
    {
        return (this.id);
    }

    //
    //-- Mutators ------------------------------------------------------------

    /**
     * Define the resultId this external request is associated with.
     */
    public void setResultId( ResultId resultId )
    {
        this.resultId = resultId;
    }

    //
    //-- Methods -------------------------------------------------------------

    private String makeId()
    {
        String id = null;
        int[] localHost = null;
        int numOctets = 4; // Assume IPv4

        // Get the IP address.
        try
        {
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] bytes = inetAddress.getAddress();
            numOctets = bytes.length;
            localHost = new int[numOctets];
            for (int octet = 0; octet < bytes.length; ++octet)
            {
                localHost[octet] = bytes[octet] & 0xff;
            }
        } catch (UnknownHostException unknownHostException)
        {
            localHost = new int[] { 127, 0, 0, 1 };
            numOctets = 4;
        }
        id = new String();
        for (int octet = 0; octet < numOctets; ++octet)
        {
            String hexDigit = Integer.toHexString( localHost[octet] );
            if (1 == hexDigit.length())
            {
                hexDigit = "0" + hexDigit;
            }
            id += hexDigit;
        }

        id += "-" + String.valueOf( (new Date()).getTime() );

        return (id);
    }

    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo( Object arg0 )
    {
        int compareTo = 0;

        if (arg0 instanceof ExternalRequestId)
        {
            ExternalRequestId other = (ExternalRequestId) arg0;
            compareTo = this.id.compareTo( other.id );
        } else
        {
            throw new ClassCastException(
                    "Cannot compare ExternalRequestId to "
                            + arg0.getClass().getName() );
        }

        return compareTo;
    }

    public boolean equals( Object arg0 )
    {
        return (0 == this.compareTo( arg0 ));
    }

}