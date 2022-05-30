/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.graph;

import freak.core.control.*;
import freak.core.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import org.jgraph.*;
import org.jgraph.graph.*;

/**
 * @author  Andrea, Matthias
 */
public class FreakGraphModel extends DefaultGraphModel implements PortsChangedEventListener, Serializable {

	Map operatorMapper = new HashMap(); // maps a Operator to its OperatorGraphCell
	private OperatorGraph graph = null;

	/**
	 * Construct a new FreakGraphModel reflecting <code>graph</code>.
	 */
	public FreakGraphModel(OperatorGraph graph) {
		super();
		initializeFromGraph(graph);
		this.graph = graph;
	}

	public FreakGraphModel(Schedule schedule) {
		graph = new OperatorGraph(schedule);
		initializeFromGraph(graph);

		// default layout for empty graph
		Map attributes = getAttributes(operatorMapper.get(graph.getStart()));
		attributes.put("bounds", new Rectangle(new Point(40, 10), new Dimension(100, 20)));
		attributes = getAttributes(operatorMapper.get(graph.getFinish()));
		attributes.put("bounds", new Rectangle(new Point(40, 250), new Dimension(100, 20)));
	}

	/**
	 * Initializes this <code>FreakGraphModel</code> from an 
	 * <code>OperatorGraph</code>. All existing Operators are added 
	 * as <code>OperatorGraphCell</code>s, their Ports are added as
	 * <code>FreakPort</code>s, and Ports having a partner are 
	 * connected.
	 */
	protected void initializeFromGraph(OperatorGraph graph) {
		OperatorGraphCell[] toInsert = new OperatorGraphCell[graph.getOperatorCount()];

		Map portMapper = new HashMap(); // maps a Port to its FreakPort			

		Hashtable attributes = new Hashtable();
		for (int i = 0; i < graph.getOperatorCount(); i++) {
			Operator currentOperator = graph.getOperator(i);

			toInsert[i] = new OperatorGraphCell(currentOperator);
			operatorMapper.put(currentOperator, toInsert[i]);

			currentOperator.setPortsChangedListener(this);

			Map map = toInsert[i].getAttributes();
			attributes.put(currentOperator, map);

			// build map to map Ports to FreakPorts
			for (Iterator ports = toInsert[i].getPortIterator(); ports.hasNext();) {
				FreakPort port = (FreakPort)ports.next();
				portMapper.put(port.getPort(), port);
			}

		}

		insert(toInsert, attributes, null, null, null);

		for (Iterator operators = operatorMapper.values().iterator(); operators.hasNext();) {
			OperatorGraphCell currentOperator = (OperatorGraphCell)operators.next();

			for (Iterator ports = currentOperator.getPortIterator(); ports.hasNext();) {
				FreakPort port = (FreakPort)ports.next();
				Port portModel = port.getPort();
				for (Iterator iter = portModel.getPartnerIterator(); iter.hasNext();) {
					Port partnerModel = (Port)iter.next();
					FreakPort partner = (FreakPort)portMapper.get(partnerModel);

					if (partner != null && partnerModel instanceof InPort) {
						ConnectionSet cs = new ConnectionSet();

						FreakEdge edge = new FreakEdge(port, partner);

						cs.connect(edge, port, partner);
						Map map = GraphConstants.createMap();
						GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
						attributes = new Hashtable();
						attributes.put(edge, map);

						insert(new Object[] { edge }, attributes, cs, null, null);
					}					
				}

			}
		}
		refreshFloatingPorts();
	}

// TODO matthias delete if no error occurs
/*	public boolean acceptsSource(Object edge, Object port) {
		if (edge instanceof DefaultEdge && port instanceof FreakPort) {
			DefaultEdge e = (DefaultEdge)edge;
			FreakPort p = (FreakPort)port;
			if ((e.getTarget() == null || (e.getTarget() instanceof InPort) && ((Port)e.getTarget()).getPartner() == null) && p.getPort() instanceof OutPort && p.getPort().getPartner() == null) {
				return true;
			}
		}
		return false;
	}

	public boolean acceptsTarget(Object edge, Object port) {
		if (edge instanceof DefaultEdge && port instanceof FreakPort) {
			DefaultEdge e = (DefaultEdge)edge;
			FreakPort p = (FreakPort)port;
			if ((e.getSource() == null || (e.getSource() instanceof OutPort) && ((Port)e.getSource()).getPartner() == null) && p.getPort() instanceof InPort && p.getPort().getPartner() == null) {
				return true;
			}
		}
		return false;
	} */

	/**
	 * Removes <code>roots</code> from the graph. The in- and outgoing 
	 * edges of each vertex are removed before the vertex itself is removed.
	 * @see com.jgraph.graph.GraphModel#remove(java.lang.Object[])
	 */
	public void remove(Object[] roots) {
		List toRemove = Arrays.asList(roots);
		toRemove = new Vector(toRemove);

		for (int i = 0; i < roots.length; i++) {
			Object element = roots[i];

			if (element instanceof OperatorGraphCell) {
				OperatorGraphCell cell = (OperatorGraphCell)element;
				List edgesToRemove = new Vector();

				// TODO Andrea Kante koennte schon vor Knoten in toRemove stehen
				for (Iterator ports = cell.getPortIterator(); ports.hasNext();) {
					FreakPort port = (FreakPort)ports.next();
					for (Iterator edges = port.edges(); edges.hasNext();) {
						Object edge = edges.next();
						edgesToRemove.add(edge);
						if (toRemove.contains(edge)) {
							toRemove.remove(edge);
						}
					}
				}

				super.remove(edgesToRemove.toArray());
				operatorMapper.remove(cell.getOperator());
			} else if (element instanceof Port) {
				toRemove.remove(element);
			}
		}
		super.remove(toRemove.toArray());
	}

