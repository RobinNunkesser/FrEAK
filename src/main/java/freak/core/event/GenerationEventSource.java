/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.event;

/**
 * @see freak.core.event.GenerationEvent
 * 
 * @author Stefan
 */
public interface GenerationEventSource extends EventSource {
	void addGenerationEventListener(GenerationEventListener l);
	void removeGenerationEventListener(GenerationEventListener l);
}
