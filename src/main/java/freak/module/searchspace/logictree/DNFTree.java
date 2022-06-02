/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import freak.core.control.ScheduleInterface;

import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Iterator;

/**
 * @author  Melanie
 */
public class DNFTree implements Serializable {

    // maximaler Wert, den die Variablen annehmen k�nnen
	private final int xyRange;
	
	// Parameter f�r Zufallsentscheidungen 
    private final int maxPercent;
	private boolean emptyAndsForbidden; // wenn true, dann darf es keine AndNode ohne Kinder geben (sind immer true)
	private boolean emptyTreeForbidden; // wenn true, dann darf es keine OrNode ohne Kinder geben (sind immer false)
	
	private String inputFilePath;
	
//	private static boolean isread = false;
	private static int currentBatch;
	
	private ScheduleInterface schedule;
	
	private OrNode root;
	
	private short[] count; 
	private short noFulfilledLines=0;
	private short noFulfilled1Lines=0;
	
// number of population this tree is in.	
	private int population=0;
	

	/* **************************** Constructors ************************************* */	
	
	/**
	 * Creates a new DNF-Tree with one orNode as root.
	 * Sets default parameters.
	 **/
	public DNFTree(String inputFilePath, ScheduleInterface schedule, int population, boolean neuLaden) {
		this(new short[0],(short)0,(short)0,new OrNode(),3,100,true,true,inputFilePath, schedule, population,neuLaden);	
		if (emptyTreeForbidden) {
			insertAndWithCompare();
		}
	}

	/**
	 * Creates a new DNF-Tree with a given orNode as root that has the given parameters.
	 * @param count short-array that contains how often each line of the input table is fullfilled by the tree
	 * @param xyRange maximum value input variables can have
	 * @param maxPercent Probability that a andNode is deleted instead of a compareNode (in the deleting operator)
	 * @parma andORCompare Probability that a andNode is created instead of a compareNode (in the creating operator) 
	 **/
	public DNFTree(short[] count, short noFulfilledLines, short noFulfilled1Lines, OrNode o, int xyRange, int maxPercent, //int opDelValue, int andORCompare, 
			boolean emptyAndsForbidden, boolean emptyTreeForbidden, String inputFilePath, ScheduleInterface schedule, int population, boolean neuLaden) {
		root = o;
		this.xyRange=xyRange;
		this.maxPercent=maxPercent;
		this.emptyAndsForbidden=emptyAndsForbidden;
		this.emptyTreeForbidden=emptyTreeForbidden;
		this.inputFilePath=inputFilePath;
		this.schedule = schedule;
		this.population = population;
		Data.setRandomElement(schedule);
//        if (! isread) {
		if (schedule.getCurrentBatch()>currentBatch || neuLaden){
			currentBatch = schedule.getCurrentBatch();
        	Data.setDataLocation(inputFilePath);
        	try { 
        		Data.readData();
        	} catch (IOException e) {
        		System.out.println("Unknown read error. Path set correctly ?");
        	//	throw new RuntimeException();
        	//	System.exit(0);
        	}
      //  	isread = true;        	
        }
		if (count.length==0){
			this.count = new short[Data.getNumRows()];
			this.noFulfilledLines = 0;
			this.noFulfilled1Lines = 0;
			for (int i = 0; i < this.count.length; i++){
				this.count[i] = 0;				
				if ((this.count[i]==1) == (Data.getResultOfNr(i))){
					this.noFulfilledLines++;
					if (this.count[i]==1) this.noFulfilled1Lines++;
				}
			}
		} else {
			this.count = new short[count.length];
			for (int i = 0; i < count.length; i++) this.count[i] = count[i];
			this.noFulfilledLines = noFulfilledLines;
			this.noFulfilled1Lines = noFulfilled1Lines;
		}
	}

	
	/* **************************** Evaluation Methods ******************************* */	
	