	protected Object[] handleInsert(Object[] cells) {
		if (cells == null) {
			return super.handleInsert(cells);
		}

		for (int i = 0; i < cells.length; i++) {
			Object object = cells[i];
			if (object instanceof OperatorGraphCell) {
				OperatorGraphCell cell = (OperatorGraphCell)object;
				Operator operator = cell.getOperator();

				operatorMapper.put(operator, object);
				operator.setPortsChangedListener(this);
			}
		}
		return super.handleInsert(cells);
	}

	public void adjustLayout(JGraph graph) {
		CellView[] all = graph.getGraphLayoutCache().getAllDescendants(graph.getGraphLayoutCache().getRoots());
		for (int i = 0; i < all.length; i++) {
			CellView view = all[i];
			if (view.getCell() instanceof OperatorGraphCell) {
				OperatorGraphCell cell = (OperatorGraphCell)view.getCell();
				Point point = ((Rectangle)cell.getAttributes().get(GraphConstants.BOUNDS)).getLocation();

				FontMetrics metrics = graph.getFontMetrics(graph.getFont());
				int height = metrics.getHeight();
				int width = metrics.stringWidth(cell.getOperator().toString());
				Dimension size = new Dimension(width + 10, height + 8);

				Map map = new HashMap();
				GraphConstants.setBounds(map, new Rectangle(point, size));
				cell.changeAttributes(map);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param source
	 * @param target
	 */
	public boolean hasCircle(FreakPort source, FreakPort target) {
		HashSet visitedOperators = new HashSet();
		for (int i = 0; i < getRootCount(); i++) {
			if (getRootAt(i) instanceof OperatorGraphCell) {
				if (!visitedOperators.contains(getRootAt(i))) {
					if (hasCircle((OperatorGraphCell)getRootAt(i), new HashSet(), visitedOperators, source, target)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean hasCircle(OperatorGraphCell operator, HashSet activeOperators, HashSet visitedOperators, FreakPort source, FreakPort target) {
		activeOperators.add(operator);
		visitedOperators.add(operator);
		for (int i = 0; i < operator.getChildCount(); i++) {
			if (((FreakPort)operator.getChildAt(i)).getPort() instanceof OutPort) {
				FreakPort fPort = (FreakPort)operator.getChildAt(i);
				for (Iterator iter = fPort.edges(); iter.hasNext();) {
					FreakPort fTarget = (FreakPort) ((Edge)iter.next()).getTarget();
					if (activeOperators.contains(fTarget.getParent())) {
						return true;
					}
					if (!visitedOperators.contains(fTarget.getParent())) {
						if (hasCircle((OperatorGraphCell)fTarget.getParent(), activeOperators, visitedOperators, source, target)) {
							return true;
						}
					}
				}
				if (fPort == source) {
					FreakPort fTarget = target;
					if (activeOperators.contains(fTarget.getParent())) {
						return true;
					}
					if (!visitedOperators.contains(fTarget.getParent())) {
						if (hasCircle((OperatorGraphCell)fTarget.getParent(), activeOperators, visitedOperators, source, target)) {
							return true;
						}
					}

				}
			}
		}
		activeOperators.remove(operator);
		return false;
	}

	public void write(OutputStream s) throws IOException {
		Schedule schedule = graph.getSchedule();
		graph.setSchedule(null);
		new ObjectOutputStream(s).writeObject(this);
		graph.setSchedule(schedule);
	}

	public static FreakGraphModel read(InputStream s, Schedule schedule) throws IOException, ClassNotFoundException {
		return (FreakGraphModel)new ObjectInputStream(s).readObject();
	}

	public OperatorGraph getOperatorGraph() {
		return graph;
	}

	/**
	 * 
	 */
	public void refreshFloatingPorts() {
		for (Iterator iter = operatorMapper.values().iterator(); iter.hasNext();) {
			OperatorGraphCell cell = (OperatorGraphCell)iter.next();
			cell.refreshFloatingPorts();
		}
	}
	/**
	 * @return
	 * @uml.property  name="operatorMapper"
	 */
	public Map getOperatorMapper() {
		return operatorMapper;
	}

	/* (non-Javadoc)
	 * @see freak.core.event.PortsChangedEventListener#portAdded(freak.core.graph.Operator, freak.core.graph.Port)
	 */
	public void portAdded(Operator source, Port port) {
		((OperatorGraphCell)operatorMapper.get(source)).portAdded(port);		
	}

	/* (non-Javadoc)
	 * @see freak.core.event.PortsChangedEventListener#portRemoved(freak.core.graph.Operator, freak.core.graph.Port)
	 */
	public void portRemoved(Operator source, Port port) {
		((OperatorGraphCell)operatorMapper.get(source)).portRemoved(port);
	}

	/* (non-Javadoc)
	 * @see freak.core.event.PortsChangedEventListener#portsExchanged(freak.core.graph.Operator, freak.core.graph.Port, freak.core.graph.Port)
	 */
	public void portsExchanged(Operator source, Port port1, Port port2) {
		((OperatorGraphCell)operatorMapper.get(source)).portsExchanged(port1, port2);
	}

}
