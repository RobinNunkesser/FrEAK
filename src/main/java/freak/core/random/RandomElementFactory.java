/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.random;

import java.io.*;

import edu.cornell.lassp.houle.RngPack.*;

/**
 * Produces the RandomElements for each run.
 * 
 * @author Stefan 
 */
public interface RandomElementFactory extends Serializable {
	/**
	 * Returns the RandomElement for the next run.
	 */
	RandomElement getRandomElement();
}
