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
import freak.module.fitness.generalstring.IsingModelTorus;
import freak.module.searchspace.GeneralString;

import javax.swing.*;
import java.awt.*;

/**
 * Draws the Ising Model torus.
 *
 * @author Dirk
 */
public class IsingModelTorusView extends SingleIndividualView implements Configurable, FreakSwingModel {
	
	private static final float MAXIMAL_HUE = 0.8f;
	private static float SATURATION = 1.0f;
	private static float BRIGHTNESS = 1.0f;
	
	private boolean showGrid = true;
	
	/**
	 * Constructs a new <code>IsingModelTorusView</code> with a link back to the current
	 * schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public IsingModelTorusView(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelTorus)) {
			throw new UnsupportedEnvironmentException("Does only work with the fitness IsingModelTorus");
		}
	}
	
	public JPanel createIndividualPanel() {
		return new TorusPanel(this);
	}
	
	public String getName() {
		return "Ising Model Torus";
	}
	
	public String getDescription() {
		return "Shows the nodes of the Ising Model torus.";
	}
	
	/**
	 * Specifies if the grid is to be drawn.
	 */
	public void setPropertyShowGrid(Boolean b) {
		showGrid = b.booleanValue();
		UpdateManager.markDirty(this);
	}
	
	/**
	 * Returns true if the grid is to be drawn.
	 */
	public Boolean getPropertyShowGrid() {
		return new Boolean(showGrid);
	}
	
	public String getShortDescriptionForShowGrid() {
		return "Show grid";	
	}
	
	public String getLongDescriptionForShowGrid() {
		return "If set, a grid of black lines is drawn.";	
	}
	
	/**
	 * The <code>JPanel</code> belonging to <code>IsingModelTorusView</code>.
	 * @author  Dirk
	 */
	class TorusPanel extends JPanel {
		
		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private IsingModelTorusView view;
		private int torusWidth;
		private int torusHeight;
		
		// a cache for torus field Rectangle objects
		private Rectangle[][] fields;
		
		public TorusPanel(IsingModelTorusView view) {
			super();
			this.view = view;
			setOpaque(true);
			
			IsingModelTorus ising = (IsingModelTorus)getSchedule().getRealFitnessFunction();
			torusWidth = ising.getPropertyTorusWidth().intValue();
			torusHeight = ising.getPropertyTorusHeight().intValue();
		}
		
		private void cacheRectangles() {
			fields = new Rectangle[torusWidth][torusHeight];
			for (int y = 0; y < torusHeight; y++) {
				for (int x = 0; x < torusWidth; x++) {
					fields[x][y] = getField(x, y);
				}
			}
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;
			
			int oldCurrentWidth = currentWidth;
			int oldCurrentHeight = currentHeight;
			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;
			
			if (fields == null || oldCurrentWidth != currentWidth || oldCurrentHeight != currentHeight) {
				cacheRectangles();
			}
			
			IsingModelTorus ising = (IsingModelTorus)getSchedule().getRealFitnessFunction();
			
			synchronized (view) {
				Individual individual = getIndividualToDisplay();
				
				if (individual != null) {
					
					boolean drawGrid = showGrid && getField(0, 0).width > 3 && getField(0, 0).height > 3;
					// cache all needed colors
					Color[] colors = new Color[((GeneralString)getSchedule().getGenotypeSearchSpace()).getPropertyNumberOfChars().intValue()];
					for (int i = 0; i < colors.length; i++) {
						colors[i] = getColor(i);
					}
					
					for (int y = 0; y < torusHeight; y++) {
						for (int x = 0; x < torusWidth; x++) {
							Rectangle rect = fields[x][y];
							if (drawGrid) {
								graphics.setColor(Color.black);
								graphics.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
								graphics.setColor(getColor(ising.torusValue(individual, x, y)));
								graphics.fillRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
							} else {
								graphics.setColor(colors[ising.torusValue(individual, x, y)]);
								graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
							}
						}
					}
					
				}
			}
		}
		
		private Color getColor(int value) {
			int k = ((GeneralString)getSchedule().getGenotypeSearchSpace()).getPropertyNumberOfChars().intValue();
			return new Color(Color.HSBtoRGB((float)value / k * MAXIMAL_HUE, SATURATION, BRIGHTNESS));
		}
		
		private Rectangle getField(int j, int i) {
			int x = currentWidth * j / torusWidth;
			int y = currentHeight * i / torusHeight;
			return new Rectangle(x, y, (currentWidth * (j + 1) / torusWidth) - x, (currentHeight * (i + 1) / torusHeight) - y);
		}
		
	}
}
