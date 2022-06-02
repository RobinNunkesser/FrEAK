/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace;

import freak.core.control.ScheduleInterface;
import freak.core.population.*;
import freak.module.searchspace.logictree.AndNode;
import freak.module.searchspace.logictree.DNFTree;
import freak.module.searchspace.logictree.OperatorNodeVector;
import freak.module.searchspace.logictree.StaticCompareNode;

import java.io.Serializable;
import java.util.BitSet;
/**
 * @author  Melanie
 */
public class BooleanFunctionGenotype extends Genotype implements Serializable{

	private DNFTree dnfbaum;
//    private final int maxPercent = 100;
    private ScheduleInterface schedule;
    private String inputFilePath;
	
	/* **************************** Constructors ************************************ */
  
    public BooleanFunctionGenotype(String inputFilePath, ScheduleInterface schedule){
    	this(new DNFTree(inputFilePath,schedule,0,true),schedule);
	//	System.out.print("BooleanFunktGeno:Const\n");
	}
	
	public BooleanFunctionGenotype(DNFTree dnft, ScheduleInterface schedule){
		if (dnft instanceof DNFTree) {
			dnfbaum = dnft;
		}
		this.inputFilePath = dnft.getInputFilePath();
		this.schedule = schedule;
	}
	
	/* **************************** Get-Methods ************************************* */

	public int evaluateSize(){
		return dnfbaum.getTreeSize();
	}
	
	public int evaluate(){
		return dnfbaum.evaluate();
	}
	
	public BitSet getFullfilledLines()
	{
		return dnfbaum.getFullfilledLines();
	}

	public int getOptFitness(){
		return dnfbaum.getOptFitness();
	}
	
	public int getNum1Rows(){
		return dnfbaum.getNum1Rows();		
	}
	
	public int getNum0Rows(){
		return dnfbaum.getNum0Rows();
	}
	
	public int getNoOfMonomials(){
		return dnfbaum.getNoOfMonomials();
	}
	
	public int getMaximumMonomialSize(){
	    return dnfbaum.getMaximumMonomialSize();
    }
	
	public int evaluate1s(){
		return dnfbaum.evaluate1s();
	}
	
	public int evaluate0s(){
		return dnfbaum.evaluate0s();
	}
	
	public double paretoValue(){
		return dnfbaum.getTreeSize();
	}
	
	private DNFTree getInternTree(){
		return dnfbaum;
	}
	
	/**
	 * @return  the inputFilePath
	 * @uml.property  name="inputFilePath"
	 */
	public String getInputFilePath(){
		return inputFilePath;
	}
	
	/**
	 * @return  the schedule
	 * @uml.property  name="schedule"
	 */
	public ScheduleInterface getSchedule(){
		return schedule;
	}
	
	public BitSet getBitSet(){
		return dnfbaum.getBitset();
	}
	
	public BitSet getCharacteristicBitSet(){
		return dnfbaum.getCharacteristicBitSet();
	}	
	
	/* **************************** old methods, in work **************************** */

/*	public void mergeWith(BooleanFunctionGenotype bfg){
		DNFTree otherTree = bfg.getInternTree();
		dnfbaum.mergeWith(otherTree);
	}
	
	public void stealAnd(BooleanFunctionGenotype bfg){
		DNFTree otherTree = bfg.getInternTree();
		dnfbaum.stealAnd(otherTree);
	}
	
	public void insertOperator(double probAnd){
		dnfbaum.insertOperator(probAnd);
	}
	
	public void deleteOperator(double probAnd){
		dnfbaum.deleteOperator(probAnd);
	}
	
	public void 2(double probIns, double probInsAnd, double probDelAnd){
		int decision = randGen.choose(0,maxPercent);
//		int decision = rand.nextInt(maxPercent);
		
//		if (decision < andORCompare || root.getNumberOfChildren() == 0) {
		if (decision < 100*probIns) {
			insertAndWithCompare();		
//		int zz = rand.nextInt( (int)( Math.round(100*probIns) ) );
//		if (zz < 100*probIns) {
			insertOperator(probInsAnd);
		} else {
			deleteOperator(probDelAnd);			
		}
	}
	
	public void deleteAnd(){
		dnfbaum.deleteAnd();
	}
	
	public void pdeleteAnd(int factor){
		dnfbaum.pDeleteAnd(factor);
	}
	
	public void deleteCompare(){
		dnfbaum.deleteCompare();
	}
	
	public void insertAndWithCompare(){
	    dnfbaum.insertAndWithCompare();	
	}
	
	public void insertCompare(){
		dnfbaum.insertCompare();
	}
	
	public void pInsertCompare(int factor){
		dnfbaum.pInsertCompare(factor);
	}
	
	public void switchCompare(){
		dnfbaum.switchOperator();
	}
	*/
	/* **************************** new methods, in work **************************** */
	
