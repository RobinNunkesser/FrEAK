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

/**
 * Is thrown if a module is incompatible to the property of another module  inside the current schedule.
 * @author  Dirk
 */

public class IncompatibleModulePropertyException extends IncompatibleModuleException {

	private String property;
	
	/**
	 * Constructs a new <code>IncompatibleModuleException</code> without a 
	 * detail message.
	 */
	public IncompatibleModulePropertyException(Module module, String property) {
		super(module);
		this.property = property;
	}

	/**
	 * Constructs a new <code>IncompatibleModuleException</code> with the 
	 * specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public IncompatibleModulePropertyException(Module module, String property, String message) {
		super(module, message);
		this.property = property;
	}

	/**
	 * @return  the property inside the module that caused the exception.
	 * @uml.property  name="property"
	 */
	public String getProperty() {
		return property;
	}

}
