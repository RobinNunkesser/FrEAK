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
 * Demonstrates a non-Java client invoking a Jicos Task, in this case, the
 * Fibonacci sequence.
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services.fibonacci;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.tasks.*;
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.XmlDocument;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.services.CollectorHttp;
import edu.ucsb.cs.jicos.services.external.services.ExternalRequestProcessor;

import org.w3c.dom.Document;

final public class Fibonacci extends Task implements XmlConverter
{
    private static final int DEFAULT_Number = 8;

    private static final String CRLF = "\r\n";

    private int n;

    public Fibonacci(int n)
    {
        this.n = n;
    }

    public Object execute( Environment environment )
    {
        if (n < 2)
        {
            return new Integer( 1 );

        }
        else
        {
            compute( new Fibonacci( n - 1 ) );
            compute( new Fibonacci( n - 2 ) );
            return new AddInteger();

        }
    }

    public void setN( int n )
    {
        this.n = n;
    }

    public int getN()
    {
        return (this.n);
    }

    //~~ For ExternalRequests
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public Fibonacci()
    {
        this.n = 0;
    }

    public String toXml( String prefix )
    {
        String xml = null;
        String pre = (null != prefix) ? prefix : "";

        xml = pre + "<Fibonacci>" + CRLF + pre
                + "  <n xsi:type=\"xsd:Integer\">" + this.n + "</n>" + CRLF
                + pre + "</Fibonacci>" + CRLF;

        return (xml);
    }

    public boolean fromXml( ExternalData externalData )
    {
        boolean success = false;
        String n = null;

        if (null != (n = externalData.getValue( "/Fibonacci/n" )))
        {
            try
            {
                setN( Integer.parseInt( n ) );
                success = true;
            }
            catch (NumberFormatException formatException)
            {
            }
        }

        return (success);
    }

    public Object createInput( ExternalData externalData )
    {
        return ((Object) null);
    }

    public Shared createShared( ExternalData externalData )
    {
        return ((Shared) null);
    }

    public XmlDocument createResult( Object result ) throws Exception
    {
        String xml = null;

        if (null == result)
        {
            throw new NullPointerException( "Result cannot be null" );

        }
        else
        {
            int FofN = ((Integer) result).intValue();

            xml = "<?xml version=\"1.1\" encoding=\"UTF-8\" ?>" + CRLF
                    + "<ExternalResponse>" + CRLF + "  <Fibonacci>" + CRLF
                    + "    <n xsi:type=\"xsd:Integer\">" + this.n + "</n>"
                    + CRLF + "    <FofN xsi:type=\"xsd:Integer\">" + FofN
                    + "</FofN>" + CRLF + "  </Fibonacci>" + CRLF
                    + "</ExternalResponse>" + CRLF + "";
        }

        return (new XmlDocument( xml ));
    }

    public Document getStyleSheet( int styleSheetType )
    {
        return null;
    }

    public String toHtmlString( XmlDocument xmlResult, String hostPort )
    {
        //
        String strFib = null;

        // Fix the hostname/port.
        if (null == hostPort)
            hostPort = "localhost";

        // Get the result if it exists.
        if (null != xmlResult)
        {
            strFib = xmlResult.getValue( "/ExternalResponse/Fibonacci/FofN" );
            if ("".equals( strFib ))
            {
                strFib = null;
            }
        }

        int number = this.n;
        if (0 >= number)
        {
            number = DEFAULT_Number;
        }

        String html = "<HTML>\r\n" + "<HEAD>\r\n"
                + "  <TITLE>Fibonacci Solver</TITLE>\r\n" + "</HEAD>\r\n"
                + "<BODY>\r\n" + CollectorHttp.jicosHtmlHeader()
                + "<CENTER><H2>Fibonacci Solver</H2></CENTER>\r\n" + "\r\n";

        // If there is an answer, format it.
        if (null != strFib)
        {
            html = html + "<!-- Answer from previous request. -->\r\n"
                    + "<!>\r\n"
                    + "<FONT COLOR=green>Previous Result:</FONT><BR>\r\n"
                    + "<BLOCKQUOTE>\r\n" + "<TABLE>\r\n" + "  <TR>\r\n"
                    + "    <TD VALIGN=\"center\">Fibonacci( <B>" + this.n
                    + "</B> )&nbsp;=&nbsp;</TD>\r\n"
                    + "    <TD VALIGN=\"center\"><FONT COLOR=blue SIZE=\"+2\">"
                    + strFib + "</FONT></TD>\r\n" + "  </TR>\r\n"
                    + "</TABLE>\r\n" + "</BLOCKQUOTE>\r\n" + "<BR>\r\n";
        }

        // A request for a Fibonacci number.
        html = html
                + "\r\n"
                + "<!-- Ask for new number (start form). -->\r\n"
                + "<!>\r\n"
                + "<BR><BR>\r\n"
                + "<FORM ACTION=\"http://"
                + hostPort
                + ExternalRequest.TOP_LEVEL
                + "\" METHOD=\"post\">\r\n"
                + "<INPUT TYPE=\"hidden\"\r\n"
                + "       NAME=\""
                + ExternalRequestProcessor.TASKNAME_ATTRIBUTE
                + "\"\r\n"
                + "       VALUE=\""
                + this.getClass().getName()
                + "\"></INPUT>\r\n"
                + "<CENTER>\r\n"
                + "<TABLE>\r\n"
                + "  <TR>\r\n"
                + "    <TD BGCOLOR=\"black\">\r\n"
                + "      <TABLE BORDER=0 CELLPADDING=\"3px\" BGCOLOR=\"#EEEEEE\">\r\n"
                + "        <TR>\r\n"
                + "          <TD WIDTH=\"100\" ALIGN=\"right\"><FONT COLOR=\"blue\" SIZE=\"+1\">Number:</FONT></TD>\r\n"
                + "          <TD><INPUT TYPE=\"text\" SIZE=\"5\" VALUE=\""
                + number
                + "\"\r\n"
                + "                     NAME=\""
                + ExternalRequest.TOP_LEVEL
                + "/Fibonacci/n\"></INPUT></TD>\r\n"
                + "          <TD WIDTH=\"100\" ALIGN=\"left\"><INPUT TYPE=\"submit\"></INPUT></TD>\r\n"
                + "        </TR>\r\n" + "      </TABLE>\r\n" + "    </TD>\r\n"
                + "  </TR>\r\n" + "</TABLE>\r\n" + "</CENTER>\r\n"
                + "<BR><BR>\r\n" + "<!>\r\n"
                + CollectorHttp.createResponseSelect( null ) + "<!>\r\n"
                + "</FORM>\r\n" + "<!>\r\n" + "<!-- End form. -->\r\n"
                + CollectorHttp.jicosHtmlFooter() + "</BODY>\r\n"
                + "</HTML>\r\n";

        return (html);
    }

}