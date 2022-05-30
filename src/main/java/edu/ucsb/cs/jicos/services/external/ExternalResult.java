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
 * The result of a RequestTranslator. This is really a container class for all
 * the data that a Converter may need to have in order to respond to a client.
 * 
 * Created on July 7, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external;

import edu.ucsb.cs.jicos.services.Invoice;
import edu.ucsb.cs.jicos.services.Result;
import edu.ucsb.cs.jicos.services.ResultId;
import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

public class ExternalResult implements java.io.Serializable
{
    //
    //-- Variables -----------------------------------------------------------

    private Task task;
    private Shared shared;
    private Object input;
    private ExternalRequestId externalRequestId;
    private ResultId resultId;
    private Result result;
    private Invoice invoice;
    private Throwable throwable;

    //
    //-- Constructors --------------------------------------------------------

    /**
     * Default constructor.
     */
    public ExternalResult()
    {
        this.initialize();
    }

    /**
     * Copy constructor.
     * 
     * @param externalResult
     *            Instance to copy.
     */
    public ExternalResult(ExternalResult externalResult)
    {
        this.initialize();

        this.setTask( externalResult.getTask() );
        this.setShared( externalResult.getShared() );
        this.setInput( externalResult.getInput() );
        this.setExternalRequestId( externalResult.getExternalRequestId() );
        this.setResultId( externalResult.getResultId() );
        this.setResult( externalResult.getResult() );
        this.setInvoice( externalResult.getInvoice() );
        this.setThrowable( externalResult.getThrowable() );
    }

    /**
     * Helper method - Create an ExternalResult from an ExternalRequest.
     * 
     * @param externalRequest
     *            An external request.
     */
    public ExternalResult(ExternalRequest externalRequest)
    {
        this.initialize();

        this.setTask( externalRequest.getTask() );
        this.setInput( externalRequest.getInput() );
        this.setShared( externalRequest.getShared() );
        this.setExternalRequestId( externalRequest.getExternalRequestId() );
    }

    // Initialize member variables.
    private void initialize()
    {
        this.task = null;
        this.shared = null;
        this.input = null;
        this.externalRequestId = null;
        this.resultId = null;
        this.result = null;
        this.invoice = null;
        this.throwable = null;

        return;
    }

    //
    //-- Accessors -----------------------------------------------------------

    /** Return the task. */
    public Task getTask()
    {
        return (this.task);
    }

    /** Return the shared object. */
    public Shared getShared()
    {
        return (this.shared);
    }

    /** Return the input object. */
    public Object getInput()
    {
        return (this.input);
    }

    /** Return the external (collector's) requestId. */
    public ExternalRequestId getExternalRequestId()
    {
        return (this.externalRequestId);
    }

    /** Return the Jicos resultId. */
    public ResultId getResultId()
    {
        return (this.resultId);
    }

    /** Return the result of the Jicos computation. */
    public Result getResult()
    {
        return (this.result);
    }

    /** Return the Jicos invoice. */
    public Invoice getInvoice()
    {
        return (this.invoice);
    }

    /** Return the exception, if any. */
    public Throwable getThrowable()
    {
        return (this.throwable);
    }

    //
    //-- Mutators ------------------------------------------------------------

    /** Set the task for this result. */
    public void setTask( Task task )
    {
        this.task = task;
    }

    /** Set the shared object for this result. */
    public void setShared( Shared shared )
    {
        this.shared = shared;
    }

    /** Set the input for this result. */
    public void setInput( Object input )
    {
        this.input = input;
    }

    /** Set the external request id for this result. */
    public void setExternalRequestId( ExternalRequestId externalRequestId )
    {
        this.externalRequestId = externalRequestId;
    }

    /** Set the Jicos requestId for this result. */
    public void setResultId( ResultId resultId )
    {
        this.resultId = resultId;

        if (null != this.externalRequestId)
        {
            this.externalRequestId.setResultId( resultId );
        }
    }

    /** Set the result of the Jicos computation for this result. */
    public void setResult( Result result )
    {
        this.result = result;
    }

    /** Set the Jicos invoice for this result. */
    public void setInvoice( Invoice invoice )
    {
        this.invoice = invoice;
    }

    /** Set the exception for this result. */
    public void setThrowable( Throwable throwable )
    {
        this.throwable = throwable;
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Set the email address to send the result to&#x2e;&nbsp;&nbsp;(Needs to be
     * implemented!!!)
     */
    public void mailTo( String eMailAddress ) throws Exception
    {
        // TODO Determine how to use the mailer.
        return;
    }

    /**
     * Convert an external result to HTML text.
     * 
     * @param hostPort
     *            The host:port of the server.
     * @return The HTML encoded text.
     * @throws Exception
     *             Because Task.createResult can throw this.
     */
    public String toHtmlString( String hostPort ) throws Exception
    {
        String html = null;

        // Preliminary (error) result.
        html = "<HTML>\r\n" + "<HEAD><TITLE>Unknown Error</TITLE></HEAD>\r\n"
                + "<BODY>" + "<FONT COLOR=red SIZE=\"+2\">Error</FONT><BR>\r\n"
                + "Unknown cause\r\n" + "</BODY>\r\n" + "</HTML>\r\n";

        Task task = this.getTask();
        XmlConverter xmlConverter = (XmlConverter)task;
        Object result = this.getResult().getValue();
        XmlDocument xmlResult = xmlConverter.createResult( result );
        org.w3c.dom.Document xsltStyleSheet;
        if (null != (xsltStyleSheet = xmlConverter
                .getStyleSheet( XmlConverter.STYLESHEET_Html )))
        {
            html = xmlResult.Transform( xsltStyleSheet );
        }

        else if (null == (html = xmlConverter.toHtmlString( xmlResult, hostPort )))
        {
            // Raw Result
            html = "<HTML>\r\n"
                    + "<HEAD><TITLE>Result (raw)</TITLE></HEAD>\r\n"
                    + "<BODY>"
                    + "<FONT COLOR=green SIZE=\"+2\">Result</FONT>&nbsp;&nbsp;(raw)<BR>\r\n"
                    + "<PRE>\r\n"
                    + xmlResult.toXmlString().replaceAll( "<", "&lt;" )
                            .replaceAll( ">", "&gt;" ).replaceAll( " ",
                                    "&nbsp;" ) + "</PRE>\r\n" + "</BODY>\r\n"
                    + "</HTML>\r\n";
        }

        return (html);
    }
}