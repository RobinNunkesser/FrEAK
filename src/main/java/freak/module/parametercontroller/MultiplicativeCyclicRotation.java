/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.parametercontroller;

import freak.core.event.*;
import freak.core.graph.*;

/**
 * The Parameter value is set to Value * delta in each generation.
 * 
 * @author Kai
 */
public class MultiplicativeCyclicRotation extends AbstractCyclicRotation {

	/**
	 * Sets the schedule.
	 * 
	 * @param schedule
	 */
	public MultiplicativeCyclicRotation(OperatorGraph opGraph) {
		super(opGraph);
	}

	/**
	 * Value is set to Value * delta.
	 */
	public void generation(GenerationEvent evt) {

		if (current <= upper) {
			setParameter(NAME_VALUE, new Double(current));
			current *= delta;

		} else {
			current = lower;
			setParameter(NAME_VALUE, new Double(current));
			current *= delta;
		}

	}

	public String getName() {
		return ("Multiplicative Cyclic Rotation");
	}

	public String getLongDescriptionForDelta() {
		return ("The controlled properties are multiplied with Delta each generation.");
	}

}
