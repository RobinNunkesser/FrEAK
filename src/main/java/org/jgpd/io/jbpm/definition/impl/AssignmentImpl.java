/*
 *
 * Copyright (C) 2004 David Benson
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

package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.utils.FilePackage;

public class AssignmentImpl
{
	protected FilePackage handler = new FilePackage(null,null);
	protected ParametersImpl parameters = new ParametersImpl();

	
	/**
	 * @return Returns the handler.
	 */
	public FilePackage getHandler() {
		return handler;
	}

	/**
	 * @param handler The handler to set.
	 */
	public void setHandler(FilePackage handler) {
		this.handler = handler;
	}

	/**
	 * @return
	 */
	public ParametersImpl getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 */
	public void setParameters(ParametersImpl parameters) {
		this.parameters = parameters;
	}

}