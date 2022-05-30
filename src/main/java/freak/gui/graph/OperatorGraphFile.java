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

import freak.core.control.*;
import freak.core.event.*;
import freak.core.fitness.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.parametercontroller.*;
import freak.core.populationmanager.*;
import freak.core.searchspace.*;
import freak.core.stoppingcriterion.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 *  EXPERIMENTAL and moving target!
 * 
 * @author Matthias
 */
public class OperatorGraphFile implements Serializable {

	/**
	 * Event-associations are stored in this class. A <code>List</code> of these
	 * classes is stored in every
	 * @author Oliver
	 *
	 */
	public static class Event implements Serializable {
		final static int SOURCE_NULL = -1;
		final static int SOURCE_UNKNOWN = 0;
		final static int SOURCE_SEARCHSPACE = 1;
		final static int SOURCE_FITNESSFUNCTION = 2;
		final static int SOURCE_OPERATORGRAPH = 3;
		final static int SOURCE_STOPPINGCRITERION = 4;
		final static int SOURCE_POPULATIONMANAGER = 5;
		final static int SOURCE_SCHEDULE = 6;
		final static int SOURCE_PORT = 7;
		/** Defines the type of source of the event. Must be a SOURCE_* constant. */
		int sourceType;
		/** The human readable name of the non-static event. */
		String name;
		/** The fully qualified classname of the event (e.g. freak.core.event.RunEvent). */
		String className;
		/** If sourceType==SOURCE_PORT this defines the index of the edge in the edgeList array. */
		int edgeIndex;
		
		/**
		 * @author  nunkesser
		 */
		public static class EventPersistenceHandler implements ClassPersistenceHandler {
			private PersistenceManager pm = null;

			public EventPersistenceHandler(PersistenceManager pm) {
				this.pm = pm;
			}

			public Element toXML(Object o) {
				Event event = (Event)o;
				Element result = new Element("Event");
				result.setAttribute("sourceType", event.sourceType+"");
				result.setAttribute("name", event.name);
				result.setAttribute("className", event.className);
				result.setAttribute("edgeIndex", event.edgeIndex+"");
				return result;
			}

			public Object fromXML(Element e) throws DataConversionException {
				Event event = new Event();
				event.sourceType = e.getAttribute("sourceType").getIntValue();
				event.name = e.getAttribute("name").getValue();
				event.className = e.getAttribute("className").getValue();
				event.edgeIndex = e.getAttribute("edgeIndex").getIntValue();
				return event;
			}

			public Class handles() {
				return Event.class;
			}
		}

	}

	/**
	 * @author  nunkesser
	 */
	public static class Node implements Serializable {
		String type;
		Map attributes;
		Configuration config = null;
		List events = null;

		private Node() {
		}

		public Node(OperatorGraphCell cell) {
			// create a copy of the attributes so that we can cleanly remove key "value"
			attributes = new Hashtable(cell.getAttributes());

			// remove the reference to the actual Operator object 
			attributes.remove("value");

			// safe name of class to restore the right opertor later
			type = cell.getOperator().getClass().getName();

			// that's one of two places where we store no copy but a reference!
			// safe the configuration if any
			if (cell.getOperator() instanceof Configurable) {
				config = ((Configurable)cell.getOperator()).getConfiguration();
			}
		}

		/**
		 * @author  nunkesser
		 */
		public static class NodePersistenceHandler implements ClassPersistenceHandler {
			private PersistenceManager pm = null;

			public NodePersistenceHandler(PersistenceManager pm) {
				this.pm = pm;
			}

			public Element toXML(Object o) throws XMLizeException {
				Node node = (Node)o;
				Element result = new Element("Node");
				result.addContent(pm.toXML(node.attributes, Map.class));
				result.addContent(pm.toXML(node.config));
				result.addContent(pm.toXML(node.events, List.class));
				result.addContent(pm.toXML(node.type));
				return result;
			}

