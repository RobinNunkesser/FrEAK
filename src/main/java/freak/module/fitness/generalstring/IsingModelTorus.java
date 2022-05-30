/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.generalstring;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.population.*;
import freak.module.searchspace.*;

/**
 * An implementation of the Ising fitness function on a two-dimensional torus.
 * 
 * @author Dirk
 */
public class IsingModelTorus extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable {

	private int torusHeight;
	private int torusWidth;
	
	private boolean diagonalEdges;

	/**
	 * Creates a new <code>IsingModelTorus</code>.
	 */
	public IsingModelTorus(Schedule schedule) {
		super(schedule);
		
		torusHeight = 1;
		torusWidth = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		diagonalEdges = false;
	}
	
	public void initialize() {
		super.initialize();
		
		// check if the search space dimension has been changed
		if (torusHeight * torusWidth != ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension()) {
			// if so, reset torus lenghts to default values
			torusHeight = 1;
			torusWidth = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		}
	}

	protected double evaluate(Genotype genotype) {
		double fitness = 0;
		int[] gen = ((GeneralStringGenotype)genotype).getIntArray();
		
		for(int y = 0; y < torusHeight; y++) {
			for (int x = 0; x < torusWidth; x++) {
				// check horizontal edges
				if ((torusWidth > 2) || (x < torusWidth - 1)) {
					if (torusValue(gen, x, y) == torusValue(gen, (x + 1) % torusWidth, y)) fitness++;
				}

				// check vertical edges
				if ((torusHeight > 2) || (y < torusHeight - 1)) {
					if (torusValue(gen, x, y) == torusValue(gen, x, (y + 1) % torusHeight)) fitness++;
				}
				
				if (diagonalEdges) {
					// check northeast and southeast edges
					if (((torusWidth > 2) || (x < torusWidth - 1)) && ((torusHeight > 2) || (y < torusHeight - 1))) {
						// check southeast edges
						if (torusValue(gen, x, y) == torusValue(gen, (x + 1) % torusWidth, (y + 1) % torusHeight)) fitness++;
						if (torusHeight > 2) {
							// check northeast edges
							if (torusValue(gen, x, y) == torusValue(gen, (x + (torusWidth - 1)) % torusWidth, (y + 1) % torusHeight)) fitness++;
						}
					}
				}
			}
		}
		
		return fitness;
	}
	
	private int torusValue(int[] torusData, int x, int y) {
		return torusData[y * torusWidth + x];
	}

	/**
	 * Returns the value at the specified position in the torus.
	 */
	public synchronized int torusValue(Individual individual, int x, int y) {
		return torusValue(((GeneralStringGenotype)individual.getPhenotype()).getIntArray(), x, y);
	}

	public String getName() {
		return "Ising Model (Torus)";
	}

	public String getDescription() {
		return "The Ising Model is a model derived from statistical mechanics " +
			   "describing the behavior of atoms with two spins where neighbored atoms tend to orient in the same direction as their neighbors.\n" +
			   "Another view is an inversion of the colorability problem: an edge contributes a value of 1 to the fitness if and only if its nodes have got the same color. So, all colorings with only one color are optimal.\n" +
			   "Here, the graph is described by a two-dimensional torus, a rectangular grid of nodes with edges between nodes that are horizontally of vertically neighbored in the grid. " +
			   "The leftmost and rightmost node in one row and the upper and lower nodes in one column are neighbored as well. Note that a torus with one row is a ring.";
	}

	/**
	 * @return the height of the torus.
	 */
	public Integer getPropertyTorusHeight() {
		return new Integer(torusHeight);
	}

	/**
	 * @return the width of the torus.
	 */
	public Integer getPropertyTorusWidth() {
		return new Integer(torusWidth);
	}

	/**
	 * Sets the new torus height and adjusts the width accordingly.
	 */
	public void setPropertyTorusHeight(Integer height) {
		if (height.intValue() < 1) return;
		
		int dim = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		// check if the dimension of the search space is divisable by height 
		if (height.intValue() * (dim / height.intValue()) == dim) {
			torusHeight = height.intValue();
			torusWidth = (dim / height.intValue());
		}
	}

	/**
	 * Sets the new torus width and adjusts the height accordingly.
	 */
	public void setPropertyTorusWidth(Integer width) {
		if (width.intValue() < 1) return;
		
		int dim = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension();
		// check if the dimension of the search space is divisable by width
		if (width.intValue() * (dim / width.intValue()) == dim) {
			torusWidth = width.intValue();
			torusHeight = (dim / width.intValue());
		}
	}
	
	public String getShortDescriptionForTorusWidth() {
		return "Torus Width";
	}

	public String getShortDescriptionForTorusHeight() {
		return "Torus Height";
	}

	public String getLongDescriptionForTorusWidth() {
		return "Sets the number of columns for the torus. The torus width has to be a divisor of the search space's dimension.";
	}

	public String getLongDescriptionForTorusHeight() {
		return "Sets the number of rows for the torus. The torus height has to be a divisor of the search space's dimension.";
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		// normally, all fields contribute 2 to the fitness
		double result = torusWidth * torusHeight * 2;
		// consider non-existing edges in special cases
		if (torusWidth <= 2) result -= torusHeight;
		if (torusHeight <= 2) result -= torusWidth;
		
		// consider diagonal edges
		if (diagonalEdges) {
			result += torusWidth * torusHeight * 2;
			if (torusWidth <= 2) result -= torusHeight * 2;
			if (torusHeight == 1) result -= torusWidth * 2;
			// northeast and southeast edges melt together 
			if (torusHeight == 2) result -= torusWidth;
		}
		
		return result;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}

	/**
	 * @return <code>true</code> if the torus contains diagonal edges, <code>false</code> otherwise.
	 */
	public Boolean getPropertyDiagonalEdges() {
		return new Boolean(diagonalEdges);
	}

	/**
	 * Specifies whether the torus contains edges between diagonally neighbored
	 * fields.
	 */
	public void setPropertyDiagonalEdges(Boolean b) {
		diagonalEdges = b.booleanValue();
	}

	public String getShortDescriptionForDiagonalEdges() {
		return "Diagonal Edges";
	}

	public String getLongDescriptionForDiagonalEdges() {
		return "If checked, the torus contains additional edges between diagonally neighbored fields.";
	}

}
