/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.searchspace.logictree;

import com.Ostermiller.util.CSVParser;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.*;
import freak.core.control.Schedule;
import freak.module.searchspace.BooleanFunctionGenotype;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

/**
 * @author  Melanie
 */
public class Data implements Serializable{
	// FŸr Gecco
	// 1= Standard
	// 2= Alles
	// 3= Alle != und ==
		
	public static int compareNodeSet=1;
	//flag that tells the program to not read from a .csv file when readData() is called
	//this flag should only be set to "true", when the program is run from R
	//consequently the default value is "false".
	//the R Interface will automatically set it to "true", when needed. 
	public static boolean csvDisable = false;
	
	public static RData testData=null;
	public static RData trainingData=null;
	
	public static BitSet lineValues;

	// Path to datasheet, file was found if valid is true
	private static String path;
	private static String lastPath;	
	private static boolean valid = false;
	private static boolean read = false;
	
	// Number of variables and rows
	private static int numVars;
    private static int numRows;	
    private static int num1Rows;
    
	// Array für Eingabedaten
	private static byte[][] values;
	private static byte[][] cvalues;
	// Array für Ergebnisspalte
	private static boolean[] results;  // boolsche werte
	private static BitSet resultsBS;
	// Array of variable names
	private static String[] names;
	// Arrays: range of variables
	private static byte[] minValue;
	private static byte[] maxValue;
	
	// OperatorNodeVector with all possible compareNode-subtrees
	private static OperatorNodeVector compareSubtrees;
	
	// Determine if the second in the range of variables is to be excluded 
	private static boolean exclude=true;
	
	private static boolean debug = false;
	
	private static Schedule schedule;
	private static RandomElement randGen;

	public static void clear() {
		csvDisable = false;		
		testData=null;
		trainingData=null;		
	}
	public static void setRandomElement(Schedule se){
		schedule = se;
		randGen = schedule.getRandomElement();
	}
	
	/**
	 * Gives the number of variables in the read inputdata.
	 * Returns -1 if no data is present.
	 */
	public static int getNumVar(){
		if (read) return numVars; else return -1;
	}
	
	/**
	 * Gives the number of rows in the input table.	  Returns -1 if no data is present.
	 * @uml.property  name="numRows"
	 */
	public static int getNumRows(){
		if (read) return numRows; else return -1;
	}
	
	/**
	 * Gives the number of rows in the input table where function value is 1. Returns -1 if no data is present.
	 * @uml.property  name="num1Rows"
	 */
	public static int getNum1Rows(){
		if (read) return num1Rows; else return -1;		
	}
	
	/**
	 * Gives the number of rows in the input table where function value is 0.
	 * Returns -1 if no data is present.
	 */
	public static int getNum0Rows(){
		if (read) return (numRows - num1Rows); else return -1;		
	}
	
	/**
	 * Gives the row of the input table with number nr.
	 * Returns null if no data is present.
	 * @param nr number of needed row, numbering starting with 0
	 */
	public static byte[] getRowByNumber(int nr){
		if (read) return values[nr]; else return null;
	}

	/**
	 * Gives the result of row nr.
	 * Returns false if no data is present.
	 * @param number of the row which result is needed, numbering starting with 0 
	 */
	public static boolean getResultOfNr(int nr){
		if (read) return results[nr]; else return false;
	}
	
	/**
	 * Gives a BitSet containing all function values of the input table.
	 * Returns null if no data is present.
	 */
	public static BitSet getResultBitSet(){
		return (BitSet)resultsBS.clone();
	}
	
	/**
	 * Gives the maximum Value of an input variable.
	 * Returns -1 if no data is present or var is out of range.
	 * @var number of input variable
	 */
	public static int getMaxValue(int var){		
		if (read && var < numVars){
			return maxValue[var];
		} else return -1;
	}

	/**
	 * Gives the minimum Value of an input variable.
	 * Returns -1 if no data is present or var is out of range.
	 * @var number of input variable
	 */
	public static int getMinValue(int var){		
		if (read && var < numVars){
			return minValue[var];
		} else return -1;
	}
	
	/**
	 * Returns the name of the variable.
	 * @param var number of the input variable
	 */
	public static String getVarName(int var){
		return names[var];
	}

