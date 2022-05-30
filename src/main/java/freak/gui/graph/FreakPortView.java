/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.graph;

import freak.core.graph.*;
import java.awt.*;
import org.jgraph.*;
import org.jgraph.graph.*;

/**
 * @author  Matthias
 */
public class FreakPortView extends PortView {

	/*		public static void main(String[] args) {
					URL portUrl = MyPortView.class.getClassLoader().getResource("port.gif");
					if (portUrl != null)
							portIcon = new ImageIcon(portUrl);
					JFrame frame = new JFrame("PortView");
					JGraph graph = new JGraph() {
					};
					graph.setPortsVisible(true);
					frame.getContentPane().add(new JScrollPane(graph));
					frame.pack();
					//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
			}
	*/
	protected PortRenderer renderer = null;

	public FreakPortView(Object cell, JGraph graph, CellMapper cm) {
		super(cell, graph, cm);
		if (((FreakPort)cell).getPort().getOperator() instanceof Start && ((FreakPort)cell).getPort() instanceof InPort) {
			//don't show port
			renderer = new FreakPortRendererBlind();
		} else if (((FreakPort)cell).getPort().getOperator() instanceof Finish && ((FreakPort)cell).getPort() instanceof OutPort) {
			//don't show port
			renderer = new FreakPortRendererBlind();
		} else {
			renderer = new FreakPortRendererDefault();
		}
	}

	/** 
	* Returns the bounds for the port view. 
	*/
	public Rectangle getBounds() {
		/*		if (portIcon != null) {
					Rectangle bounds = new Rectangle(getLocation(null));
					int width = portIcon.getIconWidth();
					int height = portIcon.getIconHeight();
					bounds.x = bounds.x - width / 2;
					bounds.y = bounds.y - height / 2;
					bounds.width = width;
					bounds.height = height;
					return bounds;
				} */
		return super.getBounds();
	}

	/**
	 * @return  the renderer
	 * @uml.property  name="renderer"
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class FreakPortRendererDefault extends PortRenderer {

		public void paint(Graphics g) {

			g.setColor(graph.getBackground());
			g.setColor(Color.blue);
			if (((OperatorGraphCell)view.getParentView().getCell()).getOperator() instanceof HasFloatingNumberOfOutPorts) {
				if (((FreakPort)view.getCell()).getPort() instanceof OutPort) {
					g.setColor(Color.red);
				}
			} else if (((OperatorGraphCell)view.getParentView().getCell()).getOperator() instanceof HasFloatingNumberOfInPorts) {
				if (((FreakPort)view.getCell()).getPort() instanceof InPort) {
					g.setColor(Color.red);
				}
			}
			//g.setXORMode(graph.getBackground());
			if (preview) {
				Dimension d = getSize();
				//g.setColor(Color.red);
				g.drawRect(1, 1, d.width - 3, d.height - 3);
				g.drawRect(2, 2, d.width - 5, d.height - 5);
			} else {
				Dimension d = getSize();
				//g.setColor(Color.blue);
				g.drawRect(1, 1, d.width - 3, d.height - 3);
				g.drawRect(2, 2, d.width - 5, d.height - 5);
				//portIcon.paintIcon(graph, g, 0, 0);
			}
		}

	}

	public static class FreakPortRendererBlind extends PortRenderer {

		public void paint(Graphics g) {
			//do nothing
		}

	}

}