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

import edu.ucsb.cs.jicos.services.external.XmlDocument;

public interface HtmlConverter {

    //----------------------------------------------------------------------
    /**
     * Encode a Java class into an XML-encoded string.
     * 
     * @param prefix
     *            Prepend to each line of output (null --> blank).
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public String toHtml( XmlDocument xmlDocument ) throws Exception;

    //----------------------------------------------------------------------
    /**
     * Populate a Java class from an XML document.
     * 
     * @param xmlDocument
     *            The XML DOM.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws Exception
     *             Catch-all for about 15 types of exceptions....
     */
    public XmlDocument fromHttp( String variables ) throws Exception;

}