	public static String getDataLocation(){
		return Data.path;
	}
	
	/**
	 * Sets the path of the datasheet that shall be used.
	 * @param path Path of the datasheet, must be valid!
	 * @return returns whether the path was successfully changed
	 */
	public static boolean setDataLocation(String pathV){
		boolean change;
		try {
			new FileReader(pathV);
			change = true;
		} catch (IOException e) {	
		    // Exception -> Abbruch ;)
			if (!csvDisable) {
				System.out.println("The given path to a datasheet was not valid!");
				System.out.println("The path will not be changed, it remains "+path);
				System.out.println("Current path valid: "+valid);
			}
			change = false;
		} 
		if (change) {
			path = pathV;
			valid = true;
			
			Freak.debug("Path was changed to the valid path "+pathV,1);
		}
		return change;
	}

	private static void setMaxAsThree(){
		maxValue = new byte[numVars];
		for (int i = 0; i < numVars; i++){
			maxValue[i] = 3;
		}
	}
	
	private static void constructCompares(){
		if (compareNodeSet!=1) exclude=false;
		 compareSubtrees = new OperatorNodeVector();
		 int index=0;
		 for (int i = 0; i < numVars; i++){
			 int minI = getMinValue(i);
			 int maxI = getMaxValue(i);
			 
			 // >= min entspricht <= max entspricht 1,
			 // >= min+1 entspricht != min
			 // <= min entspricht = min
			 // <= max-1 entspricht != max
			 // >= max entspricht = max
			 
			// switch (compareNodeSet)			 
			 for (int j = minI; j <= maxI ; j++){
				 if ((!exclude) || (j!=minI+1)) {
					 //=					 
					 StaticConstantNode cn1 = new StaticConstantNode(j);
					 StaticInputNode in1 = new StaticInputNode(i);
					 StaticCompareNode com1 = new StaticCompareNode(cn1,in1,false,true,false,numRows,values);
					 com1.setIndex(index++);
					 Freak.debug(com1.toString(),4);
					 compareSubtrees.add(com1);
					 /*System.out.println(com1.getIndex()+": "+ com1.toString());
					 System.out.println("Assigned Index"+compareSubtrees.indexOf(com1));
					 System.out.println("Equal? "+compareSubtrees.get(com1.getIndex()).toString());*/


					 //!=
					 StaticConstantNode cn3 = new StaticConstantNode(j);
					 StaticInputNode in3 = new StaticInputNode(i);
					 StaticCompareNode com3 = new StaticCompareNode(cn3,in3,true, false, true,numRows,values);
					 com3.setIndex(index++);
					 Freak.debug(com3.toString(),4);
					 compareSubtrees.add(com3);
					 /*System.out.println(com3.getIndex()+": "+ com3.toString());
					 System.out.println("Assigned Index"+compareSubtrees.indexOf(com3));
					 System.out.println("Equal? "+compareSubtrees.get(com3.getIndex()).toString());*/

					 //>=
					 if (j != minI && j != minI+1 && j!=maxI && compareNodeSet<3){
						 StaticConstantNode cn2 = new StaticConstantNode(j);
						 StaticInputNode in2 = new StaticInputNode(i);
						 StaticCompareNode com2 = new StaticCompareNode(cn2,in2,false, true, true,numRows,values);
						 com2.setIndex(index++);
						 Freak.debug(com2.toString(),4);
						 compareSubtrees.add(com2);
					 }
					 					 
					 //<=
					 if (j != minI && j!= maxI-1 && j != maxI && compareNodeSet<3){
						 StaticConstantNode cn4 = new StaticConstantNode(j);
						 StaticInputNode in4 = new StaticInputNode(i);
						 StaticCompareNode com4 = new StaticCompareNode(cn4,in4,true, true, false,numRows,values);
						 com4.setIndex(index++);
						 Freak.debug(com4.toString(),4);
						 compareSubtrees.add(com4);
					 }
				 }
			 }
		 }
	}
	
	/**
	 * Returns a random integer in range 0..max-1.
	 * @param max maximum of random number (exclusively)
	 */
	public static int nextRandInt(int max){
//		return rand.nextInt(max);
		int zz = randGen.choose(0,max-1);
/*		if (zz==0) {
			System.out.print(" ");
		}
		System.out.print(zz+" ");*/
		return zz;
	}
	
