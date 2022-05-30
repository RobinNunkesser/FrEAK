/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport;

import freak.Freak;
import freak.core.util.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import javax.swing.border.*;
import org.jdom.*;

/**
 * EXPERIMENTAL...
 * @author  Matthias
 */
public class PersistenceManager {

	private int lastId = -1;
	private Map handlerMap;
	private Map classHandler = new HashMap();
	private List objects = new ArrayList();

	private ArrayPersistenceHandler arrayHandler;

	private static Class[] allHandlers = ClassCollector.getAllImplementors(ClassPersistenceHandler.class);

	/**
	 * Does nothing but indirectly triggers the collection of PersistenceHandlers. 
	 * @see freak.core.modulesupport.PersistenceManager#allHandlers
	 */
	public static void init() {
	}

	private PersistenceManager() {
		// collect classHandlers
		for (int i = 0; i < allHandlers.length; i++) {
			ClassPersistenceHandler handler = null;
			try {
				handler = (ClassPersistenceHandler)newObject(allHandlers[i], new Object[] { this });
			} catch (UnsupportedEnvironmentException e) {
			}

			if (handler != null) {
				Class handles = handler.handles();
				if (handles != null) {
					classHandler.put(handles, handler);
				}
			}
		}
		arrayHandler = new ArrayPersistenceHandler(this);
	}

	private ClassPersistenceHandler getHandlerFor(Class c) throws XMLizeException {
		ClassPersistenceHandler cph = (ClassPersistenceHandler)classHandler.get(c);
		if (cph != null) {
			return cph;
		}

		// Arrays are treated seperately.		
		if (c.isArray()) {
			return arrayHandler;
		}

		ArrayList handledList = new ArrayList();

		// try to find the best handler
		// the current implementation might not work perfect in all cases...
		//subject of changes (but uncritical: reading doesn't depend on it)
		for (Iterator iter = classHandler.keySet().iterator(); iter.hasNext();) {
			Class handled = (Class)iter.next();
			if (handled.isInterface() && handled.isAssignableFrom(c) && handled != Serializable.class) {
				boolean ignoreHandle = false;
				for (int i = 0; i < handledList.size(); i++) {
					if (handled.isAssignableFrom((Class)handledList.get(i))) {
						ignoreHandle = true;
						break;
					}
				}
				if (!ignoreHandle) {
					for (int i = handledList.size() - 1; i >= 0; i--) {
						if (((Class)handledList.get(i)).isAssignableFrom(handled)) {
							handledList.remove(i);
						}
					}
					handledList.add(handled);
				}
			}

		}

		// if we haven't found _the_ best handler but more than one try to fallback
		if (handledList.size() != 1) {
			if (Serializable.class.isAssignableFrom(c)) {
				return (ClassPersistenceHandler)classHandler.get(Serializable.class);
			}
			throw new XMLizeException("found " + handledList.size() + " handlers for " + c.getName());
		}
		return (ClassPersistenceHandler)classHandler.get(handledList.get(0));
	}
	
	/**
	 * Generates a XML-element represnting the <code>Object</code> o.
	 * @param o the object to transform. 
	 * @return the generated XML-element.
	 * @throws XMLizeException if the conversion failed.
	 */
	public Element toXML(Object o) throws XMLizeException {
		if (o == null) {
			return new Element("null");
		}
		return toXML(o, o.getClass());
	}

