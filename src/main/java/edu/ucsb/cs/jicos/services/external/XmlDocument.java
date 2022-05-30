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
 * Contains a DOM object as well as implementing a (very) few access methods on
 * the data that are patterned after XPath.
 * 
 * Created on: July 13, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.external.services.TaskExternal;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
// For DOM.
import org.w3c.dom.*;
// For Serialization
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlDocument implements java.io.Serializable, ExternalData
{
    //
    //-- Constants -----------------------------------------------------------

    /** Line terminator. */
    public static final String CRLF = "\r\n";

    //
    //-- Variables -----------------------------------------------------------

    protected String xmlText;

    protected Document document;

    //
    //-- Constructors --------------------------------------------------------

    /**
     * No-argument constructor of XmlDocument.
     */
    public XmlDocument()
    {
        this.initialize();
        return;
    }

    /**
     * Construct an XmlDocument from an XML-encoded string.
     * 
     * @param xml
     *            An XML-encoded string.
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public XmlDocument(String xml) throws SAXException, IOException
    {
        this.initialize();
        this.parse( xml );

        return;
    }

    /**
     * Construct an XmlDocument from a XML file.
     * 
     * @param file
     *            File containing XML.
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public XmlDocument(File file) throws SAXException, IOException
    {
        this.initialize();
        this.parse( file );

        return;
    }

    /**
     * Construct an XmlDocument from a Task, and it's result.
     * 
     * @param task
     *            A Jicos task.
     * @param result
     *            The result of a compute on the above <CODE>task</CODE>.
     */
    public XmlDocument(TaskExternal task, Object result) throws Exception
    {
        this.initialize();

        XmlDocument xmlResult = task.createResult( result );
        if (null != xmlResult)
        {
            this.xmlText = new String( xmlResult.xmlText );
            this.document = (Document) xmlResult.document.cloneNode( true );
        }

        return;
    }

    /**
     * Copy constructor.
     * 
     * @param xmlDocument
     *            An XmlDocument instance.
     */
    public XmlDocument(XmlDocument xmlDocument)
    {
        this.initialize();
        this.xmlText = new String( xmlDocument.xmlText );
        this.document = (Document) xmlDocument.document.cloneNode( true );

        return;
    }

    /**
     * Construct an XmlDocument from an XML node
     * 
     * @param rootNode
     *            A node in a DOM (XML) tree.
     */
    public XmlDocument(Node rootNode)
    {
        this.initialize();
        this.document = (Document) rootNode.cloneNode( true );

        return;
    }
    
    /**
     * Construct an XmlDocument on external data.
     * 
     * @param externalData  External data - currently a XmlDocument or List.
     * @throws IllegalArgumentException  If the externalData cannot be converted.
     */
    public XmlDocument( ExternalData externalData )
    {
        this.initialize();
        
        if( externalData instanceof XmlDocument )
        {
            this.xmlText = new String( ((XmlDocument)externalData).xmlText );
            this.document = (Document)((XmlDocument)externalData).document.cloneNode( true );
        }
        else if (externalData instanceof List)
        {
            try
            {
                createFromList( (List) externalData );
            }
            catch (Exception ignoreThis)
            {
            }
        }
    }

    /**
     * Construct an XmlDocument from a list.
     *  
     * @param list
     *            A list of key/value pairs.
     * @throws Exception  Catch-all
     * @see #createFromList(List)
     */
    public XmlDocument(List list) throws Exception
    {
        assert null != list : "List cannot be null!";
        
        this.initialize();
        createFromList( list );
    }
    
    

    /**
     * Construct an XmlDocument from a list. The objects in the list are
     * key/value pairs, where the keys form a well-formed XML tree. <BR>
     * For example, the keys would look like:
     * <UL>
     * <LI>/root/child1/leaf1</LI>
     * <LI>/root/child1/leaf2</LI>
     * <LI>/root/child2/child</LI>
     * <LI>/root/child2/child</LI>
     * <LI>/root/child2/child</LI>
     * </UL>
     * 
     * @param list
     *            A list of key/value pairs.
     */
    private void createFromList( List list ) throws Exception
    {
        this.initialize();

        if (null != list)
        {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            this.document = documentBuilder.newDocument();

            Iterator iterator = list.iterator();
            Element elem = null;

            while (iterator.hasNext())
            {
                Object[] keyValue = (Object[])iterator.next();
                String key = (String)keyValue[0];
                String val = (String)keyValue[1];

                // Make sure it is a valid key (fully qualified, at least 1
                // character).
                //
                if ((null == key) || (2 > key.length())
                        || ('/' != key.charAt( 0 )))
                {
                    throw new Exception(
                            "First component is not fully qualified: \"" + key
                                    + "\"" );
                }

                // Break it up.
                String[] path = key.substring( 1 ).split( "/" );

                String elementName = path[0];

                // Treat the root element special.
                //
                // See if the first value is an attribute. If so, make sure the
                // root element exists.
                if (('@' == elementName.charAt( 0 ))
                        || elementName.startsWith( "attribute::" ))
                {
                    if ('@' == elementName.charAt( 0 ))
                    {
                        key = key.substring( 1 );
                    }
                    else
                    {
                        key = key.substring( 11 );
                    }

                    if (null != (elem = this.document.getDocumentElement()))
                    {
                        elem.setAttribute( key, val );
                    }
                    else
                    {
                        throw new Exception(
                                "Cannot set the attribute of the root element until it is set." );
                    }
                }

                // No root element yet.
                else if (null == (elem = this.document.getDocumentElement()))
                {
                    elem = this.document.createElement( elementName );
                    this.document.appendChild( elem );
                }

                // Already a root element. Make sure the first component
                // matches.
                else
                {
                    elem = this.document.getDocumentElement();
                    String rootName = elem.getNodeName();

                    // Must match the root element.
                    if (!elementName.equals( rootName ))
                    {
                        throw new Exception( "First component (" + elementName
                                + ") doesn't match root element (" + rootName
                                + ")" );
                    }
                }

                // Now drill down the path.
                //
                boolean isAttribute = false;
                boolean isAdded = false;
                Element parent = null;

                for (int e = 1; e < path.length && !isAttribute && !isAdded; ++e)
                {
                    // elem is the current element

                    elementName = path[e];
                    NodeList nodeList = null;

                    // It's an attribute.
                    if ('@' == elementName.charAt( 0 ))
                    {
                        isAttribute = true;
                        elementName = elementName.substring( 1 );
                    }
                    else if (elementName.startsWith( "attribute::" ))
                    {
                        isAttribute = true;
                        elementName = elementName.substring( 11 );
                    }

                    else if (elementName.startsWith( "child::" ))
                    {
                        elementName = elementName.substring( 7 );
                    }

                    // Attribute?
                    //
                    if (isAttribute)
                    {
                        elem.setAttribute( elementName, val );
                        isAdded = true; // Done with this path.
                    }

                    // There are children, so look for it.
                    else if (elem.hasChildNodes()
                            && (null != (nodeList = elem.getChildNodes())))
                    {

                        // How many children? Was there an error? Did we find
                        // it?
                        int numNodes = nodeList.getLength();
                        boolean error = false;
                        boolean found = false;

                        for (int n = 0; (n < numNodes) && !error && !found; ++n)
                        {
                            Node scout = nodeList.item( n );
                            if ((Node.ELEMENT_NODE == scout.getNodeType())
                                    && (elementName
                                            .equals( scout.getNodeName() )))
                            {
                                // Duplicate leaf.
                                if( (e == path.length-1) && (null != parent) )
                                {
                                    Element element = this.document.createElement( elementName );
                                    element.appendChild( this.document.createTextNode( val ) );
                                    parent.appendChild( element );
                                    found = isAdded = true;
                                }
                                else
                                {
                                    elem = (Element) scout;
                                    found = true;
                                }
                            }
                        } // end for-loop.

                        // If not found, add it.
                        if (!found)
                        {
                            Element child = this.document
                                    .createElement( elementName );
                            elem.appendChild( child );
                            elem = child;

                            continue; // Get the next component.
                        }
                    }

                    // There are no children, so add this.
                    else
                    {
                        Element child = this.document
                                .createElement( elementName );
                        elem.appendChild( child );
                        elem = child;

                        continue; // Get the next path component..
                    }
                    
                    parent = elem;
                }

                // Never added it, goes at the bottom.
                if (!isAdded)
                {
                    elem.appendChild( this.document.createTextNode( val ) );
                }
            }
        }

        return;
    }

    /**
     * Construct an XmlDocument from a DOM (XML) element.
     * 
     * @param element
     *            A DOM element.
     */
    public XmlDocument(Element element)
    {
        this.initialize();
        try
        {

            //-- Translate response from XmlDocument to String

            java.io.StringWriter stringWriter = new java.io.StringWriter();

            Transformer serializer = TransformerFactory.newInstance()
                    .newTransformer();

            serializer.transform( new DOMSource( element ), new StreamResult(
                    stringWriter ) );

            String xmlString = stringWriter.toString();
            this.parse( xmlString );

        }
        catch (IOException ioException)
        {
            LogManager.log( ioException );
        }
        catch (SAXException saxException)
        {
            LogManager.log( saxException );
        }
        catch (TransformerException xsltException)
        {
            LogManager.log( xsltException );
        }

        return;
    }

    /**
     * Create an XML document from an exception. This does NOT throw any
     * exceptions as it is usually called in a catch block of a different
     * exception.
     * 
     * @param exception
     *            The badness that occurred.
     */
    public XmlDocument(Throwable exception)
    {
        String exceptionString = new String();

        // If this is a remote exception, strip off the wrapper.
        if (exception instanceof java.rmi.RemoteException)
            exception = exception.getCause();

        exceptionString += "<ExternalResponse exception=\""
                + exception.getClass().getName() + "\">\r\n";

        exceptionString += "  <message>" + exception.getMessage()
                + "</message>\r\n";

        StackTraceElement[] stackTrace = exception.getStackTrace();

        if (0 == stackTrace.length)
        {
            exceptionString += "  <StackTrace numberElements=\"0\"/>\r\n";
        }
        else
        {
            exceptionString += "  <StackTrace numberElements=\""
                    + stackTrace.length + "\">\r\n";
            for (int element = 0; element < stackTrace.length; ++element)
            {
                exceptionString += "    <StackTraceElement>\r\n";
                exceptionString += "      <MethodName>"
                        + stackTrace[element].getMethodName().replaceAll(
                                "<init>", "(constructor)" )
                        + "</MethodName>\r\n";
                exceptionString += "      <ClassName>"
                        + stackTrace[element].getClassName()
                        + "</ClassName>\r\n";
                exceptionString += "      <LineNumber>"
                        + stackTrace[element].getLineNumber()
                        + "</LineNumber>\r\n";
                exceptionString += "      <FileName>"
                        + stackTrace[element].getFileName() + "</FileName>\r\n";
                exceptionString += "    </StackTraceElement>\r\n";
            }
            exceptionString += "  </StackTrace>\r\n";
        }
        exceptionString += "</ExternalResponse>\r\n";

        try
        {
            this.parse( exceptionString );
        }
        catch (Exception parseException)
        {
            LogManager.log( parseException );
        }
    }

    // Inititialize a new instance of XmlDocument.
    private void initialize()
    {
        this.xmlText = null;
        this.document = null;

        return;
    }

    //
    //-- Accessors -----------------------------------------------------------

    /**
     * Get the text used to create the DOM.
     */
    public String getText()
    {
        return (this.xmlText);
    }

    /**
     * Get the raw XML (DOM - Document Object Model) document.
     */
    public Document getDocument()
    {
        return (this.document);
    }

    //
    //-- Mutators ------------------------------------------------------------

    public boolean add( Invoice invoice )
    {
        boolean success = false;
        return (success);
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Parse XML-encoded text.
     * 
     * @param xmlFile
     *            File containing XML-encoded text.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public boolean parse( File xmlFile ) throws SAXException, IOException
    {
        assert  null != xmlFile : "Cannot parse null xmlFile!";

        document = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware( true );
        documentBuilderFactory.setValidating( false );

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse( xmlFile );
        } catch (ParserConfigurationException pce) {
            System.out.println( pce.getMessage() );
        }

        return( null != document );
    }
    
    /**
     * Parse XML-encoded text.
     * 
     * @param xmlString
     *            String containing XML-encoded text.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public boolean parse( String xmlString ) throws SAXException, java.io.IOException
    {
        assert  null != xmlString : "Cannot parse null xmlString!";

        document = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware( true );
        documentBuilderFactory.setValidating( false );

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource xmlInput = new InputSource(
                    new java.io.StringReader( xmlString ) );

            document = documentBuilder.parse( xmlInput );
        } catch (ParserConfigurationException pce) {
            System.out.println( pce.getMessage() );
        }

        return( null != document );
    }

    //------------------------------------------------------------------------
    /**
     * Perform an XPath query on the document.
     * 
     * @param xpathQuery
     *            An XPath query.
     * @return An individual node.
     */

    /*
     * Someday I'll get this working.
     * 
     * 
     * private Node get( String xpathQuery ) { Node result = null;
     * 
     * if( (null != xpathQuery) && (null != this.document) ) { try {
     * NodeIterator nodeIterator = XPathAPI.selectNodeIterator( this.document,
     * xpathQuery ); Node node;
     * 
     * 
     * while( null != (node = nodeIterator.nextNode()) ) { if( isTextNode( node ) ) {
     * for( Node nextNode = node.getNextSibling(); isTextNode( nextNode );
     * nextNode = nextNode.getNextSibling() ) { System.out.println(
     * "nextNode.getNodeValue() = \"" + nextNode.getNodeValue() + "\"" ); } }
     * else { System.out.println( "node.getNodeName() = \"" + node.getNodeName() +
     * "\" (" + node.getNodeType() + ")" ); } } } catch( TransformerException
     * transformerException ) { System.err.println(
     * transformerException.getMessage() ); } }
     * 
     * return( result ); }
     * 
     * 
     * private static boolean isTextNode( Node node ) { boolean isTextNode =
     * false;
     * 
     * if( null != node ) { short nodeType = node.getNodeType(); isTextNode = (
     * (Node.CDATA_SECTION_NODE == nodeType) || (Node.TEXT_NODE == nodeType) ); }
     * 
     * return( isTextNode ); }
     */
    //------------------------------------------------------------------------

    /**
     * Get an individual value from the document. This supports a very limited
     * subset of the XPath queries. Right now, it only supports paths, leaves,
     * and attributes. Although it will return an array of nodes IFF they are
     * the leaf nodes.
     * 
     * <STRONG>SOME </STRONG> Limitations: <BR>
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
    public Element[] getElement( String xpathQuery )
    {
        Element[] result = null;
        LinkedList elementList = new LinkedList();

        if ((null != xpathQuery) && (0 < xpathQuery.length())
                && ('/' == xpathQuery.charAt( 0 )) && (null != this.document))
        {
            String[] component = xpathQuery.substring( 1 ).split( "/" );

            Node node = this.document.getDocumentElement();
            int numElements = component.length;
            Node nextNode = null;
            boolean error = false;

            NodeList nodeList = null;
            int numNodes = 0;

            for (int e = 0; (e < numElements - 1) && !error; ++e)
            {
                // Handle the root attribute.
                //
                if ((0 == e) && (component[e].equals( node.getNodeName() )))
                {
                    // The child is itself. I am not implementing
                    // descendant-or-self::para
                }

                // Make sure there is something to parse.
                //
                else if (0 == component[e].length())
                    error = true;

                // Attributes are mot allowed in the middle of the path.
                //
                else if (('@' == component[e].charAt( 0 ))
                        || (component[e].startsWith( "attribute::" )))
                {
                    error = true;
                }

                // Look up a child element.
                //
                else if (node.hasChildNodes()
                        && (null != (nodeList = node.getChildNodes())))
                {

                    // Remove the superfluous 'child::'
                    String nodeName = component[e];
                    if (nodeName.startsWith( "child::" ))
                        nodeName = nodeName.substring( 7 );

                    numNodes = nodeList.getLength();

                    for (int n = 0; (n < numNodes) && !error; ++n)
                    {
                        Node scout = nodeList.item( n );
                        if ((Node.ELEMENT_NODE == scout.getNodeType())
                                && (nodeName.equals( scout.getNodeName() )))
                        {
                            if (null != nextNode)
                                error = true;
                            else
                                nextNode = scout;
                        }
                    } // end for-loop.

                    // Go to the next node.
                    if (null == nextNode)
                        error = true;
                    else
                        node = nextNode;

                } // end hasChildNodes.

                else
                {
                    error = true;
                }
            } // end of depth-first search.

            // At the leaf of the search, now get all the values.
            if (!error)
            {

                String nodeName = component[numElements - 1];

                // Remove the superfluous 'child::'
                if (nodeName.startsWith( "child::" ))
                    nodeName = nodeName.substring( 7 );

                if (node.hasChildNodes()
                        && (null != (nodeList = node.getChildNodes())))
                {
                    numNodes = nodeList.getLength();

                    // The "greedy quantifier needs something (anything) to
                    // consume.
                    if ('*' == nodeName.charAt( 0 ))
                        nodeName = "." + nodeName;
                    Pattern pattern = Pattern.compile( nodeName );
                    Matcher matcher = null;

                    for (int n = 0; n < numNodes; ++n)
                    {
                        Node scout = nodeList.item( n );
                        if ((Node.ELEMENT_NODE == scout.getNodeType())
                                && (null != (matcher = pattern.matcher( scout
                                        .getNodeName() )))
                                && (matcher.matches()))
                        {
                            elementList.add( scout );
                        }
                    }
                } // end hasChildNodes.

                Element[] typeCast = new Element[0];
                result = (Element[]) elementList.toArray( typeCast );
            }
        }

        return (result);
    }

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
    public String getValue( String xpathQuery )
    {
        String value = null;
        String[] component = null;

        if ( (null != xpathQuery) && (0 < xpathQuery.length()) )
        {
            if( '/' == xpathQuery.charAt( 0 ) )
            {
                component = xpathQuery.substring( 1 ).split( "/" );
            }
            else
            {
                component = xpathQuery.split( "/" );
            }


            Node node = this.document.getDocumentElement();
            int numElements = component.length;
            boolean gotValue = false;

            NodeList nodeList = null;
            int numNodes = 0;

            for (int e = 0; (e < numElements) && !gotValue; ++e)
            {

                // Handle the root attribute.
                //
                if ((0 == e) && (component[e].equals( node.getNodeName() )))
                {
                }

                // Make sure there is something to parse.
                //
                else if (0 == component[e].length())
                    gotValue = true;

                // Handle attributes.
                //
                else if (('@' == component[e].charAt( 0 ))
                        || component[e].startsWith( "attribute::" ))
                {
                    String attrName = null;
                    if (component[e].startsWith( "attribute::" ))
                        attrName = component[e].substring( 11 );
                    else
                        attrName = component[e].substring( 1 );

                    gotValue = true;
                    NamedNodeMap attrMap = null;
                    Node attrNode = null;

                    if ((null != (attrMap = node.getAttributes()))
                            && (null != (attrNode = attrMap
                                    .getNamedItem( attrName ))))
                    {
                        value = attrNode.getNodeValue();
                    }
                }

                // Look up a child element.
                //
                else if (node.hasChildNodes()
                        && (null != (nodeList = node.getChildNodes())))
                {
                    gotValue = true;
                    numNodes = nodeList.getLength();
                    //
                    for (int n = 0; n < numNodes; ++n)
                    {
                        Node scout = nodeList.item( n );
                        if ((Node.ELEMENT_NODE == scout.getNodeType())
                                && (component[e].equals( scout.getNodeName() )))
                        {
                            node = scout;
                            gotValue = false;
                            break;
                        }
                    } // End for-loop through elements.

                } // end element.

                else
                {
                    value = null;
                    gotValue = true;
                }
            }

            // If we don't have the value yet, it's in the last entity.
            //
            if (!gotValue && (null != (nodeList = node.getChildNodes()))
                    && (null != (node = nodeList.item( 0 )))
                    && (Node.TEXT_NODE == node.getNodeType()))
            {
                value = node.getNodeValue();
            }
        }

        return (value);
    }

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
    public String[] getArray( String xpathQuery )
    {

        LinkedList stringList = new LinkedList();

        if ((null != xpathQuery) && (0 < xpathQuery.length())
                && ('/' == xpathQuery.charAt( 0 )))
        {
            String[] component = xpathQuery.substring( 1 ).split( "/" );

            Node node = this.document.getDocumentElement();
            int numElements = component.length;
            Node nextNode = null;
            boolean error = false;

            NodeList nodeList = null;
            int numNodes = 0;

            for (int e = 0; (e < numElements - 1) && !error; ++e)
            {
                // Handle the root attribute.
                //
                if ((0 == e) && (component[e].equals( node.getNodeName() )))
                {
                    // The child is itself. I am not implementing
                    // descendant-or-self::para
                }

                // Make sure there is something to parse.
                //
                else if (0 == component[e].length())
                    error = true;

                // Attributes are mot allowed in the middle of the path.
                //
                else if (('@' == component[e].charAt( 0 ))
                        || (component[e].startsWith( "attribute::" )))
                {
                    error = true;
                }

                // Look up a child element.
                //
                else if (node.hasChildNodes()
                        && (null != (nodeList = node.getChildNodes())))
                {

                    nextNode = null; // reset

                    // Remove the superfluous 'child::'
                    String nodeName = component[e];
                    if (nodeName.startsWith( "child::" ))
                        nodeName = nodeName.substring( 7 );

                    numNodes = nodeList.getLength();

                    for (int n = 0; (n < numNodes) && !error; ++n)
                    {
                        Node scout = nodeList.item( n );
                        //String debugNodeName = scout.getNodeName();
                        if ((Node.ELEMENT_NODE == scout.getNodeType())
                                && (nodeName.equals( scout.getNodeName() )))
                        {
                            if (null != nextNode)
                            {
                                error = true;
                            }
                            else
                            {
                                nextNode = scout;
                            }
                        }
                    } // end for-loop.

                    // Go to the next node.
                    if (null == nextNode)
                        error = true;
                    else
                        node = nextNode;

                } // end hasChildNodes.

                else
                {
                    error = true;
                }
            } // end of depth-first search.

            String nodeName = component[numElements - 1];
            // At the leaf of the search, now get all the values.
            if (!error && (null != nodeName))
            {

                // Is this an attribute? child? what?
                boolean isAttribute = false;
                if (nodeName.startsWith( "child::" ))
                {
                    nodeName = nodeName.substring( 7 );
                    isAttribute = false;
                }
                else if (nodeName.startsWith( "attribute::" ))
                {
                    nodeName = nodeName.substring( 11 );
                    isAttribute = true;
                }
                else if (nodeName.startsWith( "@" ))
                {
                    nodeName = nodeName.substring( 1 );
                    isAttribute = true;
                }

                // The "greedy" matcher ('*') needs a character ('.').
                Pattern matchPattern = null;
                if (nodeName.startsWith( "*" ))
                {
                    matchPattern = Pattern.compile( "." + nodeName );
                }
                else
                {
                    matchPattern = Pattern.compile( nodeName );
                }

                if (isAttribute)
                {

                    NamedNodeMap attrMap = null;
                    Node attrNode = null;

                    // For each attribute entity, see if the name matches. If
                    // so, add
                    // the value to the list.
                    if (null != (attrMap = node.getAttributes()))
                    {
                        for (int i = 0; i < attrMap.getLength(); ++i)
                        {
                            if (null != (attrNode = attrMap.item( i )))
                            {
                                String attrName = attrNode.getNodeName();
                                if (matchPattern.matcher( attrName ).matches())
                                {
                                    stringList.add( attrNode.getNodeValue() );
                                }
                            }
                        }
                    }

                }
                else
                {

                    if (node.hasChildNodes()
                            && (null != (nodeList = node.getChildNodes())))
                    {
                        numNodes = nodeList.getLength();

                        for (int n = 0; n < numNodes; ++n)
                        {
                            Node scout = nodeList.item( n );
                            if (Node.ELEMENT_NODE == scout.getNodeType())
                            {
                                String elementName = scout.getNodeName();

                                // If it matches, concatenate all the text
                                // children.
                                if (matchPattern.matcher( elementName )
                                        .matches())
                                {
                                    String v = new String();
                                    NodeList textNodeList = scout
                                            .getChildNodes();
                                    int numTextNodes = textNodeList.getLength();
                                    for (int tn = 0; tn < numTextNodes; ++tn)
                                    {
                                        Node textNode = textNodeList.item( tn );
                                        if (Node.TEXT_NODE == textNode
                                                .getNodeType())
                                        {
                                            v += textNode.getNodeValue();
                                        }
                                    }
                                    stringList.add( v );
                                }
                            }
                        }
                    }

                } // End !isAttribute

            }
        }

        String[] typeCast = new String[0];
        return ((String[]) stringList.toArray( typeCast ));
    }

    /**
     * Get the data out of the (optional) wrapper. This may return the same
     * object.
     * 
     * @return The data.
     */
    public ExternalData removeWrapper()
    {
        ExternalData nakedExternalData = null;

        // Use the first child element.
        org.w3c.dom.Element[] element = getElement( "/ExternalRequest/child::*" );

        if (0 < element.length)
        {
            nakedExternalData = new XmlDocument( element[0] );
        }
        else
        {
            nakedExternalData = this;
        }

        return (nakedExternalData);
    }

    /**
     * Convert the exception to an XML-encoded string.
     * 
     * @return An XML encoded string representing the exception.
     */
    public static String toXmlString( Throwable exception )
    {
        XmlDocument xmlException = new XmlDocument( exception );
        return (xmlException.toXmlString());
    }

    /**
     * Convert the document to an XML-encoded string.
     * 
     * @return An XML encoded string representing the current DOM structure.
     */
    public String toXmlString()
    {
        String xmlString = null;

        try
        {

            //-- Translate response from XmlDocument to String

            java.io.StringWriter stringWriter = new java.io.StringWriter();

            Transformer serializer = TransformerFactory.newInstance()
                    .newTransformer();

            serializer.transform( new DOMSource( this.document ),
                    new StreamResult( stringWriter ) );

            xmlString = stringWriter.toString();

        }
        catch (Exception ignore)
        {
        }

        return (xmlString);
    }

    /**
     * Convert the exception to an XML-encoded string that can be interpreted as
     * HTML.
     * 
     * @return An HTML encoded string representing the exception.
     */
    public static String toHtmlString( Throwable exception )
    {
        XmlDocument xmlException = new XmlDocument( exception );

        String error = xmlException.toXmlString();

        error = error.replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" )
                .replaceAll( " ", "&nbsp;" );

        String html = ""
                + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 TRANSITIONAL//EN\">\r\n"
                + "<HTML>\r\n" + "<HEAD>\r\n" + "  <TITLE>"
                + exception.getClass().getName() + "</TITLE>\r\n"
                + "</HEAD>\r\n" + "<BODY>\r\n" + "<CENTER>\r\n"
                + "<FONT SIZE=\"+4\" COLOR=\"red\">Exception</FONT><BR>\r\n"
                + "<FONT SIZE=\"+2\">" + exception.getClass().getName()
                + "</FONT><BR>\r\n" + "</CENTER>\r\n" + "<BR>\r\n<BR>\r\n"
                + "<CENTER>\r\n" + "<TABLE BORDER=\"0\"><TR><TD>\r\n"
                + "<PRE>\r\n" + error + "</PRE>\r\n" + "</TR></TR></TABLE>\r\n"
                + "<BR><HR>\r\n" + "</CENTER>\r\n" + "</BODY>" + "</HTML>";

        return (html);
    }

    /**
     * Convert the document to an XML-encoded string that can be interpreted as
     * HTML. <BR>
     * The necessary conversions are:
     * <UL>
     * <LI><CODE>&lt;</CODE> --> <CODE>&amp;lt;"</LI>
     * <LI><CODE>&gt;</CODE> --> <CODE>&amp;gt;"</LI>
     * <LI><CODE>&nbsp;</CODE> --> <CODE>&amp;nbsp;"</LI>
     * </UL>
     */
    public String toHtmlString()
    {
        String xmlString = this.toXmlString();
        return (xmlString.replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" )
                .replaceAll( " ", "&nbsp;" ));
    }

    /**
     * Convert the document to a "Java" string (class[member, [...]]).
     * 
     * @return An instance of the correct result.
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( 1000 );

        buffer.append( getClass().getName() );
        buffer.append( "[ xmlText=" + ((null == this.xmlText) ? "" : "!null") );
        buffer
                .append( ", document="
                        + ((null == this.document) ? "" : "!null") );
        buffer.append( " ]" );

        return (new String( buffer ));
    }

    /**
     * Convert the document to a (human readable) string.
     * 
     * @return An instance of the correct result.
     */
    public String toVerboseString()
    {
        StringBuffer buffer = new StringBuffer( 1000 );

        buffer.append( CRLF + "+--[ xmlText ]--------------------------"
                + "---------------------------------------+" + CRLF );

        buffer.append( CRLF );
        if (null != xmlText)
            buffer.append( xmlText );

        buffer.append( CRLF + "+--[ document ]-------------------------"
                + "---------------------------------------+" + CRLF );

        try
        {
            buffer.append( CRLF );
            Transformer serializer = TransformerFactory.newInstance()
                    .newTransformer();
            serializer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION,
                    "yes" );

            StringWriter stringWriter = new StringWriter();
            serializer.transform( new DOMSource( this.document ),
                    new StreamResult( stringWriter ) );

            buffer.append( stringWriter.getBuffer() + CRLF );
        }

        catch (Exception exception)
        {
            // @throws TransformerConfigurationException If trying to
            //          construct the Templates object, and fails.
            // @throws TransformerException If an unrecoverable error
            //          occurs during the course of the transformation.
            //
            buffer.append( "  **  Error translating DOM." + CRLF + "  **  "
                    + exception.getClass().getName() + "  **  "
                    + exception.getMessage() + CRLF );
        }

        buffer.append( CRLF + "+---------------------------------------"
                + "---------------------------------------+" + CRLF );

        return (new String( buffer ));
    }

    /**
     * Convert the DOM object to a string using a particular stylesheet.
     * 
     * @param xsltStyleSheet
     *            The style sheet to use for the transformation.
     */
    public String Transform( org.w3c.dom.Document xsltStyleSheet )
    {
        String transformResult = null;

        if (null == xsltStyleSheet)
        {
            transformResult = this.toXmlString();
        }

        else
        {
        }

        return (transformResult);
    }

    public static void main( String[] cmdLine ) throws Exception
    {
        java.util.List list = new java.util.LinkedList();
        list.add( new String[]
            { "/ExternalRequest/@taskName", "fibonacci" } );
        list.add( new String[]
            { "/ExternalRequest/Fibonacci/n", "10" } );
        XmlDocument xmlDocument = new XmlDocument( list );
        System.out.println( xmlDocument.toXmlString() );
    };

}
