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
 * ServiceListMap is an extension of LinkedHashMap - which provides both
 * ordered sequences, and O(1) lookups.  The ServiceListMap should be populated
 * with <CODE>ServiceListMapEntry</CODE>'s only, although there is nothing to prevent the
 * developer from being stupid and putting something else in there.
 * 
 * The hierarchy should be as follows:
 * <PRE>
 *   Root Node (invisible)
 *   +- Hsp
 *   |  +- TaskServer
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  +- TaskServer
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  |  +- Host
 *   |  +- TaskServerMatlab
 *   |     +- HostMatlab
 *   +- Hsp
 *      +- TaskServer
 *         +- Host
 * </PRE>
 * 
 * @author pippin
 */

package edu.ucsb.cs.jicos.admin.common;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.ucsb.cs.jicos.services.external.XmlDocument;

public class ServiceListMap extends LinkedHashMap {

    //
    //-- Constants -----------------------------------------------------------

    public static final int DEFAULT_InitialCapacity = 16;

    public static final float DEFAULT_LoadFactor = 0.75f;

    public static final boolean DEFAULT_OrderingMode = false;

    private static final int TREELEVEL_Root = 0;

    private static final int TREELEVEL_Hsp = 1;

    private static final int TREELEVEL_TaskServer = 2;

    private static final int TREELEVEL_Host = 3;

    private static final String[] LEVEL_NAME = { "Root", "Hsp", "TaskServer",
            "Host" };

    public static final String ATTRIB_Name = "name";

    public static final String ATTRIB_Machine = "machine";

    //
    //-- Variables -----------------------------------------------------------

    protected String thisHostName;

    protected String name;

    protected String machine;

    protected ServiceListMap children;


    //
    //-- Constructors --------------------------------------------------------

    /**
     * Constructs an empty, ensertion-ordered <CODE>LinkedHashMap>/CODE>
     * instance with the default capacity (16) and load factor (0.75).
     */
    public ServiceListMap()
    {
        super( DEFAULT_InitialCapacity, DEFAULT_LoadFactor,
                DEFAULT_OrderingMode );
        this.initVariables();
    }

    /**
     * Constructs an empty, ensertion-ordered <CODE>LinkedHashMap>/CODE>
     * instance with the specified initial capacity and the default load factor
     * (0.75).
     * 
     * @param initialCapacity
     *            the initial capacity.
     */
    public ServiceListMap(int initialCapacity)
    {
        super( initialCapacity, DEFAULT_LoadFactor, DEFAULT_OrderingMode );
        this.initVariables();
    }

    /**
     * Constructs an empty, ensertion-ordered <CODE>LinkedHashMap>/CODE>
     * instance with the specified initial capacity and load factor.
     * 
     * @param initialCapacity
     *            the initial capacity.
     * @param loadFactor
     *            the load factor.
     */
    public ServiceListMap(int initialCapacity, float loadFactor)
    {
        super( initialCapacity, loadFactor, DEFAULT_OrderingMode );
        this.initVariables();
    }

    /**
     * Constructs an empty <CODE>LinkedHashMap>/CODE> instance with the
     * specified initial capacity, load factor and ordering mode.
     * 
     * @param initialCapacity
     *            the initial capacity.
     * @param loadFactor
     *            the load factor.
     * @param accessOrder
     *            the ordering mode -<CODE>true</CODE> for access-order,
     *            <CODE>false</CODE> for insertion-order.
     */
    public ServiceListMap(int initialCapacity, float loadFactor,
            boolean accessOrder)
    {
        super( initialCapacity, loadFactor, accessOrder );
        this.initVariables();
    }

    /**
     * Constructs an <CODE>LinkedHashMap>/CODE> instance with default initial
     * capacity, load factor, and access ordering with the same mappings as the
     * specified map.
     * 
     * @param map
     *            the map whose mappings are to be placed in this map.
     */
    public ServiceListMap(Map map)
    {
        super( DEFAULT_InitialCapacity, DEFAULT_LoadFactor,
                DEFAULT_OrderingMode );
        this.initVariables();
        putAll( map );
    }

    /**
     * Constructs an <CODE>LinkedHashMap>/CODE> instance with default initial
     * capacity, load factor, and access; then adds all services to the <CODE>
     * LinkedHashMap</CODE>.
     * 
     * @param xmlDocument XmlDocument specifying the service list.
     */
    public ServiceListMap(XmlDocument xmlDocument) throws SAXException
    {
        super( DEFAULT_InitialCapacity, DEFAULT_LoadFactor,
                DEFAULT_OrderingMode );
        this.initVariables();
        putAll( xmlDocument );
    }

    
    /**
     * Create a node with the specified name and machine.
     * 
     * @param name
     * 			The name of the node.
     * @param machine
     * 			The machine it is on.
     */
    public ServiceListMap( String name, String machine ) {
        super( DEFAULT_InitialCapacity, DEFAULT_LoadFactor,
                DEFAULT_OrderingMode );
        this.initVariables();
        
        this.name = name;
        this.machine = machine;
    }

