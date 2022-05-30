/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.stoppingcriterion;

import freak.core.modulesupport.*;

/**
 * This interface represents a stopping criterion of an evolutionary algorithm.  
 * The method createEvents() inherited from Module can be used
 * to perform necessary initializations. In particular the criterion can be
 * registered at some event sources. 
 * 
 * @author Heiko
 */
public interface StoppingCriterion extends Module {

}
