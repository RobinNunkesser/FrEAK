/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.gui.gpas;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.filechooser.FileFilter;

import freak.Freak;
import freak.core.control.Actions;
import freak.core.control.BatchProcessor;
import freak.core.control.GenerationIndex;
import freak.core.control.Replay;
import freak.core.control.RunControl;
import freak.core.control.Schedule;
import freak.core.control.StateListener;
import freak.core.control.Actions.Action;
import freak.gui.runframe.SingleExtensionFileFilter;
import freak.rinterface.control.LogRegInterface;
import freak.rinterface.model.ScheduleConfigurator;
import javax.swing.JProgressBar;

public class GPASDialog extends JDialog implements StateListener {

	private static final long serialVersionUID = 1L;
	
	private JTextField jTextField = null;

	private JButton jButton = null;
	
	private File dataFile = null;

	private File testFile = null;

	private JButton jButton1 = null;

	private JPanel jContentPane = null;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JLabel jLabel2 = null;

	private JTextField jTextField1 = null;

	private JTextField jTextField2 = null;

	private JButton jButton2 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel5 = null;

	private JLabel jLabel6 = null;

	private JTextField jTextField3 = null;

	private JButton jButton3 = null;

	private JProgressBar jProgressBar = null;
		
	private RunControl runControl = null;

	private JLabel jLabel7 = null;

	private JButton jButton4 = null;
	
	private int generations = 10000;
	
	private int runs = 1;

	private JLabel jLabel8 = null;

	private JLabel jLabel9 = null;

	private JLabel jLabel10 = null;

	private JLabel jLabel11 = null;

	private JTextField jTextField4 = null;

	private JLabel jLabel12 = null;

	private JTextField jTextField5 = null;

	private JTextField jTextField6 = null;

	private JLabel jLabel13 = null;

	private JLabel jLabel14 = null;

	private JLabel jLabel15 = null;

	private JLabel jLabel16 = null;

	private JLabel jLabel17 = null;

	private JTextField jTextField7 = null;

