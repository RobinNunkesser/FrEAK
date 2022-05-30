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
 * An object of this class contains information about a module which can be used by the GUI for displaying a list of available modules etc.  
 * @author  Heiko
 */
public class ModuleInfo implements Comparable {

	private String name;
	private String description;
	private String className;

	public ModuleInfo(String name, String description, String className) {
		this.name = name;
		this.description = description;
		this.className = className;
	}

	/**
	 * @return  the name
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return  the description
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return  the className
	 * @uml.property  name="className"
	 */
	public String getClassName() {
		return className;
	}

	public String toString() {
		return name;
	}

	public String toStringExt() {
		String s = "(" + className + "," + name + "," + description + ")";
		return s;
	}

	/**
	 * @param name  the name to set
	 * @uml.property  name="name"
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description  the description to set
	 * @uml.property  name="description"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param className  the className to set
	 * @uml.property  name="className"
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	public int compareTo(Object o) {
		return name.compareTo(((ModuleInfo)o).name);
	}

}
