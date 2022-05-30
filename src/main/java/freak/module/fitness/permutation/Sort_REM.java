/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.permutation;

import java.util.*;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * The sorting problem can be viewed as an optimization problem: the sortedness
 * of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the
 * optimal permutation is the identity. <br>
 * REM(pi) equals the largest k such that pi(i_1) < ... < pi(i_k) for some
 * i_1 < ... < i_k (length of the longest sorted subsequence)
 *
 * @author Heiko
 */
public class Sort_REM extends AbstractStaticSingleObjectiveFitnessFunction {
	
	private class IntStack {
		
		private Stack stack;
		
		public IntStack() {
			stack = new Stack();
		}
		
		public void push(int k) {
			stack.push(new Integer(k));
		}
		
		public int pop() {
			return ((Integer)stack.pop()).intValue();
		}
		
		public int peek() {
			return ((Integer)stack.peek()).intValue();
		}
	}
	
	/**
	 * Creates a new object.
	 *
	 * @param schedule a reference of a schedule.
	 */
	public Sort_REM(Schedule schedule) {
		super(schedule);
	}
	
	protected double evaluate(Genotype genotype) {
		return calculateREM((PermutationGenotype)genotype);
	}
	
	public String getName() {
		return "Sorting with measure REM";
	}
	
	public String getDescription() {
		return "The sorting problem can be viewed as an optimization problem: the sortedness " + "of a sequence has to be maximized. We assume a_1 < a_2 < ... < a_n, so the " + "optimal permutation is the identity. \n" + "The measure of sortedness used by this fitness function is REM.\n" + "REM(pi) equals the largest k such that pi(i_1) < ... < pi(i_k) for some " + "i_1 < ... < i_k (length of the longest sorted subsequence)";
	}
	
	/**
	 * This method returns the length of the longest sorted subsequence.
	 * @param gt the genotype which specifies the permutation
	 * @return the length of the longest sorted subsequence
	 */
	private int calculateREM(PermutationGenotype gt) {
		// We use an algorithm described by Orlowski and Pachter in "An
		// algorithm for the determination of a longest increasing subsequence
		// in a sequence", Computers and Mathematics with Applications 17.
		
		int[] a = gt.getIntArray();
		// At most a.length many stacks are needed.
		// We do not explicitly use the sequence b described in the paper. We
		// construct this sequence from the top elements of the stacks instead.
		IntStack[] stack = new IntStack[a.length];
		// The first r stacks are initialized.
		int r = 0;
		
		// Step 1 of the algorithm
		stack[0] = new IntStack();
		stack[0].push(a[0]);
		r++;
		// Step 2 of the algorithm
		for (int i = 1; i < a.length; i++) {
			// At this stage there exist non-empty stacks B_1,...,B_r and the
			// sequence b consists of r elements a_{i_1},...,a_{i_r} which
			// occupy the top positions in the stacks B_1,...,B_r respectively.
			if (a[i] > stack[r - 1].peek()) {
				// Step 2 (i)
				stack[r] = new IntStack();
				stack[r].push(a[i]);
				r++;
			} else {
				// Step 2 (ii)
				// We need to find the smallest j such that a[i] is smaller
				// than stack[j].peek(). We use binary search to find this j.
				int left = 0;
				int right = r - 1;
				int middle = left + (int)Math.floor((right - left) / 2);
				while (right - left > 0) {
					// In order to avoid rounding problems when the interval is
					// small we stop the binary search if right-left < 3.
					if (right - left < 3) {
						boolean found = false;
						int j = left;
						while (!found) {
							if (a[i] < stack[j].peek()) {
								found = true;
								left = right = j;
							} else {
								j++;
							}
						}
					} else {
						if (a[i] < stack[middle].peek()) {
							right = middle;
						} else {
							left = middle;
						}
						middle = left + (int)Math.floor((right - left) / 2);
					}
				}
				// Now j = left = right.
				stack[left].push(a[i]);
			}
		}
		return r;
	}
	
	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		int n = ((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int[] a = new int[n];
		for (int i = 0; i < n; i++) {
			a[i] = i;
		}
		return new PermutationGenotype(a);
	}
	
	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		return ((Permutation)getSchedule().getPhenotypeSearchSpace()).getDimension();
	}
	
	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}
	
	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}
	
}