			public Object fromXML(Element e) throws XMLizeException {
				Node node = new Node();
				pm.registerObject(node);

				node.attributes = (Map)pm.fromXML((Element)e.getChildren().get(0));
				node.config = (Configuration)pm.fromXML((Element)e.getChildren().get(1));
				node.events = (List)pm.fromXML((Element)e.getChildren().get(2));
				node.type = (String)pm.fromXML((Element)e.getChildren().get(3));
				return node;
			}

			public Class handles() {
				return Node.class;
			}
		}
	}

	public static class Edge implements Serializable {
		int source = -1;
		int sourcePort = -1;
		int target = -1;
		int targetPort = -1;

		private Edge() {
		}

		public Edge(FreakEdge edge, Map map) {

			// safe source coordinates if any
			if (edge.getSourcePort() != null) {
				source = ((Integer)map.get(edge.getSourcePort().getParent())).intValue();
				sourcePort = ((FreakPort)edge.getSource()).getPort().getNumber();
			}

			// safe target coordinates if any		
			if (edge.getTargetPort() != null) {
				target = ((Integer)map.get(edge.getTargetPort().getParent())).intValue();
				targetPort = ((FreakPort)edge.getTarget()).getPort().getNumber();
			}
		}
		
		/**
		 * @author  nunkesser
		 */
		public static class EdgePersistenceHandler implements ClassPersistenceHandler {
			private PersistenceManager pm = null;

			public EdgePersistenceHandler(PersistenceManager pm) {
				this.pm = pm;
			}

			public Element toXML(Object o) {
				Edge edge = (Edge)o;
				Element result = new Element("Edge");
				result.setAttribute("source", edge.source + "");
				result.setAttribute("sourcePort", edge.sourcePort + "");
				result.setAttribute("target", edge.target + "");
				result.setAttribute("targetPort", edge.targetPort + "");
				return result;
			}

			public Object fromXML(Element e) throws DataConversionException {
				Edge edge = new Edge();
				pm.registerObject(edge);
				edge.source = e.getAttribute("source").getIntValue();
				edge.sourcePort = e.getAttribute("sourcePort").getIntValue();
				edge.target = e.getAttribute("target").getIntValue();
				edge.targetPort = e.getAttribute("targetPort").getIntValue();
				return edge;
			}

			public Class handles() {
				return Edge.class;
			}
		}
	}

	public static class PWrapper implements Serializable {
		public String type;
		public int operator;
		public String propName;

		/**
		 * @author  nunkesser
		 */
		public static class PWrapperPersistenceHandler implements ClassPersistenceHandler {
			private PersistenceManager pm = null;

			public PWrapperPersistenceHandler(PersistenceManager pm) {
				this.pm = pm;
			}

			public Element toXML(Object o) {
				PWrapper pw = (PWrapper)o;
				Element result = new Element("Parameter");
				result.setAttribute("type", pw.type);
				result.setAttribute("operator", pw.operator + "");
				result.setAttribute("name", pw.propName);
				return result;
			}

			public Object fromXML(Element e) throws DataConversionException {
				PWrapper pw = new PWrapper();
				pw.type = e.getAttribute("type").getValue();
				pw.operator = e.getAttribute("operator").getIntValue();
				pw.propName = e.getAttribute("name").getValue();
				return pw;
			}

			public Class handles() {
				return PWrapper.class;
			}
		}
	}
	/**
	 * @author  nunkesser
	 */
	public static class PController implements Serializable {

		Configuration config = null;
		HashMap parameters = new HashMap();
		String type;
		List events;
		
		private PController() {
		}
		
		public PController(ParameterController parameterController, Map map, Map operatorMapper) {
			type = parameterController.getClass().getName();

			// that's one of two places where we store no copy but a reference!
			if (parameterController instanceof Configurable) {
				config = ((Configurable)parameterController).getConfiguration();
			}

			String[] params = parameterController.getListOfParameters();
			for (int i = 0; i < params.length; i++) {
				PWrapper pw = new PWrapper();
				pw.type = parameterController.getParameterType(params[i]).getName();
				pw.operator = ((Integer)map.get(operatorMapper.get(parameterController.getOperatorForParameter(params[i])))).intValue();
				pw.propName = parameterController.getPropNameForParameter(params[i]);
				parameters.put(params[i], pw);
			}
		}

