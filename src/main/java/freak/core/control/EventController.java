/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.control;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import freak.core.event.*;
import freak.core.event.EventListener;

/**
 * Controlls all event-registrations of all modules that wish to be assigned
 * as <code>EventListener</code> for one or several <code>Event</code>s.
 * There should be only one <code>EventController</code> per <code>Schedule</code>.
 * 
 * @author Oliver, Matthias
 */
public class EventController implements Serializable {

	private HashMap events = new HashMap();

	/**
	 * Returns the corresponding <code>EventSource</code> class for a given
	 * <code>Event</code> class.
	 * 
	 * @param eventClass the <code>Event</code> class.
	 * @return the class of the corresponding <code>EventSource</code>.
	 */
	public static Class getEventSourceClassFor(Class eventClass) {
		try {
			return Class.forName(eventClass.getName() + "Source");
		} catch (ClassNotFoundException e) {
			System.out.println("no source interface for event " + eventClass.getName());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the corresponding <code>EventListener</code> class for a given
	 * <code>Event</code> class.
	 * 
	 * @param eventClass the <code>Event</code> class.
	 * @return the class of the corresponding <code>EventListener</code>.
	 */
	public static Class getEventListenerClassFor(Class eventClass) {
		try {
			return Class.forName(eventClass.getName() + "Listener");
		} catch (ClassNotFoundException e) {
			System.out.println("no listener interface for event " + eventClass.getName());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the addXYZListener-Method for the event XYZ given by <code>evt</code>.
	 * 
	 * @param evt the <code>EventInfo</code> object containing the event.
	 * @return the addListener method of the source of the event.
	 */
	public static Method getAddListenerMethodFor(EventInfo evt) {
		String evtClassName = evt.getEventType().getName();
		int idx = evtClassName.lastIndexOf('.');
		evtClassName = evtClassName.substring(idx + 1);
		String mthdName = "add" + evtClassName + "Listener";
		Class[] parameterTypes = new Class[] { getEventListenerClassFor(evt.getEventType())};
		try {
			return evt.getEventSource().getClass().getMethod(mthdName, parameterTypes);
		} catch (NoSuchMethodException e) {
			System.out.println("no addListener method for event " + evt.getEventType().getName() + " in object " + evt.getEventSource());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the removeXYZListener-Method for the event XYZ given by <code>evt</code>.
	 * 
	 * @param evt the <code>EventInfo</code> object containing the event.
	 * @return the removeListener method of the source of the event.
	 */
	public static Method getRemoveListenerMethodFor(EventInfo evt) {
		String evtClassName = evt.getEventType().getName();
		int idx = evtClassName.lastIndexOf('.');
		evtClassName = evtClassName.substring(idx + 1);
		String mthdName = "remove" + evtClassName + "Listener";
		Class[] parameterTypes = new Class[] { getEventListenerClassFor(evt.getEventType())};
		try {
			return evt.getEventSource().getClass().getMethod(mthdName, parameterTypes);
		} catch (NoSuchMethodException e) {
			System.out.println("no removeListener method for event " + evt.getEventType().getName() + " in object " + evt.getEventSource());
			e.printStackTrace();
			return null;
		}
	}

	// TODO Oliver methode entfernen, wenn es absolut keine Probleme mehr mit dem EventController gibt.
	/**
	 * Debug method. Prints all events to stdout.
	 */
/*
	public void listAllEvents() {
		Set modules = events.keySet();
		for (Iterator i = modules.iterator(); i.hasNext();) {
			Object m = i.next();
			if (m instanceof Module) {
				Module module = (Module)m;
				System.out.println("Object: " + module.getName());
			} else
				System.out.println("Object: " + m);
			List eventList = getEventListFor(m);
			for (Iterator j = eventList.iterator(); j.hasNext();) {
				EventInfo evt = (EventInfo)j.next();
				String descr = evt.isStatic() ? "(static)" : evt.getEventName();
				System.out.println(" - " + descr + " [" + evt.getEventType() + "] from: " + evt.getEventSource());
			}
		}
	}
*/

	/**
	 * Creates a new entry for <code>Object</code> m in the events <code>List</code>.
	 * 
	 * @param m the <code>Object</code>.
	 * @return the <code>List</code> of <code>EventInfo</code> objects for <code>m</code>.
	 */
	private List createNewEventListFor(Object m) {
		List l = new ArrayList();
		events.put(m, l);
		return l;
	}

	/**
	 * Returns a <code>List</code> of all <code>EventInfo</code> objects of
	 * Object  <code>m</code>.
	 * @param m the <code>Object</code>
	 * @return the <code>List</code> of <code>EventInfo</code>s or null if no such list found.
	 */
	public List getEventListFor(Object m) {
		return (List)events.get(m);
	}

	/**
	 * Returns a <code>List</code> of all <code>EventInfo</code> objects that
	 * represent static (unconfigurable source) events of Object  <code>m</code>.
	 * 
	 * @param m the <code>Object</code>.
	 * @return the <code>List</code> of <code>EventInfo</code>s.
	 */
	public List getStaticEventsFor(Object m) {
		List l = getEventListFor(m);
		List returnList = new ArrayList();
		if (l != null)
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				if (evt.isStatic())
					returnList.add(evt);
			}
		return returnList;
	}

	/**
	 * Returns a <code>List</code> of all <code>EventInfo</code> objects that
	 * represent non-static (configurable source) events of Object  <code>m</code>.
	 * 
	 * @param m the owning <code>Object</code>.
	 * @return the <code>List</code> of <code>EventInfo</code>s.
	 */
	public List getCustomizableEventsFor(Object m) {
		List l = getEventListFor(m);
		List returnList = new ArrayList();
		if (l != null)
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				if (!evt.isStatic())
					returnList.add(evt);
			}
		return returnList;
	}

	/**
	 * Fetches the <code>EventInfo</code> object of a non-static event with the 
	 * name <code>eventName</code> owned by <code>Object</code>.
	 * 
	 * @param Object the <code>Object</code>.
	 * @param eventName the name of the non-static event.
	 * @return the EventInfo of the event, or <code>null</code> if such an event doesn't exist.
	 */
	public EventInfo getCustomizableEventFor(Object Object, String eventName) {
		List l = getCustomizableEventsFor(Object);
		for (Iterator i = l.iterator(); i.hasNext();) {
			EventInfo evt = (EventInfo)i.next();
			if (evt.getEventName().equals(eventName))
				return evt;
		}
		return null;
	}

	public List getUnregisteredEvents() {
		List resultList = new ArrayList();
		Set entries = events.entrySet();
		for (Iterator i = entries.iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			List eventList = (List)entry.getValue();
			for (Iterator j = eventList.iterator(); j.hasNext();) {
				EventInfo evt = (EventInfo)j.next();
				if (!evt.isRegistered())
					resultList.add(evt);
			}
		}
		return resultList;
	}

	// -- most common method
	private void addEvent(EventListener m, String name, Class type, EventSource source, boolean isStatic) {
		if (type == null)
			throw new NullPointerException("Not allowed to add an event of type null.");
		if (!Event.class.isAssignableFrom(type))
			throw new ClassCastException("Event class is not an event.");
		if (source != null && !getEventSourceClassFor(type).isAssignableFrom(source.getClass()))
			throw new ClassCastException("Object " + source + " isn't a source for event " + type);
		if (m != null && !getEventListenerClassFor(type).isAssignableFrom(m.getClass()))
			throw new ClassCastException("Object " + m + " isn't a listener for event " + type);
		List l = getEventListFor(m);
		if (l == null)
			l = createNewEventListFor(m);
		else {
			// -- inhibit addition of duplicate events
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo ei = (EventInfo)i.next();
				if (ei.getEventType() == type && ei.getEventSource() == source) {
					System.out.println("!!! WARNING !!! Tried to add duplicate event for\n\tmodule (" + m + ") event (" + type + ") source (" + source + ") static:" + isStatic);
					return;
				}
			}
		}
		EventInfo evt = new EventInfo(m, name, type, source, isStatic);
		l.add(evt);
	}

	/**
	 * Adds a static event to the list of events. The event does not have a name
	 * and is initialized with a source. The event source of static events cannot
	 * be changed by the GUI.
	 * 
	 * @param m the owning <code>Object</code>.
	 * @param type the <code>Event</code> class of the event.
	 * @param source the event source this event gets.
	 */
	public void addEvent(EventListener m, Class type, EventSource source) {
		addEvent(m, "", type, source, true);
	}

	/**
	 * Adds a non-static event to the list of events. The event has a <code>name</code> and
	 * is initialized with no source.
	 * 
	 * @param m the owning <code>Object</code>.
	 * @param name the name of the event.
	 * @param type the <code>Event</code> class of the event.
	 */
	public void addEvent(EventListener m, String name, Class type) {
		addEvent(m, name, type, null, false);
	}

	/**
	 * Adds a non-static event to the list of events. The event has a <code>name</code> and
	 * is initialized with the given <code>source</code>.
	 * 
	 * @param m the owning <code>Object</code>.
	 * @param name the name of the event.
	 * @param type the <code>Event</code> class of the event.
	 */
	public void addEvent(EventListener m, String name, Class type, EventSource source) {
		addEvent(m, name, type, source, false);
	}

	/**
	 * Returns true if all events (static and non-static) of a given Object have
	 * an event source assigned.
	 * @param m the owning <code>Object</code>
	 */
	public boolean allEventsAssignedFor(Object m) {
		List l = getEventListFor(m);
		if (l != null) {
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				if (evt.getEventSource() == null)
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if all events (static and non-static) of all modules have
	 * an event source assigned.
	 */
	public boolean allEventsAssigned() {
		Collection eventLists = events.values();
		for (Iterator i = eventLists.iterator(); i.hasNext();) {
			List eventList = (List)i.next();
			for (Iterator j = eventList.iterator(); j.hasNext();) {
				EventInfo evt = (EventInfo)j.next();
				if (evt.getEventSource() == null)
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if all events (static and non-static) of a given Object are
	 * registered at their corresponding event source (and therefore have an
	 * event source assigned).
	 * 
	 * @param m the owning <code>Object</code>.
	 */
	public boolean allEventsRegisteredFor(Object m) {
		List l = getEventListFor(m);
		if (l != null) {
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				if (!evt.isRegistered())
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if all events (static and non-static) of all modules are
	 * registered at their corresponding event source (and therefore have an
	 * event source assigned).
	 */
	public boolean allEventsRegistered() {
		Collection eventLists = events.values();
		for (Iterator i = eventLists.iterator(); i.hasNext();) {
			List eventList = (List)i.next();
			for (Iterator j = eventList.iterator(); j.hasNext();) {
				EventInfo evt = (EventInfo)j.next();
				if (!evt.isRegistered())
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if Object <code>m</code> has non-static events in the
	 * <code>EventController</code>.
	 * 
	 * @param m the owning <code>Module</code>.
	 */
	public boolean hasCustomizableEvents(Object m) {
		List l = getEventListFor(m);
		if (l != null) {
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				if (!evt.isStatic())
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if Object <code>m</code> has static events in the 
	 * <code>EventController</code>.
	 * 
	 * @param m the <code>Module</code>.
	 */
	public boolean hasStaticEvents(Object m) {
		List l = getEventListFor(m);
		if (l != null) {
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				if (evt.isStatic())
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if there are objects which would like to receive Events
	 * from Object <code>m</code>.
	 * @param m the Object in question.
	 * @return true if at least one object is registered for an event from <code>m</code>.
	 */
	public boolean isEventSource(Object m) {
		Collection eventLists = events.values();
		for (Iterator i = eventLists.iterator(); i.hasNext();) {
			List eventList = (List)i.next();
			for (Iterator j = eventList.iterator(); j.hasNext();) {
				EventInfo evt = (EventInfo)j.next();
				if (evt.getEventSource() != null && evt.getEventSource() == m) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes the Object <code>m</code> as <code>EventListener</code> and
	 * <code>EventSource</code> from the <code>EventManager</code>. Furthermore
	 * the Object gets unregistered for its already registered events at other
	 * event sources.
	 * 
	 * @param m the <code>Object</code>.
	 */
	public void removeModule(Object m) {
		if (m != null) {
			// -- remove all events the Object wanted to be registered for
			unregisterEventsFor(m);
			// -- remove this Object as event source from every other event
			replaceGlobalEventSource(m, null);
			// -- remove it completely from the HashTable
			events.remove(m);
		}
	}

	/**
	 * Removes all modules which are assignable as class <code>c</code> from
	 * the <code>EventManager</code>.
	 * Invokes <code>removeModule</code> on every found Object.
	 * 
	 * @param c the <code>Class</code> which shall be removed.
	 */
	public void removeAllOfClass(Class c) {
		Set modules = new HashSet(events.keySet());
		for (Iterator i = modules.iterator(); i.hasNext();) {
			Object m = i.next();
			if (c.isAssignableFrom(m.getClass()))
				removeModule(m);
		}
	}

	/**
	 * Registers the Object wherever possible (wherever an event source is
	 * available) for all its events at the corresponding event sources.
	 * 
	 * @param m the <code>Object</code>.
	 */
	public void registerEventsFor(Object m) {
		List l = getEventListFor(m);
		if (l != null) {
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				evt.register();
			}
		}
	}

	/**
	 * Unregisters the Object wherever possible (wherever an event source is
	 * available) for all its events from the corresponding event sources.
	 * 
	 * @param m the <code>Object</code>.
	 */
	public void unregisterEventsFor(Object m) {
		List l = getEventListFor(m);
		if (l != null) {
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				evt.unregister();
			}
		}
	}

	/**
	 * Registers all modules wherever possible (wherever an event source is
	 * available) for all its events at the corresponding event sources.
	 */
	public void registerAllEvents() {
		Set modules = events.keySet();
		for (Iterator i = modules.iterator(); i.hasNext();) {
			Object m = i.next();
			registerEventsFor(m);
		}
	}

	/**
	 * Unregisters all modules wherever possible (wherever an event source is
	 * available) for all its events from the corresponding event sources.
	 */
	public void unregisterAllEvents() {
		Set modules = events.keySet();
		for (Iterator i = modules.iterator(); i.hasNext();) {
			Object m = i.next();
			unregisterEventsFor(m);
		}
	}

	/**
	 * Replaces an event source globally by another event source. The method scans
	 * all events of all modules and replaces every occurance of the event source
	 * by the new event source.
	 * 
	 * @param oldSource the old event source.
	 * @param newSource the new event source.
	 */
	public void replaceGlobalEventSource(Object oldSource, Object newSource) {
		Collection eventLists = events.values();
		for (Iterator i = eventLists.iterator(); i.hasNext();) {
			List eventList = (List)i.next();
			for (Iterator j = eventList.iterator(); j.hasNext();) {
				EventInfo evt = (EventInfo)j.next();
				if (evt.getEventSource() != null && evt.getEventSource() == oldSource)
					evt.replaceEventSource((EventSource)newSource);
			}
		}
	}
}
