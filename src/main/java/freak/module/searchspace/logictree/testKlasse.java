/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import java.io.IOException;

public class testKlasse{
	
	public static void main(String[] args) {
	
    	Data.setDataLocation("smallTest.csv");
    	try { 
    		Data.readData();
    	} catch (IOException e) {
    		System.out.println("Unknown read error. Path set correctly ?");
    		System.exit(0);
    	}

		
/*		BitSet testit = new BitSet(5);
		testit.clear();
		testit.set(2,4);
		BitSet test2 = new BitSet();
		test2.set(0,3);
		testit.and(test2);
		System.out.println(testit.cardinality());*/
		
/*		Data data = new Data();
		data.setDataLocation("snap.csv");
		try { 
			data.readData();
		} catch(IOException e) {	
			System.out.println("Error while reading input file - correct path?");
		}*/          
		
/*		OperatorNodeVector compares = data.getCompareSubtrees();
		for (int i = 0; i < compares.size(); i ++){
			System.out.println( compares.get(i) );
		}*/

	/*	DNFTree TestTree = new DNFTree("snap.csv");
		
		for (int i=0; i<1000; i++){
			System.out.println(TestTree.getRandomCompareNode());
		}*/
		
//		System.out.println("bla");
	//	TestTree = new DNFTree(); 	
		//System.out.println("Tree:"+TestTree);
		//TestTree.insertAndWithCompare();
//		TestTree.insertAndWithCompare();
//		TestTree.insertCompare();
	//	TestTree.insertCompare();
//		System.out.println("Tree:"+TestTree);
		//System.out.println(TestTree.evaluate());
		
//		TestTree.insertOperator();
//		TestTree.insertOperator();
//		System.out.println("Tree:"+TestTree);
//		TestTree.insertOperator();
//		TestTree.insertOperator();
//		TestTree.insertOperator();
//		System.out.println("Tree:"+TestTree);
//		TestTree.insertOperator();
//		TestTree.insertOperator();
//		TestTree.insertOperator();
//		System.out.println("Tree:"+TestTree);
//		TestTree.insertOperator();
	//	TestTree.deleteOperator();
//		TestTree.deleteOperator();
	//	TestTree.deleteOperator();
//		TestTree.deleteOperator();
//		TestTree.deleteOperator();
//		TestTree.deleteOperator();
	//	TestTree.evaluate();
//		System.out.println("keine exception.");
		//DNFTree ZweiterTree = new DNFTree();
	//	ZweiterTree.insertOperator();
//		ZweiterTree.evaluate();
		//DNFTree DritterTree = (DNFTree)TestTree.clone();
	//	System.out.println("Neuer Tree:"+DritterTree);
//		System.out.println("keine exception.");
	}
	
}
