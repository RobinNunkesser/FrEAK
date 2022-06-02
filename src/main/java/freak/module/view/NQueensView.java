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
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.view.SingleIndividualView;
import freak.core.view.swingsupport.FreakSwingModel;
import freak.module.fitness.permutation.NQueensProblem;
import freak.module.searchspace.Permutation;
import freak.module.searchspace.PermutationGenotype;

import javax.swing.*;
import java.awt.*;

/**
 * Draws a chess board with N queens.
 *
 * @author Dirk
 */
public class NQueensView extends SingleIndividualView implements FreakSwingModel {
	
	/**
	 * Constructs a new <code>HyperCube</code> with a link back to the current
	 * schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public NQueensView(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof NQueensProblem)) {
			throw new UnsupportedEnvironmentException("Does only work with the fitness NQueensProblem");
		}
	}
	
	public JPanel createIndividualPanel() {
		return new ChessBoardPanel(this);
	}
	
	public String getName() {
		return "N Queens View";
	}
	
	public String getDescription() {
		return "Shows a NxN chess board with the N queens.";
	}
	
	/**
	 * The <code>JPanel</code> belonging to <code>NQueensView</code>.
	 * @author  Dirk
	 */
	class ChessBoardPanel extends JPanel {
		
		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private NQueensView view;
		
		public ChessBoardPanel(NQueensView view) {
			super();
			this.view = view;
			setOpaque(true);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;
			
			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;
			
			int n = ((Permutation)getSchedule().getGenotypeSearchSpace()).getDimension();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					graphics.setColor(((i + j) % 2 == 0) ? Color.black : Color.white);
					Rectangle rect = getField(i, j);
					graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
				}
			}
			
			if (getIndividualToDisplay() != null) {
				synchronized (view) {
					drawQueens(getIndividualToDisplay());
				}
			}
		}
		
		private void drawQueens(Individual individual) {
			int n = ((Permutation)getSchedule().getGenotypeSearchSpace()).getDimension();
			int[] perm = ((PermutationGenotype)individual.getGenotype()).getIntArray();
			for (int i = 0; i < n; i++) {
				Rectangle rect = getField(i, perm[i] - 1);
				graphics.setColor(Color.red);
				graphics.fillOval(rect.x, rect.y, rect.width, rect.height);
			}
		}
		
		private Rectangle getField(int i, int j) {
			int n = ((Permutation)getSchedule().getGenotypeSearchSpace()).getDimension();
			int x = currentWidth * j / n;
			int y = currentHeight * i / n;
			return new Rectangle(x, y, (currentWidth * (j + 1) / n) - x, (currentHeight * (i + 1) / n) - y);
		}
		
	}
}
