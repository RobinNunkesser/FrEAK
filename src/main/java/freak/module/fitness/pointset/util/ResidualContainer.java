/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.util;

import java.io.Serializable;


public class ResidualContainer implements Comparable<ResidualContainer> , Serializable{
	
	public double signedResidual;
	public double squaredResidual;
	public int pointIndexInPointSet;
	
	public ResidualContainer(int oInd,double sgnRes, double sqrRes){
		this.pointIndexInPointSet = oInd;
		this.squaredResidual = sqrRes;
		this.signedResidual = sgnRes;
	}
	
	public int compareTo(ResidualContainer arg0) {
		// TODO Auto-generated method stub
		if (squaredResidual < arg0.squaredResidual) return -1;
		if (squaredResidual > arg0.squaredResidual) return 1;
		return 0;
		
	}
	
}