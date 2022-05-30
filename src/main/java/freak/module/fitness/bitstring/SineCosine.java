/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.bitstring;

import java.util.BitSet;
import java.util.Iterator;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractStaticMultiObjectiveFitnessFunction;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.searchspace.*;

/**
 * This is a multiobjective fitness function. Let n be the dimension of the
 * search space and i the number of ones in the genotype. Then the fitness is
 * calculated as (sin(2*Pi*i/n),cos(2*Pi*i/n)).  
 * 
 * @author Heiko
 */
public class SineCosine extends AbstractStaticMultiObjectiveFitnessFunction {

	/**
	 * Creates a new SineCosine object.
	 * 
	 * @param schedule a backlink to a schedule.
	 */
	public SineCosine(Schedule schedule) {
		super(schedule);
	}

	protected double[] evaluate(Genotype genotype) {
		BitSet bs = ((BitStringGenotype)genotype).getBitSet();
		int card = bs.cardinality();
		int n = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
		double x  = (double)card/(double)n;
		x = x * 2*Math.PI;
		double[] result = {Math.sin(x),Math.cos(x)};		
		return result;
	}

	public int getDimensionOfObjectiveSpace() {
		return 2;
	}

	public String getName() {		
		return "Sine and Cosine";
	}

	public String getDescription() {
		return "Let n be the dimension of the search space and i the number of ones in the genotype. "+
			"Then the fitness is calculated as (sin(2*Pi*i/n),cos(2*Pi*i/n)).";
	}

	public boolean containsParetoFront(IndividualList list) {
		int dimension = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
		
		int paretoFrontEnds = dimension/4;
		if (dimension%4 == 3) {
			paretoFrontEnds = dimension/4+1;
		}
		
		if (list.size() < paretoFrontEnds+1) {
			return false;
		}
		boolean[] exists = new boolean[paretoFrontEnds+1];
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Individual ind = (Individual) it.next();
			int card = ((BitStringGenotype)ind.getGenotype()).getBitSet().cardinality();
			if (card <= paretoFrontEnds) {			
				exists[card] = true;
			} else {
				if (card == dimension) {
					exists[0] = true;		
				}
			}
		}
		for (int i = 0; i < paretoFrontEnds+1; i++) {
			if (!exists[i]) {
				return false;
			}
		}
		return true;	
	}

}