	/**
	 * Evaluates how many cases of the input-value-table are fullfilled by the current tree.
	 */
	public int getNumFullfilledCases(int andNode)
	{
		AndNode an = (AndNode)root.getChildAt(andNode);
		BitSet monom=an.getValueBitset();
		monom.and(Data.lineValues);
		int numFullfilledCases=monom.cardinality();
		return numFullfilledCases;
	}

	/**
	 * Evaluates how many controls of the input-value-table are not fullfilled by the current tree.
	 */
	public int getNumWrongControls(int andNode)
	{
		AndNode an = (AndNode)root.getChildAt(andNode);
		BitSet monom=an.getValueBitset();
		BitSet data=(BitSet)Data.lineValues.clone();

		data.flip(0, data.length());
		
		monom.and(data);
	
		int numFullfilledCases=monom.cardinality();
		return numFullfilledCases;
	}
	
	/**
	 * Evaluates the number of controls of the input-value-table with the property that 
	 * all and-node nodes outputs true. Therefore, there is no and-node that could explain these controls. 
	 */
	
	public int getNumControlsExplainedByNoOne(){
		
		BitSet result = new BitSet(count.length);
		result.clear();
		result.flip(0,result.length());
		
        for(int i=0;i<getNoOfMonomials();i++)
        {
        	AndNode an = (AndNode)root.getChildAt(i);
        	result.and(an.getValueBitset());
        }
		

		
		
		BitSet dataStatus = (BitSet)Data.getResultBitSet().clone();
		
		dataStatus.flip(0, dataStatus.length());
		result.and(dataStatus);
		int numControlsExplainedByNoOne=result.cardinality();
		
		return numControlsExplainedByNoOne;
	}

	public int getNumLiteralsInMonom(int andNode)
	{
		AndNode an = (AndNode)root.getChildAt(andNode);
		return an.getNumberOfChildren();
	}
	
	
	public int evaluate(){
/*		BitSet test = getBitset();
		test.get(0);
		int counteri = 0;
		for (int i = 0; i < test.length(); i++)
		 if (test.get(i)) counteri++;
	    System.out.println(counteri+" "+noFulfilledLines);
	    if (counteri!= noFulfilledLines) throw new RuntimeException("Wert stimmt nicht �berein!");*/
		return noFulfilledLines;
	}	

// von Thorsten:
	public BitSet getFullfilledLines()
	{
		BitSet testit = (BitSet) root.getValueBitset().clone();
		//System.out.println(testit);
		//int debug=testit.cardinality();
		testit.xor(Data.lineValues);
		testit.flip(0,root.numRows);
		//int debug2=Data.lineValues.cardinality();
		//int debug3=testit.cardinality();
		
		//System.out.println(Data.lineValues);
		//System.out.println(testit);
		//System.out.println("----");
		
		
		return testit;
	}

/*	public int evaluate(){
		int counter = 0;
		BitSet testit = root.getValueBitset();
		for (int i=0; i < Data.getNumRows();i++){
			if ( (testit.get(i)) == Data.getResultOfNr(i)) {
				counter++;			
			}
		}
		System.out.println("("+counter+","+noFulfilledLines+")");
		if (noFulfilledLines != counter) throw new RuntimeException("noFulfilledLines is wrong!");
		return noFulfilledLines;
	}*/
	
	/**
	 * Counts how many rows of the input-value-table are fullfilled by the current tree,
	 * but counts only those where the value of the row is 1.
	 * @return
	 */
	public int evaluate1s(){
		return noFulfilled1Lines;
	}
/*    public int evaluate1s(){
		int counter = 0;
		BitSet testit = root.getValueBitset();
		for (int i=0; i < Data.getNumRows();i++){
			if (Data.getResultOfNr(i)){
				if ( (testit.get(i)) == Data.getResultOfNr(i)) {
					counter++;			
				}
			}
		}
		System.out.println("("+counter+","+noFulfilled1Lines+")");
		if (noFulfilled1Lines != counter) throw new RuntimeException("noFulfilled1Lines is wrong!");		
		return counter;    		
    }*/

	public int evaluate0s(){
		return noFulfilledLines-noFulfilled1Lines;
	}
	
