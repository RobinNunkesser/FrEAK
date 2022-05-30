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
 * description
 *
 * @author  pippin
 */

package edu.ucsb.cs.jicos.admin;

public final class CommandLine
{
    public String commandName;
    public String croppedCommand;
    public Command command;
    public int minimum;

    public CommandLine(String commandName, Command command)
    {
        assert null != commandName : "command name cannot be null";
        assert null != command : "command cannot be null";

        this.commandName = commandName;
        this.command = command;
        this.minimum = -1;
        this.croppedCommand = this.commandName.toLowerCase();
    }

    public CommandLine(String commandName, Command command, int minimum)
    {
        assert null != commandName : "command name cannot be null";
        assert null != command : "command cannot be null";

        this.commandName = commandName;
        this.command = command;
        this.minimum = Math.min( commandName.length(), minimum );
        this.croppedCommand = this.commandName.substring( 0, this.minimum )
                .toLowerCase();
    }

    public boolean equals( String cmdLine0 )
    {
        boolean equals = false;
        if (null != cmdLine0)
        {
            int len = cmdLine0.length();
            if (0 > this.minimum)
            {
                equals = this.croppedCommand.equals( cmdLine0 );
            }
            else if (len >= this.minimum)
            {
                equals = this.croppedCommand.equals( cmdLine0.substring( 0,
                        this.minimum ) );
            }
        }
        return (equals);
    }

    public int invoke( String[] cmdLine ) throws Exception
    {
        return (this.command.invoke( cmdLine ));
    }
}