	/**
	 * Returns an AndNode that exists in the DNFTree.
	 * @return randomly chosen AndNode.
	 */
	public AndNode getUsedAndNodeRandomly(){
		return dnfbaum.getUsedAndNodeRandomly();
	}

	/**
	 * Returns a new, empty AndNode.
	 * @return new, empty AndNode.
	 */
	public AndNode getEmptyAndNode(){
		return dnfbaum.getEmptyAndNode();
	}
	
	/**
	 * Returns a new AndNode with one StaticCompareNode as child.
	 * @return a new AndNode with one child.
	 */
	public AndNode getNewAndNodeWithCompareNode(){
		return dnfbaum.getNewAndNodeWithCompareNode();
	}
	
	/**
	 * Returns a list with copies of all used AndNodes in this DNFTree.
	 * @return a list of copies of all AndNodes
	 */
	public OperatorNodeVector getCopyOfAllUsedAndNodes(){
		return dnfbaum.getCopyOfAllUsedAndNodes();
	}

	/**
	 * Returns a list with all used AndNodes in this DNFTree.
	 * @return a list of all AndNodes
	 */
	public OperatorNodeVector getAllUsedAndNodes(){
		return dnfbaum.getAllUsedAndNodes();
	}
	
	/**
	 * Returns a child of the AndNode an.
	 * @param an The result node is chosen from the children list of an.
	 * @return A StaticCompareNode that is child of an (or null).
	 */
	public StaticCompareNode getUsedCompareNodeRandomly(AndNode an){
		return dnfbaum.getUsedCompareNodeRandomly(an);
	}
	
	/**
	 * Returns a StaticCompareNode from our List in the Data-class.
	 * @return a StaticCompareNode that exists in the current batch
	 */
	public StaticCompareNode getExistingCompareNodeRandomly(){
		return dnfbaum.getExistingCompareNodeRandomly();
	}
	
	/**
	 * Returns a list with copies of all children of this AndNode.
	 * @param an the children list of this node is copied
	 * @return a list with a copy of all compareNode in this AndNode-subtree.
	 */
	public OperatorNodeVector getAllUsedCompareNodes(AndNode an){
		return dnfbaum.getCopyOfAllUsedCompareNodes(an);
	}
	
	
	/**
	 * Deletes this AndNode from the DNFTree.
	 * @param an AndNode to be deleted.
	 */
	public void deleteAnd(AndNode an){
		dnfbaum.deleteAnd(an);
	}
	
	/**
	 * Deletes this StaticCompareNode from the given AndNode.
	 * @param an Parentnode, must have cn as child.
	 * @param cn StaticCompareNode to be deleted.
	 */
	public void deleteCompare(AndNode an, StaticCompareNode cn){
		dnfbaum.deleteCompare(an,cn);
	}

	public void deleteCompare(int index1, int index2){
		dnfbaum.deleteCompare(index1,index2);
	}
	
	/**
	 * Inserts the AndNode an to this DNFTree.
	 * @param an AndNode that should be inserted.
	 */
	public void insertAnd(AndNode an){
		dnfbaum.insertAnd(an);
	}
	
	/**
	 * Adds the StaticCompareNode cd to the AndNode an, if an is in the tree.
	 * @param an AndNode that gets new child
	 * @param cn StaticCompareNode to be added
	 */
	public void insertCompare(AndNode an, StaticCompareNode cn){
		dnfbaum.insertCompare(an,cn);
	}	
	
	public void insertCompare(int index, StaticCompareNode cn){
		dnfbaum.insertCompare(index,cn);
	}	

	/**
	 * Adds a list of AndNodes to the DNFTree.
	 * @param onodes list of AndNodes.
	 */
	public void insertListOfAndNodes(OperatorNodeVector onodes){
		dnfbaum.insertListOfAndNodes(onodes);
	}
	
	/**
	 * Sets the population this tree is in.
	 */
	public void setPopulation(int population){
		dnfbaum.setPopulation(population);
	}
	
	/**
	 * Gets the number of the population this tree is in.
	 */
	public int getPopulation(){
		return dnfbaum.getPopulation();
	}
	
	public String toDotGraph(){
		return dnfbaum.toDotGraph();
	}
	/* **************************** Overwritten standards *************************** */
	
	public String toString(){
		return dnfbaum.toString();
	}
	
	public boolean equals(Object o){
		return dnfbaum.equals(o);
	}
	
	public int hashCode(){
		return 0;
	}
	
	/**
	 * Clones a BooleanFunctionGenotype
	 **/
	public Object clone() {
		BooleanFunctionGenotype copy;
		DNFTree cp = (DNFTree)dnfbaum.clone();
		copy = new BooleanFunctionGenotype(cp, schedule);
		return copy;
	}

	/**
	 * @return  the dnfbaum
	 * @uml.property  name="dnfbaum"
	 */
	public DNFTree getDnfbaum() {
		return dnfbaum;
	}
	
}