	/**
	 * Returns a BitSet that contains whether this DNFTree returns 1
	 * for the variable assignment in each line of the input table.
	 * Does not consider the function value in the table!
	 */
	public BitSet getCharacteristicBitSet(){
		BitSet result = new BitSet(count.length);
		for (int i = 0; i <count.length; i ++){
			if (count[i]>0) result.set(i);
		}
		return result;
	}

	/**
	 * Returns a BitSet that contains whether this DNFTree returns 1
	 * for the variable assignment in each line of the input table.
	 * Does not consider the function value in the table!
	 */
	public int[] getCharacteristicIntSet(){
		int[] result = new int[count.length];
		for (int i = 0; i <count.length; i ++){
			if (count[i]>0) {
				result[i]=1; 
			} else {
				result[i]=0;
			}
		}
		return result;
	}

	/**
	 * Returns a BitSet that contains a 1 if the function value in this line is the
	 * same as the value in the DNFTree and a 0 elsewise.
	 */
	public BitSet getBitset(){
		BitSet result = new BitSet(count.length);
		result = Data.getResultBitSet();
		BitSet countSet = getCharacteristicBitSet();		
		result.xor(countSet);		
		result.flip(0,Data.getNumRows());
		return result;
	}


	/* **************************** Get-Methods ************************************** */	    
    
    /**
     * Gives the number of lines in the input table.
     * @return
     */
    public int getOptFitness(){
    	return Data.getNumRows();
    }
    
    public int getNum1Rows(){
    	return Data.getNum1Rows();
    }
    
    public int getNum0Rows(){
    	return Data.getNum0Rows();
    }
    
	/**
	 * Returns the number of CompareNodes in this subtree.
	 */
	public int getTreeSize(){
		return root.getSubtreeSize();
	}
		
	 /**
	  * Returns the maximum size of a monomial.
      */
	public int getMaximumMonomialSize() {
		return root.getMaximumMonomialSize();
	}	
	
	/**
	* Returns the number of monomials in this subtree.
	*/
	public int getNoOfMonomials(){
		return root.getNumberOfChildren();
	}
	
	/**
	 * Gives the root of this DNFTree.
	 * @uml.property  name="root"
	 */
	// f�r die equals methode
	public MultipleOperatorNode getRoot(){
		return root;
	}
	
	/**
	 * Gives the number of the population this tree is in.
	 * @uml.property  name="population"
	 */
	public int getPopulation(){
		return population;
	}
	
	/**
	 * Gives the inputFilePath
	 * @uml.property  name="inputFilePath"
	 */
	public String getInputFilePath(){
		return inputFilePath;
	}
	
	/* **************************** set methods ******************************************* */
	
	/**
	 * Sets the number of the population this tree is in.
	 * @uml.property  name="population"
	 */
	public void setPopulation(int population){
		this.population = population;
	}
	
	/* **************************** new methods: getting nodes **************************** */
	
	private void rootCheck(){
		if (root == null) throw new RuntimeException("Root is null!");
	}
	
	private void AndNodeCheck(AndNode an){
		rootCheck();
		if (an == null) return;
		if ((!root.contains(an))) throw new RuntimeException("This node is not in the tree!");		
	}
	
	private void AndNodeCheck(int index){
		rootCheck();
		if (root.getChildAt(index)==null) throw new RuntimeException("This node is not in the tree!");		
	}

	
	private void AndPlusCompareNodeCheck(AndNode an, StaticCompareNode cn){
		AndNodeCheck(an);
		if (!an.contains(cn)) throw new RuntimeException("This StaticCompareNode is not a child of this AndNode!");
	}

	private void AndPlusCompareNodeCheck(int index1, int index2){
		AndNodeCheck(index1);
		if (((AndNode)root.getChildAt(index1)).getChildAt(index2)==null) throw new RuntimeException("This StaticCompareNode is not a child of this AndNode!");
	}
	
	private void CompareNodeValidCheck(StaticCompareNode cn){
		if (!Data.compareNodeValid(cn)) throw new RuntimeException("This StaticCompareNode is not known!");
	}
	
