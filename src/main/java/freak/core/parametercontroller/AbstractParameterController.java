/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.parametercontroller;

import freak.core.control.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * An abstract superclass for all <code>ParameterController</code>s.
 * @author  Kai, Stefan
 */

public abstract class AbstractParameterController extends AbstractBasicModule implements ParameterController, CompatibleWithDifferentSearchSpaces {

	// an inner class  for wrapping multiple things associated with a parameter
	/**
	 * @author  nunkesser
	 */
	private static class ParameterWrapper implements Serializable {

		public Class type; // a Class representing the type of the parameter
		public Operator operator; // the operator of which a parameter is controlled
		public String propName;

	}

	/**
	 * maps names of parameters to vectors (type,operator,getMethod,setMethod). 
	 * An access to a parameter with a given name is carried out by invoking
	 * getMethod or setMethod on the object specified by operator. 
	 * type is the Class representing the type of parameter.
	 * operator is the Operator of which a property is controlled.
	 * getMethod and setMethod are the methods invoked to read or write
	 * the controlled property.
	 */
	protected HashMap parameters;

	/**
	 * The schedule to which this ParameterController is associated.
	 */
	protected OperatorGraph opGraph;

	
	/**
	 * We cache the getter and setter methods. 
	 */
	private transient Method getterMethod = null;
	private transient Method setterMethod = null;

	/**
	 * Initializes the maps and sets the attribute schedule.
	 * 
	 * @param schedule the schedule
	 */	
	public AbstractParameterController(OperatorGraph opGraph) {
		this.opGraph = opGraph;

		// TEMPORARILY REMOVED
		//events = new HashMap();

		parameters = new HashMap();
	}

	/**
	 * Returns the schedule of the ParameterController.
	 * 
	 * @return the schedule
	 */
	public Schedule getSchedule() {
		return opGraph.getSchedule();
	}

	public void addParameter(String name, Class type) {
		// clear cache
		getterMethod = null;
		setterMethod = null;

		ParameterWrapper tmp = new ParameterWrapper();
		tmp.type = type;
		tmp.operator = null;
		tmp.propName = null;

		parameters.put(name, tmp);

	}

	public void objectRemoved(Object obj) {
		// check for associated parameters
		String[] pNames = this.getListOfParameters();

		ParameterWrapper pw;
		for (int i = 0; i < pNames.length; i++) {
			pw = (ParameterWrapper)parameters.get(pNames[i]);

			if (pw.operator == obj)
				this.removePropertyForParameter(pNames[i]);

		}
	}

	public void removePropertyForParameter(String name) {
		// clear cache
		getterMethod = null;
		setterMethod = null;

		// get ParameterWrapper for name
		ParameterWrapper pw = (ParameterWrapper)parameters.get(name);

		if (pw == null)
			throw new RuntimeException("No Parameter " + name + " in parameters map");

		// remove operator and property
		pw.operator = null;
		pw.propName = null;
	}

	/**
	 * Removes the packages from the given name of a class.
	 * 
	 * @param source the class name
	 * @return the name without the packages prefix
	 */
	protected String removePackages(String source) {
		int lastIndexOfDot = source.lastIndexOf(".");

		String tmp = source.substring(lastIndexOfDot + 1);

		return tmp;
	}

	public void setPropertyForParameter(String paraName, Operator operator, String propName) throws RuntimeException {
		// clear cache
		getterMethod = null;
		setterMethod = null;
		
		// get ParameterWrapper for paraName
		ParameterWrapper pw = (ParameterWrapper)parameters.get(paraName);

		if (pw == null)
			throw new RuntimeException("No Parameter " + paraName + " in parameters map");

		// test if there is a Property with name propName in the operator
		ArrayList l = new ArrayList(Arrays.asList(operator.getListOfPropertyNames()));

		if (!(l.contains(propName)))
			throw new RuntimeException("No Property " + propName + " in " + operator.getClass());

		// test whether the types of Property and Parameter match
		if (!PropertyManager.assignableFrom(operator.getPropertyType(propName), this.getParameterType(paraName)))
			throw new RuntimeException("type mismatch in setPropertyForParameter, para=" + this.getParameterType(paraName) + ", prop=" + operator.getPropertyType(propName));

		// set values in pw
		pw.operator = operator;
		pw.propName = propName;
	}

