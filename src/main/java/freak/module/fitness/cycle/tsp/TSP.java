/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.cycle.tsp;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.ClassCollector;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Genotype;
import freak.module.searchspace.Cycle;
import freak.module.searchspace.PermutationGenotype;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Symmetric TSP The symmetric traveling salesman problem is to find a roundtrip through n nodes with minimal length. The distance calculated by a selected function between two nodes i and j is the same like the distance between node j and i.
 * @author  Michael, Stefan
 */
public class TSP extends AbstractStaticSingleObjectiveFitnessFunction implements BatchEventListener, Configurable {
	
	private double[][] points;
	private double[][] distanceArrayDouble;
	private int[][] distanceArrayInt;
	private Object[] tspFileArrayCache;
	private String[] tspFileStringCache;
	private int tspChoice;
	private TSPFile tsp;
	private double[] borderRectangle;
	
	public final static int EXACT = 0;
	public final static int EUC_2D = 1;
	public final static int CEIL_2D = 2;
	public final static int GEO = 3;
	public final static int ATT = 4;
	public final static int MAN_2D = 5;
	public final static int MAX_2D = 6;
	private String[] propertyEdgeWeightTypeStrings = new String[] { "Exact euclidean distance", "EUC_2D: Rounded euclidean distance", "CEIL_2D: Rounded up euclidean distance", "GEO: Geographical distance (experimental)", "ATT: Pseudo-euclidean distance", "MAN_2D: Manhatten distance", "MAX_2D: Maximum distance" };
	
	private final int X = 0;
	private final int Y = 1;
	
	private int edge_weight_type = EXACT;
	
	/**
	 * the constructor of the class.
	 *
	 * @param schedule the fitness function has to know the schedule
	 */
	public TSP(Schedule schedule) {
		super(schedule);
	}
	
	/**
	 * @return  the points
	 * @uml.property  name="points"
	 */
	public synchronized double[][] getPoints() {
		return points;
	}
	
	/**
	 * @return  the borderRectangle
	 * @uml.property  name="borderRectangle"
	 */
	public synchronized double[] getBorderRectangle() {
		return borderRectangle;
	}
	
	public String getTSPName() {
		if (tspChoice == 0) {
			return "Random";
		} else {
			return tsp.getName();
		}
	}
	
	private String[] createTSPFileList() {
		if (tspFileStringCache != null)
			return tspFileStringCache;
		

		String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
		String startedFrom=ClassCollector.getStartedFrom();
		
		// if started from the jar file
		// NEW CHECK
		if (startedFrom.toLowerCase().endsWith(".jar")) {
			String resDir = startedFrom.substring(0, startedFrom.lastIndexOf("/") + 1) + "resource";
			classpaths = new String[] { resDir };
		}
		Collection auswahl = new LinkedList();
		for (int cp = 0; cp < classpaths.length; cp++) {
			File tspFolder = new File(classpaths[cp] + "/freak/module/fitness/cycle/tsp");
			if (tspFolder.exists()) {
				File[] files = tspFolder.listFiles(new TSPFileFilter());
				for (int i = 0; i < files.length; i++) {
					auswahl.add(new TSPFileInfo(files[i]));
				}
			}
		}
		Object[] o = auswahl.toArray();
		Arrays.sort(o);
		tspFileArrayCache = o;
		String[] result = new String[o.length + 1];
		result[0] = "Random 2D Points";
		for (int i = 0; i < o.length; i++) {
			result[i + 1] = ((TSPFileInfo)o[i]).name;
		}
		tspFileStringCache = result;
		return result;
	}
	
	public synchronized void setPropertyPoints(StringArrayWrapper wrapper) {
		int i = wrapper.getIndex();
		// not random 2d points
		if (i == 0) {
			tsp = null;
		} else {
			try {
				tsp = new TSPFile(((TSPFileInfo)tspFileArrayCache[i - 1]).file);
				edge_weight_type = tsp.getEdgeWeightType();
			} catch (Exception e) {
				System.out.println(e);
				return;
			}
			tspFileStringCache[i] = tsp.getName() + ": " + tsp.getComment();
			((Cycle)getSchedule().getGenotypeSearchSpace()).setPropertyDimension(new Integer(tsp.getDimension()));
			
			// notify the schedule that the search space dimension has been
			// changed
			schedule.callInitialize();
			
		}
		tspChoice = i;
	}
	
	public synchronized StringArrayWrapper getPropertyPoints() {
		return new StringArrayWrapper(createTSPFileList(), tspChoice);
	}
	
	public String getShortDescriptionForPoints() {
		return "Coordinates";
	}
	
	public String getLongDescriptionForPoints() {
		return "Select a sample instances for the TSP. This changes the search space dimension!";
	}
	
	public void setPropertyEdgeWeightType(StringArrayWrapper wrapper) {
		edge_weight_type = wrapper.getIndex();
	}
	
	public StringArrayWrapper getPropertyEdgeWeightType() {
		return new StringArrayWrapper(propertyEdgeWeightTypeStrings, edge_weight_type);
	}
	
	public String getShortDescriptionForEdgeWeightType() {
		return "Distance function";
	}
	
	public String getLongDescriptionForEdgeWeightType() {
		return "Edge weight type";
	}
	
	private double getDegree(double p) {
		double deg = Math.floor(p);
		double min = p - deg;
		return Math.PI * (deg + min * (5.0 / 3.0)) / 180.0;
	}
	
