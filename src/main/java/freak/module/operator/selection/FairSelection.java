/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection;

import java.util.Iterator;
import java.util.Vector;

import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.GraphException;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;

/**
 * @author Heiko
 */
public class FairSelection extends Selection implements CompatibleWithDifferentSearchSpaces {

	public FairSelection(OperatorGraph graph) {
		super(graph);
		super.addInPort();
		super.addOutPort();
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		IndividualList[] result = new IndividualList[1];
		if (input[0].size() == 0) {
			result[0] = input[0];
			return result;
		}
		
		int min = Integer.MAX_VALUE;		
		Vector selection = new Vector();
		Iterator it = input[0].iterator();
		while (it.hasNext()) {
			Individual ind = (Individual)it.next();
			int numSelected = 0;
			
			Object o = ind.getTag("NumSelected");
			if (o != null) {				
				numSelected = ((Integer)o).intValue(); 
			}
			
			if (numSelected < min) {
				selection = new Vector();
				selection.add(ind);
				min = numSelected;
			} else {
				if (numSelected == min) {
					selection.add(ind);
				}				
			}					
		}
		
		result[0] = new Population(getOperatorGraph().getSchedule());
		int n = getOperatorGraph().getSchedule().getRandomElement().choose(0,selection.size()-1);		
		Individual selected = (Individual)selection.elementAt(n);
		result[0].addIndividual(selected);
		
		Object o = selected.getTag("NumSelected"); 
		if (o != null) {
			int num = ((Integer)o).intValue()+1;			
			selected.addTag("NumSelected",new Integer(num),Individual.NOT_INHERITABLE);			
		} else {
			selected.addTag("NumSelected",new Integer(1),Individual.NOT_INHERITABLE);
		}

		return result;
	}

	public String getName() {
		return "Fair Selection";
	}

	public String getDescription() {
		return "Fair Selection";
	}

}