		/**
		 * @author  nunkesser
		 */
		public static class PControllerPersistenceHandler implements ClassPersistenceHandler {
			private PersistenceManager pm = null;

			public PControllerPersistenceHandler(PersistenceManager pm) {
				this.pm = pm;
			}

			public Element toXML(Object o) throws XMLizeException {
				PController pc = (PController)o;
				Element result = new Element("ParameterController");

				result.setAttribute("type", pc.type);
				result.addContent(pm.toXML(pc.config));
				result.addContent(pm.toXML(pc.parameters));
				result.addContent(pm.toXML(pc.events));

				return result;
			}

			public Object fromXML(Element e) throws DataConversionException, XMLizeException {
				PController pc = new PController();
				
				pm.registerObject(pc);
				
				pc.type = e.getAttributeValue("type");
				pc.config = (Configuration)pm.fromXML((Element)e.getChildren().get(0));
				pc.parameters = (HashMap)pm.fromXML((Element)e.getChildren().get(1));
				pc.events = (List)pm.fromXML((Element)e.getChildren().get(2));

				return pc;
			}

			public Class handles() {
				return PController.class;
			}
		}

	}

	private ArrayList nodeList = new ArrayList();
	private ArrayList edgeList = new ArrayList();
	private ArrayList parameterControllerList = new ArrayList();

	private OperatorGraphFile() {
	}

	/**
	 * Creates a new <code>OperatorGraphFile</code> based on the information
	 * provided by <code>graphModel</code>. The <code>OperatorGraphFile</code> can
	 * than be written to a stream instead of the the <code>graphModel</code>.
	 * @param graphModel the <code>FreakGraphModel</code> which should be represented by the constructed <code>OperatorGraphFile</code>.
	 */
	public OperatorGraphFile(FreakGraphModel graphModel) {

		// maps OperatorGraphCells to integers. The integer indicates where the
		// Node which represents the OperatorGraphCell can be found in the nodeList
		Map map = new HashMap();

		// build nodeList and ignore edges for now
		for (int i = 0; i < graphModel.getRootCount(); i++) {
			Object o = graphModel.getRootAt(i);
			if (o instanceof OperatorGraphCell) {
				OperatorGraphCell cell = (OperatorGraphCell)o;
				nodeList.add(new Node(cell));
				map.put(graphModel.getRootAt(i), new Integer(nodeList.size() - 1));

			}
		}

		// build edgeList (nodes are already handled above)
		for (int i = 0; i < graphModel.getRootCount(); i++) {
			Object o = graphModel.getRootAt(i);
			if (o instanceof FreakEdge) {
				FreakEdge edge = (FreakEdge)o;
				edgeList.add(new Edge(edge, map));
			}
		}

		// -- create event-information and store in each node
		// -- must do it in extra loop because it needs a completed edgeList array
		for (int i = 0; i < graphModel.getRootCount(); i++) {
			Object o = graphModel.getRootAt(i);
			if (o instanceof OperatorGraphCell) {
				OperatorGraphCell cell = (OperatorGraphCell)o;
				int index = ((Integer)map.get(cell)).intValue();
				Node node = (Node)nodeList.get(index);
				node.events = createEventListFor(cell.getOperator(), graphModel, map);
			}
		}

		List parameterController = graphModel.getOperatorGraph().getParameterControllers();
		for (int i = 0; i < parameterController.size(); i++) {
			PController pc = new PController(((ParameterController)parameterController.get(i)), map, graphModel.getOperatorMapper());
			pc.events = createEventListFor((ParameterController)parameterController.get(i), graphModel, map);
			parameterControllerList.add(pc);
		}
	}

