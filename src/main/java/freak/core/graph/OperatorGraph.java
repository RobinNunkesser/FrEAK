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

import freak.core.control.Schedule;
import freak.core.event.ScheduleEvent;
import freak.core.event.ScheduleEventListener;
import freak.core.modulesupport.AbstractModule;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.parametercontroller.ParameterController;
import freak.core.population.IndividualList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An operator graph represents the core of an evolutionary algorithm. It obtains the individuals of the current generation and generates the individuals of the new generation. This is done by leading individuals through operators that can perform i.e. selection and variation. The <code>OperatorGraph</code> must not be edited during the run. No operators, ports or connections between ports should be added or removed. Only properties of operators can change at any time as long as they don't result in a change of ports or connections between ports. 
 * @author  Matthias, Andrea, Heiko
 */

public class OperatorGraph extends AbstractModule implements Configurable, ScheduleEventListener {
	/**
	 * A list of nodes with zero inports (the start node has one inport and
	 * is therfore not included). This list gives us all entry points for the
	 * graph processing beside the start node and is updated on the
	 * RunStartedEvent.
	 */
	private ArrayList initNodes = new ArrayList();
	private String description = "";
	private String name = "";

	/**
	 * A list of nodes. 
	 */
	private ArrayList nodes = new ArrayList();
	private Start start;

	/**
	 * The final node where the Indivuals are collected for output. 
	 */
	private Finish finish;

	/**
	 * A queue of operators that are ready to process their inputs. 
	 */
	private LinkedList ready = new LinkedList();

	/** A <code>List</code> of <code>ParameterControllers</code> */
	private List parameterControllers = new ArrayList();

	public OperatorGraph(Schedule schedule) {
		super(schedule);
		start = new Start(this);
		start.createEvents();
		finish = new Finish(this);
		finish.createEvents();
		addOperator(start);
		addOperator(finish);
	}
	
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * Propagates the Individuals through the graph and creates a new 
	 * IndividualList.
	 * 
	 * @param population the old population as graph input.
	 * @return the new population as graph output
	 * @throws GraphException if an error occurs during the processing of the Operator.
	 */
	public IndividualList process(IndividualList population) throws GraphException {

		// give start the population to start with
		start.putPopulation(population);
		
		// process all operators		
		for (int i = initNodes.size()-1; i >= 0; i--) {
			((Operator)initNodes.get(i)).activate();
		}
		// get result of processing
		IndividualList result = finish.getResultPopulation();
		if (result == null) {
			throw new NullPointerException("Graph/Finish node must not return null.");
		}
		return result;
	}

	/**
	 * Performs a syntax check and throws exceptions if the graph is not 
	 * correct.
	 * 
	 * @throws GraphSyntaxException if the OperatorGraph has syntax errors.
	 */
	public void checkSyntax() throws GraphSyntaxException {
		for (int i = 0; i < parameterControllers.size(); i++) {
			((ParameterController)parameterControllers.get(i)).checkSyntax();
		}

		for (int i = 0; i < nodes.size(); i++) {
			((Operator)nodes.get(i)).checkSyntax();
		}

		HashSet visitedOperators = new HashSet();

		for (int i = 0; i < nodes.size(); i++) {
			if (!visitedOperators.contains(nodes.get(i))) {
				if (hasCircle((Operator)nodes.get(i), new HashSet(), visitedOperators)) {
					throw new GraphSyntaxException("Graph ist not free from circles");
				}
			}
		}

		visitedOperators = new HashSet();
		undirectedDFS(start, visitedOperators);
		if (visitedOperators.size() < nodes.size()) {
			throw new GraphSyntaxException("Unconnected operators");
		}

	}

	private void undirectedDFS(Operator node, HashSet visitedOperators) {
		if (!visitedOperators.contains(node)) {
			visitedOperators.add(node);
			for (int i = 0; i < node.getNumberOfInPorts() + node.getNumberOfOutPorts(); i++) {
				for (Iterator iter = node.getPortAt(i).getPartnerIterator(); iter.hasNext();) {
					undirectedDFS(((Port)iter.next()).getOperator(), visitedOperators);
					
				}
			}
		}
	}

