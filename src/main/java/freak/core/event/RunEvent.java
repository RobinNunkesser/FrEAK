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
 * A RunEvent is sent by the Schedule, every time a run is started or finished.
 * @author  Stefan
 */
public class RunEvent extends Event {
	private RunEventSource source;
	private RunIndex runIndex;

	public RunEvent(RunEventSource source, RunIndex runIndex) {
		this.source = source;
		this.runIndex = runIndex;
	}

	/**
	 * @return  the source
	 * @uml.property  name="source"
	 */
	public RunEventSource getSource() {
		return source;
	}

	/**
	 * Returns the number of the associated run.
	 * @uml.property  name="runIndex"
	 */
	public RunIndex getRunIndex() {
		return runIndex;
	}
}
