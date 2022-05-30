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
 * TaskId is immutable.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;


final public class TaskId implements java.io.Serializable, Comparable
{
    //public long serialVersionUID = 
    private long sessionId;
    private short computationId;
    private short[] path;
    
    TaskId( long sessionId, short computationId )
    {
        // pre-conditions
        assert sessionId != 0;
        
        this.sessionId = sessionId;
        this.computationId = computationId;
        path = new short[0];
    }

    TaskId( TaskId parent, short child$ )
    {
        sessionId = parent.sessionId;
        computationId = parent.computationId;
        path = new short[ parent.path.length + 1 ];
        
        // copy parent portion of path
        System.arraycopy( parent.path, 0, path, 0, parent.path.length );
        
        // append child number to path
        path[ path.length - 1 ] = (short) child$;
    }

    TaskId( TaskId taskId )
    {
        this.sessionId = taskId.sessionId;
        this.computationId = taskId.computationId;
        this.path = new short[ taskId.path.length ];
        System.arraycopy( taskId.path, 0, path, 0, taskId.path.length );
    }

    //   Used by getParentId
    TaskId( long sessionId, short computationId, short[] path )
    {
        // pre-condition
        assert sessionId != 0;
        assert path != null;
        
        this.sessionId = sessionId;
        this.computationId = computationId;
        this.path = path;
    }
    
    public int compareTo( Object obj ) 
    {
        if ( ! ( obj instanceof TaskId ) )
        {
            throw new IllegalArgumentException();
        }
        TaskId tid = (TaskId) obj;
        if ( sessionId < tid.sessionId )
        {
            return -1;
        }
        else if ( sessionId > tid.sessionId )
        {
            return 1;
        }
        else if ( computationId < tid.computationId )
        {
            return -1;
        }
        else if ( computationId > tid.computationId )
        {
            return 1;
        }
        else if ( path.length < tid.path.length )
        {
            return -1;
        }
         else if ( path.length > tid.path.length )
        {
            return 1;
        }
        else
        {
            for ( int i = 0; i < this.path.length; i++ )
            {
                if ( path[i] < tid.path[i] )
                {
                    return -1;
                }
                if ( path[i] > tid.path[i] )
                {
                    return 1;
                }
            }
            return 0;
        }
    }
    
    boolean computationEquals( TaskId taskId )
    {
        return sessionId == taskId.sessionId && computationId == taskId.computationId;
    }
    
    public boolean equals( Object tid )
    {
        TaskId taskId;
        
        if ( ! ( tid instanceof TaskId ) )
        {
            //return false;
            throw new IllegalArgumentException();
        }
        else
        {
            taskId = (TaskId) tid;
        }
        if ( path.length != taskId.path.length )
        {
            return false;
        }
        for ( int i = 0; i < path.length; i++ )
        {
            if ( path[i] != taskId.path[i] )
            {
                return false;
            }
        }
        if ( sessionId == taskId.sessionId && 
             computationId == taskId.computationId 
           )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int hash = 0;
        for ( int i = 0; i < path.length; i++ )
        {
            //hash += path[i];
            hash = 2 * hash + path[i];

        }
        return hash;
    }
    
    /**
     * isAncestor is a partial order: reflexive, anti-symmetric, & transitive.
     * @return true iff this is an ancestor of parameter taskId
     */
    boolean isAncestor( TaskId taskId )
    {
        if ( path.length > taskId.path.length )
        {
            return false;
        }
        if ( sessionId != taskId.sessionId )
        {
            return false;
        }
        if ( computationId != taskId.computationId )
        {
            return false;
        }
        for ( int i = 0; i < path.length; i++ )
        {
            if ( path[i] != taskId.path[i] )
            {
                return false;
            }
        }
        return true;       
    }
    
    int getComputationId() { return computationId; }

    int getChild() { return path.length == 0 ? -1 : (int) path[path.length -1];}

    TaskId getParentId()
    {
        if ( path.length == 0 )
        {
            return new TaskId( sessionId, computationId );
        }        
        short[] parentTaskId = new short[path.length - 1];
        System.arraycopy( path, 0, parentTaskId, 0, parentTaskId.length );	
        return new TaskId ( sessionId, computationId, parentTaskId );
    }
    
    long getSessionId() { return sessionId; }
        
    public String toString ()
    {
        StringBuffer s = new StringBuffer().append( sessionId );
        s.append(".").append( computationId );
        for ( int i = 0; i < path.length; i++ )
        {
            s.append(".").append( path[i] );
        }
        return new String ( s );
    }
    
    public static void main( String[] args )
    {
        TaskId ancestor = new TaskId( 1, (short) 1 );
        System.out.println("ancestor: " + ancestor );
        TaskId child = new TaskId( 1, (short) 1 );
        System.out.println("child: " + child );
        System.out.println("ancestor is ancestor of child: " + ancestor.isAncestor( child ));
        System.out.println("child is ancestor of ancestor: " + child.isAncestor( ancestor ));
        child = new TaskId( child, (short) 1);
        System.out.println("child: " + child );
        System.out.println("ancestor is ancestor of child: " + ancestor.isAncestor( child ));
        System.out.println("child is ancestor of ancestor: " + child.isAncestor( ancestor ));
        ancestor = new TaskId( ancestor, (short) 1 );
        child = new TaskId( child, (short) 1);
        System.out.println("ancestor: " + ancestor );
        System.out.println("child: " + child );
        System.out.println("ancestor is ancestor of child: " + ancestor.isAncestor( child ));
        System.out.println("child is ancestor of ancestor: " + child.isAncestor( ancestor ));
        ancestor = new TaskId( ancestor, (short) 2 );
        System.out.println("ancestor: " + ancestor );
        System.out.println("child: " + child );
        System.out.println("ancestor is ancestor of child: " + ancestor.isAncestor( child ));
        System.out.println("child is ancestor of ancestor: " + child.isAncestor( ancestor ));
        
    }
}
