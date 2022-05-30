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
 * An implementation of a list that implements the ExternalData interface.
 * 
 * Created on Sep 20, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;


//-----------------------------------------------------------------------------

public class ExternalDataList extends LinkedList implements ExternalData {

    // Two-dimensional array of key and value.
    private static final int KEY = 0;

    private static final int VALUE = 1;

    /**
     * Creates a new empty List.
     */
    public ExternalDataList()
    {
        super();
    }

    /**
     * Creates a new List and populates it with the contents of <CODE>
     * collection</CODE>.
     * 
     * @param collection
     *            An existing collection to add to the list.
     */
    public ExternalDataList(Collection collection)
    {
        super( collection );
    }

    //----------------------------------------------------------------------
    /**
     * Get an array of values in the form of org.w3c.dom.Element[]. <BR>
     * <STRONG>SOME </STRONG> Known Limitations: <BR>
     * <UL>
     * <LI>All queries must begin with '/'.</LI>
     * <LI>Any descendant <CODE>(//)</CODE> is not allowed.</LI>
     * <LI>Relative child <CODE>child::para[position()= <I>n </I></CODE> is
     * not allowed.</LI>
     * <LI>Wildcards are allowed on any node <CODE>(.../&#x2A;/...)</CODE>
     * </LI>
     * <LI><CODE>.../child::para</CODE> and <CODE>.../para</CODE> are
     * equivalent.</LI>
     * </UL>
     * 
     * @param xpathQuery
     *            An XPath query.
     * @return An individual node.
     */
    public String[] getArray( String xpathQuery )
    {
        LinkedList stringList = new LinkedList();
        String value = null;
        Pattern matchPattern = null;

        if (null != xpathQuery)
        {
            // The "greedy" matcher ('*') needs a character ('.').
            if (xpathQuery.startsWith( "*" ))
            {
                matchPattern = Pattern.compile( "." + xpathQuery );
            } else
            {
                matchPattern = Pattern.compile( xpathQuery );
            }
        }

        Iterator iterator = this.iterator();
        for (; iterator.hasNext();)
        {
            Object[] item = (Object[]) iterator.next();
            String key = (String) item[KEY];

            // Allow for nulls
            if (null == xpathQuery)
            {
                if (null == key)
                {
                    stringList.add( (String) item[VALUE] );
                    break; // there can only be one null value
                }
            } else if (matchPattern.matcher( key ).matches())
            {
                stringList.add( (String) item[VALUE] );
            }
        }

        String[] typeCast = new String[0];
        return ((String[]) stringList.toArray( typeCast ));
    }

    //----------------------------------------------------------------------
    /**
     * Get an individual value from the list. This performs a regex match and
     * returns the first matching key in the list.
     * 
     * <STRONG>SOME </STRONG> Known Limitations: <BR>
     * Selections: <BR>
     * <UL>
     * <LI>Wildcards are allowed <CODE>(.../&#x2A;/...)</CODE></LI>
     * <LI>All <CODE>child::</CODE> are removed.</LI>
     * <LI>All <CODE>attrib::</CODE> are replaced with <CODE>&#x40;</CODE>.
     * </UL>
     * 
     * @param keyName
     *            A (fake) XPath query.
     * @return An individual string value.
     */
    public String getValue( String keyName )
    {
        String value = null;

        Iterator iterator = this.iterator();
        for (; iterator.hasNext();)
        {
            Object[] item = (Object[]) iterator.next();
            String key = (String) item[KEY];

            // Allow for nulls
            if (null == keyName)
            {
                if (null == key)
                {
                    value = (String) item[VALUE];
                    break;
                }
            } else if (keyName.equals( key ))
            {
                value = (String) item[VALUE];
                break;
            } else if (("/ExternalRequest" + keyName).equals( key ))
            {
                value = (String) item[VALUE];
                break;
            }

        }

        return (value);
    }

    //----------------------------------------------------------------------
    /**
     * Get the data out of the (optional) wrapper. This may return the same
     * object.
     * 
     * @return The list.
     */
    public ExternalData removeWrapper()
    {
        return (this);
    }

    //----------------------------------------------------------------------
    /**
     * Add an individual value to the list.
     * 
     * @param key  The key of this pair.
     * @param value An String associated with this key.
     */
    public boolean add( String key, String value )
    {
        Object[] element = new Object[2];
        element[KEY] = key;
        element[VALUE] = value;
        return (add( element ));
    }

    //----------------------------------------------------------------------
    /**
     * Helper method - add an array of key value pairs.
     * 
     * @param keyValuePair  An array of key/value pairs.
     */
    public boolean addKeyValuePairs( String[][] keyValuePair )
    {
        boolean success = true;

        for (int i = 0; (i < keyValuePair.length) && success; ++i)
        {
            success = add( keyValuePair[i] );
        }

        return (success);
    }

}