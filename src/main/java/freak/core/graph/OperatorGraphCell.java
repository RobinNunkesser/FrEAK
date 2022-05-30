/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.graph;

import java.awt.*;
import java.util.*;

import javax.swing.border.*;

import org.jgraph.graph.*;

/**
 * @author Andrea, Matthias
 */
public class OperatorGraphCell extends DefaultGraphCell {

	/** Maps Ports to FreakPorts */
	protected Map portMapper;
	private Color defaultColor = new Color(192, 255, 255); // light cyan
	private Color selectionColor = new Color(255, 220, 168); // very light orange
	private Color mutationColor = new Color(255, 192, 192); // light red
	private Color splitColor = new Color(255, 255, 192); // light yellow
	private Color recombinationColor = new Color(192, 255, 192); // light green
	private Color initializationColor = new Color(192, 192, 255); // light blue

	/**
	 * @param operator
	 */
	public OperatorGraphCell(Operator operator) {
		super(operator);

		portMapper = new HashMap();

		GraphConstants.setBorder(attributes, new BevelBorder(BevelBorder.RAISED));
		Color color = defaultColor;
		if (operator instanceof Selection) {
			color = selectionColor;
		} else if (operator instanceof Mutation) {
			color = mutationColor;
		} else if (operator instanceof Split) {
			color = splitColor;
		} else if (operator instanceof Recombination) {
			color = recombinationColor;
		} else if (operator instanceof Initialization) {
			color = initializationColor;
		}
		GraphConstants.setBackground(attributes, color);
		GraphConstants.setOpaque(attributes, true);

		FreakPort freakPort;
		boolean hasFreeInPort = false;
		for (int i = 0; i < operator.getNumberOfInPorts(); i++) {
			Port inPort = operator.getInPort(i);
			if (inPort.getNumberOfPartners() == 0) {
				hasFreeInPort = true;
			}
			freakPort = new FreakPort(inPort, this);
			super.add(freakPort);
			portMapper.put(inPort, freakPort);
		}

		if (operator instanceof HasFloatingNumberOfInPorts && !hasFreeInPort) {
			((HasFloatingNumberOfInPorts)operator).addInPort();
			Port newPort = operator.getInPort(operator.getNumberOfInPorts() - 1);
			freakPort = new FreakPort(newPort, this);
			super.add(freakPort);
			portMapper.put(newPort, freakPort);
		}

		boolean hasFreeOutPort = false;
		for (int i = 0; i < operator.getNumberOfOutPorts(); i++) {
			Port outPort = operator.getOutPort(i);
			if (outPort.getNumberOfPartners() == 0) {
				hasFreeOutPort = true;
			}
			freakPort = new FreakPort(outPort, this);
			super.add(freakPort);
			portMapper.put(outPort, freakPort);
		}

		if (operator instanceof HasFloatingNumberOfOutPorts && !hasFreeOutPort) {
			((HasFloatingNumberOfOutPorts)operator).addOutPort();
			Port newPort = operator.getOutPort(operator.getNumberOfOutPorts() - 1);
			freakPort = new FreakPort(newPort, this);
			super.add(freakPort);
			portMapper.put(newPort, freakPort);
		}

	}

	/**
	 * @return the Operator this OperatorGraphCell represents
	 */
	public Operator getOperator() {
		return (Operator)getUserObject();
	}

	/**
	 * Returns an iterator over the unmodifiable collection of FreakPorts this 
	 * OperatorGraphCell has as children.
	 * 
	 * @see java.util.Collections#unmodifiableCollection(java.util.Collection)
	 */
	public Iterator getPortIterator() {
		return Collections.unmodifiableCollection(portMapper.values()).iterator();
	}

	public Map changeAttributes(Map map) {
		Map changed = super.changeAttributes(map);
		if (map.containsKey(GraphConstants.BOUNDS)) {
			adjustPortPositions();
		}
		return changed;
	}

	public void adjustPortPositions() {
		for (Iterator iter = getPortIterator(); iter.hasNext();) {
			FreakPort port = (FreakPort)iter.next();
			port.arrange();
		}
	}

	void portAdded(Port port) {
		if (!portMapper.containsKey(port)) {
			FreakPort newPort = new FreakPort(port, this);
			portMapper.put(port, newPort);
			super.add(newPort);
		}
		adjustPortPositions();
	}

	void portRemoved(Port port) {
		FreakPort freakPort = (FreakPort)portMapper.get(port);
		portMapper.remove(port);
		super.remove(freakPort);
		adjustPortPositions();
	}

	/**
	 * Notifies the Cell that two Ports where exchanged in the Model.
	 * @param port1 port which was exchanged with port2.
	 * @param port2 port which was exchanged with port1.
	 */
	void portsExchanged(Port port1, Port port2) {
		FreakPort xPort = (FreakPort)portMapper.get(port1);
		FreakPort yPort = (FreakPort)portMapper.get(port2);
		int x = getChildren().indexOf(xPort);
		int y = getChildren().indexOf(yPort);
		
		getChildren().set(x,yPort);
		getChildren().set(y,xPort);
		
		adjustPortPositions();
	}

	public void refreshFloatingPorts() {
		Operator operator = getOperator();

		boolean hasFreeInPort = false;
		for (int i = 0; i < operator.getNumberOfInPorts(); i++) {
			Port inPort = operator.getInPort(i);
			if (inPort.getNumberOfPartners() == 0) {
				hasFreeInPort = true;
			}
		}
		if (operator instanceof HasFloatingNumberOfInPorts && !hasFreeInPort) {
			((HasFloatingNumberOfInPorts)operator).addInPort();
		}

		boolean hasFreeOutPort = false;
		for (int i = 0; i < operator.getNumberOfOutPorts(); i++) {
			Port outPort = operator.getOutPort(i);
			if (outPort.getNumberOfPartners() == 0) {
				hasFreeOutPort = true;
			}
		}
		if (operator instanceof HasFloatingNumberOfOutPorts && !hasFreeOutPort) {
			((HasFloatingNumberOfOutPorts)operator).addOutPort();
		}

		adjustPortPositions();
	}
	
	public void setAttributes(Map attributes) {
		super.setAttributes(attributes);
	}

}
