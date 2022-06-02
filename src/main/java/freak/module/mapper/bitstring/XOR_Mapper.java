/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.mapper.bitstring;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.mapper.AbstractMapper;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.CustomInspectable;
import freak.core.modulesupport.inspector.CustomizableInspector;
import freak.core.modulesupport.inspector.Inspector;
import freak.core.modulesupport.inspector.StandardInspectorFactory;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.BitStringGenotypeEditor;

import javax.swing.*;
import java.util.BitSet;

/**
 * This transformer performs an xor operation on the genotype before the fitness
 * is evaluated. The shift with which the xor is performed can either be given
 * explicitly or chosen at random.
 *
 * @author Heiko, Michael
 */
public class XOR_Mapper extends AbstractMapper implements BatchEventListener, Configurable,
		CustomInspectable {
	
	private BitSet shift;
	
	private boolean randomly = false;
	
	public XOR_Mapper(Schedule schedule) {
		super(schedule);

		if (schedule.getPhenotypeSearchSpace() instanceof BitString) {
			int dim = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
			shift = new BitSet(dim);
			shift.set(0, dim);
		}
	}
	
	public void initialize() {
		super.initialize();
		
		int dim = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
		// re-compute shift if the search space dimension has changed
		if (shift != null && shift.size() != dim) { 
			// set shift to 1^n
			shift = new BitSet(dim);
			shift.set(0, dim);
		}
	}
	
	public Genotype genotypeToPhenotype(Genotype genotype) {
		BitSet set = (BitSet) ((BitStringGenotype)genotype).getBitSet().clone();
		set.xor(shift);
		return new BitStringGenotype(set, ((BitString)schedule.getPhenotypeSearchSpace()).getDimension());
	}

	public Genotype phenotypeToGenotype(Genotype genotype) {
		BitSet set = (BitSet) ((BitStringGenotype)genotype).getBitSet().clone();
		set.xor(shift);
		return new BitStringGenotype(set, ((BitString)schedule.getPhenotypeSearchSpace()).getDimension());
	}
	
	public SearchSpace getGenotypeSearchSpace() {
		return schedule.getPhenotypeSearchSpace();
	}
	
	public String getName() {
		return "XOR Mapper";
	}
	
	public String getDescription() {
		return "Before the fitness of an individual is evaluated an xor operation on its genotype is performed.";
	}
	
	/**
	 * Returns the value of attribute <code>randomly</code>.
	 *
	 * @return the wrapped value of attribute <code>randomly</code>.
	 */
	public Boolean getPropertyRandomly() {
		return new Boolean(randomly);
	}
	
	/**
	 * Sets the value of the attribute <code>randomly</code>.
	 *
	 * With this attribute set to true, the attribute <code>shift</code> is
	 * initialized randomly.
	 *
	 * @param randomly value you wish attribute<code>randomly</code> to be.
	 */
	public void setPropertyRandomly(Boolean randomly) {
		this.randomly = randomly.booleanValue();
	}
	
	/**
	 * Returns the value of attribute <code>shift</code>.
	 *
	 * @return the object <code>shift</code>.
	 */
	public BitStringGenotype getPropertyShift() {
		return new BitStringGenotype(shift, ((BitString)getGenotypeSearchSpace()).getDimension());
	}
	
	/**
	 * Sets the value of the attribute <code>shift</code>.
	 *
	 * Everytime the fitness function is evaluated, it returns f(a xor shift)
	 * instead of f(a).
	 *
	 * @param shift the shift value.
	 */
	public void setPropertyShift(BitStringGenotype geno) {
		this.shift = geno.getBitSet();
	}
	
	public Inspector getInspector() {
		CustomizableInspector inspector = StandardInspectorFactory.getStandardInspectorFor(this);
		inspector.customize(BitStringGenotype.class, new JTable().getDefaultRenderer(String.class), new BitStringGenotypeEditor());
		return inspector;
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	public void batchStarted(BatchEvent evt) {
		if (randomly) {
			BitString bs = new BitString(getSchedule(), ((BitString)schedule.getPhenotypeSearchSpace()).getDimension());
			shift = ((BitStringGenotype)bs.getRandomGenotype()).getBitSet();
		}
	}
	
}