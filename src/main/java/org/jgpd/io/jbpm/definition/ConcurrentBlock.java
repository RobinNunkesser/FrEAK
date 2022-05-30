package org.jgpd.io.jbpm.definition;

import org.jgpd.jgraph.JoinCell;
import org.jgpd.jgraph.SplitCell;

public interface ConcurrentBlock extends ProcessBlock {
  SplitCell getSplit();
  JoinCell getJoin();
}
