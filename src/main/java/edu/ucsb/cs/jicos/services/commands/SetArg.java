/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

/**
 * SetArg is invoked by ExecuteTask when the target TaskServer (i.e., the one
 * that contains the task whose argument is to be set) is not the host's.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services.commands;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.foundation.*;


public final class SetArg implements Command  
{
    private TaskId taskId; 
    private int    index;
    private Object value;
    private long criticalPathTime;
    
    public SetArg( TaskId taskId, int index, Object value, long criticalPathTime ) 
    {
        this.taskId = taskId;
        this.index  = index;
        this.value  = value;
        this.criticalPathTime = criticalPathTime;
    }
    
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }

    public void execute( ServiceImpl myService ) throws Exception 
    {
        //LogManager.getLogger().log( LogManager.DEBUG, "taskId: " + taskId );
        Session session = ((TaskServer) myService).session();
        session.setArg( taskId, index, value, criticalPathTime );
    }
}
