/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.scheduleeditor;

import freak.core.modulesupport.*;

/**
 * This is a helper-class for the ScheduleCreationDialog. It represents one node in the tree of observers and views and provides access to the displayed string as well as to the module itself.
 * @author  Oliver
 */
public class ObserverTreeElement {
	private Module module;

	public ObserverTreeElement(Module module) {
		this.module = module;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s;
		if (module != null) {
			s = module.getName();
			if (s == null) s = "<no name yet>";
		} else
			s = "<nothing>";
		return s;
	}

	public String getDescription() {
		if (module != null) return module.getDescription();
		else return "This is just a placeholder. No view has been added to this observer yet.";
	}

	/**
	 * @return  the module
	 * @uml.property  name="module"
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * @param  module
	 * @uml.property  name="module"
	 */
	public void setModule(Module module) {
		this.module = module;
	}

	/**
	 * @return an empty ObserverTreeElement for displaying an Observer with no
	 * associated view.
	 */
	public static ObserverTreeElement getNoViewTreeElement() {
		return new ObserverTreeElement(null);
	}
}