    /**
     * Initialize all variables.
     */
    private void initVariables()
    {
        // Get this hosts name.
        try
        {
            this.thisHostName = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception anyException)
        {
            this.thisHostName = "localhost";
        }
        
        this.name = null;
        this.machine = null;
        this.children = null;
    }

    //
    //-- Accessors -----------------------------------------------------------
    
    public String getName()
    {
        return( this.name );
    }

    public String getMachine()
    {
        return( this.machine );
    }

    public ServiceListMap getChildren()
    {
        return( this.children );
    }
    
    public String getId()
    {
        String id = null;

        if (null != this.name)
        {
            id = this.name;
            if (null != this.machine)
            {
                id += "-" + this.machine;
            }
        }
        else if (null != this.machine)
        {
            id = this.machine;
        }

        if (null == id)
        {
            id = ((Object) this).toString();
        }

        return (id);
    }

    //
    //-- Mutators ------------------------------------------------------------
    
    public void setName( String name )
    {
        this.name = name;
    }

    public void setMachine( String machine )
    {
        this.machine = machine;
    }

    public void setChildren( ServiceListMap children )
    {
        this.children = children;
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Adds all &lt;HSP&gt;, &lt;TaskServer&gt;, and &lt;Host&gt; elements in
     * the <CODE>XmlDocument</CODE> to the <CODE>LinkedHashMap</CODE>.
     * 
     * @param xmlDocument
     */
    public void putAll( XmlDocument xmlDocument ) throws SAXException
    {
        assert null != xmlDocument : "XmlDocument can not be null.";

        Document document = xmlDocument.getDocument();
        assert null != document : "XmlDocument can not be empty.";

        Node node = document.getDocumentElement();
        if (null != node)
        {
            this.name = getAttributeValue( node,
                    ATTRIB_Name, "Jicos" );
            this.machine = getAttributeValue( node,
                    ATTRIB_Machine, this.thisHostName );
            this.children = new ServiceListMap();

            // Handle the root attribute.
            //
            if (isRootNode( node ))
            {
                addChildren( TREELEVEL_Root, this, node );
            }
            else if (isConfigNode( node ))
            {
                setConfiguration( this, node );
            }

            // Oops!
            else if (isHspNode( node ))
            {
                throw new SAXException(
                        "Root node must be a <Jicos> or <Configuration> element" );
            }
        }

        return;
    }
    
    
    private void addChild( ServiceListMap child ) {
        this.children.put( child.getId(), child );
    }

    
    private void addChildren( int treeLevel, ServiceListMap parent,
            Node node ) throws SAXException
    {
        assert null != parent : "parent cannot be null";
        assert null != node : "node cannot be null";

        Node nextNode = null;
        boolean error = false;

        NodeList nodeList = null;
        int numNodes = 0;

        if (node.hasChildNodes() && (null != (nodeList = node.getChildNodes())))
        {
            int listLength = nodeList.getLength();
            for (int item = 0; item < listLength; ++item)
            {
                Node scout = nodeList.item( item );

                if (Node.ELEMENT_NODE == scout.getNodeType())
                {
                    // HSP node
                    if (isHspNode( scout ))
                    {
                        if (TREELEVEL_Root != treeLevel)
                        {
                            String name = scout.getNodeName();
                            throw new SAXException( "Illegal child (" + name
                                    + ") for this (" + LEVEL_NAME[treeLevel]
                                    + ") level" );
                        }

                        // Create the Hsp entry.
                        int numHsps = parent.children.size();
                        String nodeName = getAttributeValue( scout,
                                ATTRIB_Name, "Hsp"+numHsps );
                        String nodeMachine = getAttributeValue( scout,
                                ATTRIB_Machine, this.thisHostName );
                        HspEntry hsp = new HspEntry( nodeName, nodeMachine );
                        hsp.children = new ServiceListMap();
                        parent.addChild( hsp );

                        // Add all the child TaskServers.
                        addChildren( TREELEVEL_TaskServer, hsp, scout );
                    }

                    // TaskServer node
                    else if (isTaskServerNode( scout ))
                    {
                        if (TREELEVEL_TaskServer != treeLevel)
                        {
                            String name = scout.getNodeName();
                            throw new SAXException( "Illegal child (" + name
                                    + ") for this (" + LEVEL_NAME[treeLevel]
                                    + ") level" );
                        }

                        // Create the TaskServer entry.
                        int numTaskServers = parent.children.size();
                        String nodeName = getAttributeValue( scout,
                                ATTRIB_Name, "TaskServer"+numTaskServers );
                        String nodeMachine = getAttributeValue( scout,
                                ATTRIB_Machine, this.thisHostName );
                        TaskServerEntry taskServer = new TaskServerEntry(
                                nodeName, nodeMachine );
                        taskServer.children = new ServiceListMap();
                        parent.addChild( taskServer );

                        // Add all the child Hosts.
                        addChildren( TREELEVEL_Host, taskServer, scout );
                    }

                    // Host node.
                    else if (isHostNode( scout ))
                    {
                        if (TREELEVEL_Host != treeLevel)
                        {
                            String name = scout.getNodeName();
                            throw new SAXException( "Illegal child (" + name
                                    + ") for this (" + LEVEL_NAME[treeLevel]
                                    + ") level" );
                        }

                        // Create the Host entry.
                        int numHosts = parent.children.size();
                        String nodeName = getAttributeValue( scout,
                                ATTRIB_Name, "Host"+numHosts );
                        String nodeMachine = getAttributeValue( scout,
                                ATTRIB_Machine, this.thisHostName );
                        HostEntry host = new HostEntry( nodeName, nodeMachine );
                        host.children = new ServiceListMap();
                        parent.addChild( host );

                        // Add all the configuration, if any.
                        addChildren( TREELEVEL_Host, host, scout );
                    }

                    else if (isConfigNode( scout ))
                    {
                        setConfiguration( parent, scout );
                    }
                }
            }
        }
    }

    /**
     *  Set a configuration value(s)
     * 
     * @param  parent
     * @param  node
     */
    private void setConfiguration( ServiceListMap parent,
            Node node )
    {
        if( null == parent ) 
        {}
        else if( parent instanceof HostEntry )
        {
        }
        else if( parent instanceof TaskServerEntry )
        {
        }
        else if( parent instanceof HspEntry )
        {
        }
        else
        {
        }
    }

    /**
     * Convert to String representation.
     */
    public String toString()
    {
        return( toString( null, this ) );
    }
    
    public String toString( String prefix, ServiceListMap serviceListMap )
    {
        String p = (null == prefix) ? "" : prefix;
        
        String string = p + "+--" + serviceListMap.name + "\r\n";
        
        // Get all the children.
        Iterator iterator = this.children.keySet().iterator();
        while( iterator.hasNext() )
        {
            String childName = (String)(iterator.next());
            ServiceListMap child = (ServiceListMap)this.children.get( childName );
            
            string += child.toString( "|  "+p, child );
        }
        
        return( string );
    }
    
    /**
     * Convert to an XML document.
     * @return
     */
    public XmlDocument toXml()
    {
        XmlDocument xml = null;
        return( xml );
    }
    
    //
    //-- Static Methods ------------------------------------------------------
    
    private static String getAttributeValue( Node node, String name )
    {
        assert null != node : "node cannot be null";
        assert null != name : "attribute name cannot be null";

        String attribValue = null;
        
        NamedNodeMap attrMap = null;
        Node attrNode = null;

        if ((null != (attrMap = node.getAttributes()))
                && (null != (attrNode = attrMap.getNamedItem( name ))))
        {
            attribValue = attrNode.getNodeValue();
        }

        return (attribValue);
    }

    private static String getAttributeValue( Node node, String attributeName,
            String defaultValue )
    {
        String attribValue = getAttributeValue( node, attributeName );
        return ((null != attribValue) ? attribValue : defaultValue);
    }

    /** Is this the root element? */
    private static boolean isRootNode( Node node )
    {
        boolean equals = false;
        if (null != node)
        {
            String name = node.getNodeName();
            if (null != name)
            {
                equals = "jicos".equalsIgnoreCase( name );
            }
        }
        return (equals);
    }

    /** Is this a HSP element? */
    private static boolean isHspNode( Node node )
    {
        boolean equals = false;
        if (null != node)
        {
            String name = node.getNodeName();
            if (null != name)
            {
                equals = "hsp".equalsIgnoreCase( name );
            }
        }
        return (equals);
    }

    /** Is this a TaskServer element? */
    private static boolean isTaskServerNode( Node node )
    {
        boolean equals = false;
        if (null != node)
        {
            String name = node.getNodeName();
            if (null != name)
            {
                int len = Math.min( name.length(), 10 );
                equals = "taskserver"
                        .equalsIgnoreCase( name.substring( 0, len ) );
            }
        }
        return (equals);
    }

    /** Is this a host element? */
    private static boolean isHostNode( Node node )
    {
        boolean equals = false;
        if (null != node)
        {
            String name = node.getNodeName();
            if (null != name)
            {
                int len = Math.min( name.length(), 4 );
                equals = "host".equalsIgnoreCase( name.substring( 0, len ) );
            }
        }
        return (equals);
    }

    /** is this a config element? */
    private static boolean isConfigNode( Node node )
    {
        boolean equals = false;
        if (null != node)
        {
            String name = node.getNodeName();
            if (null != name)
            {
                int len = Math.min( name.length(), 6 );
                equals = "config".equalsIgnoreCase( name.substring( 0, len ) );
            }
        }
        return (equals);
    }

}
