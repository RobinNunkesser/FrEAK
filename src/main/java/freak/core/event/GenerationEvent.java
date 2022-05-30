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

/**
 * A GenerationEvent is sent by the Schedule after processing of the operator graph. Observers, that investigate the population on each generation should register on GenerationEvents.
 * @author  Stefan
 */
public class GenerationEvent extends Event {
	private int number;
	private GenerationEventSource source;

	public GenerationEvent(GenerationEventSource source, int number) {
		this.source = source;
		this.number = number;
	}

	/**
	 * @return  the source
	 * @uml.property  name="source"
	 */
	public GenerationEventSource getSource() {
		return source;
	}

	/**
	 * Returns the number of the generation, that was created.
	 * @uml.property  name="number"
	 */
	public int getNumber() {
		return number;
	}
}
