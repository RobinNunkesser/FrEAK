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

import freak.core.modulesupport.inspector.Inspector;

/**
 * This interface contains two methods which are used for displaying and editing the state of modules in the GUI.
 * @author  Kai, Stefan
 */
public interface Configurable extends Module {

	/**
	 * Returns an inspector for editing properties of the Object.
	 * 
	 * @return the inspector.
	 */
	public Inspector getInspector();

	/**
	 * Returns the current configuration of the object implementing this
	 * interface.
	 */
	public Configuration getConfiguration();

	/**
	 * Sets the configuration to a previous state, acquired by getConfiguration.
	 * @uml.property  name="configuration"
	 */
	public void setConfiguration(Configuration config);
	
	/**
	 * Tests if all properties are set correctly.
	 * This method is intended to be used by modules with a rather complex
	 * property structure only where there are dependencies among the properties.
	 * 
	 * If an exception is thrown, the GUI will wait for the user to correct
	 * the invalid properties before doing something else.
	 * So, when only one property is involved, it is better for the module
	 * to just reset the property to either the old value or a corrected one
	 * rather than to throw an exception.  
	 * 
	 * @throws InvalidPropertyException if there are invalid properties that must be corrected by the user immediately.
	 */
	public void testProperties() throws InvalidPropertyException;

}
