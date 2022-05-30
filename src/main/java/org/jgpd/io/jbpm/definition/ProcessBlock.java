package org.jgpd.io.jbpm.definition;

import java.util.*;

import org.jgraph.graph.GraphModel;

public interface ProcessBlock extends DefinitionObject {
	Collection getNodes();
	HashMap getAttributes();
	public void populateProcessBlock( Object beginCell,
										Object finishCell,
										GraphModel model,
										Vector concurrentBlocks );
}