	private void directedDFS(Operator node, HashSet visitedOperators, ArrayList dfsNumbers) {
			if (!visitedOperators.contains(node)) {
				visitedOperators.add(node);
				for (int i = 0; i < node.getNumberOfOutPorts(); i++) {
					for (Iterator iter = node.getOutPort(i).getPartnerIterator(); iter.hasNext();) {
						directedDFS(((Port)iter.next()).getOperator(), visitedOperators, dfsNumbers);
					
					}
				}
				if (dfsNumbers != null) {
					dfsNumbers.add(node);
				}
			}
		}

	private boolean hasCircle(Operator operator, HashSet activeOperators, HashSet visitedOperators) {
		activeOperators.add(operator);
		visitedOperators.add(operator);
		for (int i = 0; i < operator.getNumberOfOutPorts(); i++) {
			for (Iterator iter = operator.getOutPort(i).getPartnerIterator(); iter.hasNext();) {
				Port port = (Port)iter.next();
				if (activeOperators.contains(port.getOperator())) {
					return true;
				}
				if (!visitedOperators.contains(port.getOperator())) {
					if (hasCircle(port.getOperator(), activeOperators, visitedOperators)) {
						return true;
					}
				}
			}
		}
		activeOperators.remove(operator);
		return false;
	}

	/**
	 * An operator may append itself to the queue using this method
	 * if it is ready to be processed.
	 * 
	 * @param operator operator to enqueue.
	 */
	void enqueueReadyOperator(Operator operator) {
		ready.add(operator);
	}

	/**
	 * Returns the number of operators of this OperatorGraph.
	 * @return number of operators
	 */
	public int getOperatorCount() {
		return nodes.size();
	}

	public boolean containsOperator(Operator operator) {
		return nodes.contains(operator);
	}

	/**
	 * Returns the start operator inside the graph.
	 * @return  the start operator.
	 * @uml.property  name="start"
	 */
	public Start getStart() {
		return start;
	}

	/**
	 * Returns the finish operator inside the graph.
	 * @return  the finish operator.
	 * @uml.property  name="finish"
	 */
	public Finish getFinish() {
		return finish;

	}

	/**
	 * Returns the operator with the specified index. 
	 * 
	 * @param index index of operator to return. 
	 * @return the operator with the specified index.
	 */
	public Operator getOperator(int index) {
		return (Operator)nodes.get(index);
	}

	/**
	 * Adds a operator to the graph.
	 * 
	 * @param operator the operator to add.
	 */
	public void addOperator(Operator operator) {
		if (operator == null) {
			throw new NullPointerException("Can't add null as operator.");
		}
		nodes.add(operator);
	}

	/**
	 * Removes a operator from the graph.
	 * 
	 * @param operator the operator to remove.
	 */
	public void removeOperator(Operator operator) {
		if (operator.equals(start) || operator.equals(finish)) {
			throw new UnsupportedOperationException();
		}

		// -- remove the operator as possible eventlistener / -source
		schedule.getEventController().removeModule(operator);

		// -- also remove all ports (possible eventsources)
		for (int i = 0; i < operator.getNumberOfInPorts() + operator.getNumberOfOutPorts(); i++) {
			schedule.getEventController().removeModule(operator.getPortAt(i));
		}

		for (int i = 0; i < parameterControllers.size(); i++) {
			((ParameterController)parameterControllers.get(i)).objectRemoved(operator);
		}
		nodes.remove(operator);
	}

	/** 
	 * Returns if this operator may be removed from this graph.
	 * 
	 * @param operator the operator to be removed 
	 */
	public boolean isOperatorRemovable(Operator operator) {
		return !(operator.equals(start) || operator.equals(finish));
	}
	
