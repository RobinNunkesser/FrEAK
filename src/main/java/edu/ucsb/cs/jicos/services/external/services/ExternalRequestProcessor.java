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

/*
 * ExternalRequestProcessorImpl.java
 *
 * Created on July 6, 2004, 11:07 AM
 */

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.Client2Hsp;
import edu.ucsb.cs.jicos.services.ClientToHsp;
import edu.ucsb.cs.jicos.services.Environment;
import edu.ucsb.cs.jicos.services.HspAgent;
import edu.ucsb.cs.jicos.services.Invoice;
import edu.ucsb.cs.jicos.services.Result;
import edu.ucsb.cs.jicos.services.ResultId;
import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.ExternalRequestId;
import edu.ucsb.cs.jicos.services.external.ExternalResult;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.XmlConverter;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.RemoteException;

/**
 * 
 * @author Andy Pippin and Pete Cappello
 */
public class ExternalRequestProcessor extends Thread
{
    //
    //-- Constants -----------------------------------------------------------

    /** Verify that the class specified is actually a Task? */
    private static final boolean VERIFY_TASK = false;

    /** The name of the attribute defining the task. */
    public static final String TASKNAME_ATTRIBUTE = "/ExternalRequest/@taskName";

    private static final Logger logger = LogManager.getLogger();
    private static final Level DEBUG = LogManager.DEBUG;

    //
    //-- Variables -----------------------------------------------------------

    /** The current Host Service Provider. */
    private Client2Hsp hsp;

    //
    //-- Inner Classes -------------------------------------------------------

    private class NewTask
    {
        Task task;
        boolean success;
        
        public NewTask( Task task, boolean success )
        {
            this.task = task;
            this.success = success;
        }
    }

    //
    //-- Constructors --------------------------------------------------------

    /**
     * Construct a Request Translator unassociated with any particular HSP.
     */
    protected ExternalRequestProcessor()
    {
        this.initialize();
    }

    /**
     * Construct a Request Translator associated with a particular HSP.
     * 
     * @param hsp
     *            Which HSP all requests will be sent to.
     * @throws java.rmi.RemoteException
     *             If the HspAgent cannot get a reference to the Hsp.
     */
    protected ExternalRequestProcessor(ClientToHsp hsp)
            throws java.rmi.RemoteException
    {
        this.initialize();
        this.setHsp( hsp );
    }

    private void initialize()
    {
        this.hsp = null;
    }

    //
    //-- Accessors -----------------------------------------------------------

    /**
     * Return the HSP.
     */
    protected Client2Hsp getHsp()
    {
        return (this.hsp);
    }

    //
    //-- Mutators ------------------------------------------------------------

