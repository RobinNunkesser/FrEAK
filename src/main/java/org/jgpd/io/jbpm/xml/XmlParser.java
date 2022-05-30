package org.jgpd.io.jbpm.xml;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XmlParser {

  public static void main(String[] args) {
    try {
      XmlElement rootElement = XmlParser.parse(new FileInputStream(new File(args[0])));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static XmlElement parse(InputStream inputStream) {
    return parse( new InputSource( inputStream ) );
  }

  public static XmlElement parse(Reader reader) {
    return parse( new InputSource( reader ) );
  }

  public static XmlElement parse(InputSource inputSource) {
    XmlElement rootElement = null;
    ParserContentHandler defaultHandler = new ParserContentHandler();

    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(inputSource, defaultHandler);
      rootElement = defaultHandler.getRootElement();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return rootElement;
  }
  
  private static class ParserContentHandler extends DefaultHandler {

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
      XmlElement newElement = new XmlElement(qName);

      Map attributeMap = new HashMap();
      for (int i = 0; i < atts.getLength(); i++) {
        attributeMap.put(atts.getQName(i), atts.getValue(i));
      }

      newElement.setAttributes(attributeMap);

      int elementStackSize = elementStack.size();
      if (elementStackSize > 0) {
        XmlElement containingElement = (XmlElement) elementStack.getLast();
        containingElement.addChild(newElement);
      } else {
        rootElement = newElement;
      }

      elementStack.add(newElement);
    }

    public void endElement(String namespaceURI, String localName, String qName) {
      elementStack.removeLast();
    }

    public void characters(char[] ch, int start, int length) {
      String text = new String(ch, start, length).trim();
      if (!"".equals(text)) {
        XmlElement element = (XmlElement) elementStack.getLast();
        element.addText(text);
      }
    }

    public XmlElement getRootElement() {
      return rootElement;
    }

    private LinkedList elementStack = new LinkedList();
    private XmlElement rootElement = null;
  }

  private static final String LINESEPARATOR = System.getProperty("line.separator");
}