	/**
	 * Tests if the operator graph is empty, i.e. it only contains start and 
	 * finish nodes and no edges.
	 * 
	 * @return <code>true</code> if the operator graph is empty; <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return (nodes.size() == 2) && (start.getOutPort(0).getNumberOfPartners() == 0) && (finish.getInPort(0).getNumberOfPartners() == 0);
	}

	/**
	 * Sets the name of the <code>OperatorGraph</code>.
	 * @param name  the name to set.
	 * @uml.property  name="name"
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the description of the <code>OperatorGraph</code>.
	 * @param description  the description to set.
	 * @uml.property  name="description"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return  the name
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return  the description
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Generates a dot graph string which outlines the structure of the OperatorGraph.
	 * You can generate an image out of this string by using tools like dot or neato.
	 * The resulting image should give an overview over the OperatorGraph.
	 * 
	 * @return dot graph string.
	 */
	public String toDotGraph() {
		StringBuffer s = new StringBuffer();
		s.append("digraph G {\n");
		s.append("\trankdir=LR;\n");
		HashMap map = new HashMap();
		for (int i = 0; i < nodes.size(); i++) {
			map.put(nodes.get(i), new Integer(i));
			s.append("\tn" + i + "[shape=record,label=\"{");
			for (int j = 0; j < ((Operator)nodes.get(i)).getNumberOfInPorts(); j++) {
				if (j == 0) {
					s.append("{");
				}
				s.append("<in" + j + "> " + j);
				if (j == ((Operator)nodes.get(i)).getNumberOfInPorts() - 1) {
					s.append("}");
				}
				s.append("|");
			}
			s.append("" + ((Operator)nodes.get(i)).getName() + "");
			for (int j = 0; j < ((Operator)nodes.get(i)).getNumberOfOutPorts(); j++) {
				if (j == 0) {
					s.append("|");
					s.append("{");
				}
				s.append("<out" + j + "> " + j);
				if (j < ((Operator)nodes.get(i)).getNumberOfOutPorts() - 1) {
					s.append("|");
				} else {
					s.append("}");
				}
			}
			s.append("}\"];\n");
		}
		for (int i = 0; i < nodes.size(); i++) {
			Operator g = ((Operator)nodes.get(i));
			for (int j = 0; j < g.getNumberOfOutPorts(); j++) {
				for (Iterator iter = g.getOutPort(j).getPartnerIterator(); iter.hasNext();) {
					Port p = (Port)iter.next();
					s.append("\t\"n" + i + "\":out" + j + " -> \"n" + map.get(p.getOperator()) + "\":in" + p.getNumber() + ";\n");
				}
			}
		}
		s.append("}");
		return s.toString();
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Name: " + name);
		s.append("Description: " + description);
		s.append("Nodes: ");
		for (int i = 0; i < nodes.size(); i++) {
			s.append(i + ") " + nodes.get(i).toString());
		}
		return s.toString();
	}

	/**
	 * Connects sourcePort and targetPort.
	 * @param sourcePort
	 * @param targetPort
	 * @throws AlreadyConnectedException, PortConnectException
	 */
	public void connect(Port sourcePort, Port targetPort) throws PortConnectException {
		if (sourcePort instanceof OutPort && targetPort instanceof InPort) {
			sourcePort.addPartner(targetPort);
		} else {
			throw new PortConnectException("sourcePort has to be an OutPort and targetPort has to be an InPort.");
		}
	}

	/**
	 * Disconnects <code>sourcePort</code> and <code>targetPort</code>.
	 * @param sourcePort
	 * @param targetPort
	 */
	public void disconnect(Port sourcePort, Port targetPort) {
		// disconnecting the source port. Note that this will
		// disconnect the target port too (that's handled in setPartner())
		sourcePort.removePartner(targetPort);

	}

	public void scheduleEdited(ScheduleEvent evt) {
		// clean Graph
		for (int i = 0; i < nodes.size(); i++) {

			// remove unneeded inports from operators with a floating number of inports
			if (nodes.get(i) instanceof HasFloatingNumberOfInPorts) {
				for (int j = ((Operator)nodes.get(i)).getNumberOfInPorts() - 1; j >= 0; j--) {
					if (((Operator)nodes.get(i)).getInPort(j).getNumberOfPartners() == 0) {
						((HasFloatingNumberOfInPorts)nodes.get(i)).removeInPort(j);
					}
				}
			}

			// remove unneeded outports from operators with a floating number of outports
			if (nodes.get(i) instanceof HasFloatingNumberOfOutPorts) {
				for (int j = ((Operator)nodes.get(i)).getNumberOfOutPorts() - 1; j >= 0; j--) {
					if (((Operator)nodes.get(i)).getOutPort(j).getNumberOfPartners() == 0) {
						((HasFloatingNumberOfOutPorts)nodes.get(i)).removeOutPort(j);
					}
				}
			}

		}
		
		// TODO matthias clean up
		
		initNodes.clear();
		
		// topological sorting
		HashSet visited = new HashSet();
		for (int i = 0; i < nodes.size(); i++) {
			if (!visited.contains(nodes.get(i))) {
				directedDFS((Operator)nodes.get(i), visited, initNodes);
			}
		}
		// initNodes now contains the topological sorted graph in reverse order
		// ie. initNodes.get(0) is the finish node and initNodes.get(initNodes.size()-1) is the start node
		
	}

