/*
 * @(#)GPMarqueeHandler.java	1.2 05.02.2003
 *
 * Copyright (C) 2003 sven.luzar
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jgraph.pad;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.jgraph.GPGraphpad;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;
import org.jgraph.pad.resources.Translator;

import org.jgpd.UI.utils.*;
import org.jgpd.jgraph.*;


/**
 * MarqueeHandler that can insert cells.
 *
 * @author sven.luzar
 * @version 1.0
 *
 */
public class GPMarqueeHandler extends BasicMarqueeHandler {

	int m_XDifference, m_YDifference, dx, dy;
	boolean m_dragging;
	Container c;

	/** A reference to the graphpad object
	 */
	protected GPGraphpad graphpad;

	/** The default color for borders
	 */
	protected transient Color defaultBorderColor = Color.black;

	protected transient JToggleButton buttonSelect = new JToggleButton();
	protected transient JToggleButton buttonText = new JToggleButton();
	protected transient JToggleButton buttonLine = new JToggleButton();
	protected transient JToggleButton buttonEdge = new JToggleButton();
	protected transient JToggleButton buttonZoomArea = new JToggleButton();
	protected transient JToggleButton buttonActivity = new JToggleButton();
	protected transient JToggleButton buttonDecision = new JToggleButton();
	protected transient JToggleButton buttonStart = new JToggleButton();
	protected transient JToggleButton buttonEnd = new JToggleButton();
	protected transient JToggleButton buttonSplit = new JToggleButton();
	protected transient JToggleButton buttonJoin = new JToggleButton();

	protected Point start, current;

	protected Rectangle bounds;

	protected PortView port, firstPort, lastPort;

	/**
	 * Constructor for GPMarqueeHandler.
	 */
	public GPMarqueeHandler(GPGraphpad graphpad) {
		super();
		this.graphpad = graphpad;
		ButtonGroup grp = new ButtonGroup();
		grp.add(buttonSelect);
		grp.add(buttonActivity);
		grp.add(buttonDecision);
		grp.add(buttonStart);
		grp.add(buttonEnd);
		grp.add(buttonSplit);
		grp.add(buttonJoin);
		grp.add(buttonText);
		grp.add(buttonLine);
		grp.add(buttonEdge);
		grp.add(buttonZoomArea);
	}

	/* Return true if this handler should be preferred over other handlers. */
	public boolean isForceMarqueeEvent(MouseEvent e) {
		return !buttonSelect.isSelected()
			|| isPopupTrigger(e)
			|| super.isForceMarqueeEvent(e);
	}

	protected boolean isPopupTrigger(MouseEvent e) {
		if (e==null) return false;
		return SwingUtilities.isRightMouseButton(e) && !e.isShiftDown();
	}

	public void mousePressed(MouseEvent event) {
		m_XDifference = event.getX();
		m_YDifference = event.getY();
		dx = 0;
		dy = 0;
		if (!isPopupTrigger(event)
			&& !event.isConsumed()
			&& !buttonSelect.isSelected()) {
			start = graphpad.getCurrentGraph().snap(event.getPoint());
			firstPort = port;
			if (buttonEdge.isSelected() && firstPort != null)
			{
				start =
					graphpad.getCurrentGraph().toScreen(
						firstPort.getLocation(null));
			}
			event.consume();
		}
		if (!isPopupTrigger(event))
			super.mousePressed(event);
		else {
			boolean selected = false;
			Object[] cells = graphpad.getCurrentGraph().getSelectionCells();
			for (int i = 0; i < cells.length && !selected; i++)
				selected =
					graphpad.getCurrentGraph().getCellBounds(
						cells[i]).contains(
						event.getPoint());
			if (!selected)
				graphpad.getCurrentGraph().setSelectionCell(
					graphpad.getCurrentGraph().getFirstCellForLocation(
						event.getX(),
						event.getY()));
			event.consume();
		}
	}

