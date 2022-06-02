/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.generalstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.CustomInspectable;
import freak.core.modulesupport.inspector.Inspector;
import freak.core.population.Genotype;
import freak.core.searchspace.HasDimension;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * 
 * This class implements the fitness function for the championship problem. <br>
 * 
 * It simulates the situation within a soccer-league. Different clubs are 
 * competing each other to gain the most points. So there are matches and in 
 * each match the winning club and the loosing club will get points. Sometimes
 * there is no winner or looser, thus in these situations each of the two clubs
 * will get the same ammount of points.<br><br>
 * 
 * The MP is described as follows: You have the current situation in the league.
 * It is given in form of a table containing the points of the different clubs.
 * You also have the "open-matches" which are planned matches, not played yet.
 * And you have one favourite club. The question is: Can this club be at the 
 * first place at the end of the tournament, and if it can be, how many points
 * more than the second club can it have. 
 * 
 * @author Christian, Stefan
 */

public class Champ extends AbstractStaticSingleObjectiveFitnessFunction implements Configurable,
		CustomInspectable {

	private int rule[];
	private int[][] match;
	private int[] score;

	private boolean randomly = false;

	/**
	 * The constructor of the class. 
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public Champ(Schedule schedule) {
		super(schedule);

		// set problem to MP3
		rule = new int[3];
		rule[0] = 3; // winning club
		rule[1] = 0; // losing club
		rule[2] = 1; // draw

		// set typical scores
		score = new int[10];
		for (int i = 0; i < 10; i++)
			score[i] = i;

		// set typical matches
		match = new int[((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension()][];
		for (int i = 0; i < ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension(); i++) {
			match[i] = new int[2];
			match[i][0] = i % 10;
			match[i][1] = (i + 1) % 10;
		}
	}

	public void initialize() {
		super.initialize();

		int dim = ((HasDimension)getSchedule().getGenotypeSearchSpace()).getDimension();
		// choose new instance if the search space dimension has been changed
		if (match != null && match.length != dim) {
			chooseInstance();
		}
	}
	
	public void batchStarted(BatchEvent evt) {
		if (randomly) chooseInstance();
	}
	
	private void chooseInstance() {
		// try to get to a point of time just in the middle of a term
		int noOfClubs = ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension() / 10;
		if (noOfClubs <= 2)
			noOfClubs = 3;

		int noOfMatches = noOfClubs / 2;

		RandomElement random = getSchedule().getRandomElement();

		for (int i = 0; i < noOfMatches; i++) {
			match[i][0] = random.choose(0, noOfClubs - 1);

			int next;
			while ((next = random.choose(0, noOfClubs - 1)) == match[i][0]);

			match[i][1] = next;
		}
	}

	public double evaluate(Genotype genotype) {
		int[] currentScore = new int[score.length];
		for (int k = 0; k < score.length; k++)
			currentScore[k] = score[k];

		int[] ind = ((GeneralStringGenotype)genotype).getIntArray();

		for (int k = 0; k < match.length; k++) {
			if (ind[k] == 0) { // first looses
				currentScore[match[k][0]] += rule[0];
				currentScore[match[k][1]] += rule[2];
			}
			if (ind[k] == 1) { // draw
				currentScore[match[k][0]] += rule[1];
				currentScore[match[k][1]] += rule[1];
			}
			if (ind[k] == 2) { // first wins
				currentScore[match[k][0]] += rule[2];
				currentScore[match[k][1]] += rule[0];
			}
		}

		return Math.exp(currentScore[0]);
	}

	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}

	public double getUpperBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the value of the attribute <code>rule</code>.
	 * 
	 * This is an array of double of size 3, which contains the number of points
	 * a club gets if it wins, looses or does nothing of the two mentioned.
	 * 
	 * @param rule value you wish attribute <code>rule</code> to be.
	 */
	public void setPropertyRule(int rule[]) {
		if (rule.length == 3)
			this.rule = rule;
	}

	/**
	 * Returns the value of attribute <code>rule</code>.
	 * 
	 * @return the object <code>rule</code>.
	 */
	public int[] getPropertyRule() {
		return rule;
	}

	/**
	 * Sets the value of the attribute <code>match</code>.
	 * 
	 * This is an array of int[2]. Each entry in the array specifies one match
	 * between the two clubs contained. The size of the array has to equal the 
	 * dimension of the <code>SearchSpace</code>.
	 * 
	 * @param match value you wish attribut <code>match</code> to be.
	 */
	public void setPropertyMatch(int match[][]) {
		if (match.length == ((GeneralString)getSchedule().getPhenotypeSearchSpace()).getDimension())
			this.match = match;
	}

	/**
	 * Returns the value of attribute <code>match</code>
	 * 
	 * @return the object <code>match</code>
	 */
	public int[][] getPropertyMatch() {
		return match;
	}

	/**
	 * Sets the value of the attribute <code>score</code>.
	 * 
	 * This is an array of int, which specifies the score at the beginning of
	 * the tournament.
	 * 
	 * @param score value you wish attribute <code>score</code> to be.
	 */
	public void setPropertyScore(int score[]) {
		this.score = score;
	}

	/**
	 * Returns the value of attribute <code>score</code>.
	 * 
	 * @return the object <code>score</code>.
	 */
	public int[] getPropertyScore() {
		return score;
	}

	/**
	 * Returns the value of attribute <code>randomly</code>.
	 * 
	 * @return the wrapped value of <code>randomly</code>.
	 */
	public Boolean getPropertyRandomly() {
		return new Boolean(randomly);
	}

	/**
	 * Sets the value of the attribute <code>randomly</code>.
	 * 
	 * With this attribute set to true, the fitness funcion is initialized
	 * randomly.
	 * 
	 * @param randomly value you wish attribute <code>randomly</code> to be.
	 */
	public void setPropertyRandomly(Boolean randomly) {
		this.randomly = randomly.booleanValue();
	}

	public String getDescription() {
		return "Simulates the situation within a soccer-league. Different clubs are " + "competing each other to gain the most points. So there are matches and in " + "each match the winning club and the losing club will get points. Sometimes " + "there is no winner or loser, thus in these situations each of the two clubs " + "will get the same amount of points.\r\n" + "The MP is described as follows: You have the current situation in the league. " + "It is given in form of a table containing the points of the different clubs. " + "You also have the 'open-matches' which are planned matches, not played yet. " + "And you have one favourite club. The question is: Can this club be at the " + "first place at the end of the tournament, and if it can be, how many points " + "more than the second club can it have.";
	}

	public String getName() {
		return "Champ";
	}

	public Inspector getInspector() {
		return new ChampInspector(this);
	}

}
