/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.stoppingcriterion;

import freak.core.control.Schedule;
import freak.core.event.GenerationEvent;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.stoppingcriterion.AbstractGenerationStoppingCriterion;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.fitness.generalstring.IsingModelCliques.Clique;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * This stopping criterion can only be applied if the fitness function selected
 * is IsingModelCliques. It stopps the run as soon as in each clique
 * all nodes are colored identically.
 * 
 * @author Heiko
 */
public class IsingModelCliquesFinished extends AbstractGenerationStoppingCriterion {
	
	private boolean allCliquesIdenticallyColored = true;

	public IsingModelCliquesFinished(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException();		
		}
	}

	protected void checkCriterion(GenerationEvent evt) {
		IndividualList p = getSchedule().getPopulationManager().getPopulation();
		Individual ind = p.getIndividualWithRank(1);
		IsingModelCliques ising = (IsingModelCliques)getSchedule().getRealFitnessFunction();
		Clique[] cliques = ising.getCliques();
		int[] gen = ((GeneralStringGenotype)ind.getGenotype()).getIntArray();
		allCliquesIdenticallyColored = true;				
		for (int i = 0; i < cliques.length; i++) {
			int color = gen[cliques[i].getStart()];
			if (color != gen[0]) {
				allCliquesIdenticallyColored = false;					
			}
			for (int j = cliques[i].getStart(); j < cliques[i].getStart()+cliques[i].getSize(); j++) {
				if (gen[j]!=color) {
					return;
				}
			}			
		}
		stopRun();
	}

	public String getName() {
		return "Cliques Finished";
	}

	public String getDescription() {
		return "Stopps the run if in each clique all nodes are colored identically.";
	}

	public boolean getAllCliquesColoredIdentically() {
		return allCliquesIdenticallyColored;
	}

}
