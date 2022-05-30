/*
 *
 * Copyright (c) 2002-2004, Tom Baeyens
 * Copyright (c) 2004, David Benson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of 'jBpm', the name 'JGpd' nor the names of their
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jgpd.lang;

import java.io.*;
import java.util.*;

public class Enum implements Serializable {
  
  private static Map enums = new HashMap();
  protected Object id = null;
  
  public Object getId() {
	return id; 
  }

  protected Enum( Object id ) {
	if ( id == null ) throw new NullPointerException( "id of a '" + getClass().getName() + "' is null" );
	this.id = id;

	// add this enum to the enums
	Class enumClass = this.getClass();
		Map classEnums = (Map) enums.get( enumClass );
		if ( classEnums == null ) {
		  classEnums = new HashMap();
		  enums.put( enumClass, classEnums );
		}
	if ( classEnums.containsKey( id ) ) throw new IllegalArgumentException( "duplicate id '" + id + "' was used to create an enum of class '" + getClass().getName() + "'" );
	classEnums.put( id, this );
  }
  
  public static Enum findById( Class enumClass, Object id ) {
	Enum enumer = null;
    
	Map classEnums = (Map) enums.get( enumClass );
	if ( classEnums != null ) {
	  enumer = (Enum) classEnums.get( id );
	}
    
	return enumer;
  }
  
  Object readResolve() throws ObjectStreamException {
	return findById( this.getClass(), id );
  }
  
  public String toString() {
	return id.toString();
  }
}