	public void mouseDragged(MouseEvent event) {
		if (!event.isConsumed() && !buttonSelect.isSelected()) {
			Graphics g = graphpad.getCurrentGraph().getGraphics();
			Color bg = graphpad.getCurrentGraph().getBackground();
			Color fg = Color.black;
			g.setColor(fg);
			g.setXORMode(bg);
			overlay(g);
			current = graphpad.getCurrentGraph().snap(event.getPoint());
			if (buttonEdge.isSelected() || buttonLine.isSelected()) {
				port =
					getPortViewAt(
						event.getX(),
						event.getY(),
						!event.isShiftDown());
				if (port != null)
					current =
						graphpad.getCurrentGraph().toScreen(
							port.getLocation(null));
			}

			bounds = new Rectangle(start).union(new Rectangle(current));
            // If any of the circle ( not ellipse ) drawing button actions
            // are being dragged, ensure we are drawing a circle by making
            // the smaller of the
			if (buttonStart.isSelected() || buttonEnd.isSelected())
			{
				if ( bounds.width > bounds.height )
				{
					bounds.setSize(bounds.width,bounds.width);
				}
				else
				{
					bounds.setSize(bounds.height,bounds.height);
				}
			}
			g.setColor(bg);
			g.setXORMode(fg);
			overlay(g);
			event.consume();
		} else if (
			!event.isConsumed()
				&& isForceMarqueeEvent(event)
				&& isPopupTrigger(event)) {
			c = graphpad.getCurrentGraph().getParent();
			if (c instanceof JViewport) {
				JViewport jv = (JViewport) c;
				Point p = jv.getViewPosition();
				int newX = p.x - (event.getX() - m_XDifference);
				int newY = p.y - (event.getY() - m_YDifference);
				dx += (event.getX() - m_XDifference);
				dy += (event.getY() - m_YDifference);

				int maxX =
					graphpad.getCurrentGraph().getWidth() - jv.getWidth();
				int maxY =
					graphpad.getCurrentGraph().getHeight() - jv.getHeight();
				if (newX < 0)
					newX = 0;
				if (newX > maxX)
					newX = maxX;
				if (newY < 0)
					newY = 0;
				if (newY > maxY)
					newY = maxY;

				jv.setViewPosition(new Point(newX, newY));
				event.consume();
			}
		}
		super.mouseDragged(event);
	}

	// Default Port is at index 0
	public PortView getPortViewAt(int x, int y, boolean jump) {
		Point sp = graphpad.getCurrentGraph().fromScreen(new Point(x, y));
		PortView port = graphpad.getCurrentGraph().getPortViewAt(sp.x, sp.y);
		// Shift Jumps to "Default" Port (child index 0)
		if (port == null && jump) {
			Object cell =
				graphpad.getCurrentGraph().getFirstCellForLocation(x, y);
			if (graphpad.getCurrentGraph().isVertex(cell)) {
				Object firstChild =
					graphpad.getCurrentGraph().getModel().getChild(cell, 0);
				CellView firstChildView =
					graphpad
						.getCurrentGraph()
						.getGraphLayoutCache()
						.getMapping(
						firstChild,
						false);
				if (firstChildView instanceof PortView)
					port = (PortView) firstChildView;
			}
		}
		return port;
	}

