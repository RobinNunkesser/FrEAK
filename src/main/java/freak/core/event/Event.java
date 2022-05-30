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
 * The superclass of all events in freak.
 * 
 * Freak uses the standard java event model, without sublassing the actual
 * java event classes. Its events are fired directly, no event queue is used. 
 * 
 * Each concrete Event in freak has a getSource method, that will return the
 * Object, that created the Event. The method is not in the superclass to avoid
 * casting.
 * 
 * @author Stefan
 */
public class Event {
}
