/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport;

import java.lang.reflect.*;

/**
 * This class provides methods for retrieving the names of properties
 * and their getter and setter methods, and for getting and setting such
 * properties in objects.
 *
 * By convention, for a proprerty of name X to be included in the
 * list of accessable properties, there must be getter and setter methods
 * with signatures<br>
 *
 * type getPropertyX()<br>
 * void setPropertyY(type)<br>
 *
 * with type being the Java Wrapper class for primitive data types.
 *
 * @author Kai, Michael
 */
public class PropertyManager {
	
	/**
	 * Returns a List of the names of all properties in the given object.
	 *
	 * @param obj the object of which the property names should be extracted.
	 * @return a String array with the names of the properties
	 */
	public static String[] getListOfPropertyNames(Object obj) {
		Method[] allMethods = obj.getClass().getMethods();
		String name;
		int propCount = 0;
		
		// determine number of properties by finding their getter methods
		for (int i = 0; i < allMethods.length; i++) {
			name = allMethods[i].getName();
			
			// check if method is getter method for property. IMPORTANT: only property names of length > 0 and not "Type" (method in abstract operator) to be accepted.
			if ((name.length() > 11) && name.substring(0, 11).equals("getProperty") && (!name.substring(11).equals("Type"))) {
				propCount++;
				
			}
		}
		
		// now fill Array with names of Properties
		String[] out = new String[propCount];
		int index = 0;
		
		for (int i = 0; i < allMethods.length; i++) {
			name = allMethods[i].getName();
			if ((name.length() > 11) && name.substring(0, 11).equals("getProperty") && (!name.substring(11).equals("Type"))) {
				out[index++] = name.substring(11);
				
			}
		}
		
		return (out);
	}
	
	/**
	 * Returns a List of the names of all properties in the operator
	 * which have the given type.
	 *
	 * @param obj the object of which the property names should be extracted
	 * @param type the type for filtering the list of properties
	 * @return a String array with the names of the properties
	 */
	public static String[] getListOfPropertyNamesWithType(Object obj, Class type) {
		
		String[] allNames = getListOfPropertyNames(obj);
		
		// count matching ones
		int count = 0;
		
		for (int i = 0; i < allNames.length; i++)
			if (assignableFrom(getPropertyType(obj, allNames[i]), type))
				count++;
		
		// construct output
		String[] out = new String[count];
		int j = 0;
		
		for (int i = 0; i < allNames.length; i++)
			if (assignableFrom(getPropertyType(obj, allNames[i]), type))
				out[j++] = allNames[i];
		
		return (out);
		
	}
	
	/**
	 * Determines whether an object has any properties or not.
	 *
	 * @param obj the <code>Object</code>
	 * @return true if it has at least one property
	 */
	public static boolean hasProperties(Object obj) {
		return getListOfPropertyNames(obj).length > 0;
	}
	