	public void mouseReleased(MouseEvent event) {

		// precondition test
		/* we don't want to have a
		 * Default Esc Action

		if (event == null) {
			Action a =
				graphpad.getCurrentActionMap().get(
					Utilities.getClassNameWithoutPackage(FileClose.class));
			if (a != null)
				a.actionPerformed(new ActionEvent(this, 0, "FileClose"));
			return;

		}
		*/

		GraphModelProvider gmp = graphpad.getCurrentGraphModelProvider();
		GraphModel model = graphpad.getCurrentGraph().getModel();

		if (isPopupTrigger(event)) {
			if (Math.abs(dx) < graphpad.getCurrentGraph().getTolerance()
				&& Math.abs(dy) < graphpad.getCurrentGraph().getTolerance()) {
				Object cell =
					graphpad.getCurrentGraph().getFirstCellForLocation(
						event.getX(),
						event.getY());
				if (cell == null)
					graphpad.getCurrentGraph().clearSelection();
				Container parent = graphpad.getCurrentGraph();
				do {
					parent = parent.getParent();
				} while (parent != null && !(parent instanceof GPGraphpad));

				GPGraphpad pad = (GPGraphpad) parent;
				if (pad != null) {
					JPopupMenu pop = pad.getBarFactory().createGraphPopupMenu();

					pop.show(
						graphpad.getCurrentGraph(),
						event.getX(),
						event.getY());
				}
			}
			event.consume();
		} else if (
			event != null
				&& !event.isConsumed()
				&& !buttonSelect.isSelected())
		{
			if ( bounds == null )
			{
				// if no bounds have been set this might be because
				// the user wants a default size
				if (buttonActivity.isSelected())
				{
					bounds = new Rectangle(event.getX(),
					                       event.getY(),
					                       ActivityView.default_width,
					                       ActivityView.default_height);
				}
				else if (buttonDecision.isSelected())
				{
					bounds = new Rectangle(event.getX(),
					                       event.getY(),
					                       DecisionView.default_width,
					                       DecisionView.default_height);
				}
				else if (buttonStart.isSelected())
				{
					bounds = new Rectangle(event.getX(),
										   event.getY(),
										   StartView.defaultWidth,
										   StartView.defaultHeight);
				}
				else if (buttonEnd.isSelected())
				{
					bounds = new Rectangle(event.getX(),
										   event.getY(),
										   EndView.defaultWidth,
										   EndView.defaultHeight);
				}
				else if (buttonSplit.isSelected())
				{
					bounds = new Rectangle(event.getX(),
										   event.getY(),
										   SplitView.defaultWidth,
										   SplitView.defaultHeight);
				}
				else if (buttonJoin.isSelected())
				{
					bounds = new Rectangle(event.getX(),
										   event.getY(),
										   JoinView.defaultWidth,
										   JoinView.defaultHeight);
				}
			}

		    if ( bounds != null )
		    {
				graphpad.getCurrentGraph().fromScreen(bounds);
				bounds.width++;
				bounds.height++;
				if (buttonZoomArea.isSelected()) {
					Rectangle view = graphpad.getCurrentGraph().getBounds();
					if (graphpad.getCurrentGraph().getParent()
						instanceof JViewport)
						view =
							((JViewport) graphpad.getCurrentGraph().getParent())
								.getViewRect();
					if (bounds.width != 0
						&& bounds.height != 0
						&& SwingUtilities.isLeftMouseButton(event)) {
						double scale =
							Math.min(
								(double) view.width / (double) bounds.width,
								(double) view.height / (double) bounds.height);
						if (scale > 0.1) {
							Rectangle unzoomed =
								graphpad.getCurrentGraph().fromScreen(bounds);
							graphpad.getCurrentGraph().setScale(scale);
							graphpad.getCurrentGraph().scrollRectToVisible(
								graphpad.getCurrentGraph().toScreen(unzoomed));
						}
					} else
						graphpad.getCurrentGraph().setScale(1);
					// FIX: Set ResizeAction to null!
				}else if (buttonActivity.isSelected())
					addActivity(bounds);
				else if (buttonDecision.isSelected())
					addDecision(bounds);
				else if (buttonStart.isSelected())
					addStart(bounds);
				else if (buttonEnd.isSelected())
					addEnd(bounds);
				else if (buttonSplit.isSelected())
					addSplit(bounds);
				else if (buttonJoin.isSelected())
					addJoin(bounds);
				else if (buttonText.isSelected()) {
					Object cell =
						addVertex(
							GraphModelProvider.CELL_VERTEX_TEXT,
							"Type Here",
							bounds,
							false,
							Color.black);
					graphpad.getCurrentGraph().startEditingAtCell(cell);
				}
				else if (buttonEdge.isSelected()) {
					Point p =
						graphpad.getCurrentGraph().fromScreen(new Point(start));
					Point p2 =
						graphpad.getCurrentGraph().fromScreen(new Point(current));
					ArrayList list = new ArrayList();
					list.add(p);
					list.add(p2);
					Map map = GraphConstants.createMap();
					GraphConstants.setPoints(map, list);
					// FIXME this should be whatever the current default routing is
					GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
					Map viewMap = new Hashtable();

					//DefaultEdge cell = new DefaultEdge("");
					Object cell =
						gmp.createCell(
							model,
							GraphModelProvider.CELL_EDGE_DEFAULT,
							new GPUserObject(""),
							map);

					viewMap.put(cell, map);
					Object[] insert = new Object[] { cell };
					ConnectionSet cs = new ConnectionSet();
					if (firstPort != null)
				    {
						cs.connect(cell, firstPort.getCell(), true);
				    }
					if (port != null)
				    {
						cs.connect(cell, port.getCell(), false);
				    }

					graphpad.getCurrentGraph().getModel().insert(
						insert,
						viewMap,
						cs,
						null,
						null);

				} else if (buttonLine.isSelected()) {
					Point p =
						graphpad.getCurrentGraph().fromScreen(new Point(start));
					Point p2 =
						graphpad.getCurrentGraph().toScreen(new Point(current));
					ArrayList list = new ArrayList();
					list.add(p);
					list.add(p2);
					Map map = GraphConstants.createMap();
					GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
					GraphConstants.setPoints(map, list);
					GraphConstants.setLineEnd(map, GraphConstants.ARROW_CLASSIC);
					GraphConstants.setEndFill(map, true);
					//GraphConstants.setConnectable(map, false);
					Map viewMap = new Hashtable();
					//DefaultEdge cell = new DefaultEdge("");
					Object cell =
						gmp.createCell(
							model,
							GraphModelProvider.CELL_EDGE_DEFAULT,
							new GPUserObject(""),
							map);
					viewMap.put(cell, map);
					Object[] insert = new Object[] { cell };
					ConnectionSet cs = new ConnectionSet();
					if (firstPort != null)
					{
						cs.connect(cell, firstPort.getCell(), true);
					}
					if (port != null)
				    {
						cs.connect(cell, port.getCell(), false);
				    }

					graphpad.getCurrentGraph().getModel().insert(
						insert,
						viewMap,
						cs,
						null,
						null);

				}
				event.consume();
		    }
		}
		buttonSelect.doClick();
		firstPort = null;
		port = null;
		start = null;
		current = null;
		bounds = null;
		super.mouseReleased(event);
	}