    /**
     * Define the HSP.
     */
    protected void setHsp( ClientToHsp hsp ) throws java.rmi.RemoteException
    {
        HspAgent hspAgent = new HspAgent( hsp );
        this.hsp = hspAgent.getClient2Hsp();
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Takes external data (DOM, list, etc.), and creates a task object.
     * 
     * @param externalData
     *            External data (may not be <CODE>null</CODE>).
     * @return  A NewTask object - the task and success.
     * @throws Exception
     *             Wrapper around all other types of exceptions.
     */
    private NewTask createTask( ExternalData externalData )
            throws Exception
    {
        NewTask newTask = null;
        String className = externalData.getValue( TASKNAME_ATTRIBUTE );

        if (null != className)
        {

            // Throws a variety of exceptions.
            Class taskClass = Class.forName( className );

            // Verify that this task implements utilities.xml.XmlConverter
            boolean classOkay = true;
            if (VERIFY_TASK)
            {
                classOkay = false;
                Class[] interfaceList = taskClass.getInterfaces();
                final String requiredClass = XmlConverter.class.getName();
                int numInterfaces = interfaceList.length;

                for (int i = 0; i < numInterfaces && !classOkay; ++i)
                {
                    if (requiredClass.equals( interfaceList[i].getName() ))
                        classOkay = true;
                }

                // Check the methods?
            }

            if (classOkay)
            {
                // throws a variety of exceptions.
                Task task = (Task) taskClass.newInstance();
                ExternalData nakedExternalData = externalData.removeWrapper();

                boolean success = ((XmlConverter)task).fromXml( nakedExternalData );
                
                newTask = new NewTask( task, success );
            }
        }

        return (newTask);
    }

    /**
     * Takes an XML-encoded string, creates the appropriate Task, Input, and
     * Shared objects.
     * 
     * @param externalData
     *            XML encoded request.
     * @return The external request identifier.
     * @throws java.rmi.RemoteException
     *             Because it's an RMI method.
     */
    protected ExternalRequest makeRequest( ExternalData externalData )
            throws RemoteException
    {
        ExternalRequest externalRequest = null;

        if (null == externalData)
        {
            String errMsg = "externalRequest is null";
            throw new RemoteException( errMsg,
                    new NullPointerException( errMsg ) );
        }

        try
        {
            NewTask  newTask = null;
            Task task = null;
            externalRequest = new ExternalRequest();

            // Get the task.
            //
            if (null != (newTask = createTask( externalData )))
            {
                task = newTask.task;
                XmlConverter xmlConverter = (XmlConverter)task;
                
                externalRequest.setIsReady( newTask.success );
                externalRequest.setTask( task );

                if( newTask.success )
                {
	                String msg = "Creating: \"" + task.getClass().getName() + "\"";
	                logger.log( LogManager.INFO, msg );
	
	                externalRequest.setRequestId( new ExternalRequestId() );
	
	                // Create the shared and input.
	                externalRequest.setInput( xmlConverter.createInput( externalData ) );
	                externalRequest.setShared( xmlConverter.createShared( externalData ) );
                }
            }

        }
        catch (Exception anyException)
        {
            throw new RemoteException( anyException.getMessage(), anyException );
        }

        return (externalRequest);
    }

    /**
     * See if the requested result is available.
     * 
     * @param externalRequestId
     *            The result to look for.
     * @return The result of the computation.
     * @throws java.rmi.RemoteException
     *             Because it's an RMI method.
     */
    protected boolean checkForResult( ExternalRequestId externalRequestId )
    {
        boolean resultExists = false;
        // TODO aBp: ExternalReuqestProcessor.checkForResult() should return
        // something useful.
        return (resultExists);
    }

    /**
     * Waits for an external request, and then returns it.
     * 
     * @param externalRequest
     *            Which request to look for.
     * @return The result identifier.
     * @throws java.rmi.RemoteException
     *             Because it's an RMI method.
     */
    protected ExternalResult waitForResult( ExternalRequest externalRequest )
            throws RemoteException
    {

        ExternalResult externalResult = null;

        try
        {
            externalResult = new ExternalResult( externalRequest );
            String taskName = externalRequest.getTaskName();

            // login
            Environment environment = new Environment( externalRequest
                    .getInput(), externalRequest.getShared() );
            hsp.login( environment );

            // Create a task name.
            logger.log( LogManager.INFO, "Starting task: \"" + taskName + "\"" );

            // compute
            Task task = externalRequest.getTask();
            ResultId resultId = hsp.setComputation( task );
            externalResult.setResultId( resultId );
            //
            Result computeResult = hsp.getResult(); // blocks
logger.log( DEBUG, "compute Result = \"" + computeResult + '"' );
            externalResult.setResult( computeResult );
            logger.log( LogManager.INFO, "Finished task: \"" + taskName + "\"" );

            // logout
            Invoice invoice = hsp.logout();
            externalResult.setInvoice( invoice );
        }

        catch (Exception exception)
        {
            throw new java.rmi.RemoteException( "Error translating request.",
                    exception );
        }

        return (externalResult);
    }

}
