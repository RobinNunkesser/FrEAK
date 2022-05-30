/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.split;

import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.population.*;

/**
 * Choice routes all individuals either to the first or to the second outport,
 * depending on an adjustable probability.
 * 
 * @author Stefan
 */
public class ChoiceGeneration extends Split implements Configurable, CompatibleWithDifferentSearchSpaces {
	private double probability = 0.5;

	public ChoiceGeneration(OperatorGraph graph) {
		super(graph);
		addOutPort();
		addOutPort();
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		IndividualList empty = new Population(getOperatorGraph().getSchedule());
		
		int generation = getOperatorGraph().getSchedule().getCurrentGeneration();
		
		if (generation==2) {
			return new IndividualList[] { input[0], empty };
		} else {
			return new IndividualList[] { empty, input[0] };
		}
	}

	public Double getPropertyProbability() {
		return new Double(probability);
	}

	public void setPropertyProbability(Double probability) {
		if ((probability.doubleValue() > 1) || (probability.doubleValue() < 0)) {
			return;
		}
		this.probability = probability.doubleValue();
	}

	public String getLongDescriptionForProbability() {
		return "Will be ignored.";
	}

	public String getDescription() {
		return "Choice routes all individuals either to the first or to the second outport, depending on the generation.If generation=1 then the individuals will be routed to the first port.";
	}

	public String getName() {
		return "ChoiceGeneration";
	}

}
