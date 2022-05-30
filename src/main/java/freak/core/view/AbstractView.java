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

import freak.core.control.*;
import freak.core.modulesupport.*;
import freak.core.observer.*;

/**
 * An abstract superclass for all views. 
 * @author  Dirk, Stefan
 */
public abstract class AbstractView extends AbstractModule implements View {
	private Observer observer;

	/**
	 * A unique identification number.
	 */
	private int identificationNumber;

	/**
	 * Counts the number of updates.
	 */
	private int updateCounter;


	public AbstractView(Schedule schedule) {
		super(schedule);

		identificationNumber = schedule.getObserverManager().getIdentificationNumber();
		updateCounter = 0;
	}

	/**
	 * @return  the identificationNumber
	 * @uml.property  name="identificationNumber"
	 */
	public int getIdentificationNumber() {
		return identificationNumber;
	}

	/**
	 * @return  the observer
	 * @uml.property  name="observer"
	 */
	public Observer getObserver() {
		return observer;
	}

	/**
	 * @param observer  the observer to set
	 * @uml.property  name="observer"
	 */
	public void setObserver(Observer observer) {
		this.observer = observer;
	}

	/**
	 * Returns the current value (current run/current generation, etc.) from the 
	 * view's observer. The current value can be used as a time index, its 
	 * measure is specified by the observer.
	 * 
	 * @throws ObserverUnknownException if the view's observer is <code>null</code>.
	 */
	protected int getCurrentValue() throws ObserverUnknownException {
		if (getObserver() == null)
			throw new ObserverUnknownException("Observer for " + this +" is unknown.");

		switch (getObserver().getMeasure()) {
			case Observer.GENERATIONS :
				return getSchedule().getCurrentGeneration();
			case Observer.RUNS :
				return getSchedule().getCurrentTimeIndex().run;
			case Observer.BATCHES : 
				return getSchedule().getCurrentTimeIndex().batch;
			default :
				return updateCounter;
		}
	}

	public void update(Object data) {
		updateCounter++;
	}
}