    /**
     * Randomly chooses an AndNode within this DNFTree.
     * @return the chosen AndNode
     */
	public AndNode getUsedAndNodeRandomly(){
		AndNode an = root.getRandomChildAnd();
//		if (an!=null) return (AndNode)(an.clone()); else return null;
		if (an!=null) return an; else return null;
	}
	
	/**
	 * Returns a list with copies of all AndNodes of this DNFTree.
	 * @return a list with copies of all AndNodes
	 */
	public OperatorNodeVector getCopyOfAllUsedAndNodes(){
		rootCheck();
		return root.getCopyOfChildren();
	}

	/**
	 * Returns a list with all AndNodes of this DNFTree.
	 * @return a list with all AndNodes
	 */
	public OperatorNodeVector getAllUsedAndNodes(){
		rootCheck();
		return root.getChildren();
	}
	
	/**
	 * Randomly chooses a <code>StaticCompareNode</code> that is child of the given <code>AndNode an</code>.
	 * @param an
	 * @return the chosen <code>StaticCompareNode</code>
	 */
	public StaticCompareNode getUsedCompareNodeRandomly(AndNode an){
        AndNodeCheck(an);
		if (an == null) return null;
		StaticCompareNode cn = an.getRandomChildComp();
//		if (cn!=null) return (StaticCompareNode)(cn.clone()); else return null;
		if (cn!=null) return cn; else return null;
	}
	
	/**
	 * Returns a list with copies of all AndNodes of the given AndNode <code>an</code>.
	 * @param an the children list of this node is copied
	 * @return a liste with copies of all AndNodes
	 */	public OperatorNodeVector getCopyOfAllUsedCompareNodes(AndNode an){
		AndNodeCheck(an);
		return an.getCopyOfChildren();
	}
	
		/**
		 * Gives an empty <code>AndNode</code>.
		 * @return
		 */
		public AndNode getEmptyAndNode(){
			return new AndNode();
		}
		
		/**
		 * Gives a new constructed <code>AndNode</code> with one child that is a <code>StaticCompareNode</code>
		 * from the stored nodes in <code>Data</code>.
		 * @return
		 */
		public AndNode getNewAndNodeWithCompareNode(){
			AndNode an = new AndNode();
			StaticCompareNode cn = Data.getExistingCompareNodeRandomly();
			an.addChild(cn);
			return an;
		}
		
		/**
		 * Randomly returns one of the existing <code>StaticCompareNodes</code> from <code>Data</code>.
		 * @return a <code>StaticCompareNode</code> from our <code>StaticCompareNodeList</code>.
		 */
		public StaticCompareNode getExistingCompareNodeRandomly(){
			return Data.getExistingCompareNodeRandomly();
		}
	 
	 /* **************************** new methods: deleting nodes **************************** */
	 
	private void subBitSetFromCount(BitSet bs){
//		System.out.print("sub");
		int noOfRows = Data.getNumRows();
		 // von Melanie
		for (int i = 0; i < noOfRows; i++){
			if (bs.get(i)){				
				count[i]--;
				if (count[i] == 0){
					boolean fncValue = Data.getResultOfNr(i); 
					if (fncValue) {
						noFulfilledLines--; 
						noFulfilled1Lines--;
					} else noFulfilledLines++;
				}
			}
//			System.out.print(count[i]+" ");
		}
		 /* von mir
		for (int i = 0; i < noOfRows; i++){
			if (bs.get(i)==Data.getResultOfNr(i)){
				if (bs.get(i)){
					noFulfilledLines--;
					noFulfilled1Lines--;
				} else {
					noFulfilledLines--;						
				}
			}
		}		
		*/
/*		System.out.println();
		System.out.print("sub");
		BitSet bsr = root.getValueBitset();
		for (int i = 0; i < noOfRows; i++){
			if (bsr.get(i)) System.out.print("1 "); else System.out.print("0 ");
		}
		System.out.println();
		System.out.println();*/
	}
		
