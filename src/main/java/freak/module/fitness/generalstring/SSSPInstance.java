/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.generalstring;

import java.io.Serializable;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.modulesupport.UnsupportedEnvironmentException;

/**
 * This class represents an abstract SSSP instance. Methods for finding the
 * optimum and other helper functions are already implemented. Concrete 
 * instances only need to implement the methods nextInstance() and
 * getLargestMatrixEntry(). 
 * 
 * @author Heiko
 */
public abstract class SSSPInstance implements Serializable {

	private double[][] matrix;
	private double[] optDist = null;
	protected int dimension;
	protected RandomElement re;
	
	public SSSPInstance(int dimension, RandomElement re) {
		super();
		this.dimension = dimension;
		this.re = re;
	}
	
	public void nextInstance() {
		matrix = getNextDistanceMatrix();
		optDist = null;
	}

	// This vector will contain the distances already calculated.
	private double[] distances;
	// We will save which nodes we have already visited for recognizing circles.  
	private boolean[] visited;

	public double[] getMultiFitness(int[] gt) throws UnsupportedEnvironmentException {
		if (gt.length != dimension-1)
			throw new UnsupportedEnvironmentException();
		distances = new double[dimension-1];
		visited = new boolean[dimension-1];
		for (int i = 0; i < dimension-1; i++) {
			// First we check wheather the distance of node i is already calculated.
			if (distances[i] == 0) {
				distances[i] = matrix[gt[i]][i]+length(gt,gt[i]);
			}
		}
		return distances;
	}
	
	private double length(int[] gt, int i) {
		if (i==dimension-1) {
			return 0;
		} else {
			if (distances[i] > 0) {
				return distances[i];
			}
			// If we reach a node already visited then the graph contains a circle.
			if (visited[i]) {
				return Double.POSITIVE_INFINITY;
			}
			visited[i] = true;
			distances[i] = matrix[gt[i]][i]+length(gt,gt[i]);
			return distances[i];
		}
	}
	
	public double[] getOptimum() {
		if (optDist != null) {
			return optDist;
		}
		// We use Dijkstra's algorithm to find the optimal solution.
		
		boolean[] choosen = new boolean[dimension-1];
		// the distance to the set of nodes already choosen 
		optDist = new double[dimension-1];
		for (int i = 0; i < dimension-1; i++) {
			optDist[i] = Double.POSITIVE_INFINITY;
		}
		
		double min = Double.POSITIVE_INFINITY;
		int minNode = -1;
		
		for (int i = 0; i < dimension-1; i++) {
			optDist[i] = matrix[dimension-1][i];
			if (optDist[i] < min) {
				min = optDist[i];
				minNode = i;
			}
		}
		
		// This does only happen when the start node cannot be reached from any
		// other node.
		if (minNode == -1) {
			return optDist;
		}
		
		for (int numChoosen = 0; numChoosen < dimension; numChoosen++) {
			choosen[minNode] = true;
			// update the distances
			int newNode = minNode;
			min = Double.POSITIVE_INFINITY;
			minNode = -1;
			for (int i = 0; i < dimension-1; i++) {
				if (!choosen[i]) {
					if (matrix[newNode][i]+optDist[newNode] < optDist[i]) {
						optDist[i] = matrix[newNode][i]+optDist[newNode];
					}
					if (optDist[i] < min) {
						min = optDist[i];
						minNode = i;
					}
				}
			}
			if (minNode == -1) {
				return optDist;
			}
		}
		return optDist;
	}
	
	public void setRandomElement(RandomElement re) {
		this.re = re;
	}
	
	/**
	 * This method returns a matrix which represents the distances.
	 * @return distance matrix.
	 */
	protected abstract double[][] getNextDistanceMatrix();
	
	/**
	 * This method return the largest value in the distance matrix which is
	 * smaller than infinity.
	 * @return the largest value in the matrix.
	 */
	protected abstract double getLargestMatrixEntry();

}
