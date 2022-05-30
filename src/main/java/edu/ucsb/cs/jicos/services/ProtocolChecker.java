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

/**
 * Checks the method sequence to ensure that it adheres to protocol.
 * @author Peter Cappello
 */
public final class ProtocolChecker 
{
    // state constants
    private static final int LOGGED_IN     = 0;
    private static final int NOT_LOGGED_IN = 1;
    private static final int LOW_STATE     = 0;
    private static final int HIGH_STATE    = 1;
    
    // command constants
            static final int LOGIN          = 0;
            static final int LOGOUT         = 1;
            static final int COMPUTE        = 2;
            static final int SETCOMPUTATION = 3;
            static final int GETRESULT      = 4;
            static final int IS_COMPLETE    = 5;
    private static final int LOW_COMMAND    = 0;
    private static final int HIGH_COMMAND   = 5;
    
    // action constants
    private static final int OK                  = 0;
    private static final int NOT_LOGGED_IN_ERROR = 1;
    private static final int LOGGED_IN_ERROR     = 2;
    private static final int LOGIN_ACTION        = 3;
    private static final int LOGOUT_ACTION       = 4;
    private static final int POP                 = 5;
    private static final int PUSH                = 6;
    
    
    // attribute
    private int state = NOT_LOGGED_IN;
    private int counter;
    private int[][] actionMatrix = 
    {
        //         S  T  A  T  E                     COMMAND
        // LOGGED_IN       NOT_LOGGED_IN
        { LOGGED_IN_ERROR, LOGIN_ACTION        }, // login
        { LOGOUT_ACTION,   NOT_LOGGED_IN_ERROR }, // logout
        { OK,              NOT_LOGGED_IN_ERROR }, // compute
        { PUSH,            NOT_LOGGED_IN_ERROR }, // setComputation 
        { POP,             NOT_LOGGED_IN_ERROR }, // getResult
        { OK,              NOT_LOGGED_IN_ERROR }, // isComplete   
    };
    
    String check( int command )
    {
        assert state   >= LOW_STATE   && state   <= HIGH_STATE;
        assert command >= LOW_COMMAND && command <= HIGH_COMMAND;
        
        String signal = null;
        int actionIndex = actionMatrix[ command ] [ state ];
        switch ( actionIndex )
        {
            case OK:
                break;
                
            case NOT_LOGGED_IN_ERROR:
                signal = "Not logged in.";
                break;
                
            case LOGGED_IN_ERROR:
                signal = "Already logged in.";
                break;
                
            case LOGIN_ACTION:
                state = LOGGED_IN;
                break;
                
            case LOGOUT_ACTION:
                state = NOT_LOGGED_IN;
                break;
                
            case POP:
                if ( counter > 0 )
                {
                    counter--;
                }
                else
                {
                    signal = "No computation requested ==> no Result to get.";
                }
                break;
                
            case PUSH:
                counter++;
                break;
                
            default:
                assert false; // should never occur
        }        
        assert state   >= LOW_STATE   && state   <= HIGH_STATE;
        assert command >= LOW_COMMAND && command <= HIGH_COMMAND;
        
        return signal;
    }
}