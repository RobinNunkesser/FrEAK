/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.bitstring;

import java.util.*;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * This class implements the fitness function LongPathK. <br>
 * 
 * The fitness value of an given individual is nearly n^2-OneMax. But the first
 * k bits are stronger weighted (with weight n). There is also a very long path 
 * from 0^n to 1^n. The length of the path is nearly 2^k. And the fitness values
 * (on the path) are n^2+(# of point on path).
 * 
 * @author Christian, Heiko
 */

public class LongPathK extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable {

	/*
	 * Parameter which configures the path.
	 */
	private int k = 1;

	/**
	 * The constructor of the class.
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public LongPathK(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();
		
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		
		// uh oh, the search space dimension seems to have changed.
		if ((n - 1) % k != 0) {
			// solution: reset k to 1
			k = 1;
		}
	}

	/**
	 * Calculates whether the given search point lies on the path or not.
	 * In the former case this method returns the position of the point on the
	 * path. In the latter case -1 is returned.
	 * 
	 * @param set the given search point.
	 * @return -1 if <code>set</code> doesn't belong to the path, the position on the path otherwise. 
	 */
	private double pathPoint(BitSet set) {
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		int numberOfBlocks = (n - 1) / k;
		int pathPoint;
		// The last bit is investigated first. 
		if (set.get(n - 1)) {
			pathPoint = 2;
		} else {
			pathPoint = 1;
		}
		// The length of the path as far as we have investigated it.
		// Until now, we just checked one bit. So the path already investigated
		// is simply (0,1). 
		int pathLength = 2;
		// Now we iterate through all blocks of length k from right to left.
		for (int i = numberOfBlocks - 1; i >= 0; i--) {
			// First we check, how the k bits look like.
			// There are three cases to be distinguished:
			// 0^k, 0^i1^{k-i} and 1^k
			boolean oneBlock = false;
			int noZeros = 0;
			for (int j = i * k; j < (i + 1) * k; j++) {
				boolean bit = set.get(j);
				if (!(bit) && (oneBlock)) {
					// The point doesn't belong to the path because it doesn't
					// have one of the forms mentioned above. 
					return -1;
				}
				if (bit && (!oneBlock)) {
					oneBlock = true;
					noZeros = j - i * k;
				}
			}
			// First case: 0^k, nothing to do in this case.
			// Appending zeros on the left side of the string doesn't change the
			// position on the path.
			// Second case: 0^i1^{k-i}			
			if (oneBlock && (noZeros > 0)) {
				// We have reached the bridge.				
				if (pathPoint != pathLength) {
					// The bridge can only be reached if the right part of the
					// string represents the last path point (of the shorter
					// path consisting of the bits right of the current block). 
					return -1;
				}
				// We add the number of bridge points alredy passed.
				pathPoint += k - noZeros;
			} else {
				// Third case: 1^k
				if (oneBlock && (noZeros == 0)) {
					// We have passed the bridge. We must add the number of
					// points on the first part of the path and on the bridge
					// becaused we have passed these parts completely.
					// Furthermore, we must add the number of points passed
					// on the third part of the path already. 
					pathPoint = k + 2 * pathLength - pathPoint;
				}
			}
			// The length of the path already investigated grows.
			pathLength = 2 * pathLength + k - 1;
		}
		return pathPoint;
	}

	public double evaluate(Genotype genotype) {
		BitSet set = ((BitStringGenotype)genotype).getBitSet();
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		double pathPoint = pathPoint(set);
		// pathPoint returns -1, if the point doesn't belong to the path
		if (pathPoint == -1) {
			int card = set.cardinality();
			int count = 0;
			for (int i = 0; i < k; i++) {
				if (set.get(i)) {
					count++;
				}
			}
			return n * n - (n - 1) * count - card;
		}
		return n * n + pathPoint;
	}

	/*    public double evaluate(Genotype genotype) {   			
			BitSet set = (BitSet)((BitStringGenotype)genotype).getBitSet();
	
			int n = getSchedule().getSearchSpace().getDimension();
	
			// possible configuration of parameters?
			if (Math.round(((double)n-1)/(double)k) != (int) (n-1)/k) return -1;
	
			double pos = posOnPath(set);
		
			if (pos == 0) {
				BitSet copy = (BitSet)set.clone();
				copy.set(k,n,false);
				int firstBits = copy.cardinality();
				
				copy = (BitSet)set.clone();
				copy.set(0,k,false);
				int lastBits = copy.cardinality();
				
				return n*n-n*firstBits-lastBits; // not on path
			}
	
	        return n*n+pos; 					// on path
	    }
	    
	    private double posOnPath(BitSet set) {
	    	int size = getSchedule().getSearchSpace().getDimension();
	    	double length = Math.round(((k+1)*Math.exp((size-1)/k*Math.log(2))-k+1));
			return onPath(set,length,0,size-1);   	
	    }
	    
		private double onPath(BitSet set, double pathLength, int first, int last) {
			int i;
			boolean inStartOrEnd = true;
			double pos, lengthOfRec;
			
			// last bit reached ?
			if (first == last) {
				pos = set.get(first)==true?1:0;
				// so the position is 1+bit
				return (1+pos);
			}
			
			// calculate the pathSize of the path in the next step
			lengthOfRec = Math.round((pathLength-(double)k+1)/2.0);
			
			// Check first k values of equality
			for (i=1; i < k && inStartOrEnd;i++)
				if (set.get(first+i) != set.get(first+i-1)) inStartOrEnd = false;
			
			// in start or last part?
			if (inStartOrEnd) {
				if (set.get(first) == false) { // in the start
					return onPath(set,lengthOfRec,first+k,last);
				}
				else {  // in the back
					pos = onPath(set,lengthOfRec,first+k,last);
					return pos>0?pathLength+1.0-pos:0; 
				}
			}
			
			// in the bridge
			// get the possible position
			for (i=0; i < k && !set.get(first+i); i++);
			if (i == 0) return 0; 
			
			int position = i;
			
			// check if it is a valid position (rest has to be 1)
			for (; i < k && set.get(first+i); i++);
			if (i<k) return 0;
			
			if (onPath(set,lengthOfRec,first+k,last)==lengthOfRec)
				return lengthOfRec+(double)k-(double)position;
			else 
				return 0;
		} */

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		int size = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		double sizeOfPath = (k + 1) * Math.pow(2, (size - 1) / k) - k + 1;
		return size * size + sizeOfPath;
	}

	public double getLowerBound() throws UnsupportedOperationException {
		int size = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		return size * size * size - size * k - size - k - 1;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		BitSet bs = new BitSet(((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension());

		// optimum lies in 1^k 0^(n-k)
		bs.set(0, k);
		bs.clear(k, ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension());

		return new BitStringGenotype(bs, ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension());
	}

	/**
	 * Sets the value of the parameter <code>k</code>. (n-1)/k has to be a natural
	 * number.
	 * 
	 * @param k value you wish attribute <code>k</code> to be.
	 */
	public void setPropertyK(Integer k) {
		if (k.intValue() <= 0) {
			return;
		}
		int n = ((BitString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		if ((n - 1) % k.intValue() == 0)
			this.k = k.intValue();
	}

	/**
	 * Returns the value of attribute <code>k</code>.
	 * 
	 * @return the wrapped value of attribute <code>k</code>.
	 */
	public Integer getPropertyK() {
		return new Integer(k);
	}

	/**
	 * @return a string describing the meaning of parameter <code>k</code>.
	 */
	public String getLongDescriptionForK() {

		return "The parameter k specifies the length and the construction of the path and has " + "to be a factor of n-1. The length of the path is nearly 2^k. " + "See the User's Guide for closer description.";
	}

	public String getDescription() {
		return "LongPathK contains a path with exponential length and strictly monotone fitness values. " + "The length is nearly 2^k and thus exponential in the parameter k that is used to construct the path recursively.\n" + "Individuals on the path get fitness n^2 plus their index on the path.\n" + "The fitness value of all other individuals is n^2-OneMax, except " + "that the first k Bits are stronger weighted (with weight n).";
	}

	public String getName() {
		return "LongPathK";
	}

}
