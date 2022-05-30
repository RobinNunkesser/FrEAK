/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.generalstring;
import freak.core.modulesupport.inspector.Inspector;
import freak.module.searchspace.GeneralString;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
/**
 * The inspector for the class Champ. This class extends JPanel to make it possible to configure the fitness function Champ.
 * @author  Christian
 */
public class ChampInspector extends Inspector {
	
	Champ function;
	JTextField clubField;
	JTable scoreTab;
	JTable startScoreTab;
	JTable matchTab;
	
	public ChampInspector(Champ function) {
		super(function);
		this.function = function;
		this.setLayout(new BorderLayout(10, 10));
		
		JPanel top = new JPanel();
		top.setLayout(new FlowLayout());
		top.add(new JLabel("No of clubs: "));
		JPanel topest = new JPanel();
		topest.setLayout(new BorderLayout());
		JCheckBox random = new JCheckBox("Create Randomly", function.getPropertyRandomly().booleanValue());
		random.addActionListener(new CheckBoxListener());
		topest.add(random, BorderLayout.NORTH);
		topest.add(top, BorderLayout.SOUTH);
		this.add(topest, BorderLayout.NORTH);
		
		clubField = new JTextField(Integer.toString(function.getPropertyScore().length));
		clubField.setMinimumSize(new Dimension(50, 20));
		clubField.setPreferredSize(new Dimension(50, 20));
		ClubFieldListener listener = new ClubFieldListener();
		clubField.addActionListener(listener);
		clubField.addFocusListener(listener);
		top.add(clubField);
		
		scoreTab = new JTable(3, 2);
		ScoreTable scoreTableModel = new ScoreTable();
		scoreTab.setModel(scoreTableModel);
		top.add(scoreTab);
		
		startScoreTab = new JTable();
		startScoreTab.setModel(new StartScoreTable());
		this.add(startScoreTab, BorderLayout.CENTER);
		
		matchTab = new JTable();
		matchTab.setModel(new MatchTable());
		this.add(matchTab, BorderLayout.SOUTH);
		
		if (random.isSelected()) { // deactivate the tables
			startScoreTab.setEnabled(false);
			startScoreTab.setVisible(false);
			matchTab.setEnabled(false);
			matchTab.setVisible(false);
		}
	}
	
	class ScoreTable extends AbstractTableModel {
		public ScoreTable() {
			super();
		}
		
		public boolean isCellEditable(int row, int col) {
			return (col == 1);
		}
		public int getColumnCount() {
			return 2;
		}
		
		public int getRowCount() {
			return 3;
		}
		
		public Object getValueAt(int row, int col) {
			if (col == 0 && row == 0)
				return "win";
			if (col == 0 && row == 1)
				return "lose";
			if (col == 0 && row == 2)
				return "draw";
			if (col == 1)
				return Integer.toString(function.getPropertyRule()[row]);
			return null;
		}
		
		public void setValueAt(Object obj, int row, int col) {
			int rule[] = function.getPropertyRule();
			Integer value = new Integer(0);
			
			try {
				value = new Integer((String)obj);
				rule[row] = (value.intValue() >= 0) ? value.intValue() : 0;
			} catch (java.lang.NumberFormatException e) {
			}
			function.setPropertyRule(rule);
			fireTableCellUpdated(row, col);
		}
	}
	
	class StartScoreTable extends AbstractTableModel {
		public StartScoreTable() {
			super();
		}
		public boolean isCellEditable(int row, int col) {
			return (col == 1 && row >= 1);
		}
		
		public int getColumnCount() {
			return 2;
		}
		
		public int getRowCount() {
			return function.getPropertyScore().length + 1;
		}
		
		public Object getValueAt(int row, int col) {
			if (col == 0 && row == 0)
				return "club";
			if (col == 1 && row == 0)
				return "score";
			if (col == 0 && row >= 1)
				return Integer.toString(row - 1);
			if (col == 1 && row >= 1)
				return Integer.toString(function.getPropertyScore()[row - 1]);
			return null;
		}
		
