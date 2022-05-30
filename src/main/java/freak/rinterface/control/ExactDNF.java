/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@tu-dortmund.de>.
 */

package freak.rinterface.control;

import freak.core.control.Schedule;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.AndNode;
import freak.module.searchspace.logictree.DNFTree;
import freak.module.searchspace.logictree.Data;
import freak.module.searchspace.logictree.OrNode;
import freak.module.searchspace.logictree.StaticCompareNode;

/**
 * @author robin
 *
 */
public class ExactDNF {

	private static BooleanFunctionGenotype constructMonomial (Schedule schedule, ExactDNF.Count count) {
		DNFTree dnf=new DNFTree(new short[0],(short)0,(short)0,new OrNode(),3,100,false,false,"", schedule, 0, false);
		AndNode an = dnf.getEmptyAndNode();
		StaticCompareNode cn = (StaticCompareNode) Data.getCompareNode(count.get(0)).clone();
		dnf.insertCompareNoAndInTreeCheck(an,cn);
		dnf.insertAnd(an);
		for (int i=1;i<count.getLength();i++) {
			dnf.insertCompare(an, (StaticCompareNode) Data.getCompareNode(count.get(i)).clone());
		}
		BooleanFunctionGenotype bfg=new BooleanFunctionGenotype(dnf, schedule);
		return bfg;		
	}
	public static void doComputation(Schedule schedule){
		ExactDNF.Count count=new Count(3,9);
		while (!count.increase()) {
			BooleanFunctionGenotype bfg=constructMonomial(schedule,count);
			System.out.println(count.toString()+" "+bfg.toString()+" ("+bfg.evaluate0s()+","+bfg.evaluate1s()+")");
		}
				
	}
	private static class Count {
		private int length;
		private int size;
		private int[] count;
		private int position=0;
		private Count(int length,int size) {
			this.length=length;
			this.size=size;
			count=new int[length];
			position=length-1;
			for (int i=0;i<length-1;i++){
				count[i]=i;
			}
			count[length-1]=length-2;
		}
		public boolean increase() {
			if (count[length-1]<size) count[length-1]++; else {
				// Suche erste nicht durchgezŠhlte Position
				boolean breaking=false;
				for (int i=length-1;i>=0;i--) {
					if (count[i]<size-(length-1-i)) {
						count[i]++;
						for (int j=i+1;j<length;j++) {
							count[j]=count[j-1]+1;
						}					
						breaking=true;
						break;
					}
				}
				if (!breaking) return true;

/*				if (count[position]==size) {
					if (position==0) return true;
					position--;
				}
				count[position]++;
				for (int i=position+1;i<length;i++) {
					count[i]=count[i-1]+1;
				}*/
			}
			return false;			
		}
		public int get(int index) {
			return count[index];
		}
		public String toString() {
			StringBuffer toStr=new StringBuffer();
			for (int i=0;i<length-1;i++) {
				toStr.append(count[i]);
				toStr.append(",");
			}
			toStr.append(count[length-1]);
			return toStr.toString();
		}
		public int getLength() {
			return length;
		}
		
	}
}
