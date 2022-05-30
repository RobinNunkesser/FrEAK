/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.fitness;


/**
 * Encapsules an instance of <code>FitnessFunction</code> and transforms the fitness values returned by the inner <code>FitnessFunction</code>. <br> Possible applications are: <ul> <li>affine transformations</li> <li>niching techniques</li> </ul>
 * @author  Dirk, Christian
 */
public interface FitnessTransformer extends FitnessFunction {

	/**
	 * Sets the fitness function whose fitness values are to be transformed by this FitnessTransformer.
	 * @param function  the fitness function whose output is to be transformed.
	 * @uml.property  name="fitnessFunction"
	 */
	public void setFitnessFunction(FitnessFunction function);

	/**
	 * Returns the fitness function whose fitness values are to be transformed.
	 * 
	 * @return the fitness function whose fitness values are to be transformed.
	 */
	public FitnessFunction getFitnessFunction();
	
}
