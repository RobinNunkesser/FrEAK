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
 * @see freak.core.event.RunEvent
 * @author Stefan
 */
public interface RunEventSource extends EventSource {
    void addRunEventListener(RunEventListener l);
    void removeRunEventListener(RunEventListener l);
}
