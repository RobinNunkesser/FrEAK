/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.operator.split;

import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.GraphException;
import freak.core.graph.HasFloatingNumberOfOutPorts;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Split;
import freak.core.modulesupport.inspector.CustomizableInspector;
import freak.core.population.IndividualList;
import freak.core.population.Population;

/**
 * This operator chooses one of its outports at random and sends the whole input to this outport. The distribution over the outports can be chosen by the user. To the other outports an empty <code>IndividualList</code> is sent. 
 * @author  Heiko
 */
public class RandomChoice extends Split implements HasFloatingNumberOfOutPorts, CompatibleWithDifferentSearchSpaces {

	private Double[] prob;
	transient private CustomizableInspector inspector;
	private boolean uniformly;

	/**
	 * Creates a new object.
	 * 
	 * @param graph a reference to an <code>OperatorGraph</code>
	 */
	public RandomChoice(OperatorGraph graph) {
		super(graph);
		prob = new Double[0];
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		if (getNumberOfOutPorts() == 0) {
			return new IndividualList[0];
		}
		double rand = getOperatorGraph().getSchedule().getRandomElement().uniform(0, 1);
		double pr = 0;
		int choose = getNumberOfInPorts() - 1;
		int bound = getNumberOfOutPorts();
		for (int i = 0; i < bound; i++) {
			pr = pr + prob[i].doubleValue();
			if (rand < pr) {
				choose = i;
				break;
			}
		}

		IndividualList[] result = new IndividualList[getNumberOfOutPorts()];
		bound = getNumberOfOutPorts();
		for (int i = 0; i < bound; i++) {
			result[i] = new Population(getOperatorGraph().getSchedule());
		}
		result[choose] = input[0];
		return result;
	}

	public String getDescription() {
		return "This operator chooses one of its outports at random and sends the whole " + "input to this outport. The distribution over the outports can be specified by " + "the properties Uniformly and Distribution.";
	}

	public String getName() {
		return "Random Choice";
	}

	public void setPropertyUniformly(Boolean b) {
		uniformly = b.booleanValue();
		if (uniformly) {
			setProbUniformly();
		}
	}

	public Boolean getPropertyUniformly() {
		return new Boolean(uniformly);
	}

	private void setProbUniformly() {
		for (int i = 0; i < prob.length; i++) {
			prob[i] = new Double((double)1 / prob.length);
		}
	}

	public String getShortDescriptionForProbVector() {
		return "Distribution";
	}

	public String getShortDescriptionForUniformly() {
		return "Uniformly";
	}

	public String getLongDescriptionForProbVector() {
		return "This property specifies the distribution over the outports which determines to which outport the whole input is sent.";
	}

	public String getLongDescriptionForUniformly() {
		return "If checked, the probability distribution is set to the uniform distribution.";
	}

	public void setPropertyProbVector(Double[] vector) {
		double sum = 0;
		for (int i = 1; i < vector.length; i++) {
			sum = sum + vector[i].doubleValue();
			if (vector[i].doubleValue() < 0) {
				return;
			}
		}
		if (sum > 1) {
			return;
		}
		if (vector.length > 0) {
			vector[0] = new Double(1 - sum);
		}
		uniformly = false;
		prob = vector;
	}

	public Double[] getPropertyProbVector() {
		return prob;
	}

	public void addOutPort() {
		// First, the new outport is added.
		super.addOutPort();
		// Now, the number of real outports is counted.
		int numOutPorts = 0;
		for (int i = 0; i < getNumberOfOutPorts(); i++) {
			if (getOutPort(i).getNumberOfPartners() != 0) {
				numOutPorts++;
			}
		}
		if (numOutPorts < prob.length) {
			// This happens iff a graph is loaded. 
			return;
		}
		// Now, the probability vector is adjusted.
		// Therefore the old values are taken over...
		Double[] newProb = new Double[numOutPorts];
		for (int i = 0; i < prob.length; i++) {
			newProb[i] = prob[i];
		}
		// ... and new ones are added.
		double sum = 0;
		for (int i = 0; i < prob.length; i++) {
			sum = sum + newProb[i].doubleValue();
		}
		if (prob.length < numOutPorts) {
			newProb[prob.length] = new Double(1 - sum);
		}
		for (int i = prob.length + 1; i < numOutPorts; i++) {
			newProb[i] = new Double(0);
		}
		prob = newProb;
		// If the property uniformly is set true, the probability is adjusted.          
		if (uniformly) {
			setProbUniformly();
		}
		if (inspector != null) {
			inspector.getTable().updateTable();
		}
	}

	public void removeOutPort(int index) {
		// First, the ouport is removed.
		super.removeOutPort(index);
		// Now, the number of real outports is counted.
		int numOutPorts = 0;
		for (int i = 0; i < getNumberOfOutPorts(); i++) {
			if (getOutPort(i).getNumberOfPartners() != 0) {
				numOutPorts++;
			}
		}
		// Now, the probability vector is adjusted.
		// Therefore, the old values are taken over...          
		Double[] newProb = new Double[numOutPorts];
		for (int i = 0; i < numOutPorts; i++) {
			newProb[i] = prob[i];
		}
		// ...and the first element is changed since the sum of all elements
		// must be 1.           
		double sum = 0;
		for (int i = 1; i < numOutPorts; i++) {
			sum = sum + newProb[i].doubleValue();
		}
		if (prob.length > 0) {
			prob[0] = new Double(1 - sum);
		}
		prob = newProb;
		// If the property uniformly is set true, the probability is adjusted.
		if (uniformly) {
			setProbUniformly();
		}
		if (inspector != null) {
			inspector.getTable().updateTable();
		}
	}

}
