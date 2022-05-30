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
 * Administration interface.
 *
 * @author  Andy Pippin
 */

package edu.ucsb.cs.jicos.foundation;

import edu.ucsb.cs.jicos.services.ServiceTaskStats;

public class CurrentState {
    //
    //-- Constants -------------------------------------------------------
    
    public static final short  STATUS_Unknown = (short)0;
    public static final short  STATUS_Inactive = (short)1;
    public static final short  STATUS_Active = (short)2;
    
    //
    //-- Variables -------------------------------------------------------
    
    public short  currentStatus;
    public Class  currentActiveClass;
    public ServiceTaskStats serviceTaskStats;
    
    //
    //-- Constructor -----------------------------------------------------
    
    private CurrentState() {
        this.currentStatus = STATUS_Unknown;
        this.currentActiveClass = null;
        this.serviceTaskStats = null;
    }
    
    /**
     * Create a string out this instance.
     */
    public String toString() {
        String[] nameArray = getClass().getName().replace( '.', '\u3456' ).split( "\u3456" );
        StringBuffer result = new StringBuffer( nameArray[ nameArray.length-1 ] );
        
        result.append( '[' );
        result.append( "currentStatus=" + statusName(currentStatus) + "("+currentStatus+")" );
        
        result.append( ',' );
        result.append( "currentActiveClass=" );
        if( null != this.currentActiveClass ) {
            result.append( this.currentActiveClass.getClass().getName() );
        }
        
        result.append( ',' );
        if( null != this.serviceTaskStats ) {
            result.append( this.serviceTaskStats.toString() );
        }
        
        result.append( ']' );
        return( result.toString() );
    }
    
    /**
     * Get the name of a status code.
     * 
     * @param statusCode  The code to identify.
     * @return  The name of the code, if known.
     */
    public static String statusName( short statusCode ) {
        String statusName = null;
        
        switch( statusCode ) {
            case  STATUS_Unknown:
                statusName = "STATUS_Unknown";
                break;
                
            case  STATUS_Inactive:
                statusName = "STATUS_Inactive";
                break;
                
            case  STATUS_Active:
                statusName = "STATUS_Active";
                break;

            default:
				statusName = "unknown status code (" + String.valueOf( statusCode ) + ")";
				break;
        }
        
        return( statusName );
    }
    
}
