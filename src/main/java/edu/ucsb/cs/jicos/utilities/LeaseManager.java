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
 * A Thread-safe lease manager.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.utilities;


public abstract class LeaseManager extends Thread 
{
    private long term;
    private long expiration;
    private long remainingTime;
    
    public LeaseManager( long term ) 
    {
        this.term = term;
        renew();
        start();
    }
    
    abstract public void evict();
    
    abstract public boolean offerRenewal();
    
    public synchronized void renew()
    {
        expiration = System.currentTimeMillis() + term;
        remainingTime = term;
    }
    
    public void run()
    {
        while ( true )
        {
            try
            {
                sleep ( remainingTime );
            }
            catch ( InterruptedException ignore ) {}
            synchronized ( this )
            {
                remainingTime = expiration - System.currentTimeMillis();
                if ( remainingTime < 0 && ! offerRenewal() )
                {
                    evict();
                    break; // kill thread
                }
            }            
        }
    }
}