	/**
	 * Returns a random boolean.
	 */
	public static boolean nextRandBoolean(){
		//return rand.nextBoolean();
		return (randGen.choose(0,1)==1);
	}
	
	/**
	 * Gives a OperatorNodeVector with all possible compare-Subtrees due to the actual data. Returns null if no data is present.
	 * @uml.property  name="compareSubtrees"
	 */
	public static OperatorNodeVector getCompareSubtrees(){
		if (read) return compareSubtrees; else return null;
	}
	
	public static boolean compareNodeValid(StaticCompareNode cn){
		return compareSubtrees.contains(cn);
	}

	public static int getIndexOfCompareNode(StaticCompareNode cn){
		// Has to be implemented manually because equals in StaticCompareNode is ambigous
		int index=-1;
		Iterator it=compareSubtrees.iterator();
		while(it.hasNext()) {
			index++;
			if (((StaticCompareNode)it.next()).toString().equals(cn.toString())) return index;
		}
		return index;
	}
	
	public static StaticCompareNode getCompareNode(int i){
		return (StaticCompareNode)compareSubtrees.get(i);
	}

	public static int getNumCompareNodes(){
		return compareSubtrees.size();
	}

	public static StaticCompareNode getExistingCompareNodeRandomly(){
		if (read) {
			if (compareSubtrees.size() == 0)
				return null;
			int nr = nextRandInt(compareSubtrees.size());
			return (StaticCompareNode) compareSubtrees.get(nr);
		} else
			return null;
	}

