/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.postprocessor.booleanfunction;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Vector;

import freak.Freak;
import freak.core.control.Schedule;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.core.postprocessor.AbstractPostprocessor;
import freak.core.util.GraphViz;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.AndNode;
import freak.module.searchspace.logictree.Data;
import freak.module.searchspace.logictree.MultipleOperatorNode;
import freak.module.searchspace.logictree.OperatorNode;
import freak.module.searchspace.logictree.OperatorNodeVector;
import freak.module.searchspace.logictree.OrNode;
import freak.module.searchspace.logictree.StaticCompareNode;

/**
 * @author Robin Nunkesser
 *
 */
public class Count extends AbstractPostprocessor {
	   
	private static int outputNo=0;
	private static int nodeIndex;
	public static int minCount=2;
	private static int minCasesFulfilled=20;
	public static double minPercent=0.1;
	private static int maxChildren=3;
	private static int maxDepth=8;
	public static String fileName="count.dot";
	
	public Count(Schedule schedule) {
		super(schedule);
	}

	public String getName() {
		return "Count literals in monomials";
	}

	public String getDescription() {
		return "Counts literals in monomials";
	}
	
	private OperatorNodeVector extractMonomials(IndividualList individuals) {
		OperatorNodeVector monomials= new OperatorNodeVector();
		Iterator iInd = individuals.iterator();
		while (iInd.hasNext()) {
			Individual individual = (Individual)iInd.next();
			if (individual.getGenotype() instanceof BooleanFunctionGenotype) {
				BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)((BooleanFunctionGenotype)individual.getGenotype()).clone();
				OperatorNodeVector monomialsVector=bfg.getAllUsedAndNodes();
				Iterator iMon = monomialsVector.iterator();
				while (iMon.hasNext()) {
					monomials.add((AndNode)iMon.next());
				} 	
			}
			Freak.debug(individual.getGenotype().toString(),4);											
		}	
		return monomials;
	}
	
	private int[] determineFulfillment(OperatorNodeVector monomialsVector,BitSet fulfilledWithout) {
		int[] count=new int[Data.getNumCompareNodes()];
		Arrays.fill(count, -1);
		Iterator iMon = monomialsVector.iterator();
		Iterator iLit;
		while (iMon.hasNext()) {
			AndNode monomial = (AndNode)iMon.next();
			OperatorNodeVector literalVector=monomial.getChildren();
			iLit = literalVector.iterator();			
			while (iLit.hasNext()) {
				StaticCompareNode scn = (StaticCompareNode) iLit.next();
				int index = Data.getIndexOfCompareNode(scn);
				if (count[index]==-1) {
					BitSet fulfillment = (BitSet) scn.getValueBitset().clone();
					// conjunct with the hitherto gathered part 
					fulfillment.and(fulfilledWithout);
					fulfillment.xor(Data.getResultBitSet());
					fulfillment.flip(0,Data.getNumRows());					
					count[index]=fulfillment.cardinality();
				}
			}
		} 	
		return count;
	}
	

	// Übergebe valueBitSet des Literals und führe And darauf aus. Wenn erstes Literal, dann übergebe 1 Bitstring
	private void searchBestPredicting(OperatorNodeVector monomialsVector,countNode node,BitSet fulfilledWithout,int child,int depth) {
		// to implement this quickly it is not done as efficient as possible but in the same way as searchMostCommon
		// For example it suffices that the following is only done once
		// determine which literal predicts most observations in the monomialsVector
		Boolean orderByMCR=true;
		BitSet fulfilledWith=new BitSet(Data.getNumRows());
		fulfilledWith.set(0, Data.getNumRows());

		int[] count=countOccurences(monomialsVector);
		int[] fulfillment=determineFulfillment(monomialsVector,fulfilledWithout);	
			
		int highestCount=0;
		int highestFulfillment=0;
		int indexChosen=0;
		for (int i=0;i<Data.getNumCompareNodes();i++) {
			if (((orderByMCR) && (fulfillment[i]>highestFulfillment)) || ((!orderByMCR) && (count[i]>highestCount))) {
				highestCount=count[i];
				highestFulfillment=fulfillment[i];
				indexChosen=i;
			}
 		}
		minPercent=0.01;
		if ((highestCount>=minCount) && ((double)highestCount/(double)node.getCount()>=minPercent)){
			// construct a vector with all monomials containing the most common literal and a vector with the remaining monomials
			OperatorNodeVector monomialsWithMCL = new OperatorNodeVector();
			OperatorNodeVector monomialsWithoutMCL = new OperatorNodeVector();			
			Iterator iMon = monomialsVector.iterator();
			while (iMon.hasNext()) {
				boolean withMCL=false;
				AndNode monomial = (AndNode)iMon.next();
				OperatorNodeVector literalVector=monomial.getChildren();
				Iterator iLit = literalVector.iterator();
				while (iLit.hasNext()) {			
					StaticCompareNode literal=(StaticCompareNode)iLit.next();
					if (indexChosen==Data.getIndexOfCompareNode(literal)) {
						fulfilledWith=((BitSet) literal.getValueBitset().clone());
						fulfilledWith.and(fulfilledWithout);
						withMCL=true;
						iLit.remove();
	//					monomial.deleteChild(literal);
					}
				}
				if (withMCL) {
					if (monomial.getNumberOfChildren()>0) monomialsWithMCL.add((AndNode)monomial); 			
				} else {
					monomialsWithoutMCL.add((AndNode)monomial);
				}
			}
//			System.out.println(Data.getCompareNode(indexChosen));
			int fulfilledCases;
			int fulfilledControls=fulfilledWith.cardinality();
			BitSet fulfilledCasesBitSet=(BitSet) fulfilledWith.clone();
			fulfilledCasesBitSet.and(Data.getResultBitSet());
			fulfilledCases=fulfilledCasesBitSet.cardinality();
			fulfilledControls-=fulfilledCases;
//			System.out.println("erfüllt "+fulfilledCases+" Fälle und "+fulfilledControls+" Kontrollen.");
			if (fulfilledCases>=minCasesFulfilled) {
				BooleanFunctionGenotype newBfg=(BooleanFunctionGenotype) node.getGenotype().clone();
				//AndNode newMonomial=((AndNode) node.getGenotype().clone());
				newBfg.insertCompare(0,(StaticCompareNode) Data.getCompareNode(indexChosen).clone());
				if (node.label==0) newBfg.deleteCompare(0, 0);
		//		newMonomial.addChild((StaticCompareNode) Data.getCompareNode(indexChosen).clone());
				countNode newNode=new countNode(newBfg,highestCount,(double)highestCount/(double)node.getCount(),indexChosen);
				node.addChild(newNode);
				if ((monomialsWithMCL.size()>0) && (depth<maxDepth)) {
					searchBestPredicting(monomialsWithMCL,newNode,fulfilledWith,1,depth+1);
				}
				if ((monomialsWithoutMCL.size()>0) && (child<maxChildren)) {			
					searchBestPredicting(monomialsWithoutMCL,node,fulfilledWithout,child+1,depth);
				}
			}
		}
	}

	private int[] countOccurences(OperatorNodeVector monomialsVector) {
		int[] count=new int[Data.getNumCompareNodes()];
		Iterator iMon = monomialsVector.iterator();
		Iterator iLit;
		while (iMon.hasNext()) {
			AndNode monomial = (AndNode)iMon.next();
			OperatorNodeVector literalVector=monomial.getChildren();
			iLit = literalVector.iterator();			
			while (iLit.hasNext()) {			
				count[((StaticCompareNode)iLit.next()).getIndex()]++;
				//count[Data.getIndexOfCompareNode((StaticCompareNode)iLit.next())]++;
			}
		}		
		return count;
	}
	
	private void searchMostCommon(OperatorNodeVector monomialsVector,countNode node,int child,int depth) {
		// count which literal is the most common in the monomialsVector
		Freak.debug(" Determine most common", 5);
		int [] count=countOccurences(monomialsVector);
		
		int highestCount=0;
		int indexChosen=0;
		for (int i=0;i<Data.getNumCompareNodes();i++) {
			if (count[i]>highestCount) {
				highestCount=count[i];
				indexChosen=i;
			}
 		}
		Freak.debug("Most common: "+Data.getCompareNode(indexChosen).toString(), 5);
		
		if ((highestCount>=minCount) && ((double)highestCount/(double)node.getCount()>=minPercent)){
			// construct a vector with all monomials containing the most common literal and a vector with the remaining monomials
			OperatorNodeVector monomialsWithMCL = new OperatorNodeVector();
			OperatorNodeVector monomialsWithoutMCL = new OperatorNodeVector();			
			Iterator iMon = monomialsVector.iterator();
			while (iMon.hasNext()) {
				boolean withMCL=false;
				AndNode monomial = (AndNode)iMon.next();
				OperatorNodeVector literalVector=monomial.getChildren();
				Iterator iLit = literalVector.iterator();
				while (iLit.hasNext()) {			
					StaticCompareNode literal=(StaticCompareNode)iLit.next();
					if (indexChosen==literal.getIndex()) {
//					if (indexChosen==Data.getIndexOfCompareNode(literal)) {
						withMCL=true;
						iLit.remove();
	//					monomial.deleteChild(literal);
					}
				}
				if (withMCL) {
					if (monomial.getNumberOfChildren()>0) monomialsWithMCL.add((AndNode)monomial); 			
				} else {
					monomialsWithoutMCL.add((AndNode)monomial);
				}
			}
			BooleanFunctionGenotype newBfg=(BooleanFunctionGenotype) node.getGenotype().clone();
			//AndNode newMonomial=((AndNode) node.getGenotype().clone());
			newBfg.insertCompare(0,(StaticCompareNode) Data.getCompareNode(indexChosen).clone());
			if (node.label==0) newBfg.deleteCompare(0, 0);
	//		newMonomial.addChild((StaticCompareNode) Data.getCompareNode(indexChosen).clone());
			countNode newNode=new countNode(newBfg,highestCount,(double)highestCount/(double)node.getCount(),indexChosen);
			node.addChild(newNode);
			if ((monomialsWithMCL.size()>0) && (depth<maxDepth)) {
				searchMostCommon(monomialsWithMCL,newNode,1,depth+1);
			}
			if ((monomialsWithoutMCL.size()>0) && (child<maxChildren)) {			
				searchMostCommon(monomialsWithoutMCL,node,child+1,depth);
			}
		}
	}
	
	public void analyse(){
		Freak.debug("-------------------Extract Monomials-----------------------",3);
		OperatorNodeVector monomials= this.extractMonomials((IndividualList) this.getIndividuals());
		Freak.debug("-------------------Monomials-----------------------",3);
		if (monomials.size()>0) {
			BooleanFunctionGenotype rootBfg = new BooleanFunctionGenotype(((BooleanFunctionGenotype) this.getIndividuals().getIndividual(0).getGenotype()).getInputFilePath(),((BooleanFunctionGenotype) this.getIndividuals().getIndividual(0).getGenotype()).getSchedule());
			
//			BooleanFunctionGenotype rootBfgNewOrder = new BooleanFunctionGenotype(((BooleanFunctionGenotype) this.getIndividuals().getIndividual(0).getGenotype()).getInputFilePath(),((BooleanFunctionGenotype) this.getIndividuals().getIndividual(0).getGenotype()).getSchedule());
			countTree result = new countTree(rootBfg,monomials.size());
			Freak.debug("-------------------Search most common-----------------------",3);
			Freak.setDebugLevel(5);
			searchMostCommon(monomials,result.getRoot(),1,1);
			Freak.debug(result.getRoot().toString(),5);
			Freak.debug("-------------------Graph Erzeugung-----------------------",3);
			result.toGraphViz();
			Freak.debug("-------------------Graph Erzeugung erfolgreich-----------------------",3);

/*			OperatorNodeVector monomialsNewOrder= this.extractMonomials((IndividualList) this.getIndividuals());
			countTree resultNewOrder = new countTree(rootBfgNewOrder,monomials.size()); //Data.getNumRows()
			BitSet fulfilledWith=new BitSet(Data.getNumRows());
			fulfilledWith.set(0, Data.getNumRows());
			searchBestPredicting(monomialsNewOrder,resultNewOrder.getRoot(),fulfilledWith,1,1);
			Freak.debug(resultNewOrder.getRoot().toString(),5);
			Freak.debug("-------------------Graph Erzeugung-----------------------",3);
			resultNewOrder.toGraphViz();
			Freak.debug("-------------------Graph Erzeugung erfolgreich-----------------------",3);
			System.out.println("READY");
			System.out.flush();*/
		}
	}
	
	private class countNode {
		private Vector<countNode> children;
		private BooleanFunctionGenotype bfg;
		private int count;
		private double percent;
		private int index;
		private int label;
		/**
		 * Constructor for the root
		 * @param count
		 */
		public countNode(BooleanFunctionGenotype bfg,int count) {
			super();
			this.children=new Vector<countNode>();
			this.label=0;
			Count.nodeIndex=1;

			this.bfg=bfg;
			this.count = count;
		}
		/**
		 * @param an
		 * @param count
		 * @param index
		 */
		public countNode(BooleanFunctionGenotype bfg, int count, double percent,int index) {
			super();
			this.children=new Vector<countNode>();
			this.label=Count.nodeIndex;
			Count.nodeIndex++;

			this.bfg = bfg;
			this.count = count;
			this.percent=((double)new Double(percent*10000).intValue())/100;
			this.index = index;
		}

		public countNode() {
			super();
			this.children=new Vector<countNode>();
			this.label=Count.nodeIndex;
			Count.nodeIndex++;
		}

		void addChild(countNode child){
			if (child==null) return;
			children.add(child);
		}
		public String toString(){
			String rueckgabe="";
			if (percent>0) rueckgabe= rueckgabe+ ":"+this.count+","+this.percent+","+Data.getCompareNode(this.index).toString();
			countNode kind;
			for (int i=0; i < children.size();i++){
				kind = children.get(i);
				rueckgabe = rueckgabe+" "+kind.toString()+" ";
			}
			rueckgabe=rueckgabe+";";
			return rueckgabe;
		}
		private String buildGraph(GraphViz gv) {
			String rueckgabe=new String();
			if (this.label!=0) {
				gv.addln(this.getLabel()+" [label=\""+Data.getCompareNode(this.index).toString()+"\\n("+bfg.evaluate1s()+","+(Data.getNum0Rows()-bfg.evaluate0s())+")\\n"+this.count+" ("+this.percent+"%)\"];");	    
				rueckgabe=rueckgabe+bfg.toString();  //+"\n";
			} else {
				gv.addln(this.getLabel()+" [style=invis];"); 
			}
			countNode kind;
			for (int i=0; i < children.size();i++){
				kind = children.get(i);
			    if (this.label!=0) {
			    	gv.addln(this.getLabel()+" -> "+kind.getLabel()+";");
			    } else {		    	
			    	gv.addln(this.getLabel()+" -> "+kind.getLabel()+" [style=invis];");			    	
			    }
				rueckgabe = rueckgabe+kind.buildGraph(gv);
			}			
			return rueckgabe;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}
		/**
		 * @return the label
		 */
		public String getLabel() {
			return (new Integer(label)).toString();
		}
		/**
		 * @return the monomial
		 */
		public BooleanFunctionGenotype getGenotype() {
			return this.bfg;
		}
	}
	
	private class countTree {
		private countNode root;
		public countTree(BooleanFunctionGenotype bfg,int count) {
			super();
			this.root=new countNode(bfg,count);
		}
		/**
		 * @return the root
		 */
		public countNode getRoot() {
			return root;
		}
		
		// TODO Dateinamen
		public void toGraphViz(){
//			outputNo++;
			//Teste Grapherzeugung
			GraphViz gv = new GraphViz();
		    gv.addln(gv.start_graph());
		    String graphPopulation=this.root.buildGraph(gv);
		    gv.addln(gv.end_graph());
		    // Save Graph in dot format
		    File graphDot = new File(fileName);
		    try {
		    	FileWriter fout = new FileWriter(graphDot);
		        fout.write(gv.getDotSource());
		        fout.close();
		    }
		    catch (Exception e) {
		    	System.err.println("Error: I/O error while writing the dot source");
		    }
/*		    // Save Population building the graph
		    File graphPop = new File("count"+outputNo+".pop.txt");
		    try {
		    	FileWriter fout = new FileWriter(graphPop);
		        fout.write(graphPopulation);
		        fout.close();
		    }
		    catch (Exception e) {
		    	System.err.println("Error: I/O error while writing the dot source");
		    }
		    // Save Graph as Gif
		    File graphGif = new File("count"+outputNo+".gif");
			GraphViz.DOT="C:\\Program Files\\graphviz\\Graphviz\\bin\\dot";
		    try {
			    gv.writeGraphToFile(gv.getGraph(gv.getDotSource()), graphGif);
		    }
		    catch (Exception e1) {
				GraphViz.DOT="/usr/local/graphviz-2.12/bin/dot";
			    try {
				    gv.writeGraphToFile(gv.getGraph(gv.getDotSource()), graphGif);
			    }
			    catch (Exception e2) {		    	
			    	System.out.println("DOT not found");
			    }
		    }*/

		}		
	}
	
}
