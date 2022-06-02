/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */
package freak.gui.runframe;

import freak.core.view.swingsupport.*;

import javax.swing.*;
import java.beans.PropertyChangeEvent;

/**
 * This is a very special model designed only for the three spinners in the info panel. Although it inherits from FreakSwingModel ist has a non-transient link to a swing component, because this makes life a bit easier and is no problem as it is not part of a schedule. The whole purpose is to seperate the change events on spinners into clicks to the buttons, typings into the text field and changes from the program to treat the semantically different. The flush and update mechanism is a little bit strange with its three boolean variables. This is caused by the very strange event behaviour of JSpinners. The idea is to call each of the listener methods exactly once.
 * @author  Stefan
 */
class InfoPanelSpinnerModel extends SpinnerNumberModel implements FreakSwingModel {
	private InfoPanelSpinnerModelListener listener;
	private int simulationValue;
	
	private boolean flushing;
	private boolean textChanging;
	private boolean textReset;
	
	public void setView(JSpinner spinner) {
		spinner.setModel(this);
		spinner.setEditor(new InfoPanelSpinnerEditor(spinner));
	}
	
	public void setValue(Object value) {
		if (flushing || textReset) return;
		
		int oldValue = getNumber().intValue();
		int newValue = ((Integer) value).intValue();
		
		if (textChanging) {
			listener.absoluteChangeRequest(newValue);
			
			textReset = true;
			fireStateChanged();
			textReset = false;
		} else {
			listener.relativeChangeRequest(newValue - oldValue);
		}
	}

	/**
	 * @param simulationValue  the simulationValue to set
	 * @uml.property  name="simulationValue"
	 */
	public void setSimulationValue(int simulationValue) {
		this.simulationValue = simulationValue;
	}
	
	public void flush() {
		flushing = true;
		super.setValue(new Integer(simulationValue));
		flushing = false;
	}
	
	/**
	 * @param listener  the listener to set
	 * @uml.property  name="listener"
	 */
	public void setListener(InfoPanelSpinnerModelListener listener) {
		this.listener = listener;
	}
	
	class InfoPanelSpinnerEditor extends JSpinner.NumberEditor {
		public InfoPanelSpinnerEditor(JSpinner spinner) {
			super(spinner);
		}
		
		public void propertyChange(PropertyChangeEvent e) {
			if ((e.getSource() instanceof JFormattedTextField) && (e.getPropertyName().equals("value"))) {
				textChanging = true;
				super.propertyChange(e);
				textChanging = false;
			} else {
				super.propertyChange(e);
			}
		}
	}
	
	static interface InfoPanelSpinnerModelListener {
		public void absoluteChangeRequest(int newValue);
		public void relativeChangeRequest(int change);
	}
}