	/**
	 * This is the default constructor
	 */
	public GPASDialog(Frame owner) {		
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(640, 480);
		this.setContentPane(getJContentPane());
		this.setTitle("Genetic Programming for Association Studies");
		this.setSize(new Dimension(640, 480));
		jButton4.setEnabled(false);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.fill = GridBagConstraints.BOTH;
			gridBagConstraints22.gridy = 3;
			gridBagConstraints22.weightx = 1.0;
			gridBagConstraints22.gridx = 1;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.anchor = GridBagConstraints.WEST;
			gridBagConstraints18.gridy = 3;
			jLabel17 = new JLabel();
			jLabel17.setText(" Save result as    ");
			GridBagConstraints gridBagConstraints141 = new GridBagConstraints();
			gridBagConstraints141.gridx = 1;
			gridBagConstraints141.gridy = 15;
			jLabel16 = new JLabel();
			jLabel16.setText(" ");
			GridBagConstraints gridBagConstraints131 = new GridBagConstraints();
			gridBagConstraints131.gridx = 1;
			gridBagConstraints131.gridy = 8;
			jLabel15 = new JLabel();
			jLabel15.setText(" ");
			GridBagConstraints gridBagConstraints121 = new GridBagConstraints();
			gridBagConstraints121.gridx = 1;
			gridBagConstraints121.gridy = 1;
			jLabel14 = new JLabel();
			jLabel14.setText(" ");
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			gridBagConstraints111.gridx = 0;
			gridBagConstraints111.anchor = GridBagConstraints.WEST;
			gridBagConstraints111.gridy = 11;
			jLabel13 = new JLabel();
			jLabel13.setText(" Min ratio");
			GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
			gridBagConstraints101.fill = GridBagConstraints.BOTH;
			gridBagConstraints101.gridy = 11;
			gridBagConstraints101.weightx = 1.0;
			gridBagConstraints101.gridx = 1;
			GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
			gridBagConstraints91.fill = GridBagConstraints.BOTH;
			gridBagConstraints91.gridy = 10;
			gridBagConstraints91.weightx = 1.0;
			gridBagConstraints91.gridx = 1;
			GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
			gridBagConstraints81.gridx = 0;
			gridBagConstraints81.anchor = GridBagConstraints.WEST;
			gridBagConstraints81.gridy = 10;
			jLabel12 = new JLabel();
			jLabel12.setText(" Min occurences    ");
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			gridBagConstraints71.fill = GridBagConstraints.BOTH;
			gridBagConstraints71.gridy = 9;
			gridBagConstraints71.weightx = 1.0;
			gridBagConstraints71.gridx = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridy = 9;
			jLabel11 = new JLabel();
			jLabel11.setText(" Save graph as");
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 1;
			gridBagConstraints51.gridy = 18;
			jLabel10 = new JLabel();
			jLabel10.setText(" ");
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 1;
			gridBagConstraints41.gridy = 13;
			jLabel9 = new JLabel();
			jLabel9.setText(" ");
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 1;
			gridBagConstraints31.gridy = 6;
			jLabel8 = new JLabel();
			jLabel8.setText(" ");
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 3;
			gridBagConstraints21.gridy = 19;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.anchor = GridBagConstraints.WEST;
			gridBagConstraints17.gridy = 19;
			jLabel7 = new JLabel();
			jLabel7.setText(" Progress");
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridy = 19;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 3;
			gridBagConstraints15.gridy = 16;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.gridy = 16;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridx = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 16;
			jLabel6 = new JLabel();
			jLabel6.setText(" Test data set    ");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.gridy = 14;
			jLabel5 = new JLabel();
			jLabel5.setText("Discrimination");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.gridy = 7;
			jLabel4 = new JLabel();
			jLabel4.setText("Identification of interactions");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.gridy = 0;
			jLabel3 = new JLabel();
			jLabel3.setText("General settings");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.gridy = 17;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.gridy = 5;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.gridx = 1;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridy = 4;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.gridx = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.gridy = 5;
			jLabel2 = new JLabel();
			jLabel2.setText(" Generations    ");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.gridy = 4;
			jLabel1 = new JLabel();
			jLabel1.setText(" Runs");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.gridy = 2;
			jLabel = new JLabel();
			jLabel.setText(" Data set");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 12;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 2;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 3;
			gridBagConstraints.gridy = 2;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJButton(), gridBagConstraints);
			jContentPane.add(getJTextField(), gridBagConstraints1);
			jContentPane.add(getJButton1(), gridBagConstraints2);
			jContentPane.add(jLabel, gridBagConstraints3);
			jContentPane.add(jLabel1, gridBagConstraints4);
			jContentPane.add(jLabel2, gridBagConstraints5);
			jContentPane.add(getJTextField1(), gridBagConstraints7);
			jContentPane.add(getJTextField2(), gridBagConstraints8);
			jContentPane.add(getJButton2(), gridBagConstraints9);
			jContentPane.add(jLabel3, gridBagConstraints10);
			jContentPane.add(jLabel4, gridBagConstraints11);
			jContentPane.add(jLabel5, gridBagConstraints12);
			jContentPane.add(jLabel6, gridBagConstraints13);
			jContentPane.add(getJTextField3(), gridBagConstraints14);
			jContentPane.add(getJButton3(), gridBagConstraints15);
			jContentPane.add(getJProgressBar(), gridBagConstraints16);
			jContentPane.add(jLabel7, gridBagConstraints17);
			jContentPane.add(getJButton4(), gridBagConstraints21);
			jContentPane.add(jLabel8, gridBagConstraints31);
			jContentPane.add(jLabel9, gridBagConstraints41);
			jContentPane.add(jLabel10, gridBagConstraints51);
			jContentPane.add(jLabel11, gridBagConstraints6);
			jContentPane.add(getJTextField4(), gridBagConstraints71);
			jContentPane.add(jLabel12, gridBagConstraints81);
			jContentPane.add(getJTextField5(), gridBagConstraints91);
			jContentPane.add(getJTextField6(), gridBagConstraints101);
			jContentPane.add(jLabel13, gridBagConstraints111);
			jContentPane.add(jLabel14, gridBagConstraints121);
			jContentPane.add(jLabel15, gridBagConstraints131);
			jContentPane.add(jLabel16, gridBagConstraints141);
			jContentPane.add(jLabel17, gridBagConstraints18);
			jContentPane.add(getJTextField7(), gridBagConstraints22);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setToolTipText("The (training) data set to be loaded.");
		}
		return jTextField;
	}

	public void cancel() {
		dispose();
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("browse");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					chooseTrainingSet();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Identify interactions");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					startInteractionSearch();
				}
			});
		}
		return jButton1;
	}

	public void asynchroneousFeedback(Schedule schedule, Replay replay) {
		if (schedule!=null) {
			GenerationIndex now = schedule.getCurrentTimeIndex();
			int currentGeneration = now.generation;
			int currentRun = now.run;
			jProgressBar.setValue(currentGeneration+(currentRun-1)*generations);
		} else {
			jProgressBar.setValue(0);
		}
	}

	public void simulationCompleted(Action lastProcessed) {
		jButton.setEnabled(true);
		jButton1.setEnabled(true);
		jButton2.setEnabled(true);
		jButton3.setEnabled(true);
		jButton4.setEnabled(false);		
	}

	public void simulationException(Exception exc) {
		// TODO Auto-generated method stub
		
	}

	public void synchroneousFeedback(Schedule activeSchedule, Replay replay) {
		// TODO Auto-generated method stub		
	}

	public void terminated(Action lastProcessedBeforeTermination) {
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setText("1");
			jTextField1.setToolTipText("The number of independent runs.");
		}
		return jTextField1;
	}

	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setText("10000");
			jTextField2.setToolTipText("The number of generations for each run.");
		}
		return jTextField2;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Discriminate");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					startDiscrimination(); // TODO Auto-generated Event stub actionPerformed()
				}
			});
		}
		return jButton2;
	}
	
	private void chooseTestSet() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(".//data"));
		SingleExtensionFileFilter ff = new SingleExtensionFileFilter("csv","CSV Files");
		chooser.addChoosableFileFilter(ff);
		chooser.setFileFilter(ff);
		chooser.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (chooser.getSelectedFile()!=null) testFile = chooser.getSelectedFile();
				if (testFile!=null) jTextField3.setText(testFile.getPath());							
			}
		});
		chooser.showOpenDialog(this);
		chooser.getSelectedFile();		
	}

	private void chooseTrainingSet() {
		final JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(".//data"));
		SingleExtensionFileFilter ff = new SingleExtensionFileFilter("csv","CSV Files");
		chooser.addChoosableFileFilter(ff);
		chooser.setFileFilter(ff);
		chooser.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (chooser.getSelectedFile()!=null) dataFile = chooser.getSelectedFile();
				if (dataFile!=null) jTextField.setText(dataFile.getPath());							
			}
		});
		chooser.showOpenDialog(this);
		chooser.getSelectedFile();		
	}

	private void startDiscrimination() {
		Vector<String> errors=new Vector<String>();
		try {
			new FileReader(jTextField.getText());
		} catch (IOException e) {	
			errors.add("\""+jTextField.getText()+"\" is not a valid path to a data set.");
		}
		try {
			runs=Integer.parseInt(jTextField1.getText());
		} catch (NumberFormatException e) {
			errors.add("\""+jTextField1.getText()+"\" is not a valid number of runs.");
		}
		try {
			generations=Integer.parseInt(jTextField2.getText());
		} catch (NumberFormatException e) {
			errors.add("\""+jTextField2.getText()+"\" is not a valid number of generations.");
		}
		try {
			if (!jTextField3.getText().equals("")) new FileReader(jTextField3.getText());
		} catch (IOException e) {	
			errors.add("\""+jTextField3.getText()+"\" is not a valid path to a test data set.");
		}		
		if (jTextField7.getText().trim().equals("")) errors.add("You have to specify a file to save the result to.");
		if (errors.size()>0) {
			StringBuffer strbuf=new StringBuffer();
			Iterator<String> errit = errors.iterator();
			while (errit.hasNext()) {
				strbuf.append(errit.next());
				if (errit.hasNext()) strbuf.append("\n");
			}
			JOptionPane.showMessageDialog(this,strbuf.toString());			
		} else {
			jProgressBar.setMinimum(0);
			jProgressBar.setMaximum(generations*runs);
			jButton.setEnabled(false);
			jButton1.setEnabled(false);
			jButton2.setEnabled(false);
			jButton3.setEnabled(false);
			jButton4.setEnabled(true);
			
			ScheduleConfigurator.setDiscrimination(jTextField.getText(),runs,generations,jTextField3.getText(),jTextField7.getText());
			Schedule schedule=ScheduleConfigurator.getCurrentSchedule();

			runControl = new RunControl(this);
			runControl.setNewSchedule(schedule);
			runControl.request(new Actions.StartAction());
			
		}
	}

	private void startInteractionSearch() {
		int occurences=10;
		double ratio=0.1;
		Vector<String> errors=new Vector<String>();
		try {
			new FileReader(jTextField.getText());
		} catch (IOException e) {	
			errors.add("\""+jTextField.getText()+"\" is not a valid path to a data set.");
		}
		try {
			runs=Integer.parseInt(jTextField1.getText());
		} catch (NumberFormatException e) {
			errors.add("\""+jTextField1.getText()+"\" is not a valid number of runs.");
		}
		try {
			generations=Integer.parseInt(jTextField2.getText());
		} catch (NumberFormatException e) {
			errors.add("\""+jTextField2.getText()+"\" is not a valid number of generations.");
		}
		try {
			occurences=Integer.parseInt(jTextField5.getText());
		} catch (NumberFormatException e) {
			errors.add("\""+jTextField5.getText()+"\" is not a valid number of minimum occurences.");
		}
		try {
			ratio=Double.parseDouble(jTextField6.getText());
		} catch (NumberFormatException e) {
			errors.add("\""+jTextField6.getText()+"\" is not a valid number for the minimal ratio.");
		}			
		if (jTextField7.getText().trim().equals("")) errors.add("You have to specify a file to save the result to.");
		if (errors.size()>0) {
			StringBuffer strbuf=new StringBuffer();
			Iterator<String> errit = errors.iterator();
			while (errit.hasNext()) {
				strbuf.append(errit.next());
				if (errit.hasNext()) strbuf.append("\n");
			}
			JOptionPane.showMessageDialog(this,strbuf.toString());			
		} else {
			jProgressBar.setMinimum(0);
			jProgressBar.setMaximum(generations*runs);
			jButton.setEnabled(false);
			jButton1.setEnabled(false);
			jButton2.setEnabled(false);
			jButton3.setEnabled(false);
			jButton4.setEnabled(true);			
			
			ScheduleConfigurator.setInteraction(jTextField.getText(),runs,generations,jTextField4.getText(),occurences,ratio,jTextField7.getText());
			Schedule schedule=ScheduleConfigurator.getCurrentSchedule();

			runControl = new RunControl(this);
			runControl.setNewSchedule(schedule);
			runControl.request(new Actions.StartAction());
		}
	}

	/**
	 * This method initializes jTextField3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setToolTipText("Test data set if the training result should be tested on one.");
		}
		return jTextField3;
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("browse");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					chooseTestSet();
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setText("abort");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					runControl.request(new Actions.TerminateAction());
					jButton.setEnabled(true);
					jButton1.setEnabled(true);
					jButton2.setEnabled(true);
					jButton3.setEnabled(true);
					jButton4.setEnabled(false);
					jProgressBar.setValue(0);
				}
			});
		}
		return jButton4;
	}

	/**
	 * This method initializes jTextField4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setText("interactions.dot");
			jTextField4.setToolTipText("The name of the GraphViz file where the interaction graph should be saved in.");
		}
		return jTextField4;
	}

	/**
	 * This method initializes jTextField5	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField5() {
		if (jTextField5 == null) {
			jTextField5 = new JTextField();
			jTextField5.setText("10");
			jTextField5.setToolTipText("The minimum number of times an interaction has to occur to be included in the interaction graph.");
		}
		return jTextField5;
	}

	/**
	 * This method initializes jTextField6	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField6() {
		if (jTextField6 == null) {
			jTextField6 = new JTextField();
			jTextField6.setText("0.1");
			jTextField6.setToolTipText("The minimal ratio a single literal has to occur in relation to his ancestor in the interaction graph to be included in the interaction graph.");
		}
		return jTextField6;
	}

	/**
	 * This method initializes jTextField7	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField7() {
		if (jTextField7 == null) {
			jTextField7 = new JTextField();
			jTextField7.setText("individuals.csv");
			jTextField7.setToolTipText("The file where the resulting models will be saved in.");
		}
		return jTextField7;
	}

}  