	/**
	 * This method returns an array of all possible eventsources of the
	 * OperatorGraph (currently all outports of all operators). The result will be
	 * used if the GUI asks for possible eventsources to configure other objects,
	 * for example observers.
	 * @return an array of all possible eventsources in the graph
	 */
	public Object[] getAllPossibleEventSources() {
		// -- collect all OutPorts in List "ports"
		List ports = new ArrayList();
		// -- iterate over all nodes/operators
		for (int i = 0; i < nodes.size(); i++) {
			Operator g = (Operator)nodes.get(i);
			// -- iterate over all OutPorts of operator
			for (int j = 0; j < g.getNumberOfOutPorts(); j++) {
				if (g.getOutPort(j).getNumberOfPartners() != 0 || !(g instanceof HasFloatingNumberOfOutPorts)) {
					ports.add(g.getOutPort(j));
				}
			}
		}
		return ports.toArray();
	}

	/**
	 * @see freak.core.modulesupport.Module#createEvents()
	 */
	public void createEvents() {
		schedule.getEventController().addEvent(this, ScheduleEvent.class, schedule);
	}
	/**
	 * Returns the <code>List</code> of active <code>ParameterController</code>s.
	 * @return  the <code>List</code>
	 * @uml.property  name="parameterControllers"
	 */
	public List getParameterControllers() {
		return parameterControllers;
	}

	/**
	 * Removes the operator graph and all its operators and ports and parameter
	 * controller from the <code>EventController</code>.
	 */
	public void removeFromEventController() {
		schedule.getEventController().removeModule(this);
		Iterator it = parameterControllers.iterator();
		while (it.hasNext()) {
			schedule.getEventController().removeModule(it.next());
		}
		it = nodes.iterator();
		while (it.hasNext()) {
			Operator node = (Operator)it.next();
			schedule.getEventController().removeModule(node);
			for (int i = 0; i < node.getNumberOfInPorts(); i++) {
				schedule.getEventController().removeModule(node.getInPort(i));
			}
			for (int i = 0; i < node.getNumberOfOutPorts(); i++) {
				schedule.getEventController().removeModule(node.getOutPort(i));
			}
		}
	}

	/**
	 * Returns all parameter controllers that control the specified property 
	 * within the specified operator. 
	 * 
	 * @param operator the operator containing the specified property .
	 * @param propName the property the controllers are to be searched for. 
	 * @return a list of parameter controllers controlling the specified property.
	 */
	public List getParameterControllersForProperty(Operator operator, String propName) { 
		ArrayList controllers = new ArrayList();		

		List parameterControllers = operator.getOperatorGraph().getParameterControllers();
		for (Iterator iter = parameterControllers.iterator(); iter.hasNext();) {
			ParameterController controller = (ParameterController)iter.next();
			// search all parameters
			String[] paramNames = controller.getListOfParameters();
			for (int i = 0; i < paramNames.length; i++) {
				// if true, the operator is controlled
				if (controller.getOperatorForParameter(paramNames[i]) == operator) {
					// if true, the property is controlled 
					if (controller.getPropNameForParameter(paramNames[i]).equals(propName)) {
						// found a controller controlling the property
						controllers.add(controller);
					}
				}
			}
		}

		return controllers;
	}

	public void testSchedule(Schedule schedule)
		throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);

		// test nodes		
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			((Operator)iter.next()).testSchedule(schedule);
		}
		
		// test parameter controllers
		for (Iterator iter = parameterControllers.iterator(); iter.hasNext();) {
			((ParameterController)iter.next()).testSchedule(schedule);
		}
	}

}
