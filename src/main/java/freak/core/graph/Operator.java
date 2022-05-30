/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.graph;

import freak.core.event.*;
import freak.core.modulesupport.*;
import java.lang.reflect.*;

/**
 * A node in the operator graph where individuals are being processed. All operators must provide a configurable name for identification in the parameter controller setup.
 * @author  Dirk, Kai, Matthias
 */
public interface Operator extends Module, Configurable {

	/**
	 * Performs a syntax check and throws exceptions if the operator is not correct.
	 * 
	 * @throws GraphSyntaxException 
	 */
	void checkSyntax() throws GraphSyntaxException;

	/**
	 * Starts processing of the Operator.
	 * 
	 * @throws GraphException if an error occurs during the processing of the Operator. 
	 */
	void activate() throws GraphException;

	/**
	 * A operator has properties with getter and setter methods.
	 * By convention, the signatures of the methods for a property
	 * with name X are <code>Object getPropertyX()</code> and
	 * <code>void setPropertyX(Object value)</code>, respectively.
	 * This method returns a Method object representing the getter Method 
	 * of the given property.
	 * 
	 * @param property the name of the property
	 * @return the getter Method of the property  
	 */
	Method getGetterMethod(String property) throws RuntimeException;

	/**
	 * This method returns a Method object representing the setter Method of 
	 * the given property.
	 * 
	 * @param property the name of the property
	 * @return the setter Method of the property
	 * @throws RuntimeException when there exists no method for the given property  
	 */
	Method getSetterMethod(String property) throws RuntimeException;

	/**
	 * Returns a List of the names of all properties in the operator
	 * which have the given type.
	 * 
	 * @param type the type for filtering the list of properties 
	 * @return a String array with the names of the properties
	 * @throws RuntimeException when there exists no method for the given property
	 */
	public String[] getListOfPropertyNamesWithType(Class type);

	/**
	 * Returns a Class object representing the type of the given property.
	 * 
	 * @param key the name of the requested property
	 * @return the type of the property
	 */
	public Class getPropertyType(String key);

	/**
	 * Returns the OperatorGraph to which the Operator belongs.
	 * 
	 * @return the <code>OperatorGraph</code>.
	 */
	OperatorGraph getOperatorGraph();

	/**
	 * Returns the OutPort with a specified number. If the Operator implements
	 * HasFloatingNumberOfOutPorts and no OutPort with the specified index
	 * exists new OutPorts will be created until an OutPort with the specified
	 * index exists. 
	 * 
	 * @param index number of port to get.
	 * @return the port.
	 */
	OutPort getOutPort(int index);

	/**
	 * Returns the InPort with a specified number. If the Operator implements
	 * HasFloatingNumberOfInPorts and no InPort with the specified index
	 * exists new InPorts will be created until an InPort with the specified
	 * index exists. 
	 * 
	 * @param index number of port to get.
	 * @return the port.
	 */
	InPort getInPort(int index);

	/**
	 * Sets the value of a given property.
	 *
	 * @param key the name of the property to be set.
	 * @param property the value of the property.
	 */
	void setProperty(String key, Object property);

	/**
	 * Returns the value of a given property.
	 * 
	 * @param key the name of the property.
	 * @return the value of the property. 
	 */
	Object getProperty(String key);

	/**
	 * Returns a List of the names of all properties in the operator.
	 * 
	 * @return a String array with the names of the properties.
	 */
	String[] getListOfPropertyNames();

	/**
		 * Returns the number of outports.
		 * 
		 * @return the number of outports.
		 */
	int getNumberOfOutPorts();

	/**
	 * Returns the number of inports.
	 * 
	 * @return the number of inports.
	 */
	int getNumberOfInPorts();

	/**
	 * Returns the index of childPort. The InPorts are numbered from
	 * 0 to getNumberOfInPorts, the OutPorts starting by 
	 * getNumberOfInPorts.
	 * 
	 * @param childPort the port whose index you want.
	 * @throws java.util.NoSuchElementException if this operator doesn't 
	 * contain childPort as InPort or OutPort.
	 */
	int getIndexOfPort(Port childPort);

	/**
	 * Returns the port at index index. The InPorts are numbered from
	 * 0 to getNumberOfInPorts, the OutPorts starting by getNumberOfInPorts.
	 * 
	 * @param index get the Port with this index.
	 * @return the Port with index index.
	 */
	Port getPortAt(int index);

	/**
	 * Sets the PortsChangedEventListener of this Operator. The listener is
	 * notified every time the number of Ports of this Operator has changed.<br>
	 * 
	 * @param listener the listener to register with this operator
	 */
	public void setPortsChangedListener(PortsChangedEventListener listener);

	/**
	 * Property Name getter method.
	 * @uml.property  name="propertyName"
	 */
	public String getPropertyName();

	/**
	 * Property Name setter method.
	 * @uml.property  name="propertyName"
	 */
	public void setPropertyName(String name);

}
