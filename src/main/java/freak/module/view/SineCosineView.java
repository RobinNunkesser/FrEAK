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
import freak.core.population.IndividualList;
import freak.core.view.AbstractView;
import freak.core.view.swingsupport.FreakSwingModel;
import freak.core.view.swingsupport.UpdateManager;
import freak.module.fitness.bitstring.SineCosine;
import freak.module.searchspace.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.BitSet;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A specialized view for the fitness function SineCosine. The individuals in the population are represented as points on a circle.
 * @author  Heiko
 */
public class SineCosineView extends AbstractView implements Configurable, FreakSwingModel {
	private transient JPanel panel;
	
	private IndividualList individuals;
	private int n;
	
	/**
	 * Constructs a new <code>SineCosineView</code> with a link back to the
	 * current schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public SineCosineView(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();
		n = ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if(!(schedule.getRealFitnessFunction() instanceof SineCosine)) {
			throw new UnsupportedEnvironmentException("Does only work with the fitness function Sine Cosine.");
		}
	}
	
	public JPanel createPanel() {
		panel = new SineCosinePanel(this);
		return panel;
	}
	
	public void update(Object data) {
		super.update(data);
		
		synchronized(this) {
			individuals = (IndividualList) data;
		}
		UpdateManager.markDirty(this);
	}
	
	public void flush() {
		if (panel == null) return;
		
		synchronized(this) {
			panel.repaint();
		}
	}
	
	public String getName() {
		return "SineCosine View";
	}
	
	public String getDescription() {
		return "The individuals in the population are represented as points on a circle.";
	}
	
	public Class[] getInputDataTypes() {
		return new Class[] { IndividualList.class };
	}
	
	public IndividualList getIndividualList() {
		return individuals;
	}
	
	
	
	
	/**
	 * @author  Heiko
	 */
	private class SineCosinePanel extends JPanel {
		
		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private SineCosineView view;
		
		private int START_POS = 7;
		
		public SineCosinePanel(SineCosineView view) {
			super();
			this.view = view;
			setOpaque(true);
			setBackground(Color.white);
			setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;
			
			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;
			int minDim = (currentWidth < currentHeight) ? currentWidth : currentHeight;
			
			// draw circle
			graphics.drawOval(START_POS,START_POS,minDim,minDim);
			
			// draw axes
			graphics.drawLine(START_POS+minDim/2,0,START_POS+minDim/2,minDim+15);
			graphics.drawLine(0,START_POS+minDim/2,minDim+15,START_POS+minDim/2);
			
			// draw all search points
			graphics.setColor(Color.red);
			int prev_x = START_POS+minDim-2;
			int prev_y = (int)(START_POS+(double)minDim/2-2);
			for (int i = 0; i <= n; i++) {
				double x = minDim/2*Math.cos(2*Math.PI*i/n);
				double y = -minDim/2*Math.sin(2*Math.PI*i/n);
				int new_x = (int)(START_POS+minDim/2+x)-2;
				int new_y = (int)(START_POS+minDim/2+y)-2;
				graphics.fillOval(new_x,new_y,5,5);
				graphics.drawLine(prev_x,prev_y,new_x,new_y);
				prev_x = new_x;
				prev_y = new_y;
			}
			
			synchronized (view) {
				individuals = view.getIndividualList();
				if (individuals != null) {
					Iterator it = individuals.iterator();
					while (it.hasNext()) {
						drawIndividual((Individual)it.next(),minDim);
					}
				}
			}
		}
		
		private void drawIndividual(Individual individual, int minDim) {
			BitSet bs = ((BitStringGenotype)individual.getGenotype()).getBitSet();
			int card = bs.cardinality();
			
			if (card == ((BitString)getSchedule().getGenotypeSearchSpace()).getDimension()) {
				double x = minDim/2*Math.cos(2*Math.PI*card/n);
				double y = -minDim/2*Math.sin(2*Math.PI*card/n);
				int new_x = (int)(START_POS+minDim/2+x)-25;
				int new_y = (int)(START_POS+minDim/2+y)-8;
				
				graphics.setColor(Color.green);
				graphics.fillOval(new_x,new_y,12,12);
				return;
			}
			
			double x = minDim/2*Math.cos(2*Math.PI*card/n);
			double y = -minDim/2*Math.sin(2*Math.PI*card/n);
			int new_x = (int)(START_POS+minDim/2+x)-8;
			int new_y = (int)(START_POS+minDim/2+y)-8;
			
			graphics.setColor(Color.black);
			graphics.fillOval(new_x,new_y,12,12);
		}
		
	}
}