	public Class getParameterType(String name) throws NoSuchElementException {
		Class tmp = ((ParameterWrapper) (parameters.get(name))).type;

		if (tmp == null)
			throw new NoSuchElementException("No event with name " + name + " in events map");

		return tmp;
	}

	public String[] getListOfParameters() {
		Object[] tmp = parameters.keySet().toArray();

		// determine number of non-null Elements in Array
		int i = 0;
		while ((i < tmp.length) && (tmp[i] != null))
			i++;

		int size = i;

		String[] out = new String[size];

		for (i = 0; i < size; i++)
			out[i] = (String) (tmp[i]);

		return (out);
	}

	public Object getParameter(String name) {
		// get ParameterWrapper for name
		ParameterWrapper pw = (ParameterWrapper)parameters.get(name);
		if (pw == null)
			throw new NoSuchElementException("No pw for parameter:" + name);

		// check whether operator is assigned
		if ((pw.operator != null) && (pw.propName != null)) {

			// get operator and getter Method
			Operator operator = pw.operator;

			if (getterMethod == null) {
				getterMethod = operator.getGetterMethod(pw.propName);
			}

			// invoke Method
			try {
				return (getterMethod.invoke(operator, null));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return (null);
	}

	public void setParameter(String name, Object value) {
		// get ParameterWrapper for name
		ParameterWrapper pw = (ParameterWrapper)parameters.get(name);
		if (pw == null)
			throw new NoSuchElementException("No pw for parameter:" + name);

		// check whether operator is assigned
		if ((pw.operator != null) && (pw.propName != null)) {

			// get operator and setter Method
			Operator operator = pw.operator;

			if (setterMethod == null) {
				setterMethod = pw.operator.getSetterMethod(pw.propName);
			}

			// invoke Method
			Object[] arg = { value };

			try {
				setterMethod.invoke(operator, arg);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Operator getOperatorForParameter(String paramName) {
		// get ParameterWrapper for name
		ParameterWrapper pw = (ParameterWrapper)parameters.get(paramName);
		if (pw == null)
			throw new NoSuchElementException("No Parameter with name " + paramName + " in parameters map");
		return pw.operator;
	}

	public String getPropNameForParameter(String paramName) {
		// get ParameterWrapper for name
		ParameterWrapper pw = (ParameterWrapper)parameters.get(paramName);
		if (pw == null)
			throw new NoSuchElementException("No Parameter with name " + paramName + " in parameters map");
		return pw.propName;
	}

	public String getPropDescriptionForParameter(String paramName) {
		// get ParameterWrapper for name
		ParameterWrapper pw = (ParameterWrapper)parameters.get(paramName);
		if (pw == null)
			throw new NoSuchElementException("No Parameter with name " + paramName + " in parameters map");
		if (pw.operator != null) {
			String shortName = PropertyManager.getShortDescriptionFor(pw.operator, pw.propName);
			if (shortName != null)
				return shortName;
		}
		return pw.propName;
	}

	/**
	 * This method does nothing. The syntax check should be
	 * implemented by the concrete subclasses.
	 */
	public void checkSyntax() throws GraphSyntaxException {
	}

	/**
	 * This method fires an UnassignedParameterException if
	 * the parameter identified by the String passed is
	 * unassigned to a property.
	 * 
	 * @param parameter the parameter to be tested.
	 */
	protected void fireExceptionIfParameterIsUnassigned(String parameter) throws UnassignedParameterException {
		// get ParameterWrapper for paraName
		ParameterWrapper pw = (ParameterWrapper)parameters.get(parameter);

		if (pw == null)
			throw new RuntimeException("No Parameter " + parameter + " in parameters map");

		if (pw.operator == null)
			throw new UnassignedParameterException("Parameter " + parameter + " is unassigned in Parameter Controller " + this.getName());
	}

	public void initialize() {
	}

	public void testSchedule(Schedule schedule)
		throws UnsupportedEnvironmentException {
	}

}
