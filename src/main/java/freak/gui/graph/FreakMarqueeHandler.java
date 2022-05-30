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
import java.awt.event.*;
import javax.swing.*;
import org.jgraph.*;
import org.jgraph.graph.*;
import org.jgraph.graph.Port;

/**
 * MarqueeHandler that connects <code>Operator</code>s.
 * @author  Andrea, Matthias
 */
class FreakMarqueeHandler extends BasicMarqueeHandler {

	public FreakMarqueeHandler(JGraph graph, EditorDialog editor) {
		super();
		this.graph = graph;
		this.editor = editor;
	}

	protected EditorDialog editor;
	protected JGraph graph;

	// Holds the Start and the Current Point
	protected Point start;
	// Holds the Start and the Current Point
	protected Point current;

	// Holds the First and the Current Port
	protected PortView port;
	// Holds the First and the Current Port
	protected PortView firstPort;

	// Override to Gain Control (for PopupMenu and ConnectMode)
	public boolean isForceMarqueeEvent(MouseEvent e) {
		/*		// If Right Mouse Button we want to Display the PopupMenu
				if (SwingUtilities.isRightMouseButton(e))
					// Return Immediately
					return true;*/
		// Find and Remember Port
		port = getSourcePortAt(e.getPoint());
		// If Port Found 
		if (port != null)
			return true;
		// Else Call Superclass
		return super.isForceMarqueeEvent(e);
	}

	// Display PopupMenu or Remember Start Location and First Port
	public void mousePressed(final MouseEvent e) {
		// If Right Mouse Button
		if (SwingUtilities.isRightMouseButton(e)) {
			//			// Scale From Screen to Model
			//			Point loc = graph.fromScreen(e.getPoint());
			//			// Find Cell in Model Coordinates
			//			Object cell = graph.getFirstCellForLocation(loc.x, loc.y);
			//			// Create PopupMenu for the Cell
			//			JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
			//			// Display PopupMenu
			//			menu.show(graph, e.getX(), e.getY());

			// Else if Remembered Port is Valid
		} else if (port != null && !e.isConsumed()) {
			FreakPort fPort = (FreakPort)port.getCell();
			// TODO matthias
			//if (fPort.getPort() instanceof OutPort && fPort.getPort().getPartner() == null) {
			if (fPort.getPort() instanceof OutPort) {
				// Remember Start Location
				start = graph.toScreen(port.getLocation(null));
				// Remember First Port
				firstPort = port;
				// Consume Event
				e.consume();
			} else {
				super.mousePressed(e);
			}
		} else
			// Call Superclass
			super.mousePressed(e);
	}

	// Find Port under Mouse and Repaint Connector
	public void mouseDragged(MouseEvent e) {
		// If remembered Start Point is Valid
		if (start != null && !e.isConsumed()) {
			// Fetch Graphics from Graph
			Graphics g = graph.getGraphics();

			// Xor-Paint the old Connector (Hide old Connector)
			paintConnector(Color.BLACK, graph.getBackground(), g);

			//Reset Remembered Port
			PortView newPort = getTargetPortAt(e.getPoint());
			
			port = newPort;

			if (port != null) {
				current = graph.toScreen(port.getLocation(null));
			} else {
				// Else If no Port was found then Point to Mouse Location
				current = graph.snap(e.getPoint());
			}
			// Xor-Paint the new Connector
			paintConnector(graph.getBackground(), Color.black, g);
			// Consume Event
			e.consume();
		}
		// Call Superclass
		super.mouseDragged(e);
	}

	public PortView getSourcePortAt(Point point) {
		// Scale from Screen to Model
		Point tmp = graph.fromScreen(new Point(point));
		// Find a Port View in Model Coordinates and Remember
		PortView port = graph.getPortViewAt(tmp.x, tmp.y);

		if (port == null) {
			return null;
		}

		FreakPort fPort = (FreakPort)port.getCell();
		if (!(fPort.getPort().getOperator() instanceof Finish)) {
			// TODO matthias
			//if (fPort.getPort() instanceof OutPort && fPort.getPort().getPartner() == null) {
			if (fPort.getPort() instanceof OutPort) {
				return port;
			}
		}
		return null;
	}

	/**
	 * Find a Cell at point and Return Port at point as a PortView.
	 */
	protected PortView getTargetPortAt(Point point) {
		//Scale from Screen to Model
		Point tmp = graph.fromScreen(new Point(point));
		PortView port = graph.getPortViewAt(tmp.x, tmp.y);
		if (port != null) {
			FreakPort fPort = (FreakPort)port.getCell();
			// TODO matthias
			//if (fPort.getPort() instanceof InPort && fPort.getPort().getPartner() == null && !(((FreakGraphModel)graph.getModel()).hasCircle((FreakPort)firstPort.getCell(), fPort))) {
			if (fPort.getPort() instanceof InPort && !(((FreakGraphModel)graph.getModel()).hasCircle((FreakPort)firstPort.getCell(), fPort))) {
				// Disallow two edges with same target and source
				for (int i=fPort.getPort().getNumberOfPartners()-1; i >= 0; i--) {
					if (fPort.getPort().getPartner(i) == ((FreakPort)firstPort.getCell()).getPort()) {
						return null;			
					}
					
				}
				if (!(fPort.getPort().getOperator() instanceof Start)) {
					return port;
				}
			}
		}
		return null;
	}

	// Connect the First Port and the Current Port in the Graph or Repaint
	public void mouseReleased(MouseEvent e) {
		// If Valid Event, Current and First Port
		if (e != null && !e.isConsumed() && port != null && firstPort != null && firstPort != port) {
			FreakPort fPort = (FreakPort)port.getCell();
			// If Port was found then Point to Port Location
			// TODO matthias
			//if (port != null && fPort.getPort() instanceof InPort && fPort.getPort().getPartner() == null && !(((FreakGraphModel)graph.getModel()).hasCircle((FreakPort)firstPort.getCell(), fPort))) {
			if (port != null && fPort.getPort() instanceof InPort && !(((FreakGraphModel)graph.getModel()).hasCircle((FreakPort)firstPort.getCell(), fPort))) {
				// Then Establish Connection
				editor.connect((Port)firstPort.getCell(), (Port)port.getCell());
				// Consume Event
				e.consume();
				// Else Repaint the Graph
			} else {
				graph.repaint();
			}
		} else
			graph.repaint();
		// Reset Global Vars
		firstPort = port = null;
		start = current = null;
		// Call Superclass
		super.mouseReleased(e);
	}

	// Show Special Cursor if Over Port
	public void mouseMoved(MouseEvent e) {
		// Check Mode and Find Port
		if (e != null && getSourcePortAt(e.getPoint()) != null && !e.isConsumed() && graph.isPortsVisible()) {
			//FreakPort port = (FreakPort)getSourcePortAt(e.getPoint()).getCell();
			//if (port.getPort() instanceof OutPort && port.getPort().getPartner() == null) {
			// Set Cusor on Graph (Automatically Reset)
			graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
			// Consume Event
			e.consume();
			//}
		}
		// Call Superclass
		super.mouseReleased(e);
	}

	// Use Xor-Mode on Graphics to Paint Connector
	protected void paintConnector(Color fg, Color bg, Graphics g) {
		// Set Foreground
		g.setColor(fg);
		// Set Xor-Mode Color
		g.setXORMode(bg);
		
		// Highlight the Current Port
		// paintPort(graph.getGraphics());
		
		// If Valid First Port, Start and Current Point
		if (firstPort != null && start != null && current != null)
			// Then Draw A Line From Start to Current Point
			g.drawLine(start.x, start.y, current.x, current.y);
	}

}
