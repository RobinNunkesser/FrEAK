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
 * This class represents the fitness function LEADING ONES - TRAILING ZEROES.
 * The objectives are to increase both the number of leading ones and the number
 * of trailing zeroes. The first component of the fitness vector is the number
 * of leading ones and the second component is the number of trailing zeroes. 
 * 
 * @author Heiko
 */
public class LOTZ extends AbstractStaticMultiObjectiveFitnessFunction {

	/**
	 * Creates a new LOTZ object.
	 * 
	 * @param schedule a backlink to the schedule.
	 */
	public LOTZ(Schedule schedule) {
		super(schedule);
	}

	protected double[] evaluate(Genotype genotype) {
		BitSet bs = ((BitStringGenotype)genotype).getBitSet();
		int lo = 0;
		int tz = 0;
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();   	
		for (int k=0; k<dimension; k++) {
			if (bs.get(k)) {
				lo++;
			} else {
				break;
			}			
		}
 		for (int k=dimension-1; k >= 0; k--) {
 			if (!bs.get(k)) {
 				tz++;
 			} else {
 				break;
 			}
 		}
 		double[] result = {lo, tz};		 		
		return result;		
	}

	public boolean containsParetoFront(IndividualList list) {
		int dimension = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		if (list.size() < dimension+1) {
			return false;
		}
		boolean[] exists = new boolean[dimension+1];
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Individual ind = (Individual) it.next();
			double[] fitness = evaluate(ind, null);
			if (fitness[0]+fitness[1] == dimension) {
				exists[(int)fitness[0]] = true;		
			}
		}
		for (int i = 0; i < dimension+1; i++) {
			if (!exists[i]) {
				return false;
			}
		}
		return true;	
	}

	public int getDimensionOfObjectiveSpace() {
		return 2;
	}

	public String getName() {
		return "LOTZ";
	}

	public String getDescription() {
		return "Leading Ones - Trailing Zeroes\n"+
			"The objectives are to increase both the number of leading ones and the number "+
			"of trailing zeroes. The first component of the fitness vector is the number "+
			"of leading ones and the second component is the number of trailing zeroes.";
	}

}