	public void mouseMoved(MouseEvent event) {
		if (!buttonSelect.isSelected() && !event.isConsumed()) {
			graphpad.getCurrentGraph().setCursor(
				new Cursor(Cursor.CROSSHAIR_CURSOR));
			event.consume();
			if (buttonEdge.isSelected() || buttonLine.isSelected()) {
				PortView oldPort = port;
				PortView newPort =
					getPortViewAt(
						event.getX(),
						event.getY(),
						!event.isShiftDown());
				if (oldPort != newPort) {
					Graphics g = graphpad.getCurrentGraph().getGraphics();
					Color bg = graphpad.getCurrentGraph().getBackground();
					Color fg = graphpad.getCurrentGraph().getMarqueeColor();
					g.setColor(fg);
					g.setXORMode(bg);
					overlay(g);
					port = newPort;
					g.setColor(bg);
					g.setXORMode(fg);
					overlay(g);
				}
			}
		}
		super.mouseMoved(event);
	}

	public void overlay(Graphics g) {
		super.overlay(g);
		paintPort(graphpad.getCurrentGraph().getGraphics());
		if (bounds != null && start != null) {
			if (buttonZoomArea.isSelected())
				 ((Graphics2D) g).setStroke(GraphConstants.SELECTION_STROKE);
			if (
				(buttonLine.isSelected() || buttonEdge.isSelected())
					&& current != null)
				g.drawLine(start.x, start.y, current.x, current.y);
			else if (!buttonSelect.isSelected())
				g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
			else if (buttonActivity.isSelected())
			{
				int arcSize = ActivityView.getArcSize(bounds.width, bounds.height);
				g.drawRoundRect(bounds.x,
                                bounds.y,
                                bounds.width,
                                bounds.height,
                                arcSize,
                                arcSize);
		    }
		    else if (buttonDecision.isSelected())
		    {
				Graphics2D g2 = (Graphics2D) g;
				int [] polyx =  { 0,
					              bounds.width/2,
					              bounds.width,
					              bounds.width/2 };
				int [] polyy =  { bounds.height/2,
								  0,
					              bounds.height/2,
					              bounds.height };

				Polygon poly = new Polygon( polyx, polyy, 4 );
				g2.draw(poly);
			}
			else if (buttonStart.isSelected())
			{
				((Square)bounds).limitToSquare();
				g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
			}
			else if (buttonEnd.isSelected())
			{
				((Square)bounds).limitToSquare();
				g.drawOval(bounds.x, bounds.y, bounds.width, bounds.height);
			}
			else if ( (buttonSplit.isSelected()) || (buttonEnd.isSelected()) )
			{
				g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}
	}

	protected void paintPort(Graphics g) {
		if (port != null) {
			boolean offset =
				(GraphConstants.getOffset(port.getAllAttributes()) != null);
			Rectangle r =
				(offset) ? port.getBounds() : port.getParentView().getBounds();
			r = graphpad.getCurrentGraph().toScreen(new Rectangle(r));
			int s = 3;
			r.translate(-s, -s);
			r.setSize(r.width + 2 * s, r.height + 2 * s);
			GPGraphUI ui = (GPGraphUI) graphpad.getCurrentGraph().getUI();
			ui.paintCell(g, port, r, true);
		}
	}

	//
	// Cell Creation
	//

	public Object addVertex(
		int type,
		Object userObject,
		Rectangle bounds,
		boolean autosize,
		Color border) {
		Map viewMap = new Hashtable();
		Map map;
		GraphModelProvider gmp = graphpad.getCurrentGraphModelProvider();
		GraphModel model = graphpad.getCurrentGraph().getModel();

		// Create Vertex
		Object obj = (userObject instanceof String) ? userObject : "";

		map = GraphConstants.createMap();
		GraphConstants.setBounds(map, bounds);
		GraphConstants.setOpaque(map, false);
		if (border != null)
			GraphConstants.setBorderColor(map, border);
		String fontName = Translator.getString("FontName");
		try {
			int fontSize = Integer.parseInt(Translator.getString("FontSize"));
			int fontStyle = Integer.parseInt(Translator.getString("FontStyle"));
			GraphConstants.setFont(
				map,
				new Font(fontName, fontStyle, fontSize));
		} catch (Exception e) {
			// handle error
		}

		if (autosize)
			GraphConstants.setAutoSize(map, true);
		List toInsert = new LinkedList();

		Object cell;
		switch (type) {
			case GraphModelProvider.CELL_VERTEX_TEXT :
				cell =
					gmp.createCell(
						model,
						GraphModelProvider.CELL_VERTEX_TEXT,
						new GPUserObject(userObject.toString()),
						map);
				break;
			default :
				cell =
					gmp.createCell(
						model,
						GraphModelProvider.CELL_VERTEX_DEFAULT,
						new GPUserObject(userObject.toString()),
						map);
				break;
		}
		viewMap.put(cell, map);
		toInsert.add(cell);

		// Create Ports
		int u = GraphConstants.PERMILLE;
		//DefaultPort port;
		Object port;

		// Floating Center Port (Child 0 is Default)
		// port = new DefaultPort("Center");
		// cell.add(port);
		port = gmp.createCell(model, GraphModelProvider.CELL_PORT_DEFAULT, "Center", null);
		gmp.addPort(cell, port);
		toInsert.add(port);

		if (userObject instanceof ImageIcon) {
			GraphConstants.setIcon(map, (ImageIcon) userObject);

			// Single non-floating central-port
			map = GraphConstants.createMap();
			GraphConstants.setOffset(
				map,
				new Point((int) (u / 2), (int) (u / 2)));
			viewMap.put(port, map);
			toInsert.add(port);
		} else {
			// Top Left
			//port = new DefaultPort("Topleft");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point(0, 0));
			port = gmp.createCell(model, GraphModelProvider.CELL_PORT_DEFAULT, "Topleft", map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Top Center
			//port = new DefaultPort("Topcenter");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point((int) (u / 2), 0));
			port =
				gmp.createCell(model, GraphModelProvider.CELL_PORT_DEFAULT, "Topcenter", map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Top Right
			//port = new DefaultPort("Topright");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point(u, 0));
			port =
				gmp.createCell(model, GraphModelProvider.CELL_PORT_DEFAULT, "Topright", map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Top Center
			//port = new DefaultPort("Middleleft");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point(0, (int) (u / 2)));
			port =
				gmp.createCell(model, GraphModelProvider.CELL_PORT_DEFAULT, "Middleleft", map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Middle Right
			//port = new DefaultPort("Middleright");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point(u, (int) (u / 2)));
			port =
				gmp.createCell(
					model,
					GraphModelProvider.CELL_PORT_DEFAULT,
					"Middleright",
					map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Bottom Left
			//port = new DefaultPort("Bottomleft");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point(0, u));
			port =
				gmp.createCell(model, GraphModelProvider.CELL_PORT_DEFAULT, "Bottomleft", map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Bottom Center
			//port = new DefaultPort("Bottomcenter");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point((int) (u / 2), u));
			port =
				gmp.createCell(
					model,
					GraphModelProvider.CELL_PORT_DEFAULT,
					"Bottomcenter",
					map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);

			// Bottom Right
			//port = new DefaultPort("Bottomright");
			//cell.add(port);
			map = GraphConstants.createMap();
			GraphConstants.setOffset(map, new Point(u, u));
			port =
				gmp.createCell(
					model,
					GraphModelProvider.CELL_PORT_DEFAULT,
					"Bottomright",
					map);
			gmp.addPort(cell, port);
			viewMap.put(port, map);
			toInsert.add(port);
		}

		graphpad.getCurrentGraph().getModel().insert(
			toInsert.toArray(),
			viewMap,
			null,
			null,
			null);
		return cell;
	}

    // Add interface for activity
	public void addActivity(Rectangle bounds){}

    // Add interface for decision
	public void addDecision(Rectangle bounds){}

    // Add interface for start
	public void addStart(Rectangle bounds){}

    // Add interface for end
	public void addEnd(Rectangle bounds){}

	public void addSplit(Rectangle bounds){}

	public void addJoin(Rectangle bounds){}


	/**
	 * Returns the buttonActivity.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonActivity() {
		return buttonActivity;
	}

	/**
	 * Returns the buttonDecision.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonDecision() {
		return buttonDecision;
	}

	/**
	 * Returns the buttonStart.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonStart() {
		return buttonStart;
	}

	/**
	 * Returns the buttonEnd.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonEnd() {
		return buttonEnd;
	}

	/**
	 * Returns the buttonSplit.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonSplit() {
		return buttonSplit;
	}

	/**
	 * Returns the buttonJoin.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonJoin() {
		return buttonJoin;
	}

	/**
	 * Returns the buttonEdge.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonEdge() {
		return buttonEdge;
	}

	/**
	 * Returns the buttonLine.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonLine() {
		return buttonLine;
	}

	/**
	 * Returns the buttonSelect.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonSelect() {
		return buttonSelect;
	}

	/**
	 * Returns the buttonText.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonText() {
		return buttonText;
	}

	/**
	 * Returns the buttonZoomArea.
	 * @return JToggleButton
	 */
	public JToggleButton getButtonZoomArea() {
		return buttonZoomArea;
	}

}
