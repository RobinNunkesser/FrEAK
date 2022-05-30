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

/*
 * Matlab.java
 *
 * Created on December 21, 2004, 3:02 PM
 */

package edu.ucsb.cs.jicos.services.external.services.matlab;

import java.util.Map;

/**
 * 
 * @author Peter Cappello
 */
public interface Matlab {
    /**
     * Evaluate a matlab string. In order to make this very generic, and to fit
     * the Matlab API, the name of the variable containing the "answer" must be
     * specified. For example: <BR>
     * &nbsp; &nbsp;
     * 
     * <PRE>
     * 
     * Map data = new HashMap();
     * 
     * </PRE>
     * 
     * <BR>
     * &nbsp; &nbsp;
     * 
     * <PRE>
     * 
     * data.add( "t", new Integer( 2 ) );
     * 
     * </PRE>
     * 
     * <BR>
     * <BR>
     * &nbsp; &nbsp;
     * 
     * <PRE>
     * 
     * Object result = evaluate( "A = randn[1:5] * t", "A", data );
     * 
     * </PRE>
     * 
     * <BR>
     * will create a value <EM>t</EM>, assign the value <EM>2</EM> to it,
     * put it into the Matlab engine, and then create a vector of 5 random
     * numbers, and multiply each by 2, then get the answer in the variable <EM>
     * A</EM>, and return that.
     * 
     * @param command
     *            The command to be invoked
     * @param resultName
     *            The name of the variable containing the result.
     * @param variableNameValueMap
     *            All supporting data for the computation.
     * @param flushVariables
     *            Delete the variables at the end of the computation.
     * @return The result of the Matlab computation (most likely a double[][]).
     * @throws MatlabException
     *             On error in the engine.
     */
    public Object evaluate( String command, String resultName,
            Map variableNameValueMap, boolean flushVariables )
			throws MatlabException;

    /**
     *  Helper method for above - automatically flushes variables.
     *
     * @param command
     *            The command to be invoked
     * @param resultName
     *            The name of the variable containing the result.
     * @param variableNameValueMap
     *            All supporting data for the computation.
     * @return The result of the Matlab computation (most likely a double[][]).
     * @throws MatlabException
     *             On error in the engine.
     */
    public Object evaluate( String command, String resultName,
            Map variableNameValueMap ) throws MatlabException;

    /**
     *  Retrieve a variable from the Matlab engine.
     * 
     * @param varName  The name of the variable to retrieve.
     * @return The variable, or <CODE>null</CODE> if not available.
     * @throws MatlabException
     *             On error in the engine.
     */
    public Object getVariable( String varName ) throws MatlabException;
}
