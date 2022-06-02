/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view;

import freak.core.control.Schedule;
import freak.core.view.AbstractView;
import freak.core.view.swingsupport.FreakTextFieldModel;
import freak.core.view.swingsupport.UpdateManager;

import javax.swing.*;
import java.awt.*;

/**
 * @author  Heiko
 */
public class VoteView extends AbstractView {

	private FreakTextFieldModel textYes;
	private FreakTextFieldModel textNo;
	
	private int numYes = 0;
	private int numNo = 0;

	public VoteView(Schedule schedule) {
		super(schedule);
		textYes = new FreakTextFieldModel();
		textYes.setText("Yes: 0");
		textNo = new FreakTextFieldModel();
		textNo.setText("No: 0");		
	}

	public Class[] getInputDataTypes() {
		return new Class[] { Boolean.class };
	}

	public JPanel createPanel() {
		JPanel panel = new JPanel();
		JTextField textField = new JTextField();
		textField.setPreferredSize(new Dimension(200,40));
		JTextField textField2 = new JTextField();
		textField2.setPreferredSize(new Dimension(200,40));

		textYes.setView(textField);
		textNo.setView(textField2);
		panel.add(textField);
		panel.add(textField2);
		return panel;
	}

	public String getName() {
		return "Vote View";
	}

	public String getDescription() {
		return "Counts the number of positive and negative results.";
	}
	
	public void update(Object data) {
		Boolean wrapper = (Boolean)data;
		if (wrapper.booleanValue()) {
			numYes++;
		} else {
			numNo++;
		}
		double percYes = (double)numYes/(numYes+numNo)*100;
		double percNo = (double)numNo/(numYes+numNo)*100;
				
		synchronized(textYes) {
			textYes.setText("Yes: "+numYes+"  "+percYes+"%");
		}
		
		synchronized(textNo) {
			textNo.setText("No: "+numNo+"  "+percNo+"%");
		}
		
		UpdateManager.markDirty(textYes);
		UpdateManager.markDirty(textNo);
	}

}
