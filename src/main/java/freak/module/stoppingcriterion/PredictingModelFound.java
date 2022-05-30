/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.Schedule;
import freak.core.event.GenerationEvent;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.stoppingcriterion.AbstractGenerationStoppingCriterion;
import freak.module.fitness.booleanfunction.GenericPareto;
import freak.rinterface.model.RReturns;

/**
 * 
 * @author Robin
 */
public class PredictingModelFound extends AbstractGenerationStoppingCriterion {
	
	public PredictingModelFound(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof GenericPareto)) {
			throw new UnsupportedEnvironmentException();		
		}
	}

	protected void checkCriterion(GenerationEvent evt) {
		GenericPareto gp= (GenericPareto)getSchedule().getRealFitnessFunction();
		if (gp.isPredictingModelFound()) {
			RReturns.setGenerationFound(getSchedule().getCurrentGeneration());			
			stopRun();
			gp.setPredictingModelFound(false);
		}
	}

	public String getName() {
		return "Predicting Model Found";
	}

	public String getDescription() {
		return "Stops the run if a model predicing all cases and controls is found.";
	}

}