	private int intDistance(int p1, int p2) {
		if (distanceArrayInt != null) {
			int value = distanceArrayInt[p1][p2];
			if (value != 0)
				return value;
		}
		int result = 0;
		double[] point1 = points[p1];
		double[] point2 = points[p2];
		double dx = point1[X] - point2[X];
		double dy = point1[Y] - point2[Y];
		switch (edge_weight_type) {
			case EUC_2D :
				result = (int)Math.round(Math.sqrt(dx * dx + dy * dy));
				break;
			case CEIL_2D :
				result = (int)Math.ceil(Math.sqrt(dx * dx + dy * dy));
				break;
			case GEO :
				double latitude1 = getDegree(point1[X]);
				double longitude1 = getDegree(point1[Y]);
				double latitude2 = getDegree(point2[X]);
				double longitude2 = getDegree(point2[Y]);
				double q1 = Math.cos(longitude1 - longitude2);
				double q2 = Math.cos(latitude1 - latitude2);
				double q3 = Math.cos(latitude1 + latitude2);
				result = (int)Math.floor(6378.388 * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0);
				break;
			case ATT :
				result = (int)Math.ceil(Math.sqrt((dx * dx + dy * dy) / 10.0));
				break;
			case MAN_2D :
				result = (int)Math.round(Math.abs(dx) + Math.abs(dy));
				break;
			case MAX_2D :
				result = (int)Math.max(Math.round(Math.abs(dx)), Math.round(Math.abs(dy)));
				break;
		}
		if (distanceArrayInt != null) {
			distanceArrayInt[p1][p2] = result;
		}
		return result;
	}
	
	private double doubleDistance(int p1, int p2) {
		if (distanceArrayDouble != null) {
			double value = distanceArrayDouble[p1][p2];
			if (value != 0)
				return value;
		}
		double[] point1 = points[p1];
		double[] point2 = points[p2];
		double dx = point1[X] - point2[X];
		double dy = point1[Y] - point2[Y];
		
		double result = Math.sqrt(dx * dx + dy * dy);
		
		if (distanceArrayDouble != null) {
			distanceArrayDouble[p1][p2] = result;
		}
		return result;
	}
	
	public double evaluate(Genotype genotype) {
		int[] gene = ((PermutationGenotype)genotype).getIntArray();
		
		if (tspChoice == 0) {
			double cost = 0;
			// now calculate the costs of the path ...
			for (int pos = 0; pos < gene.length; pos++)
				cost += doubleDistance(gene[pos] - 1, gene[(pos + 1) % gene.length] - 1);
			return 0 - cost;
		} else {
			int cost = 0;
			// now calculate the costs of the path ...
			for (int pos = 0; pos < gene.length; pos++)
				cost += intDistance(gene[pos] - 1, gene[(pos + 1) % gene.length] - 1);
			return 0 - cost;
		}
	}
	
	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public double getLowerBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public double getUpperBound() throws UnsupportedOperationException {
		return 0;
	}
	
	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public String getDescription() {
		return "The symmetric traveling salesman problem is to find a roundtrip through n nodes with minimal length.\n" + "The distance calculated by a selected function between two nodes i and j is the same like the distance between node j and i.";
	}
	
	public String getName() {
		return "Symmetric TSP";
	}
	
	//property fix
	public Configuration getConfiguration() {
		Configuration c = Configuration.getConfigurationFor(this);
		c.setAdditional(new Integer(edge_weight_type), "");
		return c;
	}
	
	public void setConfiguration(Configuration configuration) {
		Configuration.setConfigurationFor(this, configuration);
		edge_weight_type = ((Integer)configuration.getAdditional()).intValue();
	}
	
	/**
	 * Used to signalize the fitness function to initialize itself for the next
	 * batch. <br>
	 */
	public synchronized void batchStarted(BatchEvent evt) {
		int dim = ((Cycle)getSchedule().getPhenotypeSearchSpace()).getDimension();
		// initialize the fitness function
		if (tspChoice == 0) {
			borderRectangle = new double[4];
			borderRectangle[0] = 0;
			borderRectangle[1] = 0;
			borderRectangle[2] = 100;
			borderRectangle[3] = 100;
			
			points = new double[dim][2];
			
			for (int i = 0; i < dim; i++)
				for (int j = 0; j < 2; j++)
					points[i][j] = getSchedule().getRandomElement().raw() * 100;
		} else {
			points = tsp.getPoints();
			borderRectangle = tsp.getBorderRectangle();
		}
		if (dim <= 550) {
			if (tspChoice == 0) {
				distanceArrayDouble = new double[dim][dim];
			} else {
				distanceArrayInt = new int[dim][dim];
			}
		} else {
			distanceArrayInt = null;
			distanceArrayDouble = null;
		}
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	class TSPFileInfo implements Comparable, Serializable {
		File file;
		String name;
		
		public TSPFileInfo(File f) {
			String tspName = f.getName();
			name = tspName.substring(0, tspName.length() - 4).toUpperCase();
			file = f;
		}
		
		public int compareTo(Object o) {
			return name.compareTo(((TSPFileInfo)o).name);
		}
	}
	
	private static class TSPFileFilter implements FileFilter {
		public boolean accept(File file) {
			return file.getName().endsWith(".tsp");
		}
	}
	
}