	/**
	 * Deletes the <code>AndNode</code> <code>an</code> from the tree.
	 * If this is the last node in the tree and emptry trees are forbidden, it will not be deleted.
	 * @param an This AndNode shall be deleted.
	 */
	public void deleteAnd(AndNode an){
		AndNodeCheck(an);
		if (!emptyTreeForbidden || root.getNumberOfChildren() > 1) {
			BitSet bs = an.getValueBitset();
			subBitSetFromCount(bs);
			root.deleteChild(an);
		}
	}
	
	/**
	 * Deletes the <code>StaticCompareNode cn</code> from the childrenlist of the AndNode <code>an</code>.
	 * If empty Ands are forbidden and cn is the last child of an, it will not be deleted.
	 * @param an parent
	 * @param cn compareNode to be deleted
	 */
	public void deleteCompare(AndNode an, StaticCompareNode cn){
		AndPlusCompareNodeCheck(an,cn);
		if (!emptyAndsForbidden || an.getNumberOfChildren() > 1) {
			BitSet bs = an.getValueBitset();
			subBitSetFromCount(bs);
			an.deleteChild(cn);
			bs = an.getValueBitset();
			addBitSetToCount(bs);
		}
	}

	public void deleteCompare(int index1, int index2){
		AndPlusCompareNodeCheck(index1,index2);
		AndNode an=(AndNode)root.getChildAt(index1);
		StaticCompareNode cn=(StaticCompareNode)an.getChildAt(index2);
		deleteCompare(an,cn);
	}
	
	 /* **************************** new methods: inserting nodes **************************** */	
	
	private void addBitSetToCount(BitSet bs){
		int noOfRows = Data.getNumRows();
//		short[] countcopy = new short[count.length];
		 // von Melanie
		for (int i = 0; i < noOfRows; i++){
//			countcopy[i] = count[i];
			if (bs.get(i)){
				if (count[i]==0) {
					boolean fncValue = Data.getResultOfNr(i);
					if (fncValue) {
						noFulfilledLines++;
						noFulfilled1Lines++;
					} else {
						noFulfilledLines--;						
					}
				}
				count[i]++;
			}
//			System.out.print(count[i]+" ");
		}
		/* von mir
		for (int i = 0; i < noOfRows; i++){
			if (bs.get(i)==Data.getResultOfNr(i)){
				if (bs.get(i)){
					noFulfilledLines++;
					noFulfilled1Lines++;
				} else {
					noFulfilledLines++;						
				}
			}
		}
		*/
//		System.out.println();		
//		BitSet bsr = root.getValueBitset();
//		for (int i = 0; i < noOfRows; i++){
//			if (bsr.get(i)) System.out.print("1 "); else System.out.print("0 ");
//			if ( (bsr.get(i)) && (count[i] == 0)){
//				System.out.println("Panik");
//			}
//		}
//		System.out.println();
//		System.out.println();
	}
	
	private AndNode convertONtoAN(OperatorNode on){
		if (!(on instanceof AndNode)) throw new RuntimeException("on is no AndNode.");
		return (AndNode)on;
	}
	
	/**
	 * Adds the <code>AndNode an</code> to this tree.
	 * If empty Ands are forbidden and this <code>AndNode</code> has no children then it will be ignored.
	 * @param an <code>AndNode</code> to add to the tree.
	 */
	public void insertAnd(AndNode an){
		rootCheck();
		if (an == null) throw new RuntimeException("You are trying to insert an AndNode that is null.");
		if (!emptyAndsForbidden || an.getNumberOfChildren() != 0){
			root.addChild(an);
			BitSet bs = an.getValueBitset();
			addBitSetToCount(bs);
		}
	}

	/**
	 * Inserts a list of AndNodes to this DNFTree.
	 * If empty Ands are forbidden then <code>AndNode</code>s without children will be ignored.
	 * @param onodes list of AndNodes
	 */
	public void insertListOfAndNodes(OperatorNodeVector onodes){
		for (int i = 0; i < onodes.size(); i++){
			OperatorNode on = onodes.get(i);
			AndNode an = convertONtoAN(on);
			insertAnd(an);
//			if (!emptyAndsForbidden || an.getNumberOfChildren() != 0) root.addChild(an);	
		}
	}
	