	/**
	 * Converts the first entry of a row in the table into an int.
	 * The first entry gives the value of the function. 
	 * It might be given as a number or as one of certain Strings.
	 * Throws an exception if the String is not an integer and is unknown. 
	 * @param obj string that comes from the first column of the input table 
	 * @return an integer
	 */
	private static int convertObjToInt(String obj){
		int result=0;
		if (obj.equals("true")
			    || obj.equals("case")
			    || obj.equals("wahr")
			    || obj.equals("Fall")) return 1;
		if (obj.equals("false")
			    || obj.equals("control")
			    || obj.equals("Kontrolle")
			    || obj.equals("falsch")) return 0;
		try {
		 result = (new Integer(obj)).intValue();
		} 
		catch (RuntimeException e){
			throw new RuntimeException("The string '"+obj+"' was contained in the input table and is unknown!");
		}
		return result;
	}
	
	
	/**
	 * Reads a datasheet from the current path if it is valid.
	 * 
	 */
	public static void readData() throws IOException{		
		Freak.setDebugLevel(3);
		if(csvDisable){
			//do not read anything because Data has already been delivered by R
		}else{ //normal execution of this method
			if (!path.equals(lastPath)) {
				byte[][] intValues;
				FileReader in;
				byte[] iresults;

				// Try to open the datasheet
				if (valid) {
					try {
						in = new FileReader(path);
					} catch (IOException e) {
						throw new IOException("Path to datasheet is no longer valid! ("+path+")");
					}
				} else {
					throw new IOException("Before reading data, a valid path must be set! ("+path+")");
				}

				// File could be opened => read Data
				// Read datasheet, seperated by semicolon
	
				CSVParser parser = new CSVParser(in, ';');		
				ArrayList<byte[]> v = new ArrayList<byte[]>();
				String[] line;
				byte[] intLine;
	
				//	The first row only contains names of variables
				if ((line = parser.getLine()) != null) {
					numVars = line.length-1;
			        names = new String[numVars];		
			        for (int i = 1; i <= numVars; i++) {
			        	names[i-1] = line[i];
			        }			
				}
				
				//	Reading rows
	
				int count=0; 
				while((line = parser.getLine()) != null){
					intLine=new byte[line.length];
					intLine[0]=(byte) convertObjToInt(line[0]);
					for (int i=1;i<line.length;i++) intLine[i]= (new Byte(line[i])).byteValue();
					v.add(intLine);
				 	Freak.debug("Line "+ ++count +" FK "+line[0]+" Value "+line[1],3);
				}
				intValues = new byte[v.size()][];
				v.toArray(intValues);
			
				// Reading rows, saving result column to extra array               
	
				// Size of tabular
				
				numRows = intValues.length;
				num1Rows = 0;
				
	
				// read data must be converted to integer,
				// the first column gives the result of the function
				
				values = new byte[numRows][];
				cvalues = new byte[numVars][];
				iresults = new byte[numRows];
				results = new boolean[numRows];
				maxValue = new byte[numVars];
				minValue = new byte[numVars];
				        
				Freak.debug("Tabular has "+numRows+" rows and "+numVars+" variables.",2);

			
				// Eingelesene Daten in ints umwandeln
				// und dabei die Beschriftungszeile entfernen
				// sowie die ergebnisspalte abspalten
		        
				for (int i = 0; i < numVars; i++){
					cvalues[i] = new byte[numRows];
				}
				lineValues=new BitSet(numRows);		
		        for (int i = 0; i < numRows; i++){
		        	values[i] = new byte[numVars];
		        	iresults[i] = intValues[i][0];
		        	results[i] = (iresults[i]==1);
		           	lineValues.set(i,results[i]);        	
		        	if (results[i]) num1Rows++;
		        		        	
		        	for (int j=1; j <= numVars; j++){
		        		values[i][j-1] = intValues[i][j];
		        		cvalues[j-1][i] = intValues[i][j];
		        	}
		        	
		        }
			
		        for (int j = 0; j < numVars; j++){
		            if (numRows == 0){
		            	maxValue[j] = -1;
		            	minValue[j] = -1;
		            } else {
		            	maxValue[j] = cvalues[j][0];
		            	minValue[j] = cvalues[j][0];
		            }
		            for (int i = 0; i < numRows; i++){
		            	if (cvalues[j][i] > maxValue[j]) maxValue[j] = cvalues[j][i];
		            	if (cvalues[j][i] < minValue[j]) minValue[j] = cvalues[j][i];            	
		            }
		        }
	        
	       
	  //      setMaxAsThree();
		        read = true;
		        lastPath=path;		        
		        Freak.debug("0-Rows: "+getNum0Rows()+", 1-Rows: "+getNum1Rows(),2);
		        constructCompares();
		        Freak.debug("Compares are finished.",2);
		        // construct a BitSet that contains the resultarray
		        
		        Freak.viewFilenameInTitle(path);
		        
		        resultsBS = new BitSet(results.length);
		        for (int i = 0; i < results.length; i++){
		        	if (results[i]) resultsBS.set(i);
		        	else resultsBS.clear(i);
		        }

			}    
	        
	        
		}
			

			
			
	}
		
