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
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.view.AbstractView;
import freak.core.view.swingsupport.FreakSmallTableModel;
import freak.core.view.swingsupport.FreakTableModel;
import freak.core.view.swingsupport.UpdateManager;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * Shows a table of all individuals with their genotypes, fitness values and  dates of birth.
 * @author  Stefan
 */
public class IndividualTable extends AbstractView {
	private FreakTableModel model;

	public IndividualTable(Schedule schedule) {
		super(schedule);
		model = new FreakSmallTableModel(new String[] { "Date of Birth", "Fitness", "Genotype" });
	}

	public JPanel createPanel() {
		JTable table = new JTable();
		JScrollPane scrollPane = new JScrollPane(table);
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);

		model.setView(table);

		return panel;
	}

	public Class[] getInputDataTypes() {
		return new Class[] { IndividualList.class };
	}

	public void update(Object data) {
		super.update(data);

		FitnessFunction fitness = getSchedule().getFitnessFunction();
		boolean singleObjective = (fitness instanceof SingleObjectiveFitnessFunction); 
		IndividualList individuals = (IndividualList)data;
		Iterator i = individuals.iterator();

		synchronized (model) {
			model.clear();
			while (i.hasNext()) {
				Individual individual = (Individual)i.next();
				String s = "";
				if (singleObjective) {
					s = (new Double(((SingleObjectiveFitnessFunction)fitness).evaluate(individual, individuals))).toString();
				} else {
					double[] result = ((MultiObjectiveFitnessFunction)fitness).evaluate(individual, individuals);
					s = " , ";
					for (int j = 0; j < result.length; j++) {
						s = s + result[j];
						if (j < result.length-1) {
							s = s + " , "; 
						}
					}
					s = s + " , ";
				}
				model.addRow(new Object[] {
					new Integer(individual.getDateOfBirth()),
					s,
					individual.getGenotype()
				});
			}
		}

		UpdateManager.markDirty(model);
	}

	public String getName() {
		return "Individual Table";
	}

	public String getDescription() {
		return "Shows a table of all individuals with their genotypes, fitness values and dates of birth.";
	}

}