	/**
	 * Adds the <code>StaticCompareNode cn</code> to the existing <code>AndNode an</code>.
	 * @param an Parentnode, must exist.
	 * The <code>StaticCompareNode</code> must be one of those that are stored in the <code>Data</code>-Class.
	 * @param cn New Child.
	 */
	public void insertCompare(AndNode an, StaticCompareNode cn){
		AndNodeCheck(an);
		BitSet bs = an.getValueBitset();
		subBitSetFromCount(bs);
		insertCompareNoAndInTreeCheck(an,cn);
		bs = an.getValueBitset();
		addBitSetToCount(bs);
	}
	
	public void insertCompare(int index, StaticCompareNode cn){
		AndNodeCheck(index);
		AndNode an=(AndNode) root.getChildAt(index);
		insertCompare(an,cn);
	}
	
	/**
	 * Adds the <code>StaticCompareNode cn</code> to <code>AndNode an</code>.
	 * an must not be null, but it needn't be in the tree.
	 * @param an Parentnode
	 * The <code>StaticCompareNode</code> must be one of those that are stored in the <code>Data</code>-Class.
	 * @param cn New Child.
	 */
	public void insertCompareNoAndInTreeCheck(AndNode an, StaticCompareNode cn){
		CompareNodeValidCheck(cn);
		if (an != null) an.addChild(cn);
//		System.out.println("Compare added!");
	}
	
	/*
	 * Needed to assure that a tree has an AndNode at the start.
	 */ 
	private void insertAndWithCompare(){
		AndNode an = getEmptyAndNode();
		StaticCompareNode cn = getExistingCompareNodeRandomly();
		insertCompareNoAndInTreeCheck(an,cn);
		insertAnd(an);
	}
	
	/* **************************** old methods, in work **************************** */	    
		
/*	public StaticCompareNode getRandomCompareNode(){
		OperatorNodeVector v = Data.getCompareSubtrees();
		int zz = nextIntFromRand(v.size());
//		System.out.println((CompareNode)v.get(zz));
		return (StaticCompareNode)v.get(zz);
	}

// f�r mergen
	private OperatorNodeVector getCopyOfChildren(){
		return root.getCopyOfChildren();
	}
	
	private AndNode getRandomAndNode(){
		AndNode an = (AndNode)root.getRandomChild();
		if (an!=null) return (AndNode)an.clone(); else return null;
	}*/
	
	/**
	 * Adds all AndNodes of ind to this DNFTree.
	 */
/*	public void mergeWith(DNFTree ind){
		OperatorNodeVector newNodes = ind.getCopyOfChildren();
		root.addChildrenVector(newNodes);
	}
	*/
	/**
	 * Gets one And-Children from ind and adds it to this DNFTree.
	 */
/*	public void stealAnd(DNFTree ind){
//		System.out.print(ind+" "+this+" ");
		AndNode an = ind.getRandomAndNode();
		if (an != null)	root.addChild(an);
//		System.out.println(this);
	}
	*/
	/**
	 * Inserts an andNode with one compareNode as child.
	 */
/*	public void insertAndWithCompare(){
		AndNode an = new AndNode();
		root.addChild(an);
    	StaticCompareNode cn = getRandomCompareNode();
		an.addChild(cn);
//	        System.out.println("andNode hinzugef�gt.");
//	        System.out.println("compareNode hinzugef�gt.");	
	}
	*/
	/**
	 * Inserts a compareNode to an existent andNode.
	 */
/*	public void insertCompare(){
		if ( root.getNumberOfChildren() > 0){
			AndNode an = (AndNode)root.getRandomChild();
			StaticCompareNode cn = getRandomCompareNode();
			an.addChild(cn);
		}
	}*/
	
