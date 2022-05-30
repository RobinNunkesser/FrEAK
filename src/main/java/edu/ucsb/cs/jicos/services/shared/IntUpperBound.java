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
 * IntUpperBound.java  IMMUTABLE
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services.shared;

import edu.ucsb.cs.jicos.services.*;


public class IntUpperBound extends Shared
{
    private Integer shared;
    
    /** Construct an IntUpperBound from the int argument.
     * @param shared The <CODE>int</CODE> value to be shared.
     */    
    public IntUpperBound( int shared ) { this.shared = new Integer( shared ); }

    /** Returns a reference to the shared Integer Object.
     * @return a reference to the shared Integer Object.
     */    
    public synchronized Object get() { return shared; }
    
    /** This method operationally defines the semantics of <I>newer</I>.
     * @return true if & only if this is newer than the argument Shared Object.
     * @param shared The Shared Object whose value is proposed as OLDER.
     */    
    public synchronized boolean isNewerThan( Shared shared )
    {
        assert shared != null;
        
        return this.shared.intValue() < ( (Integer) shared.get() ).intValue();
    }
}
