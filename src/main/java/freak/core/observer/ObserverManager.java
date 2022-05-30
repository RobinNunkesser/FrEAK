/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.observer;

import java.io.*;
import java.util.*;

/**
 * This class provides the basic means for managing the set of  existing Observers.
 * @author  Patrick, Dirk
 */

public class ObserverManager implements Serializable {

	/**
	 * A list of observers chosen by the user. 
	 */
	private LinkedList activeObservers;

	/**
	 * Every view asks the ObserverManager for a unique identification number
	 * when it's created. viewIdent contains the last identification number
	 * that has been used.
	 */
	private int viewIdent;

	public ObserverManager() {
		activeObservers = new LinkedList();
		viewIdent = 0;
	}

	/**
	 * Adds the given observer to the list of active Observers.
	 */
	public void addObserver(Observer obs) {
		activeObservers.add(obs);
	}

	/**
	 * Removes the Observer on which a reference is passed from the list of
	 * active Observers.
	 */
	public void removeObserver(Observer obs) {
		activeObservers.remove(obs);
	}

	/**
	 * Returns a unique identification number for views.
	 */
	public int getIdentificationNumber() {
		viewIdent++;
		return viewIdent;
	}

	/**
	 * Returns a list of all active observers.
	 * @uml.property  name="activeObservers"
	 */
	public List getActiveObservers() {
		return Collections.unmodifiableList(activeObservers);
	}

}
