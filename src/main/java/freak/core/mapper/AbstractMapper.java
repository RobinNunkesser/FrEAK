/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.mapper;

import freak.core.control.*;
import freak.core.modulesupport.*;
import freak.core.population.*;

/**
 * An abstract superclass for all search space mappers.
 * 
 * @author Michael, Stefan
 */
public abstract class AbstractMapper extends AbstractModule implements Mapper {

	public AbstractMapper(Schedule schedule) {
		super(schedule);
	}

	public Genotype phenotypeToGenotype(Genotype phenotype) throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}
}
