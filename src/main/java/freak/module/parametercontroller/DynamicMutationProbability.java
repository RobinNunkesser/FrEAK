/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.parametercontroller;

import freak.core.event.*;
import freak.core.graph.*;
import freak.core.parametercontroller.*;
import freak.core.searchspace.*;

/**
 * This is a parameter controller which can be used to simulate a dynamic (1+1)EA. It controls a property of type <code>Double</code>, normally this will be the mutation probabilty of a standard mutation operator. The property controlled by the controller is doubled each time a new generation is created. If it reaches a value larger than 1/2 it is set back to 1/n. In fact, this is a special case of a <code>MultiplicativeCyclicRotation</code>.
 * @author  Heiko
 */
public class DynamicMutationProbability extends AbstractParameterController implements GenerationEventListener {
	
	String name = "Mutation Probabiliy";
	
	public DynamicMutationProbability(OperatorGraph opGraph) {
		super(opGraph);
		addParameter(name, Double.class);
	}
	
	/**
	 * @return  the name
	 * @uml.property  name="name"
	 */
	public String getName() {
		return "Dynamic Mutation Probability";
	}
	
	public String getDescription() {
		return "This controller can be used to simulate a dynamic (1+1)EA. " + "It controls one real valued property which is initialized with 1/n. It is doubled each time a " + "new generation is created. If the property reaches a value larger than 1/2 it is " + "set back to 1/n. (n denotes the dimension of the search space.)";
	}
	
	public void createEvents() {
		getSchedule().getEventController().addEvent(this, GenerationEvent.class, getSchedule());
	}
	
	public void generation(GenerationEvent evt) {
		double oldValue = ((Double)getParameter(name)).doubleValue();
		double newValue = 2 * oldValue;
		if (newValue > 0.5) {
			if (getSchedule().getGenotypeSearchSpace() instanceof HasDimension) {
				newValue = 1 / (double)((HasDimension)getSchedule().getGenotypeSearchSpace()).getDimension();
			} else {
				newValue = 0.001;
			}
		}
		setParameter(name, new Double(newValue));
	}
	
	/**
	 * This checks whether the only controlled Parameter is assigned.
	 */
	public void checkSyntax() throws UnassignedParameterException {
		fireExceptionIfParameterIsUnassigned(name);
		
	}
	
}
