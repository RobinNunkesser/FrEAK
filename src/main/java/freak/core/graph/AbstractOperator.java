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
import freak.core.event.PortsChangedEventListener;
import freak.core.modulesupport.AbstractBasicModule;
import freak.core.modulesupport.PropertyManager;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.IndividualList;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * An abstract class implementing the interface <code>Operator</code>. <code>AbstractOperator</code> provides a operator's basic functionality and is to be used as a superclass for all operators. Operators must implement the process and may implement the getInspector method.  Operators may override checkSyntax() in the following way: <pre> public void checkSyntax() throws GraphSyntaxException { super.checkSyntax(); // insert more checks here } </pre> Also they may override the addInPort, addOutPort, removeInPort, removeOutPort methods i.e. to limit the number of possible ports or ensure a certain number of ports. Operators allowing their number of  InPorts or OutPorts to change should implement HasFloatingNumberOfInPorts or  HasFloatingNumberOfOutPorts, respectively.
 * @see HasFloatingNumberOfInPorts
 * @see HasFloatingNumberOfOutPorts
 * @author  Matthias, Dirk, Kai, Andrea, Stefan
 */
public abstract class AbstractOperator extends AbstractBasicModule implements Operator {
	protected OperatorGraph graph = null;
	
	private ArrayList outPorts = new ArrayList();
	private ArrayList inPorts = new ArrayList();

	/**
	 * This name is used for GUI displaying purposes and exported as a property.
	 */
	protected String name;

	private PortsChangedEventListener portsChangedListener;

	/**
	 * Counts the number of received inputs. Set to 0 after each process().
	 * Used to fasten up the check if all inputs are available.  
	 */
	private int receivedInputs = 0;

