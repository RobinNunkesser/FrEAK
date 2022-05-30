/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.event;

import freak.core.control.*;

/**
 * This Event is the RunEvent that is sent by the Schedule on the beginning of each run. It contains a Batch that stores the configuration for this run.
 * @author  Stefan
 */
public class RunStartedEvent extends RunEvent {
	private Batch batch;

	public RunStartedEvent(RunEventSource source, RunIndex runIndex, Batch batch) {
		super(source, runIndex);
		this.batch = batch;
	}

	/**
	 * Returns that batch that contains the configurations for this run.
	 * @uml.property  name="batch"
	 */
	public Batch getBatch() {
		return batch;
	}
}
