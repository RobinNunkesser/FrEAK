/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.modulesupport.*;
import freak.core.stoppingcriterion.*;
import freak.core.searchspace.*;

/**
 * An additive restart strategy:
 * IN -> IN is defined by basecount, addcount in IN and is given by
 * restart(runnumber) := basecount + (runnumber-1) * addcount.
 * <br><br>
 * Thomas Jansen: On the Analysis of Dynamic Restart Strategies for Evolutionary
 * Algorithms. In J. J. Merelo Guervos, P. Adamidis, H.-G. Beyer, J.-L.
 * Fernandez-Villacanas, H.-P. Schwefel (Eds.): Parallel Problem Solving From
 * Nature (PPSN VII), Springer. 33-43, 2002.
 *
 * @author Michael
 */
public class AdditiveGenerationCount extends AbstractStoppingCriterion implements Configurable, GenerationEventListener {
	
	private int basecount = 5000;
	private int add;
	
	/**
	 * Creates a new object with a link to the schedule.
	 * @param schedule
	 */
	public AdditiveGenerationCount(Schedule schedule) {
		super(schedule);
		if (schedule.getGenotypeSearchSpace() instanceof HasDimension) {
			int dim = ((HasDimension)schedule.getGenotypeSearchSpace()).getDimension();
			basecount = (int)Math.round(dim * (Math.log(dim) / Math.log(2)));
		}
		add = basecount;
	}
	
	public String getName() {
		return "Additive Generation Count";
	}
	
	public String getDescription() {
		return "Stops a run after \"basecount + (runnumber - 1) * addcount\" number of generations.";
	}
	
	public void setPropertyBaseCount(Integer count) {
		if (count.intValue() > 0) {
			this.basecount = count.intValue();
		}
	}
	
	public Integer getPropertyBaseCount() {
		return new Integer(basecount);
	}
	
	public String getShortDescriptionForBaseCount() {
		return "Base count";
	}
	
	public String getLongDescriptionForBaseCount() {
		return "The base number of generations after a run will be stopped. The exact number of generations after the first run will be stopped.";
	}
	
	public void setPropertyAddCount(Integer count) {
		this.add = count.intValue();
	}
	
	public Integer getPropertyAddCount() {
		return new Integer(add);
	}
	
	public String getShortDescriptionForAddCount() {
		return "Add count";
	}
	
	public String getLongDescriptionForAddCount() {
		return "Each next run has this addition count of generations over than the run before.";
	}
	
	/**
	 * This method is called when a new generation is completely created.
	 */
	public void generation(GenerationEvent evt) {
		if (evt.getNumber() >= basecount + (getSchedule().getCurrentRun() - 1) * add) {
			stopRun();
		}
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
	}
}
