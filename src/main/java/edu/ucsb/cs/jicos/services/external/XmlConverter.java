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
 * Defines the necessary methods to enable a class to convert from and to an XML
 * data stream. This is necessary to enable a class to be used by non-Java
 * clients.
 * 
 * Created on July 6, 2004, 10:29 AM
 * 
 * @author Peter Cappello
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external;

import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

public interface XmlConverter {
    /** Jicos namespace. */
    public static final String NAMESPACE = "jicos";

    /** Unknown XSLT stylesheet */
    public static final int STYLESHEET_Unknown = 0;

    /** Xml (identity) XSLT stylesheet */
    public static final int STYLESHEET_Xml = 1;

    /** HTML XSLT stylesheet */
    public static final int STYLESHEET_Html = 2;

    /**
     * Encode a Java class into an XML-encoded string.
     * 
     * @param prefix
     *            Prepend to each line of output (null --> blank).
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public String toXml( String prefix ) throws Exception;

    /**
     * Populate a Java class from an XML document.
     * 
     * @param externalData
     *            External data.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public boolean fromXml( ExternalData externalData ) throws Exception;

    /**
     * Create an appropriate input object for this task.
     * 
     * @param externalData
     *            External data.
     * @return Input object (may be null).
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public Object createInput( ExternalData externalData ) throws Exception;

    /**
     * Create the appropriate shared object for this task.
     * 
     * @param externalData
     *            External data that uses XPath queries to get information.
     * @return Shared object (may be null).
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public Shared createShared( ExternalData externalData ) throws Exception;

    /**
     * Create an XML encoding for the result of the computation of this task.
     * 
     * @param result
     *            The result from a compute.
     * @return An DOM object, or null on error.
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public XmlDocument createResult( Object result ) throws Exception;

    /**
     * Get the XSLT Style sheet used to transform a result of this task to HTML.
     * 
     * @param styleSheetType
     *            The type of XSLT style sheet.
     * @return A DOM object that represents the Style sheet, or <CODE>null
     *         </CODE> if not defined for this type.
     */
    public org.w3c.dom.Document getStyleSheet( int styleSheetType );

    /**
     * Create HTML out of the result.
     * 
     * This is tried by the HttpCollector after trying to get the XSLT style
     * sheet. Therefore, the stylesheet will have precedence.
     * 
     * @param result
     *            The result from a compute. May be <CODE>null</CODE>.
     * @param hostPort
     *            The hostname:port of the HTTP "server".
     * @return An Html String.
     */
    public String toHtmlString( XmlDocument result, String hostPort );

}