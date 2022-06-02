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

import freak.core.control.Schedule;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.view.SingleIndividualView;
import freak.core.view.swingsupport.FreakSwingModel;
import freak.core.view.swingsupport.UpdateManager;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

import javax.swing.*;
import java.awt.*;

/**
 * Draws the Ising Model on cliques.
 *
 * @author Dirk
 */
public class IsingModelCliquesView extends SingleIndividualView implements Configurable, FreakSwingModel {
	
	private static final float MAXIMAL_HUE = 0.8f;
	private static float SATURATION = 1.0f;
	private static float BRIGHTNESS = 1.0f;
	
	private int maximalNumberOfBridges = 1000;
	private boolean rotateCliques = true;
	
	/**
	 * Constructs a new <code>IsingModelCliquesView</code> with a link back to the current
	 * schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public IsingModelCliquesView(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException("Does only work with the fitness IsingModelCliques");
		}
	}
	
	public JPanel createIndividualPanel() {
		return new CliquesPanel(this);
	}
	
	public String getName() {
		return "Ising Model Cliques";
	}
	
	public String getDescription() {
		return "Shows the nodes of the Ising Model on cliques. " +
			"Only the bridges between the cliques are displayed.";
	}
	
	/**
	 * Sets the maximal number of bridges displayed in the view.
	 */
	public void setPropertyMaximalNumberOfBridges(Integer number) {
		if (number.intValue() > 0)
			this.maximalNumberOfBridges= number.intValue();
	}

	/**
	 * Returns the maximal number of bridges displayed in the view.
	 */
	public Integer getPropertyMaximalNumberOfBridges() {
		return new Integer(maximalNumberOfBridges);
	}

	public String getShortDescriptionForMaximalNumberOfBridges() {
		return "Maximal number of bridges";
	}

	public String getLongDescriptionForMaximalNumberOfBridges() {
		return "Sets the maximal number of bridges displayed in the view. " +
			"If more bridges are to be displayed, only the first ones are shown.";
	}
	
	public void setPropertyRotateCliques(Boolean b) {
		rotateCliques = b.booleanValue();
		
		UpdateManager.markDirty(this);
	}

	public Boolean getPropertyRotateCliques() {
		return new Boolean(rotateCliques);
	}
	
	public String getShortDescriptionForRotateCliques() {
		return "Rotate cliques";
	}

	public String getLongDescriptionForRotateCliques() {
		return "If set, all cliques will be rotated so that they face the center of the graph.";
	}

	/**
	 * The <code>JPanel</code> belonging to <code>IsingModelCliquesView</code>.
	 * @author  Dirk
	 */
	class CliquesPanel extends JPanel {
		
		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private IsingModelCliquesView view;
		
		/**
		 * @uml.property  name="cliques"
		 * @uml.associationEnd  multiplicity="(0 -1)"
		 */
		private IsingModelCliques.Clique[] cliques;
		private boolean oldRotateCliques;
		
		// an array of (x, y) coordinates for the nodes
		private int[][] nodePositions;
		
		// an array of (x, y) coordinates for the centers of the cliques
		private int[][] cliquePositions;
		
		// the radius for a circle representing a node
		private int nodeRadius;
		// the radius for a circle containing one clique
		private double cliqueRadius;
		// the radius for the circle containing the whole graph
		private double graphRadius;
		
		public CliquesPanel(IsingModelCliquesView view) {
			super();
			this.view = view;
			setBackground(Color.white);
		}
		
