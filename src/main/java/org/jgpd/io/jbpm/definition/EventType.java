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
 
package org.jgpd.io.jbpm.definition;

import java.io.*;
import org.jgpd.lang.Enum;

public final class EventType extends Enum implements Serializable {
  
  public static final EventType PROCESS_START = new EventType( "process-start" );
  public static final EventType PROCESS_END = new EventType( "process-end" );
  public static final EventType TOKEN_ENTERS_NODE = new EventType( "token-enters-node" );
  public static final EventType TOKEN_LEAVES_NODE = new EventType( "node-exit" );
  public static final EventType TOKEN_OVER_TRANSITION = new EventType( "token-over-transition" );
  public static final EventType AFTER_STATE_ASSIGNMENT = new EventType( "after-state-assignment" );
  
  private EventType(String id) {
	super(id);
  }
}