	/**
	 * Generates a XML-element represnting the <code>Object</code> o.
	 * @param o the object to transform.
	 * @param forcedType the type to which the object is downcasted before the generation starts. 
	 * @return the generated XML-element.
	 * @throws XMLizeException if the conversion failed.
	 */
	public Element toXML(Object o, Class forcedType) throws XMLizeException {
		if (o == null) {
			return new Element("null");
		}

		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i) == o && o.getClass() != String.class) {
				Freak.debug("WARNING: reference persistence not tested yet", 2);
				return new Element("ref").setAttribute("id", i + "");
			}
		}

		ClassPersistenceHandler handler = getHandlerFor(forcedType);
		if (handler == null) {
			return null;
		}

		int id = objects.size();
		if (!(o instanceof String)) {
			objects.add(o);
		}

		Element e;
		try {
			e = handler.toXML(o);
		} catch (Exception e1) {
			throw new XMLizeException(e1);
		}
		e.setAttribute("handler", handler.getClass().getName());
		if (!(o instanceof String)) {
			e.setAttribute("id", id + "");
		}
		return e;
	}

	/**
	 * Generates an <code>Object</code> based on the informations in the XML-element <code>e</code>. 
	 * @param e a XML-element which represents an object. 
	 * @return the generated object.
	 * @throws XMLizeException if the generation fails.
	 */
	public Object fromXML(Element e) throws XMLizeException {
		if (e.getName().equals("ref")) {
			try {
				Object o = objects.get(e.getAttribute("id").getIntValue());
				if (o == null) {
					throw new XMLizeException("could not find object with id " + e.getAttribute("id").getIntValue());
				}
				return o;
			} catch (DataConversionException e1) {
				throw new XMLizeException("id Attribute is missing or has wrong format", e1);
			}
		}

		if (e.getName().equals("null")) {
			return null;
		}

		String handlerName = "";
		if (e.getAttribute("handler") != null) {
			handlerName = e.getAttribute("handler").getValue();
		} else {
			handlerName = (String)handlerMap.get(e.getName());
		}
		Object o = null;
		try {
			o = newObjectByString(handlerName, new Object[] { this });
		} catch (UnsupportedEnvironmentException e2) {
			throw new XMLizeException("failed to create handler", e2);
		}
		if (o instanceof ClassPersistenceHandler) {
			if (e.getAttribute("id") != null) {
				try {
					lastId = e.getAttribute("id").getIntValue();
				} catch (DataConversionException e1) {
					lastId = -1;
				}
			} else {
				lastId = -1;
			}

			Object result;
			try {
				result = ((ClassPersistenceHandler)o).fromXML(e);
			} catch (Exception e3) {
				throw new XMLizeException(e3);
			}
			try {
				if (e.getAttribute("id") != null) {
					registerObject(result, e.getAttribute("id").getIntValue());
				}
			} catch (DataConversionException e1) {
				throw new XMLizeException(e1);
			}
			return result;
		}
		throw new XMLizeException("no handler found");
	}

	/**
	 * Registers an object during DeXMLizing. All objects must be registered or
	 * object references might not be resolved right. If a ClassPersistenceHandler
	 * needs to call the fromXML method of the PersistenceManager he must call registerObject
	 * - with the object he intends to return - before he calls fromXML the first time.
	 * @param o the Object to register.
	 */
	public void registerObject(Object o) {
		if (lastId > 0) {
			while (objects.size() <= lastId) {
				objects.add(null);
			}
			objects.set(lastId, o);
		}
	}

	private void registerObject(Object o, int forcedId) {
		int tmp = lastId;
		lastId = forcedId;
		registerObject(o);
		lastId = tmp;
	}

	/**
	 * Generates a XML-document which represents the object <code>o</code>.
	 * @param o the object to transform.
	 * @return the generated XML-document.
	 * @throws XMLizeException
	 */
	public static Document XMLize(Object o) throws XMLizeException {
		PersistenceManager pm = new PersistenceManager();
		Element e = pm.toXML(o);

		Map map = new HashMap();
		buildHandlerMap(e, map);
		cleanHandlerAttributes(e, map);

		Collection col = new HashSet();
		buildUsedIdsList(e, col);
		cleanIdAttributes(e, col);

		Element handlerElement = new Element("handler");
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String elementName = (String)iter.next();
			if (map.get(elementName) != null) {
				handlerElement.addContent(new Element(elementName).setText((String)map.get(elementName)));
			}
		}

		Document doc = new Document(new Element("FreakPersistence").addContent(handlerElement).addContent(e));
		return doc;
	}

	private static void buildHandlerMap(Element e, Map map) {

		// if a handler is specified in Element e ...
		if (e.getAttribute("handler") != null) {

			// ... and we already have encountered an Element with a name equal to e.getName()...
			if (map.containsKey(e.getName())) {
				// ... and the "element name"<->"handler" mapping wasn't unique in the past or is no longer unique if take
				// the cuurent element into account ... 
				if (map.get(e.getName()) == null || !((String)map.get(e.getName())).equals(e.getAttributeValue("handler"))) {
					// ... mark the "element name" as not unique mappable to an handler
					map.put(e.getName(), null);
				}
			} else {
				map.put(e.getName(), e.getAttributeValue("handler"));
			}
		} else {
			if (map.containsKey(e.getName())) {
				map.put(e.getName(), null);
			}
		}

		Iterator iter = e.getChildren().iterator();
		while (iter.hasNext()) {
			buildHandlerMap((Element)iter.next(), map);
		}
	}

	private static void cleanHandlerAttributes(Element e, Map map) {
		if (e.getAttribute("handler") != null) {
			if (map.get(e.getName()) != null) {
				e.removeAttribute("handler");
			}
		}
		Iterator iter = e.getChildren().iterator();
		while (iter.hasNext()) {
			cleanHandlerAttributes((Element)iter.next(), map);
		}
	}

	private static void buildUsedIdsList(Element e, Collection col) {
		if (e.getName().equals("ref")) {
			try {
				col.add(new Integer(e.getAttribute("id").getIntValue()));
			} catch (DataConversionException e1) {
			}
		}
		Iterator iter = e.getChildren().iterator();
		while (iter.hasNext()) {
			buildUsedIdsList((Element)iter.next(), col);
		}
	}

	private static void cleanIdAttributes(Element e, Collection col) {
		if (e.getAttribute("id") != null) {
			try {
				if (!col.contains(new Integer(e.getAttribute("id").getIntValue()))) {
					e.removeAttribute("id");
				}
			} catch (DataConversionException e1) {
			}
		}
		Iterator iter = e.getChildren().iterator();
		while (iter.hasNext()) {
			cleanIdAttributes((Element)iter.next(), col);
		}
	}

	/**
	 * Generates an object based on the XML-document.
	 * @param doc the XML-document.
	 * @return the generated object.
	 * @throws XMLizeException if the generation fails.
	 */
	public static Object DeXMLize(Document doc) throws XMLizeException {
		Element handlers = doc.getRootElement().getChild("handler");
		Map map = new HashMap();
		Iterator iter = handlers.getChildren().iterator();
		while (iter.hasNext()) {
			Element entry = (Element)iter.next();
			map.put(entry.getName(), entry.getText());
		}
		handlers.detach();

		PersistenceManager pm = new PersistenceManager();
		pm.handlerMap = map;

		try {
			return pm.fromXML((Element)doc.getRootElement().getChildren().get(0));
		} catch (Exception e) {
			throw new XMLizeException(e);
		}
	}

	// use methods from ModuleCollector?
	private static Object newObjectByString(String className, Object[] param) throws UnsupportedEnvironmentException, XMLizeException {
		Class c = null;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new XMLizeException(e);
		}

		return newObject(c, param);
	}

	private static Object newObjectByString(String className) throws UnsupportedEnvironmentException, XMLizeException {
		return newObjectByString(className, new Object[] {
		});
	}

	private static Object newObject(Class c, Object[] param) throws UnsupportedEnvironmentException {

		if (c == null) {
			return null;
		}

		Constructor[] constr = c.getConstructors();
		Object o = null;

		for (int i = 0; i < constr.length; i++) {
			try {
				o = constr[i].newInstance(param);
				return o;
			} catch (IllegalArgumentException e) {
				// We have called the wrong constructor.
				// Let's try the next one...
			} catch (InvocationTargetException e) {
				// The constructor has the expected signature. But it caused an
				// exception.
				if ((e.getTargetException() instanceof UnsupportedEnvironmentException)) {
					// The module isn't compatible with the environment chosen
					// so far.
					throw new UnsupportedEnvironmentException();
				}
				System.err.println("The constructor of " + c.getName() + " caused an exception:\n" + e.getTargetException().toString());
				e.printStackTrace();
				return null;
			} catch (InstantiationException e) {
				// tried to instantiate an abstract class
				return null;
			} catch (Exception e) {
				// The constructor has the expected signature. But it doesn't
				// work properly.
				System.err.println("The constructor of " + c.getName() + " doesn't work properly.\n" + e.toString());
				e.printStackTrace();
			}
		}
		System.err.println("The class " + c.getName() + " doesn't have a constructor with the expected signature!");
		return null;
	}

	/**
	 * @author  nunkesser
	 */
	public static class Serializer implements ClassPersistenceHandler {

		private PersistenceManager pm = null;

		public Serializer(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) {
			System.out.println("WARNING: Had to use Serializer for " + o.getClass().getName());
			return new Element("object").addContent(new CDATA(freak.core.util.Base64.encodeObject((Serializable)o))).addContent(new Comment("class=" + o.getClass().getName()));
		}

		public Object fromXML(Element e) {
			return freak.core.util.Base64.decodeToObject(e.getText());
		}

		public Class handles() {
			return Serializable.class;
		}

	}

	public static class IntegerPersistenceHandler implements ClassPersistenceHandler {
		public IntegerPersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			return new Element("Integer").addContent(new Text(((Integer)o).toString()));
		}

		public Object fromXML(Element e) {
			return Integer.valueOf(e.getText());
		}

		public Class handles() {
			return Integer.class;
		}
	}

	public static class DoublePersistenceHandler implements ClassPersistenceHandler {
		public DoublePersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			return new Element("Double").addContent(new Text(((Double)o).toString()));
		}

		public Object fromXML(Element e) {
			return Double.valueOf(e.getText());
		}

		public Class handles() {
			return Double.class;
		}
	}

	public static class BooleanPersistenceHandler implements ClassPersistenceHandler {
		public BooleanPersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			return new Element("Boolean").addContent(new Text(((Boolean)o).toString()));
		}

		public Object fromXML(Element e) {
			return Boolean.valueOf(e.getText());
		}

		public Class handles() {
			return Boolean.class;
		}
	}

	public static class StringPersistenceHandler implements ClassPersistenceHandler {
		public StringPersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			return new Element("String").addContent(new Text((String)o));
		}

		public Object fromXML(Element e) {
			return e.getText();
		}

		public Class handles() {
			return String.class;
		}
	}

	public static class ColorPersistenceHandler implements ClassPersistenceHandler {
		public ColorPersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			Color c = (Color)o;
			Element result = new Element("Color");
			result.setAttribute("red", c.getRed() + "");
			result.setAttribute("green", c.getGreen() + "");
			result.setAttribute("blue", c.getBlue() + "");
			return result;
		}

		public Object fromXML(Element e) throws DataConversionException {
			int red = e.getAttribute("red").getIntValue();
			int green = e.getAttribute("green").getIntValue();
			int blue = e.getAttribute("blue").getIntValue();
			Color c = new Color(red, green, blue);
			return c;
		}

		public Class handles() {
			return Color.class;
		}
	}

	public static class BevelBorderPersistenceHandler implements ClassPersistenceHandler {
		public BevelBorderPersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			BevelBorder border = (BevelBorder)o;
			Element result = new Element("BevelBorder");
			result.setAttribute("bevelType", border.getBevelType() + "");
			// there are more attributes but the one above is sufficient for us
			return result;
		}

		public Object fromXML(Element e) throws DataConversionException {
			return new BevelBorder(e.getAttribute("bevelType").getIntValue());
		}

		public Class handles() {
			return BevelBorder.class;
		}
	}

	public static class PointPersistenceHandler implements ClassPersistenceHandler {
		public PointPersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			Point point = (Point)o;
			Element result = new Element("Point");
			result.setAttribute("x", point.x + "");
			result.setAttribute("y", point.y + "");
			return result;
		}

		public Object fromXML(Element e) throws DataConversionException {
			return new Point(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
		}

		public Class handles() {
			return Point.class;
		}
	}

	public static class RectanglePersistenceHandler implements ClassPersistenceHandler {
		public RectanglePersistenceHandler(PersistenceManager pm) {
		}

		public Element toXML(Object o) {
			Rectangle rect = (Rectangle)o;
			Element result = new Element("Rectangle");
			result.setAttribute("height", rect.height + "");
			result.setAttribute("width", rect.width + "");
			result.setAttribute("x", rect.x + "");
			result.setAttribute("y", rect.y + "");
			return result;
		}

		public Object fromXML(Element e) throws DataConversionException {
			return new Rectangle(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue(), e.getAttribute("width").getIntValue(), e.getAttribute("height").getIntValue());
		}

		public Class handles() {
			return Rectangle.class;
		}
	}
	/**
	 * @author  nunkesser
	 */
	public static class ListPersistenceHandler implements ClassPersistenceHandler {
		private PersistenceManager pm = null;

		public ListPersistenceHandler(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) throws XMLizeException {
			List list = (List)o;
			Element result = new Element("List");
			result.setAttribute("type", o.getClass().getName());
			for (int i = 0; i < list.size(); i++) {
				result.addContent(pm.toXML(list.get(i)));
			}
			return result;
		}

		public Object fromXML(Element e) throws UnsupportedEnvironmentException, XMLizeException {
			List result = (List)newObjectByString(e.getAttributeValue("type"));
			pm.registerObject(result);
			Iterator itr = (e.getChildren()).iterator();
			while (itr.hasNext()) {
				Element child = (Element)itr.next();
				result.add(pm.fromXML(child));
			}
			return result;
		}

		public Class handles() {
			return List.class;
		}
	}

	/**
	 * @author  nunkesser
	 */
	public static class MapPersistenceHandler implements ClassPersistenceHandler {
		private PersistenceManager pm = null;

		public MapPersistenceHandler(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) throws XMLizeException {
			Map map = (Map)o;
			Element result = new Element("Map");
			result.setAttribute("type", o.getClass().getName());
			Iterator itr = map.keySet().iterator();
			while (itr.hasNext()) {
				Object key = itr.next();
				result.addContent(pm.toXML(key));
				result.addContent(pm.toXML(map.get(key)));
			}
			return result;
		}

		public Object fromXML(Element e) throws UnsupportedEnvironmentException, XMLizeException {
			Map result = (Map)newObjectByString(e.getAttributeValue("type"));
			pm.registerObject(result);
			Iterator itr = (e.getChildren()).iterator();
			while (itr.hasNext()) {
				Element key = (Element)itr.next();
				Element value = (Element)itr.next();
				result.put(pm.fromXML(key), pm.fromXML(value));
			}
			return result;
		}

		public Class handles() {
			return Map.class;
		}
	}
	
	/**
	 * @author  nunkesser
	 */
	public static class ArrayPersistenceHandler implements ClassPersistenceHandler {

		private PersistenceManager pm;

		public ArrayPersistenceHandler(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) throws XMLizeException {
			// We need to find out which objects are stored in the array.
			Object[] array = (Object[])o;
			Element entry = new Element("Array");
			entry.setAttribute("type", o.getClass().getComponentType().getName());
			// Now, the data stored in the array can be transformed into XML.
			for (int i = 0; i < array.length; i++) {
				entry.addContent(pm.toXML(array[i]));
			}
			return entry;
		}

		public Object fromXML(Element e) throws ClassNotFoundException, XMLizeException {
			List l = e.getChildren();
			Class cl = null;

			cl = Class.forName(e.getAttribute("type").getValue());

			Object array = Array.newInstance(cl, l.size());
			pm.registerObject(array);

			Iterator iter = l.iterator();
			int i = 0;
			while (iter.hasNext()) {
				Element el = (Element)iter.next();
				((Object[])array)[i] = pm.fromXML(el);
				i++;
			}

			return array;
		}

		public Class handles() {
			return null;
		}
	}

}