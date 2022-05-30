/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

/**
 * This interface must be implemented by all operators which work on more than
 * one search space and are therefore placed in a "common"-folder. Those
 * operators must check in their constructors whether they are compatible with 
 * the chosen search space or not. They must throw an
 * UnsupportedEnvironmentException in the latter case. 
 * 
 * @author Heiko, Michael
 */
public interface CompatibleWithDifferentSearchSpaces {
}