	/**
	 * Constructs a new Operator with a link back to the operator graph.
	 * 
	 * @param graph a link back to the operator graph. 
	 */
	public AbstractOperator(OperatorGraph graph) {
		if (graph == null)
			throw new NullPointerException("Operator graph is null.");
		this.graph = graph;

		// set property Name to default
		name = this.getName();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see freak.core.graph.Operator#checkSyntax()
	 */
	public void checkSyntax() throws GraphSyntaxException {
		for (int i = 0; i < inPorts.size(); i++) {
			((InPort)inPorts.get(i)).checkSyntax();
		}
		for (int i = 0; i < outPorts.size(); i++) {
			((OutPort)outPorts.get(i)).checkSyntax();
		}
	}

	public String[] getListOfPropertyNames() {

		return PropertyManager.getListOfPropertyNames(this);

	}

	/**
	 * Returns a List of the names of all properties in the operator
	 * which have the given type.
	 * 
	 * @param type the type for filtering the list of properties. 
	 * @return a String array with the names of the properties.
	 */
	public String[] getListOfPropertyNamesWithType(Class type) {

		return (PropertyManager.getListOfPropertyNamesWithType(this, type));

	}

	public void setProperty(String key, Object property) {

		PropertyManager.setProperty(this, key, property);

	}

	public Object getProperty(String key) {

		return (PropertyManager.getProperty(this, key));

	}

	/**
	 * Returns a Class object representing the type of the given property.
	 * 
	 * @param key the name of the requested property.
	 * @return the type of the property.
	 */
	public Class getPropertyType(String key) {

		return (PropertyManager.getPropertyType(this, key));

	}

	/**
	 * Appends an InPort to the list of InPorts. 
	 * @see HasFloatingNumberOfInPorts#addInPort()
	 */
	public void addInPort() {
		addInPort(inPorts.size());
	}

	/**
	 * Appends an OutPort to the list of Outports.
	 * @see HasFloatingNumberOfOutPorts#addOutPort() 
	 *
	 */
	public void addOutPort() {
		addOutPort(outPorts.size());
	}

	/**
	 * Inserts an InPort with the specified number.
	 * Increases the number of the InPort, which currently has the number index 
	 * (if any) and any subsequent InPorts, by one.
	 * 
	 * @param index position at which the port is inserted.
	 * @see HasFloatingNumberOfInPorts#addInPort(int)
	 */
	public void addInPort(int index) {
		InPort newPort = new InPort(this, index);
		inPorts.add(index, newPort);
		for (int i = index + 1; i < inPorts.size(); i++) {
			((InPort)inPorts.get(i)).setNumber(i);
		}
		firePortAddedEvent(newPort);
	}

	/**
	 * Inserts an OutPort with the specified number.
	 * Increases the number of the OutPort, which currently has the number index 
	 * (if any) and any subsequent OutPorts, by one.
	 *  
	 * @param index position at which the port is inserted.
	 * @see HasFloatingNumberOfOutPorts#addOutPort(int)
	 */
	public void addOutPort(int index) {
		OutPort newPort = new OutPort(this, index);
		outPorts.add(index, newPort);
		for (int i = index + 1; i < outPorts.size(); i++) {
			((OutPort)outPorts.get(i)).setNumber(i);
		}
		firePortAddedEvent(newPort);
	}

	/**
	 * Removes the InPort with the specified number.
	 * Decreases the number of any subsequent InPorts by one.
	 *  
	 * @param index number of port to remove.
	 * @see HasFloatingNumberOfInPorts#removeInPort(int)
	 */
	public void removeInPort(int index) {
		Port removed = (Port)inPorts.get(index);
		getOperatorGraph().getSchedule().getEventController().removeModule(removed);
		inPorts.remove(index);
		for (int i = index; i < inPorts.size(); i++) {
			((InPort)inPorts.get(i)).setNumber(i);
		}
		firePortRemovedEvent(removed);
	}

	/**
	 * Removes the OutPort with the specified number.
	 * Decreases the number of any subsequent OutPorts by one.
	 *  
	 * @param index number of port to remove.
	 * @see HasFloatingNumberOfOutPorts#removeOutPort(int)
	 */
	public void removeOutPort(int index) {
		Port removed = (Port)outPorts.get(index);
		getOperatorGraph().getSchedule().getEventController().removeModule(removed);
		outPorts.remove(index);
		for (int i = index; i < outPorts.size(); i++) {
			((OutPort)outPorts.get(i)).setNumber(i);
		}
		firePortRemovedEvent(removed);
	}

	public InPort getInPort(int index) {
		if (index > inPorts.size() - 1 && this instanceof HasFloatingNumberOfInPorts) {
			for (int i = inPorts.size() - 1; i < index; i++) {
				addInPort();
			}
		}
		return (InPort)inPorts.get(index);
	}

	public OutPort getOutPort(int index) {
		if (index > outPorts.size() - 1 && this instanceof HasFloatingNumberOfOutPorts) {
			for (int i = outPorts.size() - 1; i < index; i++) {
				addOutPort();
			}
		}
		return (OutPort)outPorts.get(index);
	}
	
	/**
	 * Exchanges the inports with index <code>x</code> <code>y</code>.
	 * @param x index of first port to exchange with second one.
	 * @param y index of second port to exchange with first one.
	 * @see HasFloatingNumberOfInPorts#exchangeInPorts(int, int)
	 */
	public void exchangeInPorts(int x, int y) {
		InPort xPort = (InPort)inPorts.get(x);
		inPorts.set(x, inPorts.get(y));
		inPorts.set(y, xPort);
		((Port)inPorts.get(y)).setNumber(y);
		((Port)inPorts.get(x)).setNumber(x);
		
		firePortsExchangedEvent((Port)inPorts.get(x), (Port)inPorts.get(y));
	}

	/**
	 * Exchanges the outports with index <code>x</code> <code>y</code>.
	 * @param x index of first port to exchange with second one.
	 * @param y index of second port to exchange with first one.
	 * @see HasFloatingNumberOfInPorts#exchangeInPorts(int, int)
	 */
	public void exchangeOutPorts(int x, int y) {
		OutPort xPort = (OutPort)outPorts.get(x);
		outPorts.set(x, inPorts.get(y));
		outPorts.set(y, xPort);
		((Port)outPorts.get(y)).setNumber(y);
		((Port)outPorts.get(x)).setNumber(x);
		
		firePortsExchangedEvent((Port)outPorts.get(x), (Port)outPorts.get(y));
	}

	public OperatorGraph getOperatorGraph() {
		return graph;
	}

	public void activate() throws GraphException {
		// get input
		IndividualList[] input = new IndividualList[inPorts.size()];
		for (int i = 0; i < input.length; i++) {
			input[i] = ((InPort)inPorts.get(i)).get();
		}
		// process input
		IndividualList[] output = process(input);

		//reduce calls
		int outPortsSize = outPorts.size();

		// send output
		if ((output == null && outPortsSize != 0) || (output.length != outPortsSize)) {
			throw new OutputLengthMismatchException("Output array doesn't match number of outPorts in " + this.getName() + ". " + outPorts.size() + " outports but output array contains " + (output != null ? output.length : 0) + " elements.");
		}
		for (int i = 0; i < outPortsSize; i++) {
			if (output[i] == null) {
				throw new OutputLengthMismatchException("Output array contains null entry.");
			}
			((OutPort)outPorts.get(i)).send(output[i]);
		}

		//reduce calls
		int inPortsSize = inPorts.size();

		// reset state
		for (int i = 0; i < inPortsSize; i++) {
			((InPort)inPorts.get(i)).clearCache();
		}
		receivedInputs = 0;
	}

	/**
	 * Computes the output of the operator from the input.
	 * Must return an array of IndividualList which has
	 * to contain as many objects as the operator has outPorts.
	 * 
	 * @param input input of the operator. Must not be null but may be an empty array.
	 * @return output of the operator. May be null.
	 */
	abstract public IndividualList[] process(IndividualList[] input) throws GraphException;

	public String toString() {
		return name;
	}

	public int getNumberOfInPorts() {
		return inPorts.size();
	}

	public int getNumberOfOutPorts() {
		return outPorts.size();
	}

	public Method getGetterMethod(String property) {
		String methodName = "getProperty" + property;

		Method m = null;

		try {
			m = getClass().getMethod(methodName, new Class[] {
			});
		} catch (NoSuchMethodException e) {
			System.out.println("Property name: " + property);
			throw new RuntimeException(e);
		}

		return m;
	}

	public Method getSetterMethod(String property) {
		String methodName = "setProperty" + property;

		Method m = null;

		try {
			Class[] args = { getPropertyType(property)};

			m = getClass().getMethod(methodName, args);
		} catch (NoSuchMethodException e) {
			System.out.println("Property name: " + property);
			throw new RuntimeException(e);
		}

		return m;

	}

	public int getIndexOfPort(Port port) {
		if (inPorts.contains(port)) {
			return inPorts.indexOf(port);
		} else if (outPorts.contains(port)) {
			int offset = inPorts.size();

			return offset + outPorts.indexOf(port);
		} else {
			throw new NoSuchElementException("This operator doesn't contain port " + port);
		}
	}

	public Port getPortAt(int index) {
		int numInPorts = inPorts.size();
		if (numInPorts > 0) {
			if (index < numInPorts) {
				return (Port)inPorts.get(index);
			} else {
				return (Port)outPorts.get(index - numInPorts);
			}
		} else {
			return (Port)outPorts.get(index);
		}
	}

	/**
	 * @param portsChangedListener  the portsChangedListener to set
	 * @uml.property  name="portsChangedListener"
	 */
	public void setPortsChangedListener(PortsChangedEventListener listener) {
		portsChangedListener = listener;
	}

	protected void firePortAddedEvent(Port port) {
		if (portsChangedListener != null) {
			portsChangedListener.portAdded(this, port);
		}
	}
	
	protected void firePortRemovedEvent(Port port) {
		if (portsChangedListener != null) {
    		portsChangedListener.portRemoved(this, port);
		}
	}

	protected void firePortsExchangedEvent(Port port1, Port port2) {
		if (portsChangedListener != null) {
			portsChangedListener.portsExchanged(this, port1, port2);
		}
	}

	/**
	 * Property Name getter method.
	 */
	public String getPropertyName() {
		return (name);
	}

	/**
	 * Property Name setter method.
	 */
	public void setPropertyName(String name) {
		this.name = name;
	}

	public void initialize() {
	}

	public void testSchedule(Schedule schedule)
		throws UnsupportedEnvironmentException {
	}

}
