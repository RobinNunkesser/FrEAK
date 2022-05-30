/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.control;

import java.util.Comparator;

import freak.core.fitness.FitnessFunction;
import freak.core.graph.Operator;
import freak.core.graph.OperatorGraph;
import freak.core.mapper.Mapper;
import freak.core.modulesupport.Module;
import freak.core.observer.Observer;
import freak.core.parametercontroller.ParameterController;
import freak.core.populationmanager.PopulationManager;
import freak.core.searchspace.SearchSpace;
import freak.core.stoppingcriterion.StoppingCriterion;
import freak.core.view.View;

/**
 * A comparator used to sort objects by the order of their classes in the 
 * Schedule Editor.
 * 
 * @author Dirk
 */
public class FreakClassComparator implements Comparator {

	/**
	 * A list of classes in the order of the Schedule Editor.
	 */
	private static final Class classes[] = new Class[] {
		SearchSpace.class,
		FitnessFunction.class,
		Mapper.class,
		OperatorGraph.class,
		Operator.class,
		ParameterController.class,
		StoppingCriterion.class,
		PopulationManager.class,
		Observer.class,
		View.class,
		Module.class
	};

	public int compare(Object o1, Object o2) {
		if (o1 == null || o2 == null) return 0;
		
		// by iterating the classes in ascending order and using short-cut 
		// evaluation, the correct order is assured even if a class implements
		// or extends multiple classes or interfaces within the classes array.
		for (int i = 0; i < classes.length; i++) {
			if (classes[i].isAssignableFrom(o1.getClass()) && classes[i].isAssignableFrom(o2.getClass())) return 0;
			if (classes[i].isAssignableFrom(o1.getClass())) return -1;
			if (classes[i].isAssignableFrom(o2.getClass())) return +1;
		}
		
		// both objects are not instances of Module
		return 0;
	}

}