	/**
	 * Inserts a compareNode to each AndNode with Probability factor / # AndNodes
	 */
	/*public void pInsertCompare(int factor){
		System.out.print("compare inserted to: ");
		int n = root.getNumberOfChildren();
		if (root.getNumberOfChildren() > 0) {
			for (int i = 0; i < n; i++) {
				int zz = nextIntFromRand(n);
				if (zz < factor) {
					System.out.print(i);
					AndNode an = (AndNode)root.getChildAt(i);
					StaticCompareNode cn = getRandomCompareNode();
					an.addChild(cn);
					System.out.print("("+an.getNumberOfChildren()+") ");
				}
			}
		}
		System.out.println();
	}*/
	
	/**
	 * Places a new andNode without one child or adds a compareNode to an existent andNode. 
	 * Alter Text:
	 * Places a new andNode with a random number of children
	 * or a new compareNode with its two children randomly.
	 */
/*	public void insertOperator(double probAnd){
		int decision = nextIntFromRand(maxPercent);
		
//		if (decision < andORCompare || root.getNumberOfChildren() == 0) {
		if (decision < 100*probAnd || root.getNumberOfChildren() == 0) {
			insertAndWithCompare();
//	        System.out.println("andNode hinzugef�gt.");
		} else {
			insertCompare();
//	        System.out.println("compareNode hinzugef�gt.");
		}
	}*/
	
/*	/**
	 * Deletes an andNode from the DNFTree.
	 **/
/*	public void deleteAnd(){
		if ( (! (emptyTreeForbidden)) || (root.getNumberOfChildren()>1)) {
			root.deleteRandomChild();		
		}
//		System.out.println("AndNode gel�scht");
    }*/

/*	/**
	 * Deletes each AndNode with probabilitiy factor / # AndNodes
	 */
/*	public void pDeleteAnd(int factor){
		System.out.print("deleted: ");
		int n = root.getNumberOfChildren();
		if ((!(emptyTreeForbidden)) || (root.getNumberOfChildren() > 1)) {
			int[] zahlen = new int[n];
			OperatorNode[] children = new OperatorNode[n]; 
			for (int i = 0; i < n; i++) {
				zahlen[i] = i;
				children[i] = root.getChildAt(i);
			}
			zahlen = permute(zahlen);
			for (int i = 0; i < n; i++) {
				int zz = nextIntFromRand(n);
				if (zz < factor) {
					if ((!(emptyTreeForbidden))
							|| (root.getNumberOfChildren() > 1)) {
						root.deleteChild(children[zahlen[i]]);
						System.out.print(zahlen[i]+" ");
					}
				}
			}
		}
		System.out.println();
	}
	
	/**
	 * Deletes a compareNode from the DNFTree.
	 * If empty trees are forbidden, a compare Node is only deleted if there are other And or CompareNodes.
	 */
/*	public void deleteCompare(){
		// if empty trees forbidden and only one Andnode => only delete if more than one comparenode
		if (emptyTreeForbidden && root.getNumberOfChildren()==1){
			AndNode an = (AndNode)root.getChildAt(0);
			if (an.getNumberOfChildren()>1){
				an.deleteRandomChild();	            
			}
		} else
		{
			AndNode an = (AndNode)root.getRandomChild();
			if (an!= null){
				an.deleteRandomChild();
	            if (emptyAndsForbidden) {
	            	if (an.getNumberOfChildren()==0) {
	            		root.deleteChild(an);
	            	}
	            }
//	            System.out.println("CompareNode gel�scht.");
			}					
		}
	}
	
	/**
	 * Deletes a operator and the connected branch randomly.
	 * It is possible that no node is deleted if the number of nodes is very small.
	 */
/*	public void deleteOperator(double probAnd){

		// methode geht von folgender Struktur aus:
		// wurzel = orNode
		// hat mehrere kinder, alle andNode
		// diese haben jeweils compareNodes als Kinder
		// => operator l�schen hei�t hier speziell,
		// eine AndNode oder eine CompareNode zu l�schen.
		int decision = nextIntFromRand(maxPercent);
				
		if (root.getNumberOfChildren()==0) return;
		
		// AndNode l�schen
//		if (decision < opDelValue && root.getNumberOfChildren() > 0) {
		if (decision < 100*probAnd && root.getNumberOfChildren() > 0) {
			
            deleteAnd();
			
		} else // CompareNode l�schen
		{
			
            deleteCompare();
            
		}
	}
	
	/**
	 * Replaces one compareNode randomly by another.
	 */
/*	public void switchOperator(){
		// methode geht von folgender Struktur aus:
		// wurzel = orNode
		// hat mehrere kinder, alle andNode
		// diese haben jeweils compareNodes als Kinder
		// => compareNodes befinden sich auf der zweiten
		// Ebene unterhalb der wurzel
		
		AndNode an = (AndNode)root.getRandomChild();
		if (an != null) {
			OperatorNode on = an.getRandomChild();
			an.deleteChild(on);
			StaticCompareNode cn = getRandomCompareNode();
			an.addChild(cn);
		}
		
	}
	
	private int nextIntFromRand(int max){
		return Data.nextRandInt(max);
	}
	
	private int[] permute(int[] zahlen){
		int n = zahlen.length;
		int[] permuted = new int[n];
		int remaining = n;
		for (int i = 0; i < n; i++){
			int zz = nextIntFromRand(remaining);
			permuted[i] = zahlen[zz];
			zahlen[zz] = zahlen[remaining-1];
			remaining--;
		}
		return permuted;
	}

	*/
	
