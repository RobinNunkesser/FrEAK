/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.core.view.SingleIndividualView;
import freak.core.view.swingsupport.*;
import freak.module.fitness.graphedgeselection.AbstractMSTFitnessFunction;
import freak.module.fitness.graphedgeselection.MSTFitnessFunctionInterface;
import freak.module.searchspace.GraphEdgeSelection;
import freak.module.searchspace.GraphEdgeSelectionGenotype;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael
 */
public class EdgeSelectionView extends SingleIndividualView implements Configurable, FreakSwingModel {
	
	private boolean displayNodeNumbers = false;
	private boolean displayEdgeWeights = false;
	private boolean displayUnselectedEdges = true;
	private Color nodeNumberColor = Color.blue;
	private Color edgeWeightColor = Color.lightGray;
	private Color nodeColor = Color.red;
	private Color selectedEdgeColor = Color.darkGray;
	private Color unselectedEdgeColor = new Color(204, 255, 204);
	
	
	public EdgeSelectionView(Schedule schedule) {
		super(schedule);
		
		setDisplayStrategy(RANDOM);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getPhenotypeSearchSpace() instanceof GraphEdgeSelection)) {
			throw new UnsupportedEnvironmentException("Unsupported search space");
		}
		
		if (schedule.getRealFitnessFunction() instanceof AbstractMSTFitnessFunction) {
			displayEdgeWeights = true;
		}
		setDisplayStrategy(RANDOM);
	}
	
	public JPanel createIndividualPanel() {
		return new ESPanel(this);
	}
	
	public String getName() {
		return "Edge Selection View";
	}
	
	public String getDescription() {
		return "Graphical view of an edge selection.";
	}
	
	public Class[] getInputDataTypes() {
		return new Class[] { IndividualList.class };
	}
	
	/**
	 * @author  nunkesser
	 */
	class ESPanel extends JPanel {
		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private EdgeSelectionView esview;
		
		public ESPanel(EdgeSelectionView esview) {
			super();
			this.esview = esview;
			setBackground(Color.white);
			setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;
			
			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;
			
			synchronized (esview) {
				Individual individual = getIndividualToDisplay();
				
				if (individual != null) {
					GraphEdgeSelectionGenotype gesGenotype;
					
					gesGenotype = (GraphEdgeSelectionGenotype)individual.getPhenotype();
					
					GraphEdgeSelection.Graph graph =  gesGenotype.getGraph();
					String infotext = null;
					
					if (schedule.getRealFitnessFunction() instanceof MSTFitnessFunctionInterface) {
						int numEdgesMinusN = graph.getNumberOfSelectedEdges(gesGenotype)-graph.getNumberOfNodes();
						String numEdgesMinusNString = "";
						if (numEdgesMinusN<0) numEdgesMinusNString = "" + numEdgesMinusN;
						else if (numEdgesMinusN>0) numEdgesMinusNString = "+"+numEdgesMinusN;
						infotext = "Edge Weights: " + graph.getSumOfWeightsOfAllSelectedEdges(gesGenotype) + " (" + graph.weightOfMST()
						+ ") Selected Edges: n" + numEdgesMinusNString
						+ ", Con. Components: " + graph.numOfConnectedComponentsForSelectedEdges(gesGenotype);
					}
					
					if (infotext == null && (schedule.getRealFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
						infotext = "Fitness: " + ((SingleObjectiveFitnessFunction)schedule.getRealFitnessFunction()).evaluate(individual, null);
					}
					
					graphics.drawString(infotext, insets.left, insets.top - 10);
					
					boolean[] map = gesGenotype.getEdgeSelection();
					if (displayUnselectedEdges) {
						// paint unselected edges
						for (int i = 0; i < map.length; i++) {
							if (!map[i]) {
								GraphEdgeSelection.Graph.Edge edge = graph.edgeForSpeedupIndex(i);
								int node1 = edge.getStartNode();
								int node2 = edge.getEndNode();
								drawLine(graph.getLayoutX(node1),1-graph.getLayoutY(node1), graph.getLayoutX(node2),1-graph.getLayoutY(node2), edge.getWeight(), esview.unselectedEdgeColor);
							}
						}
					}
					
					// paint selected edges
					for (int i = 0; i < map.length; i++) {
						if (map[i]) {
							GraphEdgeSelection.Graph.Edge edge = graph.edgeForSpeedupIndex(i);
							int node1 = edge.getStartNode();
							int node2 = edge.getEndNode();
							drawLine(graph.getLayoutX(node1),1-graph.getLayoutY(node1), graph.getLayoutX(node2),1-graph.getLayoutY(node2), edge.getWeight(),selectedEdgeColor);
						}
					}
					int nodeCount = graph.getNumberOfNodes();
					//paint nodes
					for (int i = 0; i < nodeCount; i++) {
						drawPoint(graph.getLayoutX(i),1-graph.getLayoutY(i), (new Integer(i)).toString().toCharArray());
					}
				}
			}
		}
		
		private void drawLine(double x1, double y1, double x2, double y2, int weight, Color edgeColor) {
			graphics.setColor(edgeColor);
			graphics.drawLine(insets.left + (int) (x1 * currentWidth), insets.top + (int) (y1 * currentHeight), insets.left + (int) (x2 * currentWidth), insets.top + (int) (y2 * currentHeight));
			if (esview.displayEdgeWeights) {
				graphics.setColor(esview.edgeWeightColor);
				graphics.drawString(Integer.toString(weight), insets.left + (int) ((0.55*x2+0.45*x1) * currentWidth), insets.top + (int) ((0.55*y2+0.45*y1) * currentHeight));
			}
		}
		
		private void drawPoint(double x, double y, char[] c) {
			graphics.setColor(esview.nodeColor);
			graphics.fillOval(insets.left + (int) (x * currentWidth) - 1, insets.top + (int) (y * currentHeight) - 1, 2, 2);
			if (esview.displayNodeNumbers) {
				graphics.setColor(esview.nodeNumberColor);
				graphics.drawChars(c, 0, c.length, insets.left + (int) (x * currentWidth) + 2, insets.top + (int) (y * currentHeight) + 10);
			}
		}
	}
	
	public void setPropertyDisplayNumbers(Boolean dn) {
		displayNodeNumbers = dn.booleanValue();
		UpdateManager.markDirty(this);
	}
	
	public Boolean getPropertyDisplayNumbers() {
		return new Boolean(displayNodeNumbers);
	}
	
	public String getShortDescriptionForDisplayNumbers() {
		return "Display node numbers";
	}
	
	public String getLongDescriptionForDisplayNumbers() {
		return "Displays the numbers for all nodes.";
	}
	
	public void setPropertyDisplayEdgeWeights(Boolean dn) {
		displayEdgeWeights = dn.booleanValue();
		UpdateManager.markDirty(this);
	}
	
	public Boolean getPropertyDisplayEdgeWeights() {
		return new Boolean(displayEdgeWeights);
	}
	
	public String getShortDescriptionForDisplayEdgeWeights() {
		return "Display edge weights";
	}
	
	public String getLongDescriptionForDisplayEdgeWeights() {
		return "Displays the weight for all edges.";
	}
	
	public void setPropertyDisplayUnselectedEdges(Boolean dn) {
		displayUnselectedEdges = dn.booleanValue();
		UpdateManager.markDirty(this);
	}
	
	public Boolean getPropertyDisplayUnselectedEdges() {
		return new Boolean(displayUnselectedEdges);
	}
	
	public String getShortDescriptionForDisplayUnselectedEdges() {
		return "Display unselected edges";
	}
	
	public String getLongDescriptionForDisplayUnselectedEdges() {
		return "This view will draw all edges, including the unselected edges.";
	}
	
	public void setPropertyNumberColor(Color color) {
		if (color != null) {
			nodeNumberColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyNumberColor() {
		return nodeNumberColor;
	}
	
	public String getShortDescriptionForNumberColor() {
		return "Node numbers color";
	}
	
	public String getLongDescriptionForNumberColor() {
		return "Sets the color for the displayed node numbers.";
	}
	
	public void setPropertyEdgeWeightColor(Color color) {
		if (color != null) {
			edgeWeightColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyEdgeWeightColor() {
		return edgeWeightColor;
	}
	
	public String getShortDescriptionForEdgeWeightColor() {
		return "Edge weight color";
	}
	
	public String getLongDescriptionForEdgeWeightColor() {
		return "Sets the color for the displayed edge weights.";
	}
	
	public void setPropertyPointColor(Color color) {
		if (color != null) {
			nodeColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyPointColor() {
		return nodeColor;
	}
	
	public String getShortDescriptionForPointColor() {
		return "Color of the nodes";
	}
	
	public String getLongDescriptionForPointColor() {
		return "Sets the color for the displayed nodes.";
	}
	
	public void setPropertySelectedEdgeColor(Color color) {
		if (color != null) {
			selectedEdgeColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertySelectedEdgeColor() {
		return selectedEdgeColor;
	}
	
	public String getShortDescriptionForSelectedEdgeColor() {
		return "Selected edges color";
	}
	
	public String getLongDescriptionForSelectedEdgeColor() {
		return "Sets the color for the selected edges.";
	}
	
	public void setPropertyUnselectedEdgeColor(Color color) {
		if (color != null) {
			unselectedEdgeColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyUnselectedEdgeColor() {
		return unselectedEdgeColor;
	}
	
	public String getShortDescriptionForUnselectedEdgeColor() {
		return "Unselected edges color";
	}
	
	public String getLongDescriptionForUnselectedEdgeColor() {
		return "Sets the color for the unselected edges.";
	}
}
