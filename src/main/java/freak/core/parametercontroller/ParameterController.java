/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.parametercontroller;

import java.util.*;

import freak.core.graph.*;
import freak.core.modulesupport.*;

/**
 * A parameter controller can modify the properties of operators during a run 
 * through the operator graph. It can listen to some events and modify 
 * some properties. For this reason, it contains a Map associating event 
 * or property names with a given operator, or property in a operator respectively.
 * 
 * @author Kai
 */
public interface ParameterController extends Module {

	/**
	 * Adds a Vector (type,null,null,null) to the map, associated with name.
	 * This method should be called once in the constructor for every 
	 * parameter the parameter controller can control.
	 * 
	 * @param name the name of the Parameter added
	 * @param type a Class object representing the type of the Parameter. This is used for finding properties in operators of the right type.
	 */
	public abstract void addParameter(String name, Class type);

	/**
	 * This method checks if there are any events or parameters
	 * associated with the given object. If there is an event
	 * associated to the object, the ParameterController calls
	 * removeEventListener(this) on the object, removing it as
	 * an event listener. Then the object is removed from the
	 * EventWrapper.
	 * If there is a Parameter associated to the object, it is removed
	 * from the ParameterWrapper.
	 * 
	 * @param obj the object which was removed from the graph/schedule/etc.
	 */
	public abstract void objectRemoved(Object obj);

	/**
	 * Sets the controlled operator and its property for the given
	 * parameter name to null.
	 * 
	 * @param name the name of the Property
	 */
	public abstract void removePropertyForParameter(String name);

	/**
	 * Associates the property of name propName in the Operator specified 
	 * by operator to the parameter specified by paraName.
	 * This method modifies the triplet (type,*,*,*) associated to paraName to (type,operator,getMethod,setMethod)
	 * after looking up the correct getter and setter methods for given property name
	 * 
	 * @param paraName the name of the parameter whose value controls the property
	 * @param operator the operator which should be controlled
	 * @param the property which should be controlled
	 * @throws RuntimeException when there is no Property with the given name in the given operator
	 * @throws RuntimeException when the types of the Parameter and the Property are not equal
	 * 
	 */
	public abstract void setPropertyForParameter(String paraName, Operator operator, String propName) throws RuntimeException;

	/**
	 * Returns the Class object representing the type of Parameter with the given name.
	 * 
	 * @param name the name of the parameter
	 * @return the type of the parameter 
	 */
	public abstract Class getParameterType(String name) throws NoSuchElementException;

	/**
	 * Returns an array of Strings containing the names of all parameters
	 * in the parameters map. 
	 */
	public abstract String[] getListOfParameters();

	/**
	 * Returns the value of the given parameter. This means
	 * that the value is retrieved by invoking the correct
	 * getter method of the property and operator associated to the parameter.
	 * 
	 * @param name the name of the parameter
	 * @return the name of the associated property or null if the parameter is not
	 * associated with any property
	 * @throws NoSuchElementException if no parameter with the given name exists 
	 */
	public abstract Object getParameter(String name);

	/**
	 * Sets the value of the given parameter to value. This means
	 * that the value is set by invoking the correct
	 * setter method of the property and operator associated to the parameter.
	 * 
	 * @param name the name of the parameter.
	 * @param value the value of the parameter
	 * @throws NoSuchElementException if no parameter with the given name exists
	 */
	public abstract void setParameter(String name, Object value);

	/**
	 * Returns the <code>Operator</code> that is associated with a given parameter.
	 * @param paramName the name of the parameter
	 * @return the associated <code>Operator</code>
	 */
	public abstract Operator getOperatorForParameter(String paramName);

	/**
	 * Returns the propertyname of the property that is associated with the given
	 * parameter.
	 * @param paramName the name of the parameter
	 * @return the name of the associated property
	 */
	public abstract String getPropNameForParameter(String paramName);

	/**
	 * Returns the short description of the property that is associated with the
	 * given parameter.
	 * @param paramName the name of the parameter
	 * @return the short description of the associated property
	 */
	public abstract String getPropDescriptionForParameter(String paramName);

	/**
	 * Performs a syntax check and throws exceptions if the
	 * <code>ParameterController</code> is not correct.
	 * 
	 * @throws GraphSyntaxException 
	 */
	public void checkSyntax() throws GraphSyntaxException;

}