	/**
	 * Generates a <code>FreakGraphModel</code> based on this object.
	 * This is basically a counterpart of the constructor. 
	 * @param schedule the schedule to which the resulting <code>FreakGraphModel</code> should belong to.
	 * @return the generated <code>FreakGraphModel</code>.
	 * @throws UnsupportedEnvironmentException if an needed object couldn't be created.
	 * @throws InvocationTargetException if an needed object couldn't be created.
	 */
	public FreakGraphModel generateGraph(Schedule schedule) throws UnsupportedEnvironmentException, InvocationTargetException {
		OperatorGraph operatorGraph = new OperatorGraph(null);
		operatorGraph.setSchedule(schedule);

		// operatorList contains a list of type Operator. The Operator at position i is represented by the node at position i in NodeList.
		// so: operatorList.get(i) <-> nodeList.get(i) 
		ArrayList operatorList = new ArrayList();
		ModuleCollector manager = new ModuleCollector(schedule);

		for (int i = 0; i < nodeList.size(); i++) {
			Node n = ((Node)nodeList.get(i));
			if (n.type.compareTo("freak.core.graph.Start") != 0 && n.type.compareTo("freak.core.graph.Finish") != 0) {
				Operator operator = (Operator)manager.newModuleByString(n.type, new Object[] { operatorGraph }, schedule.getGenotypeSearchSpace());
				if (n.config != null) {
					((Configurable)operator).setConfiguration(n.config);
				}
				operatorGraph.addOperator(operator);
				operatorList.add(i, operator);
			} else if (n.type.compareTo("freak.core.graph.Finish") != 0) {
				operatorList.add(i, operatorGraph.getStart());
			} else {
				operatorList.add(i, operatorGraph.getFinish());
			}
		}

		for (int i = 0; i < edgeList.size(); i++) {
			Edge e = ((Edge)edgeList.get(i));
			Port source = ((Operator)operatorList.get(e.source)).getOutPort(e.sourcePort);
			Port target = ((Operator)operatorList.get(e.target)).getInPort(e.targetPort);
			source.addPartner(target);
		}

		List parameterController = operatorGraph.getParameterControllers();
		for (int j = 0; j < parameterControllerList.size(); j++) {
			PController pcWrapper = (PController)parameterControllerList.get(j);
			ParameterController pc = (ParameterController)manager.newModuleByString(pcWrapper.type, new Object[] { operatorGraph }, schedule.getGenotypeSearchSpace());
			Set keySet = pcWrapper.parameters.keySet();
			for (Iterator i = keySet.iterator(); i.hasNext();) {
				String key = (String)i.next();
				PWrapper value = (PWrapper)pcWrapper.parameters.get(key);
				pc.setPropertyForParameter(key, (Operator)operatorList.get(value.operator), value.propName);
			}
			parameterController.add(pc);
			pc.createEvents();
			// -- restore linked events
			restoreEvents(pc, pcWrapper.events, operatorList, schedule);
		}

		FreakGraphModel result = new FreakGraphModel(operatorGraph);
		for (int i = 0; i < result.getRootCount(); i++) {
			Object o = result.getRootAt(i);
			if (o instanceof OperatorGraphCell) {
				OperatorGraphCell cell = (OperatorGraphCell)o;
				Map attributes = ((Node)nodeList.get(operatorList.indexOf(cell.getOperator()))).attributes;
				attributes.put("value", cell.getOperator());
				cell.setAttributes(attributes);
				cell.adjustPortPositions();
			} else if (o instanceof FreakEdge) {
				// edges don't have attributes which have to be restored
			}
		}

		// -- link events
		for (int i = 0; i < operatorList.size(); i++) {
			Operator operator = (Operator)operatorList.get(i);
			Node node = (Node)nodeList.get(i);
			operator.createEvents();
			restoreEvents(operator, node.events, operatorList, schedule);
		}

		operatorGraph.createEvents();

		return result;
	}

