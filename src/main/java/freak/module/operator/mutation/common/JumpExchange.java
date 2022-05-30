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

/**
 * This operator combines jump and exchange operators. The operator executes a number of local operations which is poisson distributed with the specified parameter <code>lambda</code>. When executing a local operation, either a jump operation or an exchange operation is performed. The probability that exchange will be used as local operation can be specified.
 * @author  Michael, Heiko
 */
public class JumpExchange extends PoissonOperation {

	/**
	 * With probability exchangeProb a single random exchange is done. With
	 * probability 1-exchangeProb a single random jump is performed.
	 */
	private double exchangeProb;
	private Exchange exchange;
	private Jump jump;

	/**
	 * Creates a new JumpExchange object.
	 */
	public JumpExchange(OperatorGraph graph) {
		super(graph);
		exchange = new Exchange(graph);
		jump = new Jump(graph);
		exchangeProb = 0.5;
	}

	/**
	 * With probability exchangeProb a single random exchange is done. With
	 * probability 1-exchangeProb a single random jump is performed.
	 */
	protected Individual performSingleMutation(Individual ind) {
		RandomElement re = graph.getSchedule().getRandomElement();
		if (re.raw() <= exchangeProb) {
			return exchange.performSingleMutation(ind);
		} else {
			return jump.performSingleMutation(ind);
		}
	}

	/**
	 * @return probability to do an exchange and not a jump.
	 */
	public Double getPropertyExchangeProb() {
		return new Double(exchangeProb);
	}

	/**
	 * Sets probability to do an exchange and not a jump.
	 */
	public void setPropertyExchangeProb(Double prob) {
		exchangeProb = prob.doubleValue();
	}

	public String getShortDescriptionForExchangeProb() {
		return "Prob of exchange";
	}

	public String getLongDescriptionForExchangeProb() {
		return "Probability to do an exchange and not a jump.";
	}

	public String getDescription() {
		return "With a probability a single random exchange is done. With 1-probability a single random jump is performed.";
	}

	public String getName() {
		return "Jump & Exchange";
	}
}
