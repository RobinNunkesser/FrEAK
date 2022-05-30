/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.observer.isingmodelcliques;

import freak.core.control.Schedule;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.AbstractObserver;
import freak.core.stoppingcriterion.StoppingCriterion;
import freak.module.stoppingcriterion.IsingModelCliquesFinished;

/**
 * @author  Heiko
 */
public class CliquesIdenticallyColored extends AbstractObserver implements RunEventListener {

	private IsingModelCliquesFinished sc = null;

	public CliquesIdenticallyColored(Schedule schedule) {
		super(schedule);
		setMeasure(RUNS);
	}
		
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		StoppingCriterion[] stoppingCriteria = schedule.getStoppingCriteria();
		for (int i = 0; i < stoppingCriteria.length; i++) { 
			if (stoppingCriteria[i] instanceof IsingModelCliquesFinished) {
				sc = (IsingModelCliquesFinished)stoppingCriteria[i];
			}
		}
		if (sc == null) {
			throw new UnsupportedEnvironmentException();
		}		
	}

	public Class getOutputDataType() {
		return Boolean.class;
	}

	public String getName() {
		return "Cliques Identically Colored";
	}

	public String getDescription() {
		return "Computes whether all cliques are colored identically when the run is finished.";
	}

	public void runFinalize(RunEvent evt) {
		updateViews(new Boolean(sc.getAllCliquesColoredIdentically()));
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

}
