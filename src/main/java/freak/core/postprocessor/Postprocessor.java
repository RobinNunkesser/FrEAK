/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.core.postprocessor;

import freak.core.control.Schedule;
import freak.core.modulesupport.*;
import freak.core.population.IndividualList;

/**
 */

public interface Postprocessor extends Module {

	public void addAllIndividuals(Schedule schedule,IndividualList list);

	public IndividualList getIndividuals();

	public void analyse();

	public void reset();

}
