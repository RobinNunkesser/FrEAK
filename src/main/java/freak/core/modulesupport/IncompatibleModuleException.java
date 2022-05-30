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
 * Is thrown if a module is incompatible to another module inside the current  schedule.
 * @author  Dirk
 */

public class IncompatibleModuleException extends UnsupportedEnvironmentException {

	private Module module;

	/**
	 * Constructs a new <code>IncompatibleModuleException</code> without a 
	 * detail message.
	 */
	public IncompatibleModuleException(Module module) {
		super();
		this.module = module;
	}

	/**
	 * Constructs a new <code>IncompatibleModuleException</code> with the 
	 * specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public IncompatibleModuleException(Module module, String message) {
		super(message);
		this.module = module;
	}

	/**
	 * @return  the module that is found to be incompatible with the module that threw the exception.
	 * @uml.property  name="module"
	 */
	public Module getModule() {
		return module;
	}

}
