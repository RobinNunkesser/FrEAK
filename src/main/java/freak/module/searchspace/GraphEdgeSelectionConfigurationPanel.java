/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.searchspace;

import freak.core.modulesupport.inspector.*;
import java.awt.event.*;

/**
 * @author  Stefan, Christian
 */
public class GraphEdgeSelectionConfigurationPanel extends GraphEdgeSelectionConfigurationPanelUI {
	private GraphEdgeSelection searchSpace;
	
	GraphEdgeSelectionConfigurationPanel(GraphEdgeSelection searchSpace) {
		super(searchSpace);
		this.searchSpace = searchSpace;
		
		addListeners();
		readAll();
		updateEnables();
	}
	
	private void addListeners() {
		typeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeType();
				updateEnables();
			}
		});
		verticesField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeVertices();
			}
		});
		edgesField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeEdges();
			}
		});
		probabilityField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeProb();
			}
		});
		weightField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeWeight();
			}
		});
		trianglesField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeTriangles();
			}
		});
		rngBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeRNG();
				updateEnables();
			}
		});
		seedField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeSeed();
			}
		});
		verticesLolliField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeVertLolli();
			}
		});
		degreeKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeDegreeK();
			}
		});
		
		verticesField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeVertices();
			}
		});
		edgesField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeEdges();
			}
		});
		probabilityField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeProb();
			}
		});
		weightField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeWeight();
			}
		});
		trianglesField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeTriangles();
			}
		});
		keepBox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeKeep();
			}
		});
		rngBox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeRNG();
			}
		});
		seedField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeSeed();
			}
		});
		verticesLolliField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeVertLolli();
			}
		});
		degreeKField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				writeDegreeK();
			}
		});
	}
	
	private void readAll() {
		typeBox.setSelectedIndex(searchSpace.getPropertyInitializationType().getIndex());
		verticesField.setText(searchSpace.getPropertyNumberOfVertices().toString());
		edgesField.setText(searchSpace.getPropertyNumberOfEdges().toString());
		probabilityField.setText(searchSpace.getPropertyProbabilityForEdge().toString());
		weightField.setText(searchSpace.getPropertyMaximalWeight().toString());
		keepBox.setSelected(searchSpace.getPropertyKeepGraphDuringBatch().booleanValue());
		rngBox.setSelected(searchSpace.getPropertyUseInternalRNG().booleanValue());
		seedField.setText(searchSpace.getPropertyInternalRNGSeed().toString());
		int triangles = (searchSpace.getPropertyVerticesInTriangles().intValue() - 1) / 2;
		trianglesField.setText(String.valueOf(triangles));
	}
	
	private void writeType() {
		searchSpace.setPropertyInitializationType(new StringArrayWrapper(searchSpace.initTypes, typeBox.getSelectedIndex()));
	}
	
	private void writeSeed() {
		searchSpace.setPropertyInternalRNGSeed(new Integer(seedField.getText()));
	}

	private void writeKeep() {
		searchSpace.setPropertyKeepGraphDuringBatch(new Boolean(keepBox.isSelected()));
	}

	private void writeRNG() {
		searchSpace.setPropertyUseInternalRNG(new Boolean(rngBox.isSelected()));
	}

	private void writeTriangles() {
		int triangles = Integer.parseInt(trianglesField.getText()) * 2 + 1;
		searchSpace.setPropertyVerticesInTriangles(new Integer(triangles));
	}

	private void writeWeight() {
		searchSpace.setPropertyMaximalWeight(new Integer(weightField.getText()));
	}

	private void writeProb() {
		searchSpace.setPropertyProbabilityForEdge(new Double(probabilityField.getText()));
	}

	private void writeEdges() {
		searchSpace.setPropertyNumberOfEdges(new Integer(edgesField.getText()));
	}

	private void writeVertices() {
		searchSpace.setPropertyNumberOfVertices(new Integer(verticesField.getText()));
	}

	private void writeVertLolli() {
		searchSpace.setPropertyVerticesLolli(new Integer(verticesLolliField.getText()));
	}

	private void writeDegreeK() {
		searchSpace.setPropertyDegreeK(new Integer(degreeKField.getText()));
	}

	private void updateEnables() { 
		switch (typeBox.getSelectedIndex()) {
			case 0 : fixedEdgeNumber(); break;
			case 1 : fixedEdgeProb(); break;
			case 2 : fixedEdgeBinVal(); break;
			case 3 : fullGraph(); break;
			case 4 : ingosGraph(); break;
			case 5 : fullGraph(); break;
			case 6 : path(); break;
			case 7 : randomTree(); break;
			case 8 : lollipop(); break;
			case 9 : k_tree(); break;
			case 10 : semi(); break;
			case 11 : semiP(); break;
			case 12 : trapLine(); break;
		}
	}

	private void fixedEdgeNumber() {
		edgesLabel.setEnabled(true);
		edgesField.setEnabled(true);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(true);
		weightField.setEnabled(true);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		rngPanel.setEnabled(true);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void fixedEdgeProb() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(true);
		probabilityField.setEnabled(true);
		weightLabel.setEnabled(true);
		weightField.setEnabled(true);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngPanel.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void fixedEdgeBinVal() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(true);
		probabilityField.setEnabled(true);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngPanel.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void fullGraph() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(true);
		weightField.setEnabled(true);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngPanel.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}
	
	private void path() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		keepLabel.setEnabled(false);
		keepBox.setEnabled(false);
		rngPanel.setEnabled(false);
		rngLabel.setEnabled(false);
		rngBox.setEnabled(false);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}
	
	private void randomTree() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngPanel.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void ingosGraph() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(true);
		trianglesField.setEnabled(true);
		rngPanel.setEnabled(false);
		keepLabel.setEnabled(false);
		keepBox.setEnabled(false);
		rngLabel.setEnabled(false);
		rngBox.setEnabled(false);
		seedLabel.setEnabled(false);
		seedField.setEnabled(false);
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void lollipop() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		rngPanel.setEnabled(false);
		keepLabel.setEnabled(false);
		keepBox.setEnabled(false);
		rngLabel.setEnabled(false);
		rngBox.setEnabled(false);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(true);
		verticesLolliField.setEnabled(true);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void k_tree() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		rngPanel.setEnabled(false);
		keepLabel.setEnabled(false);
		keepBox.setEnabled(false);
		rngLabel.setEnabled(false);
		rngBox.setEnabled(false);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(true);
		degreeKField.setEnabled(true);
	}

	private void semi() {
		edgesLabel.setEnabled(true);
		edgesField.setEnabled(true);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		rngPanel.setEnabled(true);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}

	private void semiP() {
		edgesLabel.setEnabled(true);
		edgesField.setEnabled(true);
		probabilityLabel.setEnabled(true);
		probabilityField.setEnabled(true);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		rngPanel.setEnabled(true);
		keepLabel.setEnabled(true);
		keepBox.setEnabled(true);
		rngLabel.setEnabled(true);
		rngBox.setEnabled(true);
		seedLabel.setEnabled(rngBox.isSelected());
		seedField.setEnabled(rngBox.isSelected());
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}
	
	private void trapLine() {
		edgesLabel.setEnabled(false);
		edgesField.setEnabled(false);
		probabilityLabel.setEnabled(false);
		probabilityField.setEnabled(false);
		weightLabel.setEnabled(false);
		weightField.setEnabled(false);
		trianglesLabel.setEnabled(false);
		trianglesField.setEnabled(false);
		rngPanel.setEnabled(false);
		keepLabel.setEnabled(false);
		keepBox.setEnabled(false);
		rngLabel.setEnabled(false);
		rngBox.setEnabled(false);
		seedLabel.setEnabled(false);
		seedField.setEnabled(false);
		verticesLolliLabel.setEnabled(false);
		verticesLolliField.setEnabled(false);
		degreeKLabel.setEnabled(false);
		degreeKField.setEnabled(false);
	}
}
