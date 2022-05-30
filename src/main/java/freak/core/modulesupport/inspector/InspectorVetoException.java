/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport.inspector;

/**
 * Is thrown by Inspectors if the inspector vetoes being closed by the GUI. 
 * 
 * @author Dirk
 */
public class InspectorVetoException extends Exception {

	public InspectorVetoException() {
		super();
	}

	public InspectorVetoException(String message) {
		super(message);
	}

	public InspectorVetoException(String message, Throwable cause) {
		super(message, cause);
	}

	public InspectorVetoException(Throwable cause) {
		super(cause);
	}

}
