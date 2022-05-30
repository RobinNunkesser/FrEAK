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
 *  This interface must be <CODE>implemented</CODE> by all objects
 *  that the application shares among tasks.
 *  
 *  As the interface indicates, Shared objects are immutable. Thus, Shared 
 *  objects should be either small or rarely replaced, for performance reasons.
 *
 * @version 2.0
 * @author  Peter Cappello
 */
package edu.ucsb.cs.jicos.services;


abstract public class Shared implements java.io.Serializable 
{
    /** Returns the shared Object.
     * <I>Its implementation must be synchronized</I>.
     * @return Returns the shared Object.
     */    
     abstract public Object get();
    
     /** Is this Shared object newer than the argument Shared object.
      * <I>Its implementation must be synchronized</I>.
      * @param shared The <CODE>Shared Object</CODE> that is proposed as being 
      * "newer": The method operationally defines the meaning of "newer".
      * @return true if and only if the argument Shared object as newer.
      */    
     abstract public boolean isNewerThan( Shared shared );
     
     public Shared xml2Shared( String xml ) { return null; }
}
