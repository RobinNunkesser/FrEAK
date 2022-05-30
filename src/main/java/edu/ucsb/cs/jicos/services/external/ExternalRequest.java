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
 *  A container for a Jicos Non-Java Request (JNJR).
 *
 *  Created on July 7, 2004
 *
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external;

import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.external.services.CollectorResponse;


public class ExternalRequest {
	
	//
	//-- Constants -----------------------------------------------------------

	/** First path component of the request. */
	public static final String TOP_LEVEL = "/ExternalRequest";

	// Common parts of the following values.
	private static final String ON_RESPONSE = TOP_LEVEL + "/OnResponse";
	private static final String ACTION_PARAM = "/@action";
	private static final String EMAIL_ELEMENT = "/EMailAddr";
	private static final String RESPONSE_Respond = ON_RESPONSE + "/Respond";
	
	/** Identier for an unknown item. */
	public static final int ITEM_Unknown = -1;
	
	/** XPath selector for the response action value. */
	public static final String RESPONSE_Action = RESPONSE_Respond + ACTION_PARAM;
	
	/** XPath selector for the response email value. */
	public static final String RESPONSE_EMail = RESPONSE_Respond + EMAIL_ELEMENT;
	
	/** List of actions to be taken to get the answer. */
	public static final String[] RESPONSE_ActionList = { "wait for answer", "poll by web" };//, "by email" };
	
	/** Index for the immediate (wait for an answer) action. */
	public static final int ACTION_Immediate = 0;
	
	/** Index for the delayed by web action. */
	public static final int ACTION_ByWeb = 1;
	
	/** Index for the delayed by email action.  */
	public static final int ACTION_ByEMail = 2;
	
	/** Value of the immediate (wait for an answer) action. */
	public static final String RESPONSE_Immediate = RESPONSE_ActionList[ ACTION_Immediate ];
	
	/** Value of the delayed by web action. */
	public static final String RESPONSE_ByWeb = RESPONSE_ActionList[ ACTION_ByWeb ];
	
	/** Value of the delayed by email action. */
	public static final String RESPONSE_ByEMail = "";
				// RESPONSE_ActionList[ ACTION_ByEMail ];

	//
	//-- Variables -----------------------------------------------------------

	private Task task;
	private Object input;
	private Shared shared;
	private ExternalRequestId externalRequestId;
	private CollectorResponse collectorResponse;
	private String taskName;
	private boolean isReady;
	
	//
	//-- Constructors --------------------------------------------------------

	/**
     * An external request.
     */
    public ExternalRequest()
    {
        this.Initialize();
    }

    /**
     * An external request.
     * 
     * @param input
     *            The Jicos Input object.
     * @param shared
     *            The Jicos shared object.
     * @param task
     *            The Jicos task.
     */
    public ExternalRequest(Object input, Shared shared, Task task)
    {
        this.Initialize();

        this.task = task;
        this.input = input;
        this.shared = shared;
        this.externalRequestId = new ExternalRequestId();
        this.isReady = true;
    }

    /**
     * Copy constructor.
     * 
     * @param externalRequest
     *            An external request.
     */
    public ExternalRequest(ExternalRequest externalRequest)
    {
        this.Initialize();

        this.task = externalRequest.task;
        this.input = externalRequest.input;
        this.shared = externalRequest.shared;
        this.externalRequestId = externalRequest.externalRequestId;
        this.collectorResponse = externalRequest.collectorResponse;
        this.isReady = externalRequest.isReady;
    }

    /**
     * Initialize this instance of ExternalRequest.
     */
    private void Initialize()
    {
        this.task = null;
        this.input = null;
        this.shared = null;
        this.externalRequestId = null;
        this.collectorResponse = null;
        this.setTaskName();
        this.isReady = false;
    }

    //
    //-- Accessors -----------------------------------------------------------

    /**
     * Return the task.
     */
    public Task getTask()
    {
        return (this.task);
    }

    /**
     * Return the task name.
     */
    public String getTaskName()
    {
        return (this.taskName);
    }

    /**
     * Return the input object.
     */
    public Object getInput()
    {
        return (this.input);
    }

    /**
     * Return the shared object.
     */
    public Shared getShared()
    {
        return (this.shared);
    }

    /**
     * Return the external request identifier.
     */
    public ExternalRequestId getExternalRequestId()
    {
        return (this.externalRequestId);
    }

    /**
     * Return the response from the collector.
     */
    public CollectorResponse getCollectorResponse()
    {
        return (this.collectorResponse);
    }

    /**
     *  Return the status.
     */
    public boolean isReady()
    {
        return( this.isReady );
    }
    
    //
    //-- Mutators ------------------------------------------------------------

    /**
     * Set the task.
     */
    public void setTask( Task task )
    {
        this.task = task;
        this.setTaskName();
    }

    /**
     * Set the input object.
     */
    public void setInput( Object input )
    {
        this.input = input;
    }

    /**
     * Set the Shared object.
     */
    public void setShared( Shared shared )
    {
        this.shared = shared;
    }

    /**
     * Set the request id.
     */
    public void setRequestId( ExternalRequestId requestId )
    {
        this.externalRequestId = requestId;
        this.setTaskName();
    }

    /**
     * Set the collector's response.
     */
    public void setCollectorResponse( CollectorResponse collectorResponse )
    {
        this.collectorResponse = collectorResponse;
    }

    /**
     * Set the name of the task from the task.  Leading "edu.ucsb.cs.jicos"
     * will be replaced by "...jicos"
     */
    private void setTaskName()
    {
        this.taskName = null;

        if (null != this.task)
        {
            String name = null;

            name = task.getClass().getName();
            name = name.replaceFirst( "edu.ucsb.cs.jicos", "...jicos" );

            if (null != externalRequestId)
            {
                name += ":" + externalRequestId.getId();
            }

            this.taskName = name;
        }
    }
	
    public void setIsReady( boolean isReady )
    {
        this.isReady = isReady;
    }
}
