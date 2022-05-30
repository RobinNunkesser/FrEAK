/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.cycle.tsp;

import java.io.*;

/**
 * A filereader for tsp-files. More information about tsp-files: http://www.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/
 * @author  Michael
 */
public class TSPFile implements Serializable {
	
	private String name = "n/a";
	private String comment = "none";
	
	private int edge_weight_type;
	
	private int dimension;
	private double[][] points;
	private double[] borderRectangle = new double[4];
	
	/**
	 * @return  the dimension
	 * @uml.property  name="dimension"
	 */
	public int getDimension() {
		return dimension;
	}
	
	public int getEdgeWeightType() {
		return edge_weight_type;
	}
	
	/**
	 * @return  the comment
	 * @uml.property  name="comment"
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * @return  the name
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return  the points
	 * @uml.property  name="points"
	 */
	public double[][] getPoints() {
		return points;
	}
	
	/**
	 * @return  the borderRectangle
	 * @uml.property  name="borderRectangle"
	 */
	public double[] getBorderRectangle() {
		return borderRectangle;
	}
	
	private void readPoints(BufferedReader reader) throws IOException {
		String line;
		String[] coords;
		borderRectangle[0] = Double.POSITIVE_INFINITY;
		borderRectangle[1] = Double.POSITIVE_INFINITY;
		borderRectangle[2] = Double.NEGATIVE_INFINITY;
		borderRectangle[3] = Double.NEGATIVE_INFINITY;
		line = reader.readLine().trim();
		for (int i = 0; line.length() > 4; i++) {
			line = line.trim();
			coords = line.split("\\s++");
			if (coords.length != 3) {
				System.out.println("Unexpected line: " + line);
			}
			points[i][0] = Double.parseDouble(coords[1]);
			points[i][1] = Double.parseDouble(coords[2]);
			
			if (points[i][0] < borderRectangle[0])
				borderRectangle[0] = points[i][0];
			if (points[i][1] < borderRectangle[1])
				borderRectangle[1] = points[i][1];
			if (points[i][0] > borderRectangle[2])
				borderRectangle[2] = points[i][0];
			if (points[i][1] > borderRectangle[3])
				borderRectangle[3] = points[i][1];
			
			if ((line = reader.readLine()) == null)
				return;
			line = line.trim();
		}
	}
	
	public TSPFile(File file) throws Exception {
		String line;
		String[] params;
		FileReader in = new FileReader(file);
		BufferedReader reader = new BufferedReader(in);
		try {
			while (!(line = reader.readLine().trim()).equalsIgnoreCase("EOF")) {
				if (line.equalsIgnoreCase("NODE_COORD_SECTION")) {
					if (dimension > 0) {
						points = new double[dimension][2];
						readPoints(reader);
						//ready
						return;
					} else {
						System.out.println(file.getName() + ": Dimension not specified.");
						throw new Exception("no dimension");
					}
				} // end "NODE_COORD_SECTION"
				params = line.split(":");
				if (params.length != 2) {
					System.out.println("Unexpected line: " + line);
					throw new Exception("Unexpected line");
				}
				params[0] = params[0].trim();
				params[1] = params[1].trim();
				if (params[0].equalsIgnoreCase("NAME"))
					name = params[1].toUpperCase();
				if (params[0].equalsIgnoreCase("TYPE"))
					if (!params[1].equalsIgnoreCase("TSP")) {
						System.out.println("No TSP file: " + file.getName());
						throw new Exception("only TSP Files");
					}
				if (params[0].equalsIgnoreCase("COMMENT"))
					comment = params[1];
				if (params[0].equalsIgnoreCase("EDGE_WEIGHT_TYPE")) {
					if (params[1].equalsIgnoreCase("EUC_2D")) {
						edge_weight_type = TSP.EUC_2D;
					} else {
						if (params[1].equalsIgnoreCase("CEIL_2D")) {
							edge_weight_type = TSP.CEIL_2D;
						} else {
							if (params[1].equalsIgnoreCase("GEO")) {
								edge_weight_type = TSP.GEO;
							} else {
								if (params[1].equalsIgnoreCase("ATT")) {
									edge_weight_type = TSP.ATT;
								} else {
									if (params[1].equalsIgnoreCase("EXPLICIT")) {
										System.out.println(file.getName() + ": edge weight type EXPLICIT not supported!");
										throw new Exception();
									} else {
										System.out.println("Unknown edge weight type: " + params[1]);
										edge_weight_type = 0;
									}
								}
							}
						}
					}
				}
				if (params[0].equalsIgnoreCase("DIMENSION"))
					dimension = new Integer(params[1]).intValue();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File " + file.getName() + " not found.");
		}
	}
}
