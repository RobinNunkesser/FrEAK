/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.view.plotter;

/**
 * Is thrown by the plotter if the x value decreases within the same run/batch.
 * 
 * @author Dirk
 */
public class DecreasingXValueException extends RuntimeException {

	public DecreasingXValueException() {
		super();
	}

	public DecreasingXValueException(String message) {
		super(message);
	}

}
