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
import freak.core.modulesupport.*;
import freak.core.parametercontroller.*;

/**
 * A Parameter Controller which rotates one double parameter with name Value
 * cyclic between a lower and upper bound.
 * 
 * @author Kai, Stefan
 */
public abstract class AbstractCyclicRotation extends AbstractParameterController implements GenerationEventListener, RunEventListener, Configurable {

	/**
	 * the name of the controlled parameter
	 */
	protected static final String NAME_VALUE = "Value";

	/**
	 * the lower bound
	 */
	protected double lower;

	/**
	 * the upper bound
	 */
	protected double upper;

	/**
	 * the current value of the parameter
	 */
	protected double current;

	/**
	 * The value which determines how current increases in each step.
	 * This depends on the implementation of the method generation().
	 */
	protected double delta;

	/**
	 * Standard constructor which only sets up the received events and
	 * handled parameter.
	 *
	 */
	public AbstractCyclicRotation(OperatorGraph opGraph) {
		super(opGraph);

		// parameters
		addParameter(NAME_VALUE, Double.class);

	}

	/**
	 * Property delta getter metod.
	 */
	public Double getPropertyDelta() {
		return new Double(delta);
	}

	/**
	 * Property delta setter method.
	 */
	public void setPropertyDelta(Double d) {
		delta = d.doubleValue();
	}

	/**
	 * Property lower getter metod.
	 */
	public Double getPropertyLower() {
		return new Double(lower);
	}

	/**
	 * Property lower setter method.
	 */
	public void setPropertyLower(Double l) {
		lower = l.doubleValue();
	}

	/**
	 * Short description for property lower.
	 */
	public String getShortDescriptionForLower() {
		return ("Lower bound");
	}

	/**
	 * Long description for property lower.
	 */
	public String getLongDescriptionForLower() {
		return ("The lower bound for the value of the controlled properties.");
	}

	/**
	 * Property upper getter metod.
	 */
	public Double getPropertyUpper() {
		return new Double(upper);
	}

	/**
	 * Property upper setter method.
	 */
	public void setPropertyUpper(Double u) {
		upper = u.doubleValue();
	}

	/**
	 * Short description for property upper.
	 */
	public String getShortDescriptionForUpper() {
		return ("Upper bound");
	}

	/**
	 * Long description for property upper.
	 */
	public String getLongDescriptionForUpper() {
		return ("The upper bound for the value of the controlled properties.");
	}

	public String getName() {
		return ("Cyclic Rotation");
	}

	public String getDescription() {
		return ("Rotates the values of the controlled properties cyclically inside an interval specified by a lower and an upper bound.");
	}

	/**
	 * this method handles the GenerationEvent and rotates the parameter.
	 * 
	 * @param evt the Generation event. This parameter is unused.
	 */
	public abstract void generation(GenerationEvent evt);

	/**
	 * Resets the ParameterController for the next run.
	 * 
	 * @param evt this parameter is ignored
	 */
	public void runStarted(RunEvent evt) {
		current = lower;
	}

	public void createEvents() {
		getSchedule().getEventController().addEvent(this, GenerationEvent.class, getSchedule());
		getSchedule().getEventController().addEvent(this, RunEvent.class, getSchedule());
	}
}
