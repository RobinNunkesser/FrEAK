/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.observer;

import freak.core.control.Schedule;
import freak.core.event.IndividualListEvent;
import freak.core.event.IndividualListEventListener;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.ModuleCollector;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.modulesupport.inspector.CustomInspectable;
import freak.core.modulesupport.inspector.Inspector;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.gui.ModuleList;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * Computes the fitness value of a pseudo fitness function.
 * @author  Dirk
 */
public class PseudoFitness extends AbstractObserver implements IndividualListEventListener, Configurable,
		CustomInspectable {

	private SingleObjectiveFitnessFunction fitness;

	public PseudoFitness(Schedule schedule) {
		super(schedule);
		setMeasure(GENERATIONS);	
		
		if (schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction) {	
			fitness = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
		}
	}
	
	public void initialize() {
		super.initialize();
		fitness.initialize();
	}

	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
		}
		
		fitness.testSchedule(schedule);
	}

	public String getName() {
		return "Pseudo-fitness Values";
	}

	public String getDescription() {
		return "Computes the fitness values of the invidiuals with respect to a " +
			"pseudo fitness function.";
	}

	public Class getOutputDataType() {
		return double[].class;
	}

	public void individualList(IndividualListEvent evt) {
		IndividualList individualList = evt.getIndividualList();
		Individual[] individuals = individualList.toArray();
		
		double[] fitnessValues = new double[individuals.length];
		for (int i = 0; i < individuals.length; i++) {
			Individual individual = (Individual)individuals[i].clone();
			// invalidate cache holding the schedule's fitness if the schedule's
			// fitness function is an instance of 
			// StaticSingleObjectiveFitnessFunction
			individual.setLatestKnownFitnessValue(null);
			fitnessValues[i] = fitness.evaluate(individual, individualList);
		}

		updateViews(fitnessValues);
	}

	public void createEvents() {
		super.createEvents();
		getSchedule().getEventController().addEvent(this, "Get individuals from", IndividualListEvent.class, getSchedule().getPopulationManager());
	}
	
	public Inspector getInspector() {
		Inspector panel = new Inspector(this);
		
		final ModuleList moduleList = new ModuleList();
		Module[] modules = new ModuleCollector(getSchedule()).getFitnessFunctions(getSchedule().getPhenotypeSearchSpace());
		
		// filter out SingleObjectiveFitnessFunctions
		ArrayList singleObjectives = new ArrayList();
		for (int i = 0; i < modules.length; i++) {
			if (modules[i] instanceof SingleObjectiveFitnessFunction) {
				singleObjectives.add(modules[i]);
			}
		}
		modules = (Module[])singleObjectives.toArray(new Module[singleObjectives.size()]);
		
		moduleList.setModules(modules, fitness);

		moduleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (moduleList.getSelectedValue() != null) {
					fitness = (SingleObjectiveFitnessFunction)moduleList.getSelectedValue();
				}
			}
		});
		
		panel.setLayout(new BorderLayout());
		panel.add(moduleList, BorderLayout.CENTER);
		
		return panel;
	}

	public void setPropertyPseudoFitnessFunction(SingleObjectiveFitnessFunction fitness) {
		this.fitness = fitness;
	}
	
	public SingleObjectiveFitnessFunction getPropertyPseudoFitnessFunction() {
		return fitness;
	}
	
	public String getShortDescriptionForPseudoFitnessFunction() {
		return "Pseudo-fitness function";
	}

}