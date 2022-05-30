package org.jgpd.io.jbpm.xml;

import java.util.*;

public class XmlElement {

  public XmlElement(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setAttributes( Map attributes ) {
    this.attributes = attributes;
  }
  
  public void removeAttribute( String attributeName ) {
    attributes.remove( attributeName );
  }
  
  public Map getAttributes() {
    return attributes;
  }
  
  public String getAttribute( String attributeName ) {
    return (String) attributes.get( attributeName );
  }
  
  public void addChild( XmlElement child ) {
    String childName = child.getName();
    List namedChildren = (List) children.get( childName );
    if ( namedChildren == null ) {
      namedChildren = new ArrayList();
      children.put( childName, namedChildren );
    }
    namedChildren.add( child );
    content.add( child );
  }
  
  public void removeXmlElement(XmlElement delegateXmlElement) {
    List namedChildren = (List) children.get( delegateXmlElement.getName() );
    namedChildren.remove( delegateXmlElement );
    content.remove( delegateXmlElement );
  }
  
  public List getChildElements( String childName ) {
    List childElements = (List) children.get( childName );
    if ( childElements == null ) {
      childElements = new ArrayList(0);
    }
    return childElements;
  }

  public XmlElement getChildElement( String childName ) {
    XmlElement child = null;
    List namedChildren = (List) children.get( childName );
    if ( namedChildren != null ) {
      if ( namedChildren.size() == 1 ) {
        child = (XmlElement) namedChildren.iterator().next();
      } else if ( namedChildren.size() > 1 ) {
        throw new RuntimeException( "expected only one child-element '" + childName + "' of element '" + name + "' while there were " + namedChildren.size() ); 
      }
    }
    return child;
  }

  public void addText( String text ) {
    this.content.add( text );
  }
  
  public List getContent() {
    return content;
  }
  
  public String getContentString() {
    StringBuffer buffer = new StringBuffer();
    getContentString( buffer, "" );
    return buffer.toString();
  }
  
  public String getProperty( String propertyName ) {
    String propertyValue = null;
    if ( attributes.containsKey( propertyName ) ) {
      propertyValue = (String) attributes.get( propertyName );
    } else {
      XmlElement child = this.getChildElement( propertyName );
      
      if ( ( child != null )
           && ( child.content.size() == 1 ) ) {

        Object contentsString = child.content.get( 0 );
        if ( ! ( contentsString instanceof String ) ) {
          throw new RuntimeException( "can't get property '" + propertyName + "' from element '" + name + "' : child-element with that name contains an element instead of text" );
        }
        propertyValue = (String) contentsString;
      }
      
    }
    return propertyValue;
  }

  public void getContentString( StringBuffer buffer, String indentation ) {
    Iterator iter = content.iterator();
    while (iter.hasNext()) {
      Object contentItem = iter.next();
      if ( contentItem instanceof XmlElement ) {
        XmlElement element = (XmlElement) contentItem; 
        element.toString( buffer, indentation );
      } else {
        if ( APPLY_INDENTATION ) buffer.append( indentation );
        buffer.append( contentItem.toString() );
      }

      if ( APPLY_INDENTATION ) buffer.append( LINESEPARATOR );
    }
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    toString( buffer, "" );
    return buffer.toString().trim();
  }
  
  private void toString( StringBuffer buffer, String indentation ) {

    if ( APPLY_INDENTATION ) buffer.append( indentation );
    buffer.append( '<' );
    buffer.append( name );
    
    Iterator iter = attributes.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      buffer.append( ' ' );
      buffer.append( (String) entry.getKey() );
      buffer.append( "=\"" );
      buffer.append( (String) entry.getValue() );
      buffer.append( "\"" );
    }
    
    if ( content.size() > 0 ) {
      buffer.append( '>' );
      if ( APPLY_INDENTATION ) buffer.append( LINESEPARATOR );
      getContentString( buffer, indentation + "  " );
      if ( APPLY_INDENTATION ) buffer.append( indentation );
      buffer.append( "</" );
      buffer.append( name );
      buffer.append( '>' );
    } else {
      buffer.append( "/>" );
    }
  }
  
  private String name = null;
  private List content = new ArrayList();
  private Map children = new HashMap();
  private Map attributes = new HashMap();
  private static final String LINESEPARATOR = System.getProperty( "line.separator" );
  private static final boolean APPLY_INDENTATION = false;
}
