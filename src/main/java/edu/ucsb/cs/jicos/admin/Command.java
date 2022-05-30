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
 * A command in a console.
 *
 * @author  pippin
 */

package edu.ucsb.cs.jicos.admin;

public interface Command
{
    public static final int CMD_Unknown = -2;
    public static final int CMD_Error = -1;
    public static final int CMD_Okay = 0;

    /**
     * 
     * @param cmdLine  An array describing the command to perform.
     * @return Success (<CODE>CMD_Okay</CODE>), or failure.
     * @throws Exception  Catches a variety of sins.
     */
    public int invoke( String[] cmdLine ) throws Exception;

    /**
     * Return a short description of the command.
     * 
     * @return A one-line description of the command.
     */
    public String getDescription();

    /**
     * Return a long description of the command.
     * 
     * @return  A multiline (probably) description of the command.
     */
    public String getHelp();
}
