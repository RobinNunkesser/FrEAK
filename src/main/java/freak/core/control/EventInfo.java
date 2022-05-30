/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.control;

import freak.core.event.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Contains information about an event-mapping. This class knows the textual description of an event, the type and a source of it. <p> It also distinguishes between static and non-static events: <p> A so called <i>static</i> event is an event for which the source cannot be changed during runtime, e.g. it won't be displayed in the GUI as event with variable source. <p> A <i>non-static</i> event is an event for which the eventsource cannot be known during runtime. The user has to configure the eventsource before the event ise useable. <p> An <code>EventInfo</code> object usually doesn't come alone. It is part of the EventController which stores a list of events for every module in the program.
 * @author  Oliver
 */
public class EventInfo implements Serializable {
	private EventListener owner;
	private boolean isStatic;
	private boolean isRegistered = false;
	private String eventName;
	private Class eventType;
	private EventSource eventSource;

	/**
	 * Creates a new <code>EventInfo</code> object. Checks if the module <code>owner</code>
	 * is an <code>EventListener</code> of the event given by <code>eventType</code>.
	 * If an <code>eventSource</code> is specified, it's also checked if it is an
	 * <code>EventSource</code> of the event given by <code>eventType</code>.
	 * If either the module or the source don't match, a <code>ClassCastException</code> is thrown.
	 * <p>
	 * <code>owner</code> and <code>eventType</code> must not be null.
	 * @param owner the event listener of the event
	 * @param eventName a human readable name for the event, may be null
	 * @param eventType the type of event
	 * @param eventSource the event source for this event, may be null
	 * @param isStatic flag, if the GUI is allowed to change the event source
	 */
	public EventInfo(EventListener owner, String eventName, Class eventType, EventSource eventSource, boolean isStatic) {
		if (owner == null)
			throw new NullPointerException("EventInfo: owner must not be null.");
		if (eventType == null)
			throw new NullPointerException("EventInfo: Not allowed to add an event of type null.");
		if (eventSource != null && !EventController.getEventSourceClassFor(eventType).isAssignableFrom(eventSource.getClass()))
			throw new ClassCastException("EventInfo: Object " + eventSource + " isn't a source for event " + eventType);
		if (owner != null && !EventController.getEventListenerClassFor(eventType).isAssignableFrom(owner.getClass()))
			throw new ClassCastException("EventInfo: Object " + eventSource + " isn't a listener for event " + eventType);
		this.owner = owner;
		this.eventName = eventName;
		this.eventType = eventType;
		this.eventSource = eventSource;
		this.isStatic = isStatic;
		if (this.eventSource != null)
			register();
	}

	/**
	 * @return  the owner of the event
	 * @uml.property  name="owner"
	 */
	public EventListener getOwner() {
		return owner;
	}

	/**
	 * @return  the human readable name for the event
	 * @uml.property  name="eventName"
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @return  the event source of the event
	 * @uml.property  name="eventSource"
	 */
	public EventSource getEventSource() {
		return eventSource;
	}

	/**
	 * @return  the event class of the event
	 * @uml.property  name="eventType"
	 */
	public Class getEventType() {
		return eventType;
	}

	/**
	 * @return  true, if the event is a static event
	 * @uml.property  name="isStatic"
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * @return  true, if the owning module is already added as event listener for the event to the source
	 * @uml.property  name="isRegistered"
	 */
	public boolean isRegistered() {
		return isRegistered;
	}

	/**
	 * Sets the event source for the event. This throws a <code>RunTimeException</code> if the event is a static event.
	 * @param source  the event source
	 * @uml.property  name="eventSource"
	 */
	public void setEventSource(EventSource source) {
		if (!isStatic) {
			if (source != null)
				replaceEventSource(source);
			else
				eventSource = source;
		} else
			throw (new RuntimeException("trying to set the eventsource of static event " + getEventName() + " (" + getEventType() + ")"));
	}

	/**
	 * Replaces the event source for the event by a new event source.
	 * This method differs from <code>setEventSource</code> in the way that it is
	 * allowed to replace the event source of a static event as long as the new
	 * source isn't null. Otherwise a <code>StaticEventSourceRemovedException</code> is thrown.
	 * This way it is guaranteed that a static event will never have <code>null</code>
	 * as its event source. If the owning module was already registered as
	 * event listener at the source, it gets ungeristered first, then the source
	 * is replaced and then the module gets registered again. This makes the
	 * replacement of event sources fully transparent for the user.
	 * @param newSource the new event source
	 */
	public void replaceEventSource(EventSource newSource) {
		if (isStatic && newSource == null)
			throw new StaticEventSourceRemovedException("trying to replace the eventsource of static event " + getEventName() + " (" + getEventType() + ") with a null object.");
		if (isRegistered)
			unregister();
		eventSource = newSource;
		register();
	}

	/**
	 * Registers the owning module as event listener for the event at the event
	 * source. This is done only if the module wasn't already registered for the
	 * event at the source and the source isn't null.
	 */
	public void register() {
		if (!isRegistered && eventSource != null)
			try {
				// -- create the method name
				Method mthd = EventController.getAddListenerMethodFor(this);
				// -- invoke method
				mthd.invoke(eventSource, new Object[] { owner });
				isRegistered = true;
			} catch (Exception e) {
				System.out.println("error while registering " + owner + " for event " + eventType + " at source " + eventSource);
				e.printStackTrace();
			}
	}

	/**
	 * Removes the event registration of the module for the event from the event
	 * source. This is done only if the module was previously registered for the
	 * event at the event source and the source isn't null.
	 */
	public void unregister() {
		if (isRegistered && eventSource != null)
			try {
				// -- create the method name
				Method mthd = EventController.getRemoveListenerMethodFor(this);
				// -- invoke method
				mthd.invoke(eventSource, new Object[] { owner });
				isRegistered = false;
			} catch (Exception e) {
				System.out.println("error while unregistering " + owner + " from event " + eventType + " at source " + eventSource);
				e.printStackTrace();
			}
	}
}