		private void computeCoordinates() {
			int n = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getDimension();

			nodePositions = new int[n][2];
			cliquePositions = new int[cliques.length][2];
			
			computeRadii();

			// the index of the current node in the genotype 			
			int currentPosition = 0;
			
			for (int i = 0; i < cliques.length; i++) {
				cliquePositions[i][0] = currentWidth / 2 + (int)(Math.sin(2 * Math.PI * i / cliques.length) * graphRadius);
				cliquePositions[i][1] = currentHeight / 2 - (int)(Math.cos(2 * Math.PI * i / cliques.length) * graphRadius);
				
				// every clique is rotated so that the first node faces the 
				// center of the graph if rotateCliques is true.
				double cliqueRotation = (rotateCliques ? -2 * Math.PI * i / cliques.length : 0);
				
				for (int j = 0; j < cliques[i].getSize(); j++) {
					nodePositions[currentPosition][0] = cliquePositions[i][0] + (int)(Math.sin(2 * Math.PI * j / cliques[i].getSize() + cliqueRotation) * cliqueRadius);
					nodePositions[currentPosition][1] = cliquePositions[i][1] + (int)(Math.cos(2 * Math.PI * j / cliques[i].getSize() + cliqueRotation) * cliqueRadius);
					currentPosition++;
				}
			}
		}
		
		private void computeRadii() {
			// compute provisional values for the radii where the graph circle
			// fills the whole screen
			graphRadius = Math.min(currentWidth, currentHeight) / 2;
			cliqueRadius = Math.sin(Math.PI / cliques.length) * graphRadius * 0.5;
			nodeRadius = (int)(Math.sin(Math.PI / cliques[0].getSize()) * cliqueRadius);
			
			// scale radii to their correct size
			double scale =  graphRadius / (graphRadius + cliqueRadius + nodeRadius);
			graphRadius *= scale;
			cliqueRadius *= scale;
			nodeRadius = (int)(nodeRadius * scale);
			
			nodeRadius = Math.max(nodeRadius, 2);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;
			
			int oldCurrentWidth = currentWidth;
			int oldCurrentHeight = currentHeight;
			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;
			
			synchronized (view) {
				IsingModelCliques ising = (IsingModelCliques)getSchedule().getRealFitnessFunction();
			
				if (cliques == null || cliques != ising.getCliques() || oldCurrentWidth != currentWidth || oldCurrentHeight != currentHeight || oldRotateCliques != rotateCliques) {
					oldRotateCliques = rotateCliques;
					cliques = ising.getCliques();
					if (cliques != null) computeCoordinates();
				}
			
				if (cliques != null) {
					// cache all needed colors
					Color[] colors = new Color[((GeneralString)getSchedule().getGenotypeSearchSpace()).getPropertyNumberOfChars().intValue()];
					for (int i = 0; i < colors.length; i++) {
						colors[i] = getColor(i);
					}

						Individual individual = getIndividualToDisplay();
				
						if (individual != null) {
					
							g.setColor(Color.black);
							// draw bridges
							int[][] bridges = ising.getBridges();
							for (int i = 0; i < Math.min(bridges.length, maximalNumberOfBridges); i++) {
								int x1 = nodePositions[bridges[i][0]][0];
								int y1 = nodePositions[bridges[i][0]][1];
								int x2 = nodePositions[bridges[i][1]][0];
								int y2 = nodePositions[bridges[i][1]][1];
								g.drawLine(x1, y1, x2, y2);
							}
					
							// draw clique borders
							g.setColor(Color.gray);
							for (int i = 0; i < cliques.length; i++) {
								g.drawOval(cliquePositions[i][0] - (int)cliqueRadius, cliquePositions[i][1] - (int)cliqueRadius, (int)cliqueRadius * 2, (int)cliqueRadius * 2);
							}
						
							// draw nodes
							int[] genotype = ((GeneralStringGenotype)individual.getGenotype()).getIntArray();
							for (int i = 0; i < genotype.length; i++) {
								g.setColor(colors[genotype[i]]);
								int x = nodePositions[i][0];
								int y = nodePositions[i][1];
								g.fillOval(x - nodeRadius / 2, y - nodeRadius / 2, nodeRadius, nodeRadius);
							}
						
						}
					}
			}
		}
		
		private Color getColor(int value) {
			int k = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getPropertyNumberOfChars().intValue();
			return new Color(Color.HSBtoRGB((float)value / k * MAXIMAL_HUE, SATURATION, BRIGHTNESS));
		}
		
	}
}
