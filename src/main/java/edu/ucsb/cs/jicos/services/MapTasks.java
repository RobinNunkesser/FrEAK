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
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import java.util.*;
/**
   taskId HashMap of HashSet of child$
*/
final public class MapTasks
{
    private Map ids = Collections.synchronizedMap ( new HashMap() );
    
    void clear() { ids.clear(); }

    /**
       returns true, if this child is in its parent's map, false otherwise.
       if false, it puts this child in its parent's map.
     * if its parent has no set of ids, it is created at this point.    
     */
    boolean isIn ( TaskId taskId )
    {
        TaskId key = taskId.getParentId();
        HashSet m = (HashSet) ids.get ( key );
        if ( m == null )
        {
            // construct id set for parent
            m = new HashSet();
            ids.put ( key, m );
        }
        Integer i = new Integer( taskId.getChild () );
        if ( m.contains ( i ) )
        {
            return true;
        }
        else
        {
            m.add ( i );
            return false;
        }
    }

    /**
     * remove map containing the TaskIds of its spawned sub-tasks
     */
    Object remove ( TaskId taskId ) { return ids.remove ( taskId ); }
    
    int size() { return ids.size(); }
}