/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.view;

import freak.core.modulesupport.*;
import freak.core.observer.*;
import javax.swing.*;

/**
 * A view in the model-view paradigm that displays data provided by an observer.
 * @author  Patrick, Dirk, Stefan
 */

public interface View extends Module {

	/**
	 * Returns the types of data the view is able to handle.
	 * This is used to match observers and views; a view can be added to an 
	 * observer if the view can handle the data of the observer.
	 *
	 * @return an array of class objects representing the types of data the view works with.
	 */
	public Class[] getInputDataTypes();

	/**
	 * Creates the output panel for this view.
	 * 
	 * @return the view's new output panel.
	 */
	public JPanel createPanel();

	/**
	 * Updates the view with the specified data.
	 * 
	 * @param data the data the view is to be updated with. 
	 */
	public void update(Object data);

	/**
	 * Returns an identification number that is used to match the view with its
	 * corresponding window in the GUI after the view has been deserialized.
	 * 
	 * @return the unique identification number of the view.
	 */
	public int getIdentificationNumber();

	/**
	 * Sets the observer this view is associated with. May be <code>null</code>.
	 * @param observer  the view's observer.
	 * @uml.property  name="observer"
	 */
	public void setObserver(Observer observer);

	/**
	 * Returns the observer this view is associated with or <code>null</code>
	 * if this view is not associated with an observer.
	 * 
	 * @return the view's observer or <code>null</code>. 
	 */
	public Observer getObserver();
}