	/**
	 * Reads a datasheet from the current path if it is valid.
	 * 
	 */
	public static void oldReadData() throws IOException{		
//		Freak.setDebugLevel(3);

		if(csvDisable){
			//do not read anything because Data has already been delivered by R
		}else{ //normal execution of this method

		String[][] svalues;
		FileReader in;
		int[] iresults;
		
		// Try to open the datasheet
		if (valid) {
			try {
				in = new FileReader(path);
			} catch (IOException e) {
				throw new IOException("Path to datasheet is no longer valid! ("+path+")");
			}
		} else {
			throw new IOException("Before reading data, a valid path must be set! ("+path+")");
		}
		
		// File could be opened => read Data
		// Read datasheet, seperated by semicolon

		System.out.println("Setting up Parser");
		CSVParser parser = new CSVParser(in, ';');		
		ArrayList<String[]> v = new ArrayList<String[]>();
		String[] line;
		System.out.println("Reading line");
		int count=0; 
		while((line = parser.getLine()) != null){
			v.add(line);
			System.out.println(++count);
		}
		svalues = new String[v.size()][];
		v.toArray(svalues);

		
		// File could be opened => read Data
		// Read datasheet, seperated by semicolon
/*		svalues = CSVParser.parse(
			    in, ';'
			);*/
		
		
		/*
		//Debug: table output
		for(int i = 0; i<=svalues.length-1; i++){
			for(int j = 0; j<=svalues[0].length-1; j++){
				System.out.print(" " + svalues[i][j]);				
			}
			System.out.println("\n");
		}
		*/
		
		
		// Size of tabular
		
		numRows = svalues.length-1;
		num1Rows = 0;
		numVars = svalues[0].length-1;
		

		// read data must be converted to integer,
		// the first column gives the result of the function
		
		values = new byte[numRows][];
		cvalues = new byte[numVars][];
		iresults = new int[numRows];
		results = new boolean[numRows];
		maxValue = new byte[numVars];
		minValue = new byte[numVars];
		
		// The first row only contains names of variables
        names = new String[numVars];		
        for (int i = 1; i <= numVars; i++) {
        	names[i-1] = svalues[0][i];
        }
        
        Freak.debug("Tabular has "+numRows+" rows and "+numVars+" variables.",2);
//        System.out.println("-------------------");
		if (debug){
			System.out.print("       ");
			for (int i = 0; i < numVars; i++){
				System.out.print(names[i].charAt(names[i].length()-2));
				System.out.print(names[i].charAt(names[i].length()-1)+" ");
			}
			System.out.println();
		}
		
        // Reading rows, saving result column to extra array               
        
		// Eingelesene Daten in ints umwandeln
		// und dabei die Beschriftungszeile entfernen
		// sowie die ergebnisspalte abspalten
        
		for (int i = 1; i <= numVars; i++){
			cvalues[i-1] = new byte[numRows];
		}
		lineValues=new BitSet(numRows);		
        for (int i = 1; i <= numRows; i++){
//        	System.out.println("Row"+i+" -------------------");
        	values[i-1] = new byte[numVars];
//        	iresults[i-1] = (new Integer(svalues[i][0])).intValue();
        	iresults[i-1] = convertObjToInt(svalues[i][0]);
        	results[i-1] = (iresults[i-1]==1);
           	lineValues.set(i-1,results[i-1]);        	
        	if (results[i-1]) num1Rows++;
        	
        	if (debug) {
        		System.out.print("Row "+i+": ");
        	}
        	
        	for (int j=1; j <= numVars; j++){
        		values[i-1][j-1] = (new Byte(svalues[i][j])).byteValue();
        		cvalues[j-1][i-1] = (new Byte(svalues[i][j])).byteValue();
        		if (debug) {
        			System.out.print(" "+values[i-1][j-1]+" ");        			
        		}
        	}
        	
        	if (debug){
        		System.out.println(results[i-1]);
        	}
        }

        
        for (int j = 0; j < numVars; j++){
            if (numRows == 0){
            	maxValue[j] = -1;
            	minValue[j] = -1;
            } else {
            	maxValue[j] = cvalues[j][0];
            	minValue[j] = cvalues[j][0];
            }
            for (int i = 0; i < numRows; i++){
            	if (cvalues[j][i] > maxValue[j]) maxValue[j] = cvalues[j][i];
            	if (cvalues[j][i] < minValue[j]) minValue[j] = cvalues[j][i];            	
            }
        }
        
        if (debug) {
        	for (int j = 0; j < numVars; j++){
    			System.out.print(names[j].charAt(names[j].length()-2));
    			System.out.print(names[j].charAt(names[j].length()-1)+" ");			
        	}
        	System.out.println();
            for (int i = 0; i < numRows; i++) {
            	System.out.print("Row "+i+":  ");
            	for (int j = 0; j < numVars; j++){
            		System.out.print(cvalues[j][i]+"  ");
            	}
            	System.out.println(results[i]);
            }
            for (int j = 0; j < numVars; j++){
            	System.out.print("---");
            }
            System.out.println("-------------");
            System.out.print("max     ");
            for (int j = 0; j < numVars; j++){
            	System.out.print(maxValue[j]+"  ");
            }
            System.out.println();
            System.out.print("min     ");
            for (int j = 0; j < numVars; j++){
            	System.out.print(minValue[j]+"  ");
            }
            System.out.println();
        }
       
  //      setMaxAsThree();
        read = true;
        Freak.debug("0-Rows: "+getNum0Rows()+", 1-Rows: "+getNum1Rows(),2);
        constructCompares();
        
        // construct a BitSet that contains the resultarray
        
        Freak.viewFilenameInTitle(path);
        
        resultsBS = new BitSet(results.length);
        for (int i = 0; i < results.length; i++){
        	if (results[i]) resultsBS.set(i);
        	else resultsBS.clear(i);
        }
		}

	}
		
