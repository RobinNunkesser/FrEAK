/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.modulesupport.*;
import freak.core.modulesupport.Module;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom JTable for assigning Events to matching EventSources.
 * @author  Oliver
 */
public class EventTable extends JTable {

	private class EventSourceWrapper {
		private Object obj;
		public EventSourceWrapper(Object o) {
			obj = o;
		}
		/** @see Object#toString() */
		public String toString() {
			if (obj instanceof Module)
				return ((Module)obj).getName();
			else
				return obj.toString();
		}
		/**
		 * @return
		 */
		public Object getObject() {
			return obj;
		}
	}

	/**
	 * A <code>TableCellRenderer</code> that provides the correct
	 * <code>JComboBox</code> for every event
	 * @author Oliver
	 */
	private class EventTableCellRenderer implements TableCellRenderer {
		/** @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int) */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (column == 1)
				return (Component)eventComboBoxes.get(row);
			else
				return null;
		}
	}

	private Module module;
	private EventController eventController;
	private ArrayList eventComboBoxes = new ArrayList();
	private List events;
	private Object[] eventSources;
	private EventTableCellRenderer tableRenderer = new EventTableCellRenderer();

	/**
	 * Constructs a new <code>EventTable</code>. The <code>EventTable</code> is
	 * a <code>JTable</code> which is able to let the user configure different
	 * eventsources for non-static events. Fot that it needs a <code>Module</code>,
	 * a global <code>EventController</code> and an array of possible eventsources.
	 * @param module the module
	 * @param eventController the <code>EventController</code> of the current <code>Schedule</code>
	 * @param eventSources possible eventsources the module can register itself at
	 */
	public EventTable(Module module, EventController eventController, Object[] eventSources) {
		super();
		if (module == null || eventController == null)
			throw new NullPointerException("EventPanel: constructor got null as parameter.");
		this.module = module;
		this.eventController = eventController;
		this.eventSources = eventSources;
		events = eventController.getCustomizableEventsFor(module);
		buildComboBoxes();

		if (events.size() != eventComboBoxes.size())
			throw new RuntimeException("EventTable: size of events is != size of comboBoxes.");
		DefaultTableModel tm = new DefaultTableModel(null, new Object[] { "Event", "recieving from" });
		for (int i = 0; i < events.size(); i++) {
			tm.addRow(new Object[] {((EventInfo)events.get(i)).getEventName(), "" });
		}
		setModel(tm);
		setRowHeight(20);
	}

	/** @see JTable#getCellEditor(int, int) */
	public TableCellEditor getCellEditor(int row, int column) {
		// -- no editor for event names, ComboBox for eventsource lists
		if (column == 1)
			return new DefaultCellEditor((JComboBox)eventComboBoxes.get(row));
		else
			return null;
	}

	/**
	 * @see JTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 1) {
			return tableRenderer;
		} else
			return super.getCellRenderer(row, column);
	}

	private void buildComboBoxes() {
		// -- build a new list of JComboBoxes
		eventComboBoxes.clear();
		for (int i = 0; i < events.size(); i++) {
			EventInfo evt = (EventInfo)events.get(i);
			// -- create a combobox for each event
			JComboBox cb = new JComboBox();
			eventComboBoxes.add(cb);
			// -- add matching eventsources to the combobx and make default selection
			boolean foundSelection = false;
			for (int j = 0; j < eventSources.length; j++) {
				Class eventSourceType = EventController.getEventSourceClassFor(evt.getEventType());
				if (eventSourceType.isAssignableFrom(eventSources[j].getClass())) {
					// -- we found a matching eventsource
					EventSourceWrapper eo = new EventSourceWrapper(eventSources[j]);
					cb.addItem(eo);
					if (!foundSelection && eventSources[j].equals(evt.getEventSource())) {
						// -- hey, it's even the one set as current eventsource
						cb.setSelectedItem(eo);
						foundSelection = true;
					}
				}
			}
			if (!foundSelection)
				cb.setSelectedIndex(-1);
			// -- now make the combobox respond to user interaction
			cb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					eventSourceChanged(eventComboBoxes.indexOf(e.getSource()));
				}
			});
		}
	}

	private void eventSourceChanged(int comboBoxIndex) {
		// -- fetch corresponding event
		EventInfo evt = (EventInfo)events.get(comboBoxIndex);
		// -- fetch corresponding combobox
		JComboBox cb = (JComboBox)eventComboBoxes.get(comboBoxIndex);
		// -- fetch selected eventsource
		EventSourceWrapper eo = (EventSourceWrapper)cb.getSelectedItem();
		// -- set the new eventsource		
		if (eo != null)
			evt.setEventSource((EventSource)eo.getObject());
		else
			evt.setEventSource(null);
	}

}