		public void setValueAt(Object obj, int row, int col) {
			int score[] = function.getPropertyScore();
			Integer value = new Integer(0);
			try {
				value = new Integer((String)obj);
				score[row - 1] = (value.intValue() >= 0) ? value.intValue() : 0;
			} catch (java.lang.NumberFormatException e) {
			}
			function.setPropertyScore(score);
			fireTableCellUpdated(row, col);
		}
	}
	
	class MatchTable extends AbstractTableModel {
		public MatchTable() {
			super();
		}
		
		public boolean isCellEditable(int row, int col) {
			return (col >= 1 && row >= 1);
		}
		
		public int getColumnCount() {
			return 3;
		}
		public int getRowCount() {
			return ((GeneralString)function.getSchedule().getPhenotypeSearchSpace()).getDimension() + 1;
		}
		
		public Object getValueAt(int row, int col) {
			if (col == 0 && row == 0)
				return "match";
			if (col == 1 && row == 0)
				return "club 1";
			if (col == 2 && row == 0)
				return "club 2";
			if (col == 0 && row >= 1)
				return Integer.toString(row - 1);
			
			if (col == 1 && row >= 1)
				return Integer.toString(function.getPropertyMatch()[row - 1][0]);
			if (col == 2 && row >= 1)
				return Integer.toString(function.getPropertyMatch()[row - 1][1]);
			return null;
		}
		
		public void setValueAt(Object obj, int row, int col) {
			
			int match[][] = function.getPropertyMatch();
			Integer value = new Integer(0);
			
			try {
				value = new Integer((String)obj);
				int realValue = value.intValue();
				if (realValue < 0)
					realValue = 0;
				if (realValue > function.getPropertyScore().length - 1)
					realValue = function.getPropertyScore().length - 1;
				
				match[row - 1][col - 1] = realValue;
			} catch (java.lang.NumberFormatException e) {
			}
			
			function.setPropertyMatch(match);
			fireTableCellUpdated(row, col);
		}
	}
	class ClubFieldListener implements ActionListener, FocusListener {
		public void actionPerformed(ActionEvent e) {
			Integer value = new Integer(0);
			int realValue = 0;
			
			try {
				JTextField textField = (JTextField)e.getSource();
				value = new Integer(textField.getText());
				realValue = value.intValue();
				if (realValue < 0)
					textField.setText(Integer.toString(function.getPropertyRule().length));
			} catch (java.lang.NumberFormatException event) {
			}
			// refresh the score-table
			int diff = realValue - function.getPropertyScore().length;
			int score[] = function.getPropertyScore();
			int newScore[] = new int[realValue];
			if (diff >= 0) { // not enough entries
				for (int i = 0; i < score.length; i++)
					newScore[i] = score[i];
			}
			if (diff < 0) { // to many entries
				for (int i = 0; i < newScore.length; i++)
					newScore[i] = score[i];
			}
			
			function.setPropertyScore(newScore);
			startScoreTab.setAutoResizeMode(2);
			// refresh the match-table
			int match[][] = function.getPropertyMatch();
			for (int i = 0; i < match.length; i++) {
				if (match[i][0] > realValue)
					match[i][0] = realValue - 1;
				if (match[i][1] > realValue)
					match[i][1] = realValue - 1;
			}
			function.setPropertyMatch(match);
			repaint();
		}
		public void focusLost(FocusEvent e) {
			actionPerformed(new ActionEvent(clubField, ActionEvent.ACTION_PERFORMED, "manual"));
		}
		public void focusGained(FocusEvent e) {
		}
	}
	class CheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			boolean checked = !function.getPropertyRandomly().booleanValue();
			if (checked) { // deactivate the tables
				startScoreTab.setEnabled(false);
				startScoreTab.setVisible(false);
				matchTab.setEnabled(false);
				matchTab.setVisible(false);
			} else { // reactivate the tables
				startScoreTab.setEnabled(true);
				startScoreTab.setVisible(true);
				matchTab.setEnabled(true);
				matchTab.setVisible(true);
			}
			function.setPropertyRandomly(new Boolean(checked));
		}
	}
}