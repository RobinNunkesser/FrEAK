/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace;

import freak.core.control.Schedule;
import freak.core.modulesupport.Configurable;
import freak.core.population.Genotype;
import freak.core.searchspace.AbstractSearchSpace;
import freak.module.searchspace.logictree.*;

import java.io.Serializable;

/**
 * @author  Melanie
 */

// HasDimension rausgenommen

public class BooleanFunction extends AbstractSearchSpace implements Configurable, Serializable{

//	private int dimension;
//	String pathInputFile = "smallTest.csv";
	String pathInputFile = "./data/snap.csv";
//	String pathInputFile = "fblr1_2_2.5.csv";
	Schedule schedule;
	
	/* 
	 * Creates a new object with default dimension 32.
	 * @param schedule a reference to a schedule object.  
	 */
	public BooleanFunction(Schedule schedule){
		super(schedule);
		this.schedule = schedule;
//		dimension = 32;
	}
	
	/*
	 * Gives the name of the search space.
	 */
	public String getName(){
		return "Boolean Functions";		
	}
	
	/*
	 * Gives a short description of the search space
	 */
	public String getDescription(){
		return "Represents the search space of all Boolean functions of (dimension) variables.";
	}
	
	/* 
	 * Gives the dimension of the search space. The dimension is the number of variables.
	 */
//	public int getDimension(){
//		return dimension;
//	}
	
	/* 
	 * Sets the dimension of the search space. The dimension is the number of variables.
	 * @param dim the value to which dimension is set.
	 */
//	public void setPropertyDimension(Integer dim){
//		if (dim.intValue() >= 1) 
//			dimension = dim.intValue();
//	}

	/* 
	 * Gives the dimension of the search space. The dimension is the number of variables.
	 */
//	public Integer getPropertyDimension(){
//		return new Integer(dimension);
//	}
	
	public void setPropertyInputPath(String path){
		pathInputFile = path;
	}
	
	public String getPropertyInputPath(){
		return pathInputFile;
	}
	
	public String getLongDescriptionForInputPath(){
		return "Path to the inputfile that contains data in cvs-format";
	}
	
	/* 
	 * Gives the number of different search points in the search space.
	 * EDIT
	 */
	public double getSize(){
		return 0;
	}
	
	/*
	 * Returns a random Genotype.
	 */
	public Genotype getRandomGenotype() {
		return new BooleanFunctionGenotype(pathInputFile,schedule);
	}
	
	public Genotype getLiteral(int index) {
		DNFTree dnf=new DNFTree(new short[0],(short)0,(short)0,new OrNode(),3,100,false,false,pathInputFile, schedule, 0, false);
		AndNode an = dnf.getEmptyAndNode();
		StaticCompareNode cn = (StaticCompareNode) Data.getCompareNode(index).clone();
		dnf.insertCompareNoAndInTreeCheck(an,cn);
		dnf.insertAnd(an);
		dnf.setEmptyAndsForbidden(true);
		dnf.setEmptyTreeForbidden(true);
		return new BooleanFunctionGenotype(dnf,schedule);
	}
	

}
