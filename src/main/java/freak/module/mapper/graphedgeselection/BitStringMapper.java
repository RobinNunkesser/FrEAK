/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.mapper.graphedgeselection;

import freak.core.control.Schedule;
import freak.core.mapper.AbstractMapper;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;

/**
 * @author  Stefan, Kai
 */
public class BitStringMapper extends AbstractMapper {
	private BitString genotypeSearchSpace;

	public BitStringMapper(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();
		
		GraphEdgeSelection space = (GraphEdgeSelection)schedule.getPhenotypeSearchSpace();
		
		try {
			int dimension = space.getDimension();
			genotypeSearchSpace = new BitString(schedule, dimension);
		} catch (IllegalStateException exc) {
			// undetermined dimension / graph was not yet created
			genotypeSearchSpace = new BitString(schedule);
		}
	}

	public Genotype genotypeToPhenotype(Genotype genotype) {
		GraphEdgeSelectionGenotype gen = new GraphEdgeSelectionGenotype(((GraphEdgeSelection)schedule.getPhenotypeSearchSpace()).getGraph());

		BitStringGenotype bsg = (BitStringGenotype)genotype;
		gen.setEdgeSelection(bsg.getBooleanArray());

		return (gen);
	}

	public Genotype phenotypeToGenotype(Genotype phenotype) throws UnsupportedOperationException {
		if (phenotype == null) return null;
		GraphEdgeSelectionGenotype graphGenotype = (GraphEdgeSelectionGenotype)phenotype;
		BitStringGenotype bsg = new BitStringGenotype(graphGenotype.getEdgeSelection());
		return bsg;
	}

	public String getName() {
		return "Edge Selection to Bit String";
	}

	public String getDescription() {
		return "Encodes Edge Selections as Bit Strings";
	}

	/**
	 * @return  the genotypeSearchSpace
	 * @uml.property  name="genotypeSearchSpace"
	 */
	public SearchSpace getGenotypeSearchSpace() {
		return genotypeSearchSpace;
	}
}