	/**
	 * Sets the value of a given property.
	 *
	 * @param obj the object of which the property should be set
	 * @param key the name of the property to be set
	 * @param property the value of the property
	 */
	public static void setProperty(Object obj, String key, Object property) {
		try {
			
			Method m = getSetterMethod(obj, key);
			
			// invoke setter method
			Object[] args = { property };
			m.invoke(obj, args);
		} catch (Exception e) {
			System.out.println("key=" + key + "; propType=" + property.getClass());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the value of a given property.
	 *
	 * @param obj the object of which the property should be read
	 * @param key the name of the property
	 * @return the value of the property
	 */
	public static Object getProperty(Object obj, String key) throws UnsupportedOperationException {
		Object ret = null;
		
		try {
			Method m = getGetterMethod(obj, key);
			
			// invoke getter method
			Object[] args = {
			};
			ret = m.invoke(obj, args);
		} catch (Exception e) {
			int level = 3;
			// Exception to hide the property "Name" in Initialization
			if (obj instanceof freak.core.graph.Initialization)
				level = 5;
			freak.Freak.debug("Exception in PropertyManager.getProperty: object="+ obj + ", key=" + key, level);
			throw new UnsupportedOperationException();
		}
		
		return ret;
	}
	
	/**
	 * Returns a Class object representing the type of the given property.
	 *
	 * @param obj the object of which the propertie's type should be determined
	 * @param key the name of the requested property
	 * @return the type of the property
	 */
	// TODO Kai hat das bugs eingefuehrt?
	public static Class getPropertyType(Object obj, String key) {
		
		Class objClass = obj.getClass();
		Class[] abstractArgs = {
		};
		
		Method m = null;
		
		try {
			m = objClass.getMethod("getProperty" + key, abstractArgs);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		}
		
		Class returnType = m.getReturnType();
		return (returnType);
		
		// alte version:
		// Object value = getProperty(obj, key);
		// return(value.getClass());
		
	}
	
	/**
	 * Returns the Short Description for the given property in the given object as
	 * returned by the getShortDescriptionForX() method for Property with
	 * name X.
	 *
	 * @param obj the object whose property description should be returned.
	 * @param property the name of the property for which to return the description.
	 * @return the description of the property in the object
	 */
	public static String getShortDescriptionFor(Object obj, String property) {
		Method m = getGetShortDescriptionForMethod(obj, property);
		String s = null;
		
		if (m != null) {
			
			try {
				Object[] args = {
				};
				s = (String)m.invoke(obj, args);
				
			} catch (Exception e) {
				throw new RuntimeException(e);
				
			}
			
		} else {
			
			s = property;
		}
		
		return s;
		
	}
	
	/**
	 * Returns the Long Description for the given property in the given object as
	 * returned by the getLongDescriptionForX() method for Property with
	 * name X.
	 *
	 * @param obj the object whose property description should be returned.
	 * @param property the name of the property for which to return the description.
	 * @return the description of the property in the object
	 */
	public static String getLongDescriptionFor(Object obj, String property) {
		Method m = getGetLongDescriptionForMethod(obj, property);
		String s = null;
		
		if (m != null) {
			
			try {
				Object[] args = {
				};
				s = (String)m.invoke(obj, args);
				
			} catch (Exception e) {
				throw new RuntimeException(e);
				
			}
			
		} else {
			
			s = "";
		}
		
		return s;
		
	}
	
	/**
	 * Returns the getShortDescriptionFor method for the given
	 * object and its property.
	 *
	 *
	 * @param obj the object for which to construct the method for.
	 * @param property the name of the property.
	 * @return the getShortDescriptionFor method or null if there is no such method.
	 */
	public static Method getGetShortDescriptionForMethod(Object obj, String property) {
		String methodName = "getShortDescriptionFor" + property;
		Method m = null;
		
		try {
			m = obj.getClass().getMethod(methodName, new Class[] {
			});
			
		} catch (NoSuchMethodException e) {
			m = null;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return m;
		
	}
	
	/**
	 * Returns the getLongDescriptionFor method for the given
	 * object and its property.
	 *
	 *
	 * @param obj the object for which to construct the method for.
	 * @param property the name of the property.
	 * @return the getLongDescriptionFor method or null if there is no such method.
	 */
	public static Method getGetLongDescriptionForMethod(Object obj, String property) {
		String methodName = "getLongDescriptionFor" + property;
		Method m = null;
		
		try {
			m = obj.getClass().getMethod(methodName, new Class[] {
			});
			
		} catch (NoSuchMethodException e) {
			m = null;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return m;
		
	}
	
	/**
	 * Returns the getter method for the given property name in the given object.
	 *
	 * @param obj the object of which the method should be returned
	 * @param property the name of the property
	 * @return the getter method for the property in the object
	 * @throws NoSuchMethodException
	 */
	public static Method getGetterMethod(Object obj, String property) throws NoSuchMethodException {
		String methodName = "getProperty" + property;
		return obj.getClass().getMethod(methodName, new Class[] {
		});
	}
	
	/**
	 * Returns the setter method for the given property name in the given object.
	 *
	 * @param obj the object of which the method should be returned
	 * @param property the name of the property
	 * @return the setter method for the property in the object
	 * @throws NoSuchMethodException
	 */
	public static Method getSetterMethod(Object obj, String property) throws NoSuchMethodException {
		String methodName = "setProperty" + property;
		Class propType = PropertyManager.getPropertyType(obj, property);
		
		return obj.getClass().getMethod(methodName, new Class[] { propType });
	}
	
	/**
	 * Returns true iff the first class is assignable from the second class.
	 *
	 * @param c1 the first class
	 * @param c2 the second class
	 * @return true iff the classes match
	 *
	 * @see java.lang.Class#isAssignableFrom(java.lang.Class)
	 */
	public static boolean assignableFrom(Class c1, Class c2) {
		
		return (c1.isAssignableFrom(c2));
		
	}
	
	/**
	 * Returns true iff the classes of the given objects are the same.
	 *
	 * @param o1 the first object
	 * @param o2 the seconf object
	 * @return true iff the classes of the objects match
	 */
	public static boolean classesOfObjectsMatch(Object o1, Object o2) {
		
		return (assignableFrom(o1.getClass(), o2.getClass()));
		
	}
	
}
