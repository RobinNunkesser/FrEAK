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
import freak.core.searchspace.*;
import freak.core.stoppingcriterion.*;

/**
 * A multiplicative restart strategy:
 * IN -> IN is defined by basecount in IN, multiplier in IR with multiplier > 1
 * and is given by
 * restart(runnumber) := basecount * multiplier^(runnumber-1).
 * <br><br>
 * Thomas Jansen: On the Analysis of Dynamic Restart Strategies for Evolutionary
 * Algorithms. In J. J. Merelo Guervos, P. Adamidis, H.-G. Beyer, J.-L.
 * Fernandez-Villacanas, H.-P. Schwefel (Eds.): Parallel Problem Solving From
 * Nature (PPSN VII), Springer. 33-43, 2002.
 *
 * @author Michael
 */
public class MultiplicativeGenerationCount extends AbstractStoppingCriterion implements Configurable, GenerationEventListener {
	
	private int basecount = 1000;
	private double multiplier;
	
	/**
	 * Creates a new object with a link to the schedule.
	 * @param schedule
	 */
	public MultiplicativeGenerationCount(Schedule schedule) {
		super(schedule);
		if (schedule.getGenotypeSearchSpace() instanceof HasDimension) {
			int dim = ((HasDimension)schedule.getGenotypeSearchSpace()).getDimension();
			basecount = (int)Math.round(dim * (Math.log(dim) / Math.log(2)));
		}
		multiplier = 2;
	}
	
	public String getName() {
		return "Multiplicative Generation Count";
	}
	
	public String getDescription() {
		return "Stops a run after \"basecount * multiplier^(runnumber-1)\" number of generations.";
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
	
	public void setPropertyMultiplier(Double count) {
		if (count.doubleValue() > 1) {
			this.multiplier = count.doubleValue();
		}
	}
	
	public Double getPropertyMultiplier() {
		return new Double(multiplier);
	}
	
	public String getShortDescriptionForMultiplier() {
		return "Multiplier";
	}
	
	public String getLongDescriptionForMultiplier() {
		return "Number of generations = (Number of generations of the run before) * multiplier";
	}
	
	/**
	 * This method is called when a new generation is completely created.
	 */
	public void generation(GenerationEvent evt) {
		if (evt.getNumber() >= basecount * Math.pow(multiplier, getSchedule().getCurrentRun() - 1)) {
			stopRun();
		}
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
	}
}
