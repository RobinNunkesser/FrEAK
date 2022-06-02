/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.graphedgeselection;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.population.Genotype;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;

/**
 * @author  Oliver
 */
public abstract class AbstractMSTFitnessFunction extends AbstractStaticSingleObjectiveFitnessFunction
implements MSTFitnessFunctionInterface, BatchEventListener, RunEventListener {

	private GraphEdgeSelectionGenotype optimum = null;
	
	public AbstractMSTFitnessFunction(Schedule schedule) {
		super(schedule);
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		GraphEdgeSelection.Graph graph = ((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getGraph();
		if (graph != null) return (-((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getGraph().getMSTWeight());
		else return 1;
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		if (optimum == null) {
			GraphEdgeSelection.Graph graph = ((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getGraph();
			if (graph != null) {
				optimum = new GraphEdgeSelectionGenotype(graph);
				optimum.setEdgeSelection(graph.getMSTEdgeSelection());
			}
		}
		return optimum;
	}

	public void runStarted(RunEvent evt) {
		// -- reset the optimal genotype
		if (!((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getPropertyKeepGraphDuringBatch().booleanValue())
			optimum = null;
	}
	
	public void batchStarted(BatchEvent evt) {
		// -- reset the optimal genotype
		optimum = null;
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

}