	public String toDotGraph(){
		return root.toDotGraph();
	}

	public BitSet getLiteralBitSet () {
		return root.getLiteralBitSet();
	}

	/* **************************** Overwritten standardmethods ********************** */	    	
	
	/**
	 * Returns whether this DNFTree is equal to a given Object o.
	 * Equality means that the given object is also a DNFTree
	 * and that all children are equal in the sense of operatorNode-Equality.
	 */
	public boolean equals(Object o){
		if ( (o==null) || (!(o instanceof DNFTree))) return false;
		DNFTree dnft = (DNFTree)o;
		MultipleOperatorNode root2 = dnft.getRoot();
		return root.equals(root2);
	}
	
	/**
	 * Overrides the standard function to provide more information about the structure of the tree.
	 * & means AndNode
	 * | means OrNode
	 * ; means end of childrenlist
	 * value gives the value of constant nodes
	 * x_1 gives the status of an input node
	 * (x_4=2), (x_5!=3) gives the status of a compare node
	 */
	public String toString(){
		return root.toString();
	}
	
	
	public Object clone(){
		OrNode or = (OrNode)root.clone();
		DNFTree tree = new DNFTree(count, noFulfilledLines, noFulfilled1Lines, or, xyRange, maxPercent, //opDelValue, andORCompare, 
				emptyAndsForbidden, emptyTreeForbidden, inputFilePath, schedule, population,false);
        return tree;		
	}
	
	/*
	 * Updates the values in case of new Data
	 */
	public void update() {
		root.updateBitset();
		this.count = new short[Data.getNumRows()];
		this.noFulfilledLines = 0;
		this.noFulfilled1Lines = 0;
		for (int i = 0; i < this.count.length; i++){
			this.count[i] = 0;				
			if ((this.count[i]==1) == (Data.getResultOfNr(i))){
				this.noFulfilledLines++;
				if (this.count[i]==1) this.noFulfilled1Lines++;
			}
		}
		OperatorNodeVector onv= this.getAllUsedAndNodes();
		Iterator it=onv.iterator();
		while (it.hasNext()) this.addBitSetToCount(((OperatorNode)it.next()).getValueBitset());
	}

	public void setEmptyAndsForbidden(boolean emptyAndsForbidden) {
		this.emptyAndsForbidden = emptyAndsForbidden;
	}

	public void setEmptyTreeForbidden(boolean emptyTreeForbidden) {
		this.emptyTreeForbidden = emptyTreeForbidden;
	}

}