	public static void setRData(RData data){
		Data.rReadData(data.getValues(),data.getDim(),data.getNames());
	}
	
	
	/**
	 * Reads a datasheet from R Data Frames
	 * 
	 */
	public static void rReadData(int[] rValues, int[] rDim, String[] rNames){
		
		//debug
		 Freak.debug("\nrValues[0]: " + rValues[0] + "\nrDim[0] " + rDim[0] + "\nrDim[1] " + rDim[1] + "\nrNames[0] " + rNames[0],2);

		int[] iresults;
		String[][] svalues = new String[rDim[0]+1][rDim[1]];
		//in svalues the first index designates the row and the second the column
		//rDim[0] contains the number of rows in the input and rDim[1] contains the number of columns
		//the number of rows is increased by 1 because the names have to be added into the first row of svalues
		
		
		//now we convert the data passed by R to a String[] equivalent to a String[] that would have been read from a CSV file,
		//if a CSV File had been used. Thus the code to convert the csv data to the desired format can be reused
		//###speed could be improved by modifying this method to take a direct approach instead of this indirect one
		
		
		//write the names into the first row
		for(int j = 0; j<=rDim[1]-1; j++){
			svalues[0][j] = rNames[j]; 
		}
		
		//then write the integers (as Strings) into the rest of the Matrix
		//note that the Array rValues is a columnswise (!!) linear representation of the matrix,
		//thus we have to read it columnwise
		int rValuesIndex = 0;
		for(int j = 0; j<=svalues[0].length-1; j++){ //iterate through the columns
			for(int i = 1; i<=svalues.length-1; i++){	//iterate through the rows, starting at 1 because row 0 contains the names
				svalues[i][j] = Integer.toString(rValues[rValuesIndex]);
				rValuesIndex++;
			}
		}
				
		
		
		//Debug: table output
	/*	for(int i = 0; i<=svalues.length-1; i++){
			for(int j = 0; j<=svalues[0].length-1; j++){
				System.out.print(" " + svalues[i][j]);				
			}
			System.out.println("\n");
		}
		System.out.println("Table output can be disabled in Data.java, method rReadData(...)");*/

		

		
		// Size of tabular
		
		numRows = svalues.length-1;
		num1Rows = 0;
		numVars = svalues[0].length-1;
		
		// read data must be converted to integer,
		// the first column gives the result of the function
		
		values = new byte[numRows][];
		cvalues = new byte[numVars][];
		iresults = new int[numRows];
		results = new boolean[numRows];
		maxValue = new byte[numVars];
		minValue = new byte[numVars];
		
		// The first row only contains names of variables
        names = new String[numVars];		
        for (int i = 1; i <= numVars; i++) {
        	names[i-1] = svalues[0][i];
        }
        
		 Freak.debug("Tabular has "+numRows+" rows and "+numVars+" variables.",2);
		if (debug){
			System.out.print("       ");
			for (int i = 0; i < numVars; i++){
				System.out.print(names[i].charAt(names[i].length()-2));
				System.out.print(names[i].charAt(names[i].length()-1)+" ");
			}
			System.out.println();
		}
		
        // Reading rows, saving result column to extra array               
        
		// Eingelesene Daten in ints umwandeln
		// und dabei die Beschriftungszeile entfernen
		// sowie die ergebnisspalte abspalten
        
		for (int i = 1; i <= numVars; i++){
			cvalues[i-1] = new byte[numRows];
		}
		lineValues=new BitSet(numRows);		
        for (int i = 1; i <= numRows; i++){
        	values[i-1] = new byte[numVars];
//        	iresults[i-1] = (new Integer(svalues[i][0])).intValue();
        	iresults[i-1] = convertObjToInt(svalues[i][0]);
        	results[i-1] = (iresults[i-1]==1);
           	lineValues.set(i-1,results[i-1]);        	
        	if (results[i-1]) num1Rows++;
        	
        	if (debug) {
        		System.out.print("Row "+i+": ");
        	}
        	
        	for (int j=1; j <= numVars; j++){
        		values[i-1][j-1] = (new Byte(svalues[i][j])).byteValue();
        		cvalues[j-1][i-1] = (new Byte(svalues[i][j])).byteValue();
        		if (debug) {
        			System.out.print(" "+values[i-1][j-1]+" ");        			
        		}
        	}
        	
        	if (debug){
        		System.out.println(results[i-1]);
        	}
        }
        
        for (int j = 0; j < numVars; j++){
            if (numRows == 0){
            	maxValue[j] = -1;
            	minValue[j] = -1;
            } else {
            	maxValue[j] = cvalues[j][0];
            	minValue[j] = cvalues[j][0];
            }
            for (int i = 0; i < numRows; i++){
            	if (cvalues[j][i] > maxValue[j]) maxValue[j] = cvalues[j][i];
            	if (cvalues[j][i] < minValue[j]) minValue[j] = cvalues[j][i];            	
            }
        }
        
        if (debug) {
        	for (int j = 0; j < numVars; j++){
//        		System.out.print(names[j]);
    			System.out.print(names[j].charAt(names[j].length()-2));
    			System.out.print(names[j].charAt(names[j].length()-1)+" ");			
        	}
        	System.out.println();
            for (int i = 0; i < numRows; i++) {
            	System.out.print("Row "+i+":  ");
            	for (int j = 0; j < numVars; j++){
            		System.out.print(cvalues[j][i]+"  ");
            	}
            	System.out.println(results[i]);
            }
            for (int j = 0; j < numVars; j++){
            	System.out.print("---");
            }
            System.out.println("-------------");
            System.out.print("max     ");
            for (int j = 0; j < numVars; j++){
            	System.out.print(maxValue[j]+"  ");
            }
            System.out.println();
            System.out.print("min     ");
            for (int j = 0; j < numVars; j++){
            	System.out.print(minValue[j]+"  ");
            }
            System.out.println();
        }
        
  //      setMaxAsThree();
        read = true;
		Freak.debug("0-Rows: "+getNum0Rows()+", 1-Rows: "+getNum1Rows(),2);
        constructCompares();
        
        // construct a BitSet that contains the resultarray
        
        Freak.viewFilenameInTitle(path);
        
        resultsBS = new BitSet(results.length);
        for (int i = 0; i < results.length; i++){
        	if (results[i]) resultsBS.set(i);
        	else resultsBS.clear(i);
        }
	
		
	}
	
