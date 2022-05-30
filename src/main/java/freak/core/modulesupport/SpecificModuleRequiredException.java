/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport;

/**
 * Is thrown if a module requires another specific module to work.
 * 
 * @author Dirk
 */

public class SpecificModuleRequiredException extends UnsupportedEnvironmentException {

	private Class module;

	/**
	 * Constructs a new <code>SpecificModuleRequiredException</code> without a 
	 * detail message.
	 * 
	 * @param module the class of the required module. 
	 */
	public SpecificModuleRequiredException(Class module) {
		super();
		this.module = module;
	}

	/**
	 * Constructs a new <code>SpecificModuleRequiredException</code> with the 
	 * specified detail message.
	 * 
	 * @param module the class of the required module. 
	 * @param message the detail message.
	 */
	public SpecificModuleRequiredException(Class module, String message) {
		super(message);
		this.module = module;
	}
	
	/**
	 * @return the class of the module required by the module that threw the exception.
	 */
	public Class getModuleClass() {
		return module;
	}

}
