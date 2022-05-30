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

import freak.core.modulesupport.*;
import freak.core.view.*;
import java.util.*;

/**
 * An observer collects data from some part of the system and contains views  that display this data in the GUI. So the Observer represents the model in the model-view paradigm.  It implements event listeners and updates its views every time a new event is received.<br> Observers and views are matched by the type of data they both work on, so i.e. an observer observing individuals can be combined with all views that can display individuals.<br> Some observers require specific search spaces, i.e. search spaces providing a metric to measure the current diversity. 
 * @author  Patrick, Dirk, Stefan
 */

public interface Observer extends Module {

	/**
	 * The measure is unknown.
	 */
	public static final int UNKNOWN = 0;

	/**
	 * The observer measures generations.
	 */
	public static final int GENERATIONS = 1;

	/**
	 * The observer measures runs.
	 */
	public static final int RUNS = 2;

	/**
	 * The observer measures runs.
	 */
	public static final int BATCHES = 3;

	/**
	 * Returns the type of data observed by the observer to match views that
	 * can be added to the observer.
	 * 
	 * @return a class object representing the type of data observed.
	 */
	public Class getOutputDataType();

	/**
	 * Adds the view to the observer so it will display the data observed by
	 * the observer.
	 * 
	 * @param view the view to be added to the observer.
	 * @throws ObserverViewMismatchException if the specified view does not match the observer. 
	 */
	public void addView(View view) throws ObserverViewMismatchException;

	/**
	 * Removes a view from the observer. 
	 */
	public void removeView(View view);

	/**
	 * Returns a list of all views.
	 * 
	 * @return a list of all views.
	 */
	public List getViews();

	/**
	 * Updates all views with the specified data.
	 * 
	 * @param data the data the views are to be updated with.
	 */
	public void updateViews(Object data);

	/**
	 * Returns the measure the observer works with.
	 * @return  the measure of the observer, either <code>GENERATIONS</code>  or <code>RUNS</code> or the default value, <code>UNKNOWN</code>.
	 * @uml.property  name="measure"
	 */
	public int getMeasure();

	/**
	 * Sets the measure of the observer.
	 * @param measure  the measure of the observer.
	 * @uml.property  name="measure"
	 */
	public void setMeasure(int measure);

}
