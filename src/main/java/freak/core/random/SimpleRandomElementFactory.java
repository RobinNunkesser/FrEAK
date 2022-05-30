/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.random;

import cern.jet.random.engine.*;
import edu.cornell.lassp.houle.RngPack.*;

/**
 * @author Stefan
 */
public class SimpleRandomElementFactory implements RandomElementFactory {
	private RandomElement seedGenerator;

	public SimpleRandomElementFactory() {
		this((int) (System.currentTimeMillis() % 1000));
	}

	public SimpleRandomElementFactory(int masterSeed) {
		seedGenerator = new MersenneTwister(masterSeed);
	}

	public RandomElement getRandomElement() {
		return new MersenneTwister(seedGenerator.choose(Integer.MIN_VALUE, Integer.MAX_VALUE));
	}
}
