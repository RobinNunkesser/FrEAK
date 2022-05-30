/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.control.*;
import freak.core.event.*;
import freak.core.stoppingcriterion.*;

/**
 * This criterion stopps the algorithm randomly.
 *
 * @author Michael
 */
public class Random extends AbstractGenerationStoppingCriterion {

	private double probability = 0.0001;

	/**
	 * Creates a new object with a link to the schedule.
	 * @param schedule a reference to the schedule.
	 */
	public Random(Schedule schedule) {
		super(schedule);
	}

	public String getName() {
		return "Random";
	}

	public String getDescription() {
		return "With a specified probability it stops a run after the new generation event.";
	}

	/**
	 * This method is called when a new generation is completely created. 
	 * It stopps with probability <code>probability</code> the run.
	 */
	public void checkCriterion(GenerationEvent evt) {
		RandomElement re = getSchedule().getRandomElement();
		if (re.raw() <= probability)
			stopRun();
	}

	public void setPropertyProbabilitytoStop(Double prob) {
		if (prob.doubleValue() >= 0 && prob.doubleValue() <= 1)
			probability = prob.doubleValue();
	}

	public Double getPropertyProbabilitytoStop() {
		return new Double(probability);
	}

	public String getShortDescriptionForProbabilitytoStop() {
		return "Probability";
	}

	public String getLongDescriptionForProbabilitytoStop() {
		return "Probability to stop at every check.";
	}

}