	public static void setRMode(){
		csvDisable = true;
		Freak.debug("Setting class \"Data\" to R-Mode",4);
	}
	
	public static void setNormalMode(){
		csvDisable = false;
		 Freak.debug("Setting class \"Data\" to Normal Mode",4);
	}

	/**
	 * @return the values
	 */
	public static byte[][] getValues() {
		return values;
	}

	/**
	 * @param testData the testData to set
	 */
	public static void setTestData(RData testData) {
		Data.testData = testData;
	}

	/**
	 * @param trainingData the trainingData to set
	 */
	public static void setTrainingData(RData trainingData) {
		Data.trainingData = trainingData;
	}

	public static void printTable() {
    	for (int j = 0; j < numVars; j++){
			System.out.print(names[j].charAt(names[j].length()-2));
			System.out.print(names[j].charAt(names[j].length()-1)+" ");			
    	}
    	System.out.println();
        for (int i = 0; i < numRows; i++) {
        	System.out.print("Row "+i+":  ");
        	for (int j = 0; j < numVars; j++){
        		System.out.print(cvalues[j][i]+"  ");
        	}
        	System.out.println(results[i]);
        }
        for (int j = 0; j < numVars; j++){
        	System.out.print("---");
        } 
        System.out.println("-------------");
        System.out.print("max     ");
        for (int j = 0; j < numVars; j++){
        	System.out.print(maxValue[j]+"  ");
        }
        System.out.println();
        System.out.print("min     ");
        for (int j = 0; j < numVars; j++){
        	System.out.print(minValue[j]+"  ");
        }
        System.out.println();		
	}
	public static void setCompareNodeSet(int compareNodeSet) {
		Data.compareNodeSet = compareNodeSet;
	}

}