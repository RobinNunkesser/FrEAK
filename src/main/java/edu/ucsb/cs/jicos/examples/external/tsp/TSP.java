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
 * Extend the traveling sales-critter problem (TSP) beyond Java.
 * 
 * @version 1.0
 * @author Andy Pippin
 */

/*
 * TSP.java
 */

package edu.ucsb.cs.jicos.examples.external.tsp;

import edu.ucsb.cs.jicos.applications.branchandbound.BranchAndBound;
import edu.ucsb.cs.jicos.applications.branchandbound.Solution;
import edu.ucsb.cs.jicos.applications.utilities.graph.*;
import edu.ucsb.cs.jicos.examples.tsp.TspSolution;
import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;
import edu.ucsb.cs.jicos.services.external.services.CollectorHttp;
import edu.ucsb.cs.jicos.services.external.services.ExternalRequestProcessor;
import edu.ucsb.cs.jicos.services.shared.IntUpperBound;

import java.util.List;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TSP extends BranchAndBound implements XmlConverter,
        java.io.Serializable
{

    //
    //-- Constants -----------------------------------------------------------

    public static final int DEFAULT_CITIES = 5;
    public static final int MAX_EDGES = edu.ucsb.cs.jicos.examples.tsp.TSP.MAX_EDGE;
    public static final int MAX_DISTANCE = Integer.MAX_VALUE;

    //
    //-- Variables -----------------------------------------------------------

    private int cities;
    private int[][] distance;

    //
    //-- Constructors --------------------------------------------------------

    public TSP()
    {
        super( (Solution) null );

        this.cities = 0;
        this.distance = null;
    }

    // Mostly for testing.
    public TSP(int[][] distance) throws Exception
    {
        super( (Solution) null );

        if (distance.length != distance[0].length)
        {
            throw new Exception( "Distance matrix is not square ( "
                    + distance.length + " x " + distance[0].length + " )" );
        }
        else if (distance.length > MAX_EDGES)
        {
            throw new Exception( distance.length + " is too many edges (max = "
                    + MAX_EDGES + ")" );
        }

        this.cities = distance.length;
        this.distance = distance;

        return;
    }

    public void setSolution( Solution solution )
    {
        super.setSolution( solution );
    }

    public Solution getSolution()
    {
        return (super.getSolution());
    }

    //
    //== Implements XmlConverter =============================================

    public String toXml( String prefix ) throws Exception
    {
        prefix = (null == prefix) ? "" : prefix;
        final String CRLF = "\r\n";

        String msg = prefix + "<?xml version=\"1.1\" encoding=\"UTF-8\" ?>"
                + CRLF + prefix + "<TSP taskName=\""
                + this.getClass().getName() + "\" >" + CRLF + prefix
                + "  <data nodes=\"" + this.cities + "\">" + CRLF;

        for (int src = 0; src < this.cities; ++src)
        {
            msg += prefix + "    <row id=\"" + src + "\">";
            for (int dst = 0; dst < this.cities; ++dst)
            {
                if (0 != dst)
                {
                    msg += " ";
                }
                msg += String.valueOf( this.distance[src][dst] );
            }
            msg += "</row>" + CRLF;
        }
        msg += prefix + "  </data>" + CRLF + prefix + "</TSP>" + CRLF + "";

        return (msg);
    }

    public boolean fromXml( ExternalData externalData ) throws Exception
    {
        boolean success = false;
        final String topLevel = ExternalRequest.TOP_LEVEL;

        String numCities = externalData.getValue( "/TSP/@cities" );
        if (null != numCities)
        {
            try
            {
                this.cities = Integer.parseInt( numCities );

                // Create the solution and stuff it into the BranchAndBound.
                Solution emptySolution = new TspSolution( this.cities );
                super.setSolution( emptySolution );

                success = true;
            }
            catch (NumberFormatException numberFormatException)
            {
                throw new Exception( "<data nodes=\"" + numCities
                        + "\"> is not a number." );
            }
        }
        else
        {
            throw new Exception( "<data> does not specify \"cities\"!" );
        }
        
        // See if there is a new number of cities.
        boolean abortWithFalse = false;
        try
        {
            int prevCities = Integer.parseInt( externalData.getValue( "/TSP/@prevcities" ) );
            abortWithFalse = ( prevCities != this.cities );
        }
        catch( Exception exception )
        {}
        
        if( abortWithFalse )
        {                     //  *************************
            return( false );  // ***  DOES NOT CONTINUE  *** //
        }                     //  *************************

        // Make sure it is an XmlDocument.
        XmlDocument xml = null;
        if (externalData instanceof List)
        {
            xml = new XmlDocument( (List) externalData );
        }
        else if (externalData instanceof XmlDocument)
        {
            xml = (XmlDocument) externalData;
        }
        else
        {
            throw new ClassCastException( "Unknown type of ExternalData!" );
        }

        // This should be part of the createInput, but it is useful to do here,
        // since after this is created, the instance can be displayed.
        //
        // If we are good so far, then get the rest of the data.
        if (success)
        {
            this.distance = new int[this.cities][this.cities];
            // Get the data.
            Element[] rowData = xml.getElement( topLevel + "/TSP/Distance" );
            if (null == rowData)
            {
                throw new Exception( "There is no data" );
            }
            if (rowData.length != (this.cities * this.cities))
            {
                throw new Exception( "Number of cities (" + this.cities
                        + ") does not match number of rows of data ("
                        + (int) Math.sqrt( rowData.length ) + ")" );
            }
            else if (this.cities > (MAX_EDGES * MAX_EDGES))
            {
                throw new Exception( "Number of nodes (" + this.cities
                        + ") exceeds the maximum (" + MAX_EDGES + ")" );
            }

            String distStr = null;
            NodeList nodeList = null;
            Node textNode = null;
            int ndx = 0;

            // For each row of data, break it up on the whitespace.
            //
            for (int src = 0; src < this.cities; ++src)
            {
                for (int dst = 0; dst < this.cities; ++dst)
                {
                    if (null == (nodeList = rowData[ndx].getChildNodes()))
                    {
                        int err = 1;
                    }
                    else if (null == (textNode = nodeList.item( 0 )))
                    {
                        int err = 2;
                    }
                    else if (Node.TEXT_NODE == textNode.getNodeType())
                    {
                        if (null != (distStr = textNode.getNodeValue()))
                        {
                            this.distance[src][dst] = (int) Double
                                    .parseDouble( distStr );
                        }
                        else
                        {
                            throw new NullPointerException( "Distance[" + src
                                    + "][" + dst + "] was null" );
                        }
                    }

                    ++ndx;
                }
            }
        }

        return (success);
    }

    /**
     * Create the Graph representing the cities.
     * 
     * @param externalData
     *            The data from the External client.
     * @return The distance graph.
     * @throws A
     *             variety of errors.
     */
    public Object createInput( ExternalData externalData ) throws Exception
    {
        Object inputObject = null;

        if (null == this.distance)
        {
            throw new Exception( "there are no distances" );
        }

        inputObject = (Object) new edu.ucsb.cs.jicos.examples.tsp.TSP(
                new GraphImpl( distance ) );

        return ((Object) inputObject);
    }

    /**
     * The bounding value: the best tour found to date.
     * 
     * @return An integer upper bound.
     * @throws Exception
     *             Not thrown, required from interface.
     */
    public Shared createShared( ExternalData externalData ) throws Exception
    {
        return (new IntUpperBound( Integer.MAX_VALUE ));
    }

    /**
     * Convert the result to XML.
     * 
     * @param result
     *            The answer to the computation.
     * @return The XML representation of the result object.
     * @throws Exception
     *             Hiding implementation details from the user.
     */
    public XmlDocument createResult( Object result ) throws Exception
    {
        String txt;

        if (null == result)
        {
            txt = "<ExternalResponse error=\"true\">\r\n"
                    + "  <description>Result is null</description>\r\n"
                    + "</ExternalResponse>\r\n";
        }
        else if (!(result instanceof Solution))
        {
            txt = "<ExternalResponse error=\"true\">\r\n"
                    + "  <description>Result is not a BranchAndBound Solution ("
                    + result.getClass().getName() + ")</description>\r\n"
                    + "</ExternalResponse>\r\n";
        }
        else
        {
            TspSolution solution = (TspSolution) result;

            int cost = solution.getCost();
            int[] tour = solution.getTour();

            txt = "<?xml version=\"1.1\" encoding=\"UTF-8\" ?>\r\n"
                    + "<ExternalResponse>\r\n" + "  <TSP>\r\n"
                    + "    <tour cost=\"" + cost + "\">";

            for (int node = 0; node < tour.length; ++node)
            {
                if (0 != node)
                {
                    txt += " ";
                }
                txt += String.valueOf( tour[node] );
            }

            txt += "</tour>\r\n" + "  </TSP>\r\n" + "</ExternalResponse>\r\n";
        }

        return (new XmlDocument( txt ));
    }

    /**
     * Get the (non-existant) stylesheet.
     * 
     * @param stylesheetType
     *            The type of stylesheet.
     * @return The stylesheet if it exists, or <CODE>null</CODE> if not.
     */
    public Document getStyleSheet( int styleSheetType )
    {
        Document xsltStyleSheet = null;

        switch (styleSheetType)
        {
            case XmlConverter.STYLESHEET_Unknown:
            case XmlConverter.STYLESHEET_Xml:
            case XmlConverter.STYLESHEET_Html:
                break;
        }

        return ((org.w3c.dom.Document) null);
    }

	/**
	 *  Convert to an HTML string.
	 * 
	 * @param   result    The result object.
	 * @param   hostPort  The "hostname:port" of the CollectorHttp.
	 */
    public String toHtmlString( XmlDocument result, String hostPort )
    {
        final String topLevel = ExternalRequest.TOP_LEVEL;
        
        String html		
            = "<HTML>\r\n"
            + "<HEAD>\r\n"
            + "  <TITLE>TSP Solver</TITLE>\r\n"
            + "</HEAD>\r\n"
            + "<BODY>\r\n"
            + CollectorHttp.jicosHtmlHeader()
            + "<CENTER><H2>TSP Solver</H2></CENTER>\r\n"
            + "\r\n"
		    ;
        
        if( null != result )
        {
	        String previousTour = result.getValue( "/ExternalResponse/TSP/tour" );
	        String tourCost = result.getValue( "/ExternalResponse/TSP/tour/@cost" );
	        if( (null != previousTour) && (null != tourCost) )
	        {
	            html += "<CENTER><FONT COLOR=\"green\" SIZE=\"+2\">"
	                  + "Best tour</FONT><BR><BR>\r\n"
	                  ;
	            
	            String[] city = previousTour.split( " " );
	            if( 0 < city.length )
	            {
	                html += city[0];
	                for( int c=1; c < city.length; ++c )
	                {
	                    html += " &rarr; " + city[c];
	                }
	            }
	            
	            html += "<BR>\r\nCost: " + tourCost + "<BR><BR>\r\n";
	            
	            // Dump the previous distance graph.
	            html += "<TABLE CELLPADDING=\"3\">\r\n"
	                  + "  <CAPTION>Distances</CAPTION>\r\n"
	                  + "  <TBODY>\r\n"
	                  ;
	            for( int dst=0; dst < this.cities; ++dst )
	            {
	                html += "    <TR>\r\n";
	                for( int src=0; src < this.cities; ++src )
	                {
	                    html += "      <TD ALIGN=\"center\">"
	                                 + this.distance[dst][src] + "</TD>";
	                }
	                html += "    </TR>\r\n";
	            }
	            
	            html += "</TBODY>\r\n"
	                  + "<TABLE>\r\n"
	                  ;
	        }
        }

        if( 0 == this.cities )
        {
            this.cities =  DEFAULT_CITIES;
        }
        
        
        html += ""
            + "\r\n"
            + "<!-- Ask for new graph (start form). -->\r\n"
            + "<!>\r\n"
            + "<BR><BR>\r\n"
            + "<FORM ACTION=\"http://" + hostPort + topLevel + "\" METHOD=\"post\">\r\n"
            + "<INPUT TYPE=\"hidden\"\r\n"
            + "       NAME=\"" + ExternalRequestProcessor.TASKNAME_ATTRIBUTE + "\"\r\n"
            + "       VALUE=\"" + this.getClass().getName() + "\"></INPUT>\r\n"
            + "<INPUT TYPE=\"hidden\"\r\n"
            + "       NAME=\"" + topLevel +"/TSP/@prevcities\" VALUE=\""
                    + this.cities + "\">\r\n"
            + "<CENTER>\r\n"
            + "<TABLE>\r\n"
            + "  <TR>\r\n"
            + "    <TD VALIGN=\"middle\">\r\n"
            + "      <SELECT NAME=\"" + topLevel + "/TSP/@cities\">\r\n"
            ;
        
        for( int c=1; c <= MAX_EDGES; ++c )
        {
            if( this.cities == c )
            {
                html += "                  <OPTION SELECTED>" + c + "</OPTION>\r\n";
            }
            else
            {
                html += "                  <OPTION>" + c + "</OPTION>\r\n";
            }
        }
        
        html += ""
            + "              </SELECT>\r\n"
            + "    </TD>\r\n"
            + "    <TD>\r\n"
            + "      <TABLE BGCOLOR=\"#dddddd\">\r\n"
            ;
        
        Random rng = new Random();
        
        for( int fr=0; fr < this.cities; ++fr )
        {
            html += "        <TR>\r\n";

            for( int to=0; to < this.cities; ++to )
            {
                int value = rng.nextInt( 100 );
                html += "          <TD><INPUT NAME=\""
                      + topLevel + "/TSP/Distance"
                      + "\"\r\n                     SIZE=\"3\" VALUE=\"" +
                      + value + "\"></INPUT></TD>\r\n";
            }
            
            html += "        </TR>\r\n";
        }

        html += ""
            + "      </TABLE>\r\n"
            + "    </TD>\r\n"
            + "  </TR>\r\n"
            + "</TABLE>\r\n"
            + "<BR><BR>\r\n"
            + "<INPUT TYPE=\"submit\" VALUE=\"Get tour\"></INPUT>\r\n"
            + "</CENTER>\r\n"
            + "<BR><BR>\r\n"
            + "<!>\r\n"
            + CollectorHttp.createResponseSelect( null )
            + "<!>\r\n"
            + "</FORM>\r\n"
            + "<!>\r\n"
            + "<!-- End form. -->\r\n"
            + CollectorHttp.jicosHtmlFooter()
            + "</BODY>\r\n"
            + "</HTML>\r\n"
            ;
	        
        return ( html );
    }

}