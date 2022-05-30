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

package edu.ucsb.cs.jicos.services.commands;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.foundation.*;

/**
 *
 * @author  Pete Cappello
 */
public final class UpdateExternalServiceProviderMap implements Command 
{
    private Class   taskClass;
    private Service taskServer;
    private Service fromService;
    
    public UpdateExternalServiceProviderMap(Class taskClass, Service taskServer, Service fromService) 
    {
        assert taskClass != null;
        assert taskServer != null;
        assert fromService != null;
        
        this.taskClass = taskClass;
        this.taskServer = taskServer;
        this.fromService = fromService;
    }
    
    public void execute(ServiceImpl serviceImpl) throws Exception 
    {
        ((TaskServer) serviceImpl).updateExternalServiceProviderMap(taskClass, taskServer, fromService);
    }
    
    public void execute(Proxy proxy) { proxy.sendCommand( this ); }   
}
