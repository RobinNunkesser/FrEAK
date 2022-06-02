/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.searchspace;

import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.inspector.CustomInspectable;
import freak.core.modulesupport.inspector.Inspector;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Genotype;
import freak.core.searchspace.AbstractSearchSpace;
import freak.core.searchspace.HasMetric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author  Kai, Stefan, Michael, Christian
 */
public class GraphEdgeSelection extends AbstractSearchSpace implements Configurable, RunEventListener, BatchEventListener, HasMetric,
		CustomInspectable {
	
	/**
	 * This class provides the Graph functionality.
	 */
	public static class Graph implements Serializable {
		
		/**
		 * The edges of the Graph are stored in objects of this class. Edge weights are integers. The isSelected bit is used for representing subsets of the set of all edges.<br> The interface Comparable is implemented to allow for sorting edges according to their weights with the sorting methods provided in java.util.Arrays. The method compare() takes into account the weights of the Edges to be compared.
		 */
		public static class Edge implements Serializable, Comparable {
			
			/** The graph to which this Edge belongs */
			protected Graph graph;
			
			/** The start node of the Edge */
			protected int startNode;
			
			/** The end node of the Edge */
			protected int endNode;
			
			/** The Edge's weight */
			protected int weight;
			
			/**
			 * For speedup purposes. See attribute edgeLUT in surrounding class Graph.
			 */
			protected int indexInGenotypeArray;
			
			/**
			 * Creates a new Edge with the given start and end nodes,
			 * the given weight and selection status.
			 */
			public Edge(Graph graph, int v, int w, int weight) {
				this.graph = graph;
				startNode = v;
				endNode = w;
				this.weight = weight;
				this.indexInGenotypeArray = -1;
				
			}
			
			/*
			 * If the given Object is not of class Edge, this method returns false.
			 * Otherwise, it returns true iff the start and end nodes of this edge are equal to
			 * the corresponding nodes in the given Object (which is of class Edge in this case).<br>
			 */
			public boolean equals(Object o) {
				
				if (!(o instanceof Edge))
					return (false);
				
				// check whether nodes match
				Edge e = (Edge) o;
				
				return (this.startNode == e.startNode && this.endNode == e.endNode);
				
			}
			
			/**
			 * Returns a hash code for this Edge.
			 */
			public int hashCode() {
				return (3628801 * startNode + 40321 * endNode);
				
			}
			
			/**
			 * Returns true iff this Edge is selected in the genotype passed.
			 */
			public boolean getIsSelected(GraphEdgeSelectionGenotype g) {
				return (g.getEdgeSelection()[indexInGenotypeArray]);
			}
			
			/**
			 * Getter for property startNode.
			 * @uml.property  name="startNode"
			 */
			public int getStartNode() {
				return startNode;
			}
			
			/**
			 * Getter for property endNode.
			 * @uml.property  name="endNode"
			 */
			public int getEndNode() {
				return endNode;
			}
			
			/**
			 * Getter for property weight.
			 * @uml.property  name="weight"
			 */
			public int getWeight() {
				return weight;
			}
			
			/**
			 * Compares this edge to another one, using the convention in interface Comparable.
			 */
			public int compareTo(Object o) {
				
				Edge other = (Edge) o;
				
				if (this.weight < other.weight)
					return (-1);
				else if (this.weight == other.weight)
					return (0);
				else
					return (1);
				
			}
			
			/**
			 * @return  the index the edge has in every edgeSelection array
			 * @uml.property  name="indexInGenotypeArray"
			 */
			public int getIndexInGenotypeArray() {
				return indexInGenotypeArray;
			}
			
		}
		// ------------------- end inner class Edge ---------------------
		
		/** Nodes run from 0 to numberOfNodes - 1 */
		protected int numberOfNodes;
		
		/** The number of edges in the Graph */
		protected int numberOfEdges;
		
		/**
		 * layout information for graph drawing. Each node has
		 * 2-dimensional euclidean coordinates with range [0,1].
		 */
		protected float[] nodeLayoutX;
		protected float[] nodeLayoutY;
		
		/**
		 * For speedup purposes when using GraphEdgeSelectionGenotypes. In that class, only boolean values are stored, one for each edge in the graph. To speedup lookup of the edge for a given index between 0 and numberOfEdges-1, in this array the edge in which the selection status is stored is stored, i.e. the edge with startNode <= endNode. Additionally, in every such edge the corresponding index in this array is stored, so the data structure is "doubly linked"
		 * @uml.property  name="edgeLUT"
		 * @uml.associationEnd  multiplicity="(0 -1)"
		 */
		protected Edge[] edgeLUT;
		
		/**
		 * The edges of the graph are stored in this array of ArrayLists.<br>
		 * IMPORTANT: an edge {v,w} is stored as follows:<br>
		 *
		 * if v == w, there is one Edge object with start and end node v in edges[v]<br>
		 * if v != w, there is one Edge object with start node v and end node w in edges[v] and
		 * one with start node w and end node v in edges[w].<br>
		 *
		 * The weight of an edge and its selection status are stored ONLY
		 * in the Edge object for which startNode <= endNode.<br>
		 */
		protected ArrayList[] edges;
		
		/** this bit maintains whether a MST for this Graph is known */
		protected boolean mstIsKnown;
		
		/** this is the weight of an MST for this Graph if mstIsKnown is true */
		protected int cachedMSTWeight;
		
		/** this is the edge selection of an MST for this Graph if mstIsKnown is true */
		protected boolean[] cachedMSTEdgeSelection;
		
		/**
		 * Creates a new Graph with the given number of nodes and no edges.
		 */
		public Graph(int numberOfNodes) {
			
			// init node and edge count
			this.numberOfNodes = numberOfNodes;
			numberOfEdges = 0;
			
			// init edges lists
			edges = new ArrayList[numberOfNodes];
			
			for (int i = 0; i < numberOfNodes; edges[i++] = new ArrayList());
			
			// init layout information
			nodeLayoutX = new float[numberOfNodes];
			nodeLayoutY = new float[numberOfNodes];
			circleLayout(); // default layout
			
			// invalidate weightOfMSTKnown
			mstIsKnown = false;
			
		}
		
		/**
		 * This methods sets up the LUT for conversion between
		 * GraphEdgeSubsetGenotype <-> a Graph's edge selection
		 */
		public void initSpeedupStructures() {
			
			if (edgeLUT != null)
				System.out.println("something may be wrong! Method initSpeedupStructures() should not be called more than once!");
			
			// create LUT
			edgeLUT = new Edge[this.getNumberOfEdges()];
			
			int count = 0;
			
			for (int i = 0; i < numberOfNodes; i++) {
				for (int j = 0; j < edges[i].size(); j++) {
					
					Edge e = (Edge) edges[i].get(j);
					
					if (e.startNode <= e.endNode) {
						// -- edge has correct orientation. update edgeLUT.
						e.indexInGenotypeArray = count;
						edgeLUT[count] = e;
						count++;
						
					} else {
						// --  edge has wrong orientation. set indexInGenotypeArray though.
						ArrayList endEdges = edges[e.endNode];
						for (int k = 0; k < endEdges.size(); k++) {
							Edge e2 = (Edge) endEdges.get(k);
							if (e2.endNode == e.startNode) {
								e.indexInGenotypeArray = e2.indexInGenotypeArray;
								break;
							}
						}
					}
					
				}
				
			}
			
		}
		
		/**
		 * Returns the Edge associated to the given speedup index.
		 * This number is the index of the bit representing the edge's
		 * selection status in GraphEdgeSelectionGenotype's edgeSelection[]
		 * array.<br>
		 * SEE ALSO speedupIndexForEdge().
		 */
		public Edge edgeForSpeedupIndex(int index) {
			return (edgeLUT[index]);
		}
		
		/**
		 * Returns the index in the speedup array for the given edge between
		 * startNode and endNode. If the edge doesn't exist -1 is returned.
		 * Note that the order of start and end node is irrelevant.
		 * @param startNode the first node of the edge
		 * @param endNode the second node of the edge
		 * @return the index or -1
		 */
		public int findEdgeSelectionIndexForEdge(int startNode, int endNode) {
			// -- swap start and end if neccessary
			if (startNode > endNode) {
				int temp = startNode;
				startNode = endNode;
				endNode = temp;
			}
			
			int j = 0;
			ArrayList nodeEdges = edges[startNode];
			// -- search for the edge
			while (j < nodeEdges.size()) {
				Edge e = (Edge) nodeEdges.get(j++);
				if (e.endNode == endNode)
					return (e.indexInGenotypeArray);
			}
			// -- we did not find such an edge in the graph
			return -1;
		}
		
		/**
		 * Returns the edges incident to (i.e. the edges which end at the given) node.
		 */
		public Edge[] getIncidentEdgesForNode(int node) {
			
			Edge[] retEdges = new Edge[edges[node].size()];
			
			// copy
			for (int i = 0; i < retEdges.length; i++) {
				retEdges[i] = (Edge) edges[node].get(i);
				
			}
			
			return (retEdges);
			
		}
		
		public ArrayList getSelectedIncidentEdgesForNode(int node, GraphEdgeSelectionGenotype gene) {
			ArrayList retEdges = new ArrayList();
			boolean[] edgeSelection = gene.getEdgeSelection();
			ArrayList incidentEdges = edges[node];
			int totalEdges = incidentEdges.size();
			for (int i = 0; i < totalEdges; i++) {
				Edge e = (Edge) incidentEdges.get(i);
				if (edgeSelection[e.indexInGenotypeArray]) {
					// -- edge is selected
					retEdges.add(edges[node].get(i));
				}
			}
			return retEdges;
		}
		
		public int getNumberOfSelectedIncidentEdgesForNode(int node, GraphEdgeSelectionGenotype gene) {
			int num = 0;
			boolean[] edgeSelection = gene.getEdgeSelection();
			ArrayList incidentEdges = edges[node];
			int totalEdges = incidentEdges.size();
			for (int i = 0; i < totalEdges; i++) {
				Edge e = (Edge) incidentEdges.get(i);
				if (edgeSelection[e.indexInGenotypeArray])
					num++;
			}
			return num;
		}
		
		/**
		 * Returns true iff the Graph contains an edge between the two
		 * given nodes.
		 */
		public boolean containsEdge(int v, int w) {
			
			Edge e = new Edge(null, v, w, 0);
			
			return (edges[v].contains(e));
			
		}
		
		/**
		 * Adds the edge between the two given nodes to the graph if it
		 * does not already exist. If it does exist, the method does nothing.
		 * Returns true if the edge has actually been added, and false if not.
		 */
		public synchronized boolean addEdge(int v, int w, int weight) {
			
			if (!this.containsEdge(v, w)) {
				
				if (v != w) {
					edges[v].add(new Edge(this, v, w, weight));
					edges[w].add(new Edge(this, w, v, weight));
				} else {
					edges[v].add(new Edge(this, v, v, weight));
				}
				
				numberOfEdges++;
				
				// invalidate cache bit
				mstIsKnown = false;
				
				return (true);
				
			}
			
			return (false);
			
		}
		
		/**
		 * If the edge between the two given nodes exists in the graph,
		 * it is removed. Otherwise, this method does nothing.
		 */
		public synchronized void removeEdge(int v, int w) {
			
			if (this.containsEdge(v, w)) {
				edges[v].remove(new Edge(this, v, w, 0));
				edges[w].remove(new Edge(this, w, v, 0));
				numberOfEdges--;
				
				// invalidate cache bit
				mstIsKnown = false;
				
			}
			
		}
		
		/**
		 * Returns the number of edges in the Graph.
		 * @uml.property  name="numberOfEdges"
		 */
		public int getNumberOfEdges() {
			return (numberOfEdges);
		}
		
		/**
		 * Returns the number of nodes in the Graph.
		 * @uml.property  name="numberOfNodes"
		 */
		public int getNumberOfNodes() {
			return (numberOfNodes);
		}
		
		/**
		 * This method sets up a circular layout for the graph.
		 */
		public void circleLayout() {
			
			for (int i = 0; i < numberOfNodes; i++) {
				nodeLayoutX[i] = 0.5f + 0.5f * (float) Math.cos(2f * Math.PI * i / numberOfNodes);
				nodeLayoutY[i] = 0.5f + 0.5f * (float) Math.sin(2f * Math.PI * i / numberOfNodes);
				
			}
			
		}
		
		/**
		 * Sets the X layout information for the given node. Value
		 * must be between 0 and 1.
		 */
		public void setLayoutX(int node, float value) {
			nodeLayoutX[node] = value;
		}
		
		/**
		 * Sets the Y layout information for the given node. Value
		 * must be between 0 and 1.
		 */
		public void setLayoutY(int node, float value) {
			nodeLayoutY[node] = value;
		}
		
		/**
		 * Returns the X layout information of the given node.
		 */
		public float getLayoutX(int node) {
			return (nodeLayoutX[node]);
		}
		
		/**
		 * Returns the Y layout information of the given node.
		 */
		public float getLayoutY(int node) {
			return (nodeLayoutY[node]);
		}
		
		/** The sets in a union-find data structure */
		protected ArrayList[] sets;
		
		/** The representatives of elements in the UF structure */
		protected int[] repr;
		
		/** Initialize the Union Find data structure */
		public void initUF() {
			sets = new ArrayList[getNumberOfNodes()];
			for (int i = 0; i < getNumberOfNodes(); i++) {
				
				sets[i] = new ArrayList();
				sets[i].add(new Integer(i));
			}
			
			repr = new int[getNumberOfNodes()];
			for (int i = 0; i < getNumberOfNodes(); i++)
				repr[i] = i;
			
		}
		
		/** unites two classes */
		protected void union(int oa, int ob) {
			
			int a = repr[oa];
			int b = repr[ob];
			
			ArrayList listA = sets[a];
			ArrayList listB = sets[b];
			
			for (int i = 0; i < listB.size(); i++) {
				Integer num = (Integer) listB.get(i);
				
				repr[num.intValue()] = a;
			}
			
			listA.addAll(listB);
			
		}
		
		/** tests for containment of the two elements in the same UF class */
		protected boolean sameClass(int a, int b) {
			return (repr[a] == repr[b]);
			
		}
		
		/**
		 * Determines an MST on this graph.
		 */
		protected synchronized void calcMST() {
			
			// create array of edges in this graph sorted by weight
			int numEdges = this.getNumberOfEdges();
			
			Edge[] sortedEdges = new Edge[numEdges];
			for (int i = 0; i < numEdges; i++)
				sortedEdges[i] = edgeForSpeedupIndex(i);
			Arrays.sort(sortedEdges);
			
			// init an edge selection array with all edges unselected
			boolean[] selEdges = new boolean[numEdges];
			for (int i = 0; i < numEdges; i++)
				selEdges[i] = false;
			
			// test all edges for containment in MST
			int accWeight = 0;
			initUF();
			
			for (int i = 0; i < numEdges; i++) {
				
				Edge testEdge = sortedEdges[i];
				
				if (!sameClass(testEdge.startNode, testEdge.endNode)) {
					
					selEdges[testEdge.indexInGenotypeArray] = true;
					accWeight += testEdge.weight;
					union(testEdge.endNode, testEdge.startNode);
					
				}
				
			}
			
			// cache results
			mstIsKnown = true;
			cachedMSTWeight = accWeight;
			cachedMSTEdgeSelection = selEdges;
			
		}
		
		/**
		 * Determines the weight of a MST on this graph.
		 */
		public int getMSTWeight() {
			if (!mstIsKnown)
				calcMST();
			
			// return cached value
			return (cachedMSTWeight);
		}
		
		public boolean[] getMSTEdgeSelection() {
			if (!mstIsKnown)
				calcMST();
			
			// return cached value
			return (cachedMSTEdgeSelection);
		}
		
		public int weightOfMST() {
			return (getMSTWeight());
		}
		
		/**
		 * Returns the sum of the weights of all Edges in the Graph.
		 */
		public int getSumOfWeightsOfAllEdges() {
			
			int sum = 0;
			
			for (int i = 0; i < numberOfNodes; i++) {
				for (int j = 0; j < edges[i].size(); j++) {
					
					Edge e = (Edge) edges[i].get(j);
					
					if (e.startNode <= e.endNode)
						// consider each edge only once! See comment on edges[] array.
						sum += e.weight;
					
				}
				
			}
			
			return (sum);
			
		}
		
		/**
		 * Returns the maximum edge weight in the graph.
		 */
		public int getMaxEdgeWeight() {
			
			int maxWeight = 0;
			
			for (int i = 0; i < numberOfNodes; i++) {
				for (int j = 0; j < edges[i].size(); j++) {
					
					Edge e = (Edge) edges[i].get(j);
					maxWeight = Math.max(maxWeight, e.getWeight());
					
				}
				
			}
			
			return (maxWeight);
			
		}
		
		/**
		 * Returns the sum of the weights of all selected Edges in the Graph.
		 */
		public int getSumOfWeightsOfAllSelectedEdges(GraphEdgeSelectionGenotype gen) {
			
			int sum = 0;
			
			for (int i = 0; i < numberOfNodes; i++) {
				for (int j = 0; j < edges[i].size(); j++) {
					
					Edge e = (Edge) edges[i].get(j);
					
					if (e.startNode <= e.endNode)
						// consider each edge only once! See comment on edges[] array.
						sum += (e.getIsSelected(gen) ? e.weight : 0);
						
				}
				
			}
			
			return (sum);
			
		}
		
		/*
		 * Recursive implementation of dfs using the visited[] boolean array
		 * as markers for node status. This method searches only along
		 * edges which are selected.
		 */
		protected void dfsAlongSelectedEdges(int node, GraphEdgeSelectionGenotype gen, boolean[] visited) {
			
			visited[node] = true;
			
			for (int i = 0; i < edges[node].size(); i++) {
				
				Edge e = (Edge) edges[node].get(i);
				
				// is edge selected? => follow it.
				if (gen.getEdgeSelection()[e.indexInGenotypeArray])
					if (!visited[e.endNode])
						dfsAlongSelectedEdges(e.endNode, gen, visited);
				
			}
			
		}
		
		/**
		 * Returns the number of selected edges.
		 */
		public int getNumberOfSelectedEdges(GraphEdgeSelectionGenotype gen) {
			
			int sum = 0;
			boolean[] sel = gen.getEdgeSelection();
			
			for (int i = 0; i < sel.length; i++) {
				sum += (sel[i] ? 1 : 0);
			}
			
			return (sum);
			
		}
		
		/**
		 * Returns the number of connected components of the Graph when
		 * considering only the selected Edges.
		 */
		public int numOfConnectedComponentsForSelectedEdges(GraphEdgeSelectionGenotype gen) {
			
			// init visited[] array
			boolean[] visited = new boolean[numberOfNodes];
			
			for (int i = 0; i < numberOfNodes; i++)
				visited[i] = false;
			
			// run through the graph
			int numOfComponents = 0;
			for (int i = 0; i < numberOfNodes; i++) {
				
				if (!visited[i]) {
					numOfComponents++;
					dfsAlongSelectedEdges(i, gen, visited);
				}
				
			}
			
			return (numOfComponents);
			
		}
		
		/**
		 * This selects uniformly at random the edges in the given genotype.
		 */
		public void uniformlySelectRandomEdges(RandomElement random, GraphEdgeSelectionGenotype gen) {
			
			boolean[] selection = gen.getEdgeSelection();
			
			for (int i = 0; i < selection.length; i++) {
				selection[i] = random.choose(2) == 1;
				
			}
			
		}
		
	}
	
	// --------------- end inner class Graph --------------------
	
	protected int numOfVertices = 10;
	protected int numOfEdges = 15;
	protected int maxWeight = 100;
	
	protected double probForEdge = 0.5;
	
	protected int verticesInTriangles = 5;
	
	protected int verticesLolli = 5;
	
	protected int degreeK = 2;
	
	protected boolean keepGraphDuringBatch = true;
	
	protected String[] initTypes = {
		"Random with Fixed Edge Number",
		"Random with Fixed Edge Probability",
		"Random with Fixed Edge Probability and binary valued weights",
		"Full Graph",
		"Ingo's Graph",
		"Full Star-Graph",
		"Path",
		"Random Tree",
		"Lollipop Tree",
		"k-Tree",
		"Trap Line"
	};
	
	protected int initTypeChoice = RANDOM_FIXED_EDGES;
	protected static final int RANDOM_FIXED_EDGES = 0;
	protected static final int RANDOM_FIXED_PROBABILITY = 1;
	protected static final int RANDOM_BINARY_VALUE = 2;
	protected static final int FULL = 3;
	protected static final int INGOS_GRAPH = 4;
	protected static final int STAR = 5;
	protected static final int PATH = 6;
	protected static final int TREE = 7;
	protected static final int LOLLI = 8;
	protected static final int K_TREE = 9;
	protected static final int SEMI = 10;
	protected static final int SEMI_P = 11;
	protected static final int TRAP_LINE = 12;
	
	protected Graph graph;
	
	/**
	 * This static attribute is used for determining whether the
	 * normal RNG of the schedule is used during the generation
	 * of random graphs or if an internal one is used.
	 * This functionality is needed e.g. when one wants to compare
	 * different algorithms on the same but randomly generated Graphs.
	 */
	protected boolean useInternalRNG = false;
	
	/** The internal RNG */
	protected static MersenneTwister internalRNG = new MersenneTwister(12345);
	
	/** The seed of the interna RNG */
	protected int internalRNGSeed = 12345;
	
	/**
	 * Returns the Random Number Generator according to the bit
	 * USE_OWN_RANDOM_NUMBER_GENERATOR.
	 */
	protected RandomElement getRandomElement() {
		
		if (useInternalRNG) {
			return (internalRNG);
		} else
			return (schedule.getRandomElement());
		
	}
	
	public GraphEdgeSelection(Schedule schedule) {
		super(schedule);
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	/**
	 * Creates a new Genotype.<br>
	 * IMPORTANT: The graph MUST NOT change after a random Genotype
	 * has been created. Otherwise, the speedup data structures internal
	 * to the graph may not work properly with the Genotype anymore.
	 */
	public Genotype getRandomGenotype() {
		
		GraphEdgeSelectionGenotype g = new GraphEdgeSelectionGenotype(graph);
		
		RandomElement rand = this.getRandomElement();
		
		// randomly select edges
		for (int i = 0; i < graph.getNumberOfEdges(); i++)
			g.getEdgeSelection()[i] = (rand.choose(2) == 1);
		
		return (g);
		
	}
	
	public double getDistance(Genotype gt1, Genotype gt2) {
		boolean[] b1 = ((GraphEdgeSelectionGenotype) gt1).edgeSelection;
		boolean[] b2 = ((GraphEdgeSelectionGenotype) gt2).edgeSelection;
		
		int distance = 0;
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) distance++;
		}
		
		return distance;
	}
	
	/**
	 * @return  the graph
	 * @uml.property  name="graph"
	 */
	public Graph getGraph() {
		return graph;
	}
	
	public int getDimension() {
		
		if (initTypeChoice == RANDOM_FIXED_EDGES) {
			return numOfEdges;
		} else if (initTypeChoice == INGOS_GRAPH) {
			int triangles = (verticesInTriangles - 1) / 2;
			int nodesInClique = numOfVertices - verticesInTriangles;
			return triangles * 3 + nodesInClique * (nodesInClique - 1);
		} else if (graph != null) {
			return graph.numberOfEdges;
		} else {
			
			throw new IllegalStateException("Dimension currently undetermined");
		}
	}
	
	public double getSize() {
		return Math.pow(2, numOfEdges);
	}
	
	public String getName() {
		return "Graph Edge Selection";
	}
	
	public String getDescription() {
		return "The set of all possible edge selections of a specific weighted graph";
	}
	
	public Integer getPropertyNumberOfVertices() {
		return (new Integer(numOfVertices));
	}
	
	public void setPropertyNumberOfVertices(Integer vertices) {
		numOfVertices = vertices.intValue();
		if (initTypeChoice == FULL || initTypeChoice == STAR) numOfEdges = numOfVertices * (numOfVertices - 1) / 2;
	}
	
	public Integer getPropertyNumberOfEdges() {
		return (new Integer(numOfEdges));
	}
	
	public void setPropertyNumberOfEdges(Integer edges) {
		numOfEdges = edges.intValue();
	}
	
	public Integer getPropertyVerticesLolli() {
		return (new Integer(verticesLolli));
	}
	
	public void setPropertyVerticesLolli(Integer vert) {
		verticesLolli = vert.intValue();
	}
	
	public Integer getPropertyDegreeK() {
		return (new Integer(degreeK));
	}
	
	public void setPropertyDegreeK(Integer k) {
		degreeK = k.intValue();
	}


	public Integer getPropertyMaximalWeight() {
		return (new Integer(maxWeight));
	}
	
	public void setPropertyMaximalWeight(Integer maxWeight) {
		this.maxWeight = maxWeight.intValue();
	}
	
	public Double getPropertyProbabilityForEdge() {
		return (new Double(probForEdge));
	}
	
	public void setPropertyProbabilityForEdge(Double prob) {
		probForEdge = prob.doubleValue();
	}
	
	public Integer getPropertyVerticesInTriangles() {
		return (new Integer(verticesInTriangles));
	}
	
	public void setPropertyVerticesInTriangles(Integer vertices) {
		verticesInTriangles = vertices.intValue();
	}
	
	public StringArrayWrapper getPropertyInitializationType() {
		return new StringArrayWrapper(initTypes, initTypeChoice);
	}
	
	public synchronized void setPropertyInitializationType(StringArrayWrapper wrapper) {
		initTypeChoice = wrapper.getIndex();
		if (initTypeChoice == FULL || initTypeChoice == STAR)
			numOfEdges = numOfVertices * (numOfVertices - 1) / 2;
	}
	
	
	public Boolean getPropertyKeepGraphDuringBatch() {
		return (new Boolean(keepGraphDuringBatch));
	}
	
	public void setPropertyKeepGraphDuringBatch(Boolean value) {
		keepGraphDuringBatch = value.booleanValue();
	}
	
	
	
	
	// internal RNG properties
	
	public Boolean getPropertyUseInternalRNG() {
		return (new Boolean(useInternalRNG));
	}
	
	public void setPropertyUseInternalRNG(Boolean value) {
		useInternalRNG = value.booleanValue();
		
	}
	
	public String getLongDescriptionForUseInternalRNG() {
		return ("If this property is set to true, a Random Number " + "Generator internal to this searchspace with a configurable random seed is used during " + "initialization of random graphs. This is useful when " + "one wants to run different algorithms on the same randomly generated" + "graphs. If this property is set to false (default), the normal" + "RNG of freak is used.");
	}
	
	public String getShortDescriptionForUseInternalRNG() {
		return ("use internal RNG");
	}
	
	public Integer getPropertyInternalRNGSeed() {
		return (new Integer(internalRNGSeed));
	}
	
	public void setPropertyInternalRNGSeed(Integer value) {
		internalRNGSeed = value.intValue();
		internalRNG = new MersenneTwister(internalRNGSeed);
	}
	
	public String getShortDescriptionForInternalRNGSeed() {
		return ("internal RNG seed");
	}
	
	public String getLongDescriptionForInternalRNGSeed() {
		return ("The random seed used for the internal RNG");
		
	}
	
	public void batchStarted(BatchEvent evt) {
		if (keepGraphDuringBatch) initNewGraph();
	}
	
	public void runStarted(RunEvent evt) {
		if (!keepGraphDuringBatch) initNewGraph();
	}
	
	public void initNewGraph() {
		switch (initTypeChoice) {
			case INGOS_GRAPH : {
				initIngosGraph();
				postInit();
				break;
			}
			case RANDOM_FIXED_EDGES : {
				do {
					initRandomGraphWithFixedEdgeNumber();
				} while (!postInitCheck());
				break;
			}
			case RANDOM_FIXED_PROBABILITY : {
				do {
					initRandomGraphWithFixedEdgeProbability();
				} while (!postInitCheck());
				break;
			}
			case RANDOM_BINARY_VALUE : {
				do {
					initRandomGraphWithBinaryValuedWeights();
				} while (!postInitCheck());
				break;
			}
			case FULL : {
				initFullGraph();
				postInit();
				break;
			}
			case STAR: {
				initStarGraph();
				postInit();
				break;
			}
			case PATH: {
				initPath();
				postInit();
				break;
			}
			case TREE: {
				initRandomTree();
				postInit();
				break;
			}
			case LOLLI: {
				initLolliTree();
				postInit();
				break;
			}
			case K_TREE: {
				initKTree();
				postInit();
				break;
			}
			case SEMI: {
				initSemi();
				postInit();
				break;
			}
			case SEMI_P: {
				initSemiP();
				postInit();
				break;
			}
			case TRAP_LINE : {
				initTrapLine();
				postInit();
				break;
			}
		}
	}
	
	private boolean postInitCheck() {
		graph.initSpeedupStructures();
		if (!isGraphConnected()) return false;
		schedule.callInitialize();
		return true;
	}
	
	private void postInit() {
		graph.initSpeedupStructures();
		schedule.callInitialize();
	}
	
	private boolean isGraphConnected() {
		GraphEdgeSelectionGenotype allEdges = new GraphEdgeSelectionGenotype(graph);
		Arrays.fill(allEdges.edgeSelection, true);
		int components = graph.numOfConnectedComponentsForSelectedEdges(allEdges);
		return components == 1;
	}
	
	private void initStarGraph() {
		RandomElement random = this.getRandomElement();
		graph = new Graph(numOfVertices);
		int numCircle = numOfVertices - 1;
		
		for (int i = 0; i < numCircle; i++) graph.addEdge(i, numCircle, 1);
		for (int i = 0; i < numCircle; i++) {
			for (int j = i + 1; j < numCircle; j++) {
				graph.addEdge(i, j, random.choose(2, maxWeight));
			}
		}
		
		graph.setLayoutX(numOfVertices-1, 0.5f);
		graph.setLayoutY(numOfVertices-1, 0.5f);
		for (int i = 0; i < numCircle; i++) {
			graph.setLayoutX(i, 0.5f + 0.5f * (float) Math.cos(2f * Math.PI * i / numCircle));
			graph.setLayoutY(i, 0.5f + 0.5f * (float) Math.sin(2f * Math.PI * i / numCircle));
		}
		
	}
	
	private void initPath() {
		graph = new Graph(numOfVertices);
		
		for (int i = 0; i < numOfVertices-1; i++) {
			graph.addEdge(i, i+1, 1);
		}
		
		int sqrt = (int)Math.sqrt(numOfVertices);
		int rows = (int)Math.ceil(numOfVertices/(float)sqrt)-1;
		
		for (int i = 0; i < numOfVertices; i++) {
			graph.setLayoutX(i, i/sqrt/(float)rows);
			if (i/sqrt%2==0) {
				graph.setLayoutY(i, (i-((i/sqrt)*sqrt))/(float)(sqrt-1));
			} else {
				graph.setLayoutY(i, 1-(i-((i/sqrt)*sqrt))/(float)(sqrt-1));
			}
		}
	}

	private void initRandomTree() {
		graph = new Graph(numOfVertices);
		
		// -- get prufernr
		int[] pruferNr = new int[numOfVertices - 2];
		RandomElement re = this.getRandomElement();
		for (int i = 0; i < numOfVertices - 2; i++) {
			pruferNr[i] = re.choose(0, numOfVertices - 1);
		}
		
		// -- the lookupMap contains as many entries as nodes and each entry represents how often the node is "selected"
		int[] lookupMap = new int[pruferNr.length + 2];
		for (int i = 0; i < pruferNr.length; i++) lookupMap[pruferNr[i]]++;
		
		int startAt = 0;
		
		// -- now parse the prufer number
		for (int i = 0; i < pruferNr.length; i++) {
			// -- we search the first node that doesn't occur in the lookupMap
			int j = startAt;
			while (j < graph.getNumberOfNodes() && lookupMap[j] != 0) j++;
			// -- the next number of the prufer number is removed here
			lookupMap[pruferNr[i]]--;
			// -- and the found node is added
			lookupMap[j]++;
			// -- adjust index to start next scan
			if (lookupMap[pruferNr[i]] == 0 && pruferNr[i] < j) startAt = pruferNr[i];
			else startAt = j+1;
			// -- add edge
			graph.addEdge(pruferNr[i], j, 1);
		}
		// -- add last edge
		int node1 = startAt;
		while (node1 < graph.getNumberOfNodes() && lookupMap[node1] != 0) node1++;
		int node2 = node1 + 1;
		while (node2 < graph.getNumberOfNodes() && lookupMap[node2] != 0) node2++;
		graph.addEdge(node1, node2,1);
	}	
	
	private void initFullGraph() {
		RandomElement random = this.getRandomElement();
		graph = new Graph(numOfVertices);
		
		for (int i = 0; i < numOfVertices; i++) {
			for (int j = i + 1; j < numOfVertices; j++) {
				graph.addEdge(i, j, random.choose(1, maxWeight));
			}
		}
	}
	
	private void initRandomGraphWithFixedEdgeNumber() {
		int maxEdges = numOfVertices * (numOfVertices - 1) / 2;
		if (numOfEdges > maxEdges)
			throw new RuntimeException("Too many edges requested in random graph creation.");
		if (numOfEdges < numOfVertices - 1)
			throw new RuntimeException("Too few edges requested in random graph creation.");
		
		if (numOfEdges < maxEdges / 2) {
			initRandomGraphByEdgeCollection();
		} else {
			initRandomGraphByEdgeRemoval();
		}
	}
	
	private void initRandomGraphByEdgeCollection() {
		RandomElement random = this.getRandomElement();
		graph = new Graph(numOfVertices);
		int added = 0;
		while (added < numOfEdges) {
			int node1 = random.choose(numOfVertices) - 1;
			int node2 = random.choose(numOfVertices) - 1;
			
			if (node1 == node2)
				continue;
			if (graph.containsEdge(node1, node2))
				continue;
			
			graph.addEdge(node1, node2, random.choose(1, maxWeight));
			added++;
		}
	}
	
	private void initRandomGraphByEdgeRemoval() {
		RandomElement random = this.getRandomElement();
		graph = new Graph(numOfVertices);
		
		for (int i = 0; i < numOfVertices; i++) {
			for (int j = i + 1; j < numOfVertices; j++) {
				graph.addEdge(i, j, random.choose(1, maxWeight));
			}
		}
		
		int maxEdges = numOfVertices * (numOfVertices - 1) / 2;
		int remaining = maxEdges - numOfEdges;
		while (remaining > 0) {
			int node1 = random.choose(numOfVertices) - 1;
			int node2 = random.choose(numOfVertices) - 1;
			
			if (node1 == node2)
				continue;
			if (!graph.containsEdge(node1, node2))
				continue;
			
			graph.removeEdge(node1, node2);
			remaining--;
		}
	}
	
	public void initRandomGraphWithFixedEdgeProbability() {
		RandomElement random = this.getRandomElement();
		graph = new Graph(numOfVertices);
		
		for (int i = 0; i < numOfVertices; i++) {
			for (int j = i + 1; j < numOfVertices; j++) {
				if (random.raw() < probForEdge) {
					graph.addEdge(i, j, random.choose(1, maxWeight));
				}
			}
		}
	}
	
	private void initIngosGraph() {
		if ((verticesInTriangles - 1) % 2 != 0)
			throw new RuntimeException("Illegal Number of Nodes in Triangles");
		if (verticesInTriangles > numOfVertices)
			throw new RuntimeException("Illegal Number of Nodes in Triangles");
		
		graph = new Graph(numOfVertices);
		
		int verticesInClique = numOfVertices - verticesInTriangles + 1;
		int numOfTriangles = (verticesInTriangles - 1) / 2;
		int edgesInClique = verticesInClique * (verticesInClique - 1) / 2;
		int triangleEdgeWeightBase = edgesInClique + 1;
		// optimalMSTWeight = triangleEdgeWeightBase * numOfTriangles * 4 + verticesInClique - 1;
		
		// layout
		float margin = 0.05f;
		float edgeLength = (1 - margin * 2) / (numOfTriangles + (verticesInClique + 2) / ((float) Math.PI));
		float triangleHeight = ((float) Math.sin(Math.PI / 3)) * edgeLength;
		float circleRadius = (1 - margin * 2 - edgeLength * numOfTriangles) / 2;
		float circleCenterX = 1 - margin - circleRadius;
		
		// create triangles
		for (int i = 0; i < numOfTriangles; i++) {
			graph.addEdge(i * 2, i * 2 + 1, triangleEdgeWeightBase * 2);
			graph.addEdge(i * 2, i * 2 + 2, triangleEdgeWeightBase * 3);
			graph.addEdge(i * 2 + 1, i * 2 + 2, triangleEdgeWeightBase * 2);
			
			// layout
			graph.setLayoutX(i * 2, margin + edgeLength * i);
			graph.setLayoutY(i * 2, 0.5f);
			graph.setLayoutX(i * 2 + 1, margin + edgeLength * (i + 0.5f));
			graph.setLayoutY(i * 2 + 1, triangleHeight + 0.5f);
		}
		
		// create clique
		for (int i = numOfTriangles * 2; i < numOfVertices; i++) {
			for (int j = i + 1; j < numOfVertices; j++) {
				graph.addEdge(i, j, 1);
			}
			
			// layout
			int position = i - numOfTriangles * 2;
			double angle = Math.PI * 2 / verticesInClique * position + Math.PI;
			graph.setLayoutX(i, (float) (circleCenterX + Math.cos(angle) * circleRadius));
			graph.setLayoutY(i, (float) (0.5 + Math.sin(angle) * circleRadius));
		}
	}
	
	private void initRandomGraphWithBinaryValuedWeights() {
		
		RandomElement random = this.getRandomElement();
		int maxEdges = numOfVertices * (numOfVertices - 1) / 2;
		int edgesCount = 0;
		
		/* Choose the number of edges randomly */
		for (int i = 0; i < maxEdges; i++) {
			if (random.raw() < probForEdge) {
				edgesCount++;
			}
		}
		setPropertyNumberOfEdges(new Integer(edgesCount));
		
		if (numOfEdges > maxEdges)
			throw new RuntimeException("Too many edges requested in random graph creation.");
		initRandomGraphByEdgeCollectionBinaryValue();
		
	}
	
	private void initRandomGraphByEdgeCollectionBinaryValue() {
		
		RandomElement random = this.getRandomElement();
		
		graph = new Graph(numOfVertices);
		int added = 0;
		int bc = 1; // binary count
		
		while (added < numOfEdges) {
			int node1 = random.choose(numOfVertices) - 1;
			int node2 = random.choose(numOfVertices) - 1;
			
			if (node1 == node2)
				continue;
			if (graph.containsEdge(node1, node2))
				continue;
			
			graph.addEdge(node1, node2, bc);
			bc = bc * 2;
			added++;
		}
		
	}

	private void initLolliTree() {
		// create graph
		graph = new Graph(numOfVertices);
		
		for (int i = 1; i < verticesLolli; i++) {
			graph.addEdge(0, i, 1);
		}
		
		for (int i = verticesLolli-1; i < numOfVertices-1; i++) {
			graph.addEdge(i,i+1,1);
		}
		
		// set layout
		float centerX = 0.25f;
		float centerY = 0.5f;

		graph.setLayoutX(0,centerX);
		graph.setLayoutY(0,centerY);
		
		for (int i = 1; i < verticesLolli; i++) {
			float posX = (float)Math.cos(2*Math.PI/(verticesLolli-1)*i)/5.0f+centerX;
			float posY = (float)Math.sin(2*Math.PI/(verticesLolli-1)*i)/5.0f+centerY;
			graph.setLayoutX(i,posX);
			graph.setLayoutY(i,posY);
		}
		
		for (int i = verticesLolli; i < numOfVertices; i++) {
			float posX = centerX+0.2f+(1-centerX-0.1f)/(numOfVertices-verticesLolli+1)*(i-verticesLolli+1);
			float posY = centerY;
			graph.setLayoutX(i,posX);
			graph.setLayoutY(i,posY);		
		}
	}

	private void initKTree() {
		// create graph
		graph = new Graph(numOfVertices);
		
		int temp = 1+numOfVertices*(degreeK-1);
		int numOfLevels = (int) Math.ceil(Math.log(temp)/Math.log(degreeK));

		for (int l = 0; l < numOfLevels-1; l++) {
			int nodesOnLevel = (int) Math.round(Math.exp(l*Math.log(degreeK)));
			int offset = (int)Math.round((Math.exp(l*Math.log(degreeK))-1)/(degreeK-1));
			int k = 0;
				
			loop:for (int i = 0; i < nodesOnLevel; i++) {
				for (int j = 0; j < degreeK; j++) {
					if (offset+nodesOnLevel+k >= numOfVertices) break loop;
					graph.addEdge(offset+i, offset+nodesOnLevel+k, 1);
					k++;					
				}
			}
		}
		
		// set layout		
		int j = 0;
		
		for (int l = 0; l < numOfLevels; l++) {
			
			int nodesOnLevel = (int)Math.round(Math.exp(l*Math.log(degreeK)));
			float posY = 1.0f/numOfLevels*l;
			for (int i = 0; i < nodesOnLevel; i++) {
				float posX = 1.0f/(nodesOnLevel+1)*(i+1);
				graph.setLayoutX(j,posX);
				graph.setLayoutY(j,posY);
				j++;
				if (j >= numOfVertices) return;
			}
		}
	}

	private void initSemiP() {
		// create graph
		graph = new Graph(numOfVertices);
		
		// create numOfEdges matching edges between the first 2*numOfEdges nodes
		for (int i=0; i<numOfEdges; i++)
			graph.addEdge(2*i,2*i+1,1);

		// now create the other edges between numOfVertices Nodes
		RandomElement random = this.getRandomElement();
		
		for (int i=0; i<numOfVertices-1; i++)
			for (int j=i+1; j<numOfVertices; j++)
			{
				if (random.uniform(0.0f,1.0f) < probForEdge) graph.addEdge(i,j,1);
			}
			
	}

	private void initSemi() {
		// create graph
		graph = new Graph(numOfVertices);
		
		// create numOfEdges matching edges between the first 2*numOfEdges nodes
		for (int i=0; i<Math.round(numOfVertices/2); i++)
			graph.addEdge(2*i,2*i+1,1);

		// now create the other edges between numOfVertices Nodes
		RandomElement random = this.getRandomElement();

		int missing = (int) Math.round(numOfEdges-numOfVertices/2);
	
		while (missing > 0) {
			int start = random.choose(numOfVertices)-1;
			int end = random.choose(numOfVertices)-1;
		
			if (start == end) continue;
			if (graph.containsEdge(start,end)) continue;
		
			graph.addEdge(start,end,1);		
			
			missing--;
		}
	}
	
	private void initTrapLine() {
		graph = new Graph(numOfVertices);
		
		int leftLine = (numOfVertices - 2) / 2;
		int rightLine = leftLine - 1;
		float spacing = 1f / (numOfVertices - 2);
		for (int i = 0; i < leftLine; i++) {
			graph.setLayoutX(i, (i + 0.5f) * spacing);
			graph.setLayoutY(i, 0.4f);
		}
		for (int i = leftLine; i < leftLine + rightLine + 1; i++) {
			graph.setLayoutX(i, (i + 0.5f) * spacing);
			graph.setLayoutY(i, 0.5f);
		}
		graph.setLayoutX(numOfVertices - 2, 0.5f - (0.5f * spacing));
		graph.setLayoutY(numOfVertices - 2, 0.6f);
		graph.setLayoutX(numOfVertices - 1, 0.5f - (1.5f * spacing));
		graph.setLayoutY(numOfVertices - 1, 0.6f);
		
		for (int i = 0; i < numOfVertices - 3; i++) {
			graph.addEdge(i, i + 1, 1);
		}
		graph.addEdge(leftLine, numOfVertices - 2, 1);
		graph.addEdge(numOfVertices - 2, numOfVertices - 1, 1);
	}
	
	public Inspector getInspector() {
		return new GraphEdgeSelectionConfigurationPanel(this);
	}
	
	public Configuration getConfiguration() {
		Configuration conf = new GraphEdgeSelectionConfiguration();
		Configuration.getConfigurationFor(this, conf);
		return conf;
	}

}
