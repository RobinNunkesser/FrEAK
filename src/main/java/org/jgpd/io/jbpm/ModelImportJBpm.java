/*
 *
 *
 * Copyright (C) 2003 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgpd.io.jbpm;

import org.jgpd.io.JGpdModelNode;
import org.jgpd.io.ModelImportImpl;
import org.jgpd.io.jbpm.definition.impl.ActivityStateImpl;
import org.jgpd.io.jbpm.definition.impl.AttributeImpl;
import org.jgpd.io.jbpm.definition.impl.DecisionImpl;
import org.jgpd.io.jbpm.definition.impl.DefinitionObjectImpl;
import org.jgpd.io.jbpm.definition.impl.EndStateImpl;
import org.jgpd.io.jbpm.definition.impl.ForkImpl;
import org.jgpd.io.jbpm.definition.impl.JoinImpl;
import org.jgpd.io.jbpm.definition.impl.StartStateImpl;
import org.w3c.dom.Node;

public class ModelImportJBpm extends ModelImportImpl {

	public void importActivity()
	{

	}

	public void importDecision()
	{

	}

	public void importFork()
	{

	}

	public void importJoin()
	{

	}

	public void importStart()
	{

	}

	public void importEnd()
	{

	}

	public JGpdModelNode importNode(Node node, String nodeType)
	{
		JGpdModelNode modelNode = null;

		// Check the prefix of nodeType indicates jBpm
		int modelTypeLength = DefinitionObjectImpl.modelType.length();
		if (nodeType.length() > modelTypeLength)
		{
			if ( nodeType.substring(0,modelTypeLength).equals
			           (DefinitionObjectImpl.modelType) )
			{
				// Match on model type
				if (node.getNodeName().toLowerCase().equals("start-state"))
				{
					modelNode = new StartStateImpl(node);
				}
				else if (node.getNodeName().toLowerCase().equals("end-state"))
				{
					modelNode = new EndStateImpl(node);
				}
				else if (node.getNodeName().toLowerCase().equals("activity-state"))
				{
					modelNode = new ActivityStateImpl(node);
				}
				else if (node.getNodeName().toLowerCase().equals("decision"))
				{
					modelNode = new DecisionImpl(node);
				}
				else if (node.getNodeName().toLowerCase().equals("attribute"))
				{
					modelNode = new AttributeImpl(node);
				}
				else if (node.getNodeName().toLowerCase().equals("fork"))
				{
					modelNode = new ForkImpl(node);
				}
				else if (node.getNodeName().toLowerCase().equals("join"))
				{
					modelNode = new JoinImpl(node);
				}

			}
			else
			{
				// Wrong model, indicate as such
				// FIXME
			}
		}
		else
		{
			// Node string isn't long enough to actually store a value
			// after the model type indentifier
			// FIXME dialog saying we have an error
		}

		return modelNode;
	}
}