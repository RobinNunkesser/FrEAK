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

import freak.core.control.Schedule;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.view.swingsupport.FreakSwingModel;
import freak.core.view.swingsupport.UpdateManager;

import javax.swing.*;

/**
 * An abstract superclass for views displaying a single individual. Common methods for maintaining the panel and receiving individuals via  updates are provided. Subclasses must implement  <code>createIndividualPanel</code> to provide the panel displaying an  individual and they may access the individual to display by calling <code>getIndividualToDisplay</code>. 
 * @author  Dirk
 */
public abstract class SingleIndividualView extends AbstractView implements FreakSwingModel, Configurable {

	private transient JPanel individualPanel;

	private IndividualList individuals;
	private Individual individual;
	
	private int displayStrategy = FIXED_INDEX;
	private int indexToDisplay = 0;
	
	public static final int AUTO_CYCLE = 0;
	public static final int RANDOM = 1;
	public static final int FIXED_INDEX = 2;

	public SingleIndividualView(Schedule schedule) {
		super(schedule);
	}

	public final JPanel createPanel() {
		individualPanel = createIndividualPanel();
		
		return individualPanel;
	}
	
	/**
	 * Creates an output panel displaying an individual.
	 * 
	 * @return an output panel displaying an individual.
	 */
	public abstract JPanel createIndividualPanel();
	
	public Class[] getInputDataTypes() {
		return new Class[] { IndividualList.class };
	}

	public final void update(Object data) {
		super.update(data);

		synchronized (this) {
			individuals = (IndividualList)data;

			individual = chooseIndividual();
		}
		UpdateManager.markDirty(this);
	}

	public void flush() {
		if (individualPanel == null)
			return;

		synchronized (this) {
			individualPanel.repaint();
		}
	}

	/**
	 * Returns the individual to be displayed by the view.
	 * 
	 * @return the individual to be displayed by the view.
	 */
	public Individual getIndividualToDisplay() {
		return individual;	
	}
	
	private Individual chooseIndividual() {
		if (individuals == null || individuals.isEmpty()) {
			return null;
		} else {
			switch (displayStrategy) {
				case AUTO_CYCLE: {
					indexToDisplay = (indexToDisplay + 1) % individuals.size();
				} break;
				case RANDOM: {
					indexToDisplay = getSchedule().getRandomElement().choose(individuals.size() - 1);
				} break;
			}
		}
		
		if (indexToDisplay >= individuals.size()) indexToDisplay = 0;
		
		return individuals.getIndividual(indexToDisplay);
	}

	public IndividualList getIndividualList() {
		return individuals;
	}
	
	/**
	 * @return  the displayStrategy
	 * @uml.property  name="displayStrategy"
	 */
	public int getDisplayStrategy() {
		return displayStrategy;
	}

	/**
	 * @param displayStrategy  the displayStrategy to set
	 * @uml.property  name="displayStrategy"
	 */
	public void setDisplayStrategy(int i) {
		displayStrategy = i;
	}

	public void setPropertyDisplayStrategy(StringArrayWrapper saw) {
		displayStrategy = saw.getIndex();
	}
	
	public StringArrayWrapper getPropertyDisplayStrategy() {
		String[] strings = new String[3];
		strings[AUTO_CYCLE] = "Auto cycle";
		strings[RANDOM] = "Random";
		strings[FIXED_INDEX] = "Fixed index";
		
		return new StringArrayWrapper(strings, displayStrategy);
	}
	
	public String getShortDescriptionForDisplayStrategy() {
		return "Display strategy";
	}

	public String getLongDescriptionForDisplayStrategy() {
		return "The strategy used to determine the displayed individual. " +
			"Choices are to permanently cycle through the indices of the " +
			"population, to choose an individual randomly or to display " +
			"the individual at a fixed index.";
	}
	
	public void setPropertyIndexToDisplay(Integer i) {
		indexToDisplay = i.intValue();
	}

	public Integer getPropertyIndexToDisplay() {
		return new Integer(indexToDisplay);
	}
	
	public String getShortDescriptionForIndexToDisplay() {
		return "Index to display";
	}

	public String getLongDescriptionForIndexToDisplay() {
		return "The index of the individual to display, i.e. the position within the population. " +
			"The index is determined by the display strategy in every step.";
	}

}
