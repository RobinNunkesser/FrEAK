/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.runframe;

import freak.core.control.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author  Stefan Tannenbaum
 */
public class GoToDialog extends GoToDialogUI {
	GenerationIndex goTo;
	
	public GoToDialog(Frame parent, GenerationIndex initDefault) {
		super(parent, true);
		
		setFromIndex(initDefault);
		
		jTextFieldBatch.selectAll();
		jTextFieldBatch.requestFocus();
		
		jTextFieldBatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jTextFieldRun.selectAll();
				jTextFieldRun.requestFocusInWindow();
			}
		});
		jTextFieldRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jTextFieldGeneration.selectAll();
				jTextFieldGeneration.requestFocusInWindow();
			}
		});
		jTextFieldGeneration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ok();
			}
		});
		jButtonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				ok();
			}
		});
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cancel();
			}
		});
	}
	
	private void ok() {
		parse();
		if (goTo != null) {
			setVisible(false);
		}
	}
	
	private void cancel() {
		setVisible(false);
	}
	
	private void setFromIndex(GenerationIndex generationIndex) {
		jTextFieldBatch.setText(String.valueOf(generationIndex.batch));
		jTextFieldRun.setText(String.valueOf(generationIndex.run));
		jTextFieldGeneration.setText(String.valueOf(generationIndex.generation));
	}
	
	private void parse() {
		try {
			int batch = parse(jTextFieldBatch);
			int run = parse(jTextFieldRun);
			int generation = parse(jTextFieldGeneration);
			
			if (!(batch > 0 && run > 0 && generation > 0) || (batch == 0 && run == 0 && generation == 0)) {
				throw new NumberFormatException("Please enter only positive or only zero values.");
			}
			
			goTo = new GenerationIndex(batch, run, generation);
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this, exc.getMessage(), "Illegal Generation Index", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private int parse(JTextField textField) {
		try {
			return Integer.parseInt(textField.getText());	
		} catch (NumberFormatException exc) {
			textField.selectAll();
			textField.requestFocusInWindow();
			throw new NumberFormatException("Please enter an integer number.");
		}
	}
}