	/**
	 * Finds the correct index of a given (Out-)<code>Port</code> in the <code>edgeList</code>.
	 * The index of the first entry in <code>edgeList</code> whose <code>source</code>
	 * and <code>sourcePort</code> match <code>port</code> and its owning <code>operator</code>
	 * is returned.
	 * Note that <code>port</code> must be an <code>OutPort</code> of some operator
	 * and the <code>edgeList</code> has to be complete.
	 * 
	 * @param port the <code>Port</code>
	 * @param model the <code>FreakGraphModel</code> (contains the environment that is needed)
	 * @return the <code>edgeList</code> index or -1 if the port couldn't be found.
	 */
	private int getEdgeIndexForPort(Port port, FreakGraphModel model, Map map) {
		Operator operator = port.getOperator();
		// -- find index of operator in nodeList and store in "source"
		int source = ((Integer)map.get(model.getOperatorMapper().get(operator))).intValue();
		int sourcePort = operator.getIndexOfPort(port) - operator.getNumberOfInPorts();
		for (int i = 0; i < edgeList.size(); i++) {
			Edge edge = (Edge)edgeList.get(i);
			if (edge.source == source && edge.sourcePort == sourcePort) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Generates a <code>List</code> of <code>Event</code> classes for a given
	 * <code>Module</code> for storing event associations in the <code>OperatorGraphFile</code>.
	 * This method only works with non-static events, because all other events
	 * are associated unambiguously in the <code>createEvents()</code> method of
	 * the <code>Module</code>.
	 * If no non-static event is available for <code>m</code>, an empty <code>List</code>
	 * is returned.
	 * Note that before this method can be called, the <code>edgeList</code> List
	 * has to be complete!
	 * 
	 * @param m the <code>Module</code>
	 * @param model the <code>FreakGraphModel</code> (contains the environment that is needed)
	 * @return a <code>List</code> of <code>Event</code> objects
	 */
	private List createEventListFor(Module m, FreakGraphModel model, Map map) {
		List l = new ArrayList();
		EventController ec = model.getOperatorGraph().getSchedule().getEventController();
		List evts = ec.getCustomizableEventsFor(m);
		if (!evts.isEmpty()) {
			for (Iterator i = evts.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				Event e = new Event();
				l.add(e);
				e.className = evt.getEventType().getName();
				e.name = evt.getEventName();
				Object source = evt.getEventSource();
				if (source != null) {
					if (source instanceof SearchSpace) {
						e.sourceType = Event.SOURCE_SEARCHSPACE;
					} else if (source instanceof FitnessFunction) {
						e.sourceType = Event.SOURCE_FITNESSFUNCTION;
					} else if (source instanceof OperatorGraph) {
						e.sourceType = Event.SOURCE_OPERATORGRAPH;
					} else if (source instanceof StoppingCriterion) {
						e.sourceType = Event.SOURCE_STOPPINGCRITERION;
					} else if (source instanceof PopulationManager) {
						e.sourceType = Event.SOURCE_POPULATIONMANAGER;
					} else if (source instanceof Schedule) {
						e.sourceType = Event.SOURCE_SCHEDULE;
					} else if (source instanceof Port) {
						e.sourceType = Event.SOURCE_PORT;
						Port port = (Port)source;
						e.edgeIndex = getEdgeIndexForPort(port, model, map);
					} else
						e.sourceType = Event.SOURCE_UNKNOWN;
				} else
					e.sourceType = Event.SOURCE_NULL;

			}
		}
		return l;
	}

	/**
	 * Reconstructs the event-assignments for a given module. For this a list
	 * of <code>Events</code> as stored in Node.events (created by <code>createEventListFor()</code>)
	 * is needed.
	 * Note that before this method can be called, all operators and ports have to be
	 * created and linked properly.
	 * 
	 * @param module the <code>Module</code> whose events are to be reconstructed
	 * @param events the list of <code>Events</code> for the module
	 * @param schedule the active <code>Schedule</code>
	 */
	private void restoreEvents(Module module, List events, List operatorList, Schedule schedule) {
		EventController ec = schedule.getEventController();
		for (int i = 0; i < events.size(); i++) {
			Event event = (Event)events.get(i);
			EventInfo evt = ec.getCustomizableEventFor(module, event.name);
			Object eventSource = null;
			switch (event.sourceType) {
				case Event.SOURCE_NULL :
					eventSource = null;
					break;
				case Event.SOURCE_UNKNOWN :
					eventSource = null;
					break;
				case Event.SOURCE_SEARCHSPACE :
					eventSource = schedule.getGenotypeSearchSpace();
					break;
				case Event.SOURCE_FITNESSFUNCTION :
					eventSource = schedule.getRealFitnessFunction();
					break;
				case Event.SOURCE_OPERATORGRAPH :
					eventSource = schedule.getOperatorGraph();
					break;
				case Event.SOURCE_POPULATIONMANAGER :
					eventSource = schedule.getPopulationManager();
					break;
				case Event.SOURCE_SCHEDULE :
					eventSource = schedule;
					break;
				case Event.SOURCE_PORT :
					Edge edge = (Edge)edgeList.get(event.edgeIndex);
					Operator operator = (Operator)operatorList.get(edge.source);
					eventSource = operator.getOutPort(edge.sourcePort);
					break;
				default :
					eventSource = null;
					break;
			}
			evt.setEventSource((EventSource)eventSource);
		}
	}

	/**
	 * Generates an XML representation of this object and writes this represntation to the given stream. 
	 * @param s the stream to write to.
	 * @throws IOException if writing to the stream failed.
	 * @throws XMLizeException if the generation of the XML represntation failed.
	 */
	public void write(OutputStream s) throws IOException, XMLizeException {
		XMLOutputter outputter = new XMLOutputter("  ", true);
		outputter.output(PersistenceManager.XMLize(this), s);
	}

	/**
	 * Generates an OperatorGraphFile by reading the InputStream <code>s</code>.
	 * @param s an InputStream which should contain an OperatorGraphFile in XML format.
	 * @return the generated OperatorGraphFile. 
	 * @throws XMLizeException if the contents of the stream couldn't be interpreted as OperatorGraphFile.
	 * @throws IOException if the stream couldn't be read for some reason.
	 */
	public static OperatorGraphFile read(InputStream s) throws XMLizeException, IOException {
		SAXBuilder builder = new SAXBuilder();
		try {
			return (OperatorGraphFile)PersistenceManager.DeXMLize(builder.build(s));
		} catch (JDOMException e) {
			throw new XMLizeException(e);
		}
	}

	/**
	 * @author  nunkesser
	 */
	public static class OperatorGraphFilePersistenceHandler implements ClassPersistenceHandler {
		private PersistenceManager pm = null;

		public OperatorGraphFilePersistenceHandler(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) throws XMLizeException {
			OperatorGraphFile ogf = (OperatorGraphFile)o;
			Element result = new Element("OperatorGraphFile");
			result.addContent(pm.toXML(ogf.edgeList, List.class));
			result.addContent(pm.toXML(ogf.nodeList, List.class));
			result.addContent(pm.toXML(ogf.parameterControllerList, List.class));
			return result;
		}

		public Object fromXML(Element e) throws XMLizeException {
			OperatorGraphFile ogf = new OperatorGraphFile();
			pm.registerObject(ogf);
			ogf.edgeList = (ArrayList)pm.fromXML((Element)e.getChildren().get(0));
			ogf.nodeList = (ArrayList)pm.fromXML((Element)e.getChildren().get(1));
			ogf.parameterControllerList = (ArrayList)pm.fromXML((Element)e.getChildren().get(2));
			return ogf;
		}

		public Class handles() {
			return OperatorGraphFile.class;
		}
	}

}
