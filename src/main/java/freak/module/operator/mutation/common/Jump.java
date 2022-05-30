/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.OperatorGraph;
import freak.core.population.Individual;
import freak.module.searchspace.PermutationGenotype;

/**
 * This operator works on permutations and cycles and performs a number of jump
 * operations which is poisson distributed with the specified parameter 
 * <code>lambda</code>.
 * With every jump operation, a randomly chosen element at position i jumps to
 * a randomly chosen position j while the other elements between position i and
 * position j are shifted in the appropriate direction.
 *
 * @author Michael, Heiko
 */
public class Jump extends PoissonOperation {
	/**
	 * Creates a new Jump object.
	 */
	public Jump(OperatorGraph graph) {
		super(graph);
	}
	
	/**
	 * Chooses two positions at random and performs a jump.
	 */
	protected Individual performSingleMutation(Individual ind) {
		RandomElement re = graph.getSchedule().getRandomElement();
		int[] perm = (int[])((PermutationGenotype)ind.getGenotype()).getIntArray().clone();
		// get position of the element to jump and the new position
		int oldPos = re.choose(0,perm.length-1);
		int newPos = re.choose(0,perm.length-2);
		if (oldPos <= newPos) newPos++;
		// save element
		int jumper = perm[oldPos];
		// shift all elements between old and new position
		if (oldPos < newPos) {
			for (int i = 0; oldPos+i < newPos; i++) {
				perm[oldPos+i] = perm[oldPos+i+1];
			}
		} else {
			for (int i = 0; oldPos-i > newPos; i++) {
				perm[oldPos-i] = perm[oldPos-i-1];
			}
		}
		// jump elemnt to new position
		perm[newPos] = jumper;
		return new Individual(graph.getSchedule(), new PermutationGenotype(perm), new Individual[] {ind});
	}
	
	public String getDescription() {
		return "An element jumps to another position while the other elements between both positions are shifted";
	}
	
	public String getName() {
		return "Jump";
	}
	
}
