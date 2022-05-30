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
 * SuperInterface of all EventSources in Freak. Specific EventSources will
 * provide two methods to register and deregister for Events.
 * 
 * These methods are not part of the Interface EventSource, because some classes
 * may be EventSources for different types of Events. These classes will have to
 * provide one Method for each event type with different signatures.
 * 
 * @author Stefan
 */
public interface EventSource {
}
