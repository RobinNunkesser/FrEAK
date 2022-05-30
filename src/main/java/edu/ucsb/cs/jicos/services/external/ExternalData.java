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
 * Defines the available methods on external data collections. <BR>
 * <B><EM>Please note:</EM> </B>&nbsp;&nbsp;The underlying storage structure
 * <U>might not </U> be a DOM object. It may, for example, be a list of values.
 * <BR>
 * 
 * Created on Sep 20, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external;

//----------------------------------------------------------------------------

public interface ExternalData {

    //----------------------------------------------------------------------
    /**
     * Get an individual value from the document. This supports a very limited
     * subset of the XPath queries. Right now, it only supports paths, leaves,
     * and attributes. Although it will return an array of nodes IFF they are
     * the leaf nodes. <BR>
     * <BR>
     * <STRONG>SOME </STRONG> Known Limitations: <BR>
     * <UL>
     * <LI>All queries must begin with '/'.</LI>
     * <LI>Any descendant <CODE>(//)</CODE> is not allowed.</LI>
     * <LI>Relative child <CODE>child::para[position()= <I>n </I></CODE> is
     * not allowed.</LI>
     * <LI>Wildcards are only allowed on leaf nodes <CODE>(.../*)</CODE>
     * </LI>
     * <LI><CODE>.../child::para</CODE> and <CODE>.../para</CODE> are
     * equivalent.</LI>
     * </UL>
     * 
     * @param xpathQuery
     *            An XPath query.
     * @return An individual node.
     */
    public String[] getArray( String xpathQuery );

    //----------------------------------------------------------------------
    /**
     * Get an individual value from the document. This supports a very limited
     * subset of the XPath queries. Right now, it only supports paths, leaves,
     * and attributes.
     * 
     * <STRONG>SOME </STRONG> Known Limitations: <BR>
     * <UL>
     * <LI>All queries must begin with '/'.</LI>
     * <LI>Wildcards <CODE>*</CODE> and <CODE>//</CODE> are not allowed.
     * </LI>
     * </UL>
     * Selections: <BR>
     * <UL>
     * <LI><CODE>/path/to/node</CODE> selects the value of node.</LI>
     * <LI><CODE>/path/to/node/@attribute</CODE> selects the value of the
     * attribute of node.</LI>
     * </UL>
     * 
     * @param xpathQuery
     *            An XPath query.
     * @return An individual string value.
     */
    public String getValue( String xpathQuery );

    //----------------------------------------------------------------------
    /**
     * Get the data out of the (optional) wrapper. This may return the same
     * object.
     * 
     * @return The data.
     */
    public ExternalData removeWrapper();

}