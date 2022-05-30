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
 *  Performs a Gaussian Elimination with Partial Pivoting for factoring
 *  a nxn matrix.
 *
 *  A single loop, inner two loops replaced by outer product as in Van Loan.
 *
 *  <DL>
 *    <DT>Input:</DT>
 *    <DD>A n<I>x</I>n coefficient matrix.</DD>
 *    <DT>Output:</DT>
 *    <DD><B>L</B> lower triangular matrix after elimination.<BR>
 *        <B>U</B> upper triangular matrix after elimination.<BR>
 *        <B>piv</B> vector of pointers showing row switches.
 *    </DD>
 *  </DL>
 * 
 *  +--------------------------------------------------------------------+
 *  |  Copied from:                                                      |
 *  |    http://www.math.udel.edu/~braun/M426/Matlab/GEPP.m (2005.02.19) |
 *  |                                                                    |
 *  |  Dr. Richard J. Braun                                              |
 *  |  Department of Mathematical Sciences                               |
 *  |  501 Ewing Hall                                                    |
 *  |  University of Delaware                                            |
 *  |  Newark, DE  19716                                                 |
 *  |  braun@math.udel.edu                                               |
 *  +--------------------------------------------------------------------+
 * 
 * @author  Andy Pippin
 * @author  Dr. Richard J. Braun  <braun@math.udel.edu>
 */

package edu.ucsb.cs.jicos.examples.external.matlab;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.Environment;
import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.XmlDocument;
import edu.ucsb.cs.jicos.services.external.services.ExternalRequestProcessor;
import edu.ucsb.cs.jicos.services.external.services.CollectorHttp;
import edu.ucsb.cs.jicos.services.external.services.matlab.Matlab;
import edu.ucsb.cs.jicos.services.external.services.matlab.MatlabException;
import edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab;

import org.w3c.dom.Element;
import java.util.logging.Logger;
import java.util.logging.Level;


final public class GEPP extends TaskMatlab {

    //
    //-- Constants -------------------------------------------------------
    
    public static final String MATRIX_NAME = "A";
    public static final String UPPER_TRIANGLE = "U";
    public static final String LOWER_TRIANGLE = "L";
    public static final String PIVOT_VECTOR = "piv";

    public static final int DEFAULT_Rows = 4;
    public static final int DEFAULT_Cols = 4;
    
    public static final double DEFAULT_LowerBound = 0.0;
    public static final double DEFAULT_UpperBound = 100.0;

    public static final String stringToEval
        = "[n,n] = size(A);"
        + "piv = 1:n;"
        + "for k=1:n-1"                         // Begin elimination
        + "  [maxval r] = max(abs(A(k:n,k)));"  // find biggest value in sub-col
        + "  q = r+k-1;"                        // compute row location in A
        + "  piv([k,q]) = piv([q,k]);"          // keep track of switching
        + "  A([k q],:) = A[q k],:);"           // switch row of A
        + "  if A(k,k) ~= 0"                    // if non-zero, then pivot
        + "    A(k+1:n,k) = A(k+1:n,k)/A(k,k);" //   and continue.
        + "    A(k+1:n,k+1:n) = A(k+1:n,k+1:n) - A(k+1:n,k)*A(k,k+1:n);"
        + "  end;"
        + "end;"
        + "U = triu(A);"
        + "L = tril(A,-1)+eye(n);"
        ;

    private static final Logger logger = LogManager.getLogger();
    private static final Level DEBUG = LogManager.DEBUG;

    //
    //-- Variables -------------------------------------------------------

    double[][] U;
    double[][] L;
    double[]   pivot;

    //
    //-- Constructor -----------------------------------------------------

    public GEPP()
    {
        super( stringToEval, null, MATRIX_NAME );
    }

    public GEPP( double[][] matrix )
    {
        super( stringToEval, null, MATRIX_NAME );
        setMatrix( matrix );
    }

    //
    //-- Mutator ---------------------------------------------------------

    public void setMatrix( double[][] matrix )
    {
        super.addData( MATRIX_NAME, matrix );
    }

    //
    //-- Accessor --------------------------------------------------------

    public double[][] getMatrix()
    {
        return ((double[][]) super.getData( MATRIX_NAME ));
    }

    //
    //-- Methods ---------------------------------------------------------

    public Object execute( Environment environment )
    {
        Object result = null;
        String command = null;
        this.U = this.L = null;
        this.pivot = null;

        try
        {
            throw new Exception();
        }
        catch (Exception exception)
        {
            exception.printStackTrace( System.err );
            System.err.flush();
        }

        if (null != (command = getCommand()))
        {
            // Get the engine.
            Matlab matlab = (Matlab) environment.getProxyServiceExternal();
            if (null == matlab)
            {
                String error = "matlab proxyServiceExternal is null";
                LogManager.getLogger().log( LogManager.ERROR, error );
                throw new RuntimeException( error );
            }

            try
            {
//logger.log( DEBUG, "evaluating string" );
                Object upper = matlab.evaluate( stringToEval, UPPER_TRIANGLE,
                        getDataMap(), false );
                Object lower = matlab.getVariable( LOWER_TRIANGLE );
                Object pivot = matlab.getVariable( PIVOT_VECTOR );

                new GEPPResult( upper, lower, pivot );
            }
            catch (MatlabException matlabException)
            {
                throw new RuntimeException( matlabException.getMessage() );
            }

            // Convert the upper and lower matrices to the result.
            result = new Object[]
                { this.U, this.L, this.pivot };
        }

//result = new Integer( 10 );
//logger.log( DEBUG, "returning result (" + result + ")" );
        return (result);
    }


    //
    //== For External Requests ===========================================
    
    public String toXml(String prefix) {
        return( super.toXml( prefix ) );
    }

        
    public boolean fromXml( ExternalData externalData )
    {
        boolean success = false;

        if (null != externalData)
        {
            XmlDocument xmlDocument = new XmlDocument( externalData );
            Element[] geppElements = xmlDocument.getElement( "/ExternalRequest/GEPP" );
            
            if( (null == geppElements) || (0 == geppElements.length) )
            {
                throw new IllegalArgumentException( "Couldn't find \"/ExternalRequest/GEPP\" data" );
            }
            XmlDocument geppData = new XmlDocument( geppElements[0] );
            
            // Get the datatype, assume double.
            boolean isInteger = false;
            String dataTypeName = geppData.getValue( "@dataType" );
            if( null != dataTypeName )
            {
                int dataType = TaskMatlab.getDataType( dataTypeName );

                switch (dataType)
                {
                    case DATATYPE_Int08:
                    case DATATYPE_Int16:
                    case DATATYPE_Int32:
                    case DATATYPE_Int64:
                        isInteger = true;
                        break;

                    case DATATYPE_Float:
                    case DATATYPE_Double:
                        isInteger = false;
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Unknown datatype = \"" + dataTypeName + "\"" );
                }
            }

            // Integer or floating point?
            
            // Get the size of the matrix.
            int numRows=0, numCols=0;
            String size = geppData.getValue( "@rowcol" );
            if( null == size )
            {
                throw new IllegalArgumentException( "no rowcol attribute" );
            }
            else
            {
                String[] rowcol = size.split( "," );
                if( 2 != rowcol.length )
                {
                    throw new IllegalArgumentException( "bad row,col format: \"" + size + "\"" );
                }
                else
                {
                    numRows = Integer.parseInt( rowcol[0] );
                    numCols = Integer.parseInt( rowcol[1] );
                }
            }

            String rowCol = null;
            if ( 0 >= numRows )
            {
                throw new IllegalArgumentException( "illegal number of rows: \"" + size + "\"" );
            }
            else if( 0 >= numCols )
            {
                throw new IllegalArgumentException( "illegal number of colums: \"" + size + "\"" );
            }
            else if (isInteger)
            {
                int[][] intMatrix = new int[numRows][numCols];
                
                for (int r = 0; r < numRows; ++r)
                {
                    for (int c = 0; c < numCols; ++c)
                    {
                        rowCol = geppData.getValue( "/Element_" +r+'_'+c );
                        intMatrix[r][c] = Integer.parseInt( rowCol );
                    }
                }
                addData( MATRIX_NAME, intMatrix );
                success = true;
            }
            else
            {
                double[][] dblMatrix = new double[numRows][numCols];
                
                for (int r = 0; r < numRows; ++r)
                {
                    for (int c = 0; c < numCols; ++c)
                    {
                        rowCol = geppData.getValue( "/Element_" +r+'_'+c );
                        try
                        {
                            dblMatrix[r][c] = Double.parseDouble( rowCol );
                        }
                        catch( Exception e )
                        {
                            throw new RuntimeException( "Error parsing (r,c) = (" +r+','+c+ ") = " + rowCol );
                        }
                    }
                }
                addData( MATRIX_NAME, dblMatrix );
                success = true;
            }
        }

        return (success);
    }

    public Object createInput(ExternalData externalData)
    {
        return( super.createInput( externalData ) );
    }

    public Shared createShared(ExternalData externalData)
    {
        return( super.createShared( externalData ) );
    }

    public XmlDocument createResult(Object result) throws Exception
    {
        String xml = new String();
//logger.log( DEBUG, "creating result..." );

        xml+= "<ExternalResponse>\r\n"
            + "  <GEPP/>\r\n"
            + "</ExternalResponse>\r\n"
            ;

        XmlDocument xmlResult = new XmlDocument( xml );

        return (xmlResult);
    }

    public String toHtmlString( XmlDocument xmlResult, String hostPort )
    {
        Element[] xmlElement = null;
        final String exRqstGepp = ExternalRequest.TOP_LEVEL + "/GEPP";

        // Fix the hostname/port.
        if (null == hostPort)
        {
            hostPort = "localhost";
        }

	String html
	    = "<HTML>\r\n"
	    + "<HEAD>\r\n"
	    + "  <TITLE>Matlab&copy; GEPP</TITLE>\r\n" + "</HEAD>\r\n"
	    + "<BODY>\r\n" + CollectorHttp.jicosHtmlHeader()
	    + "<CENTER><H2>Matlab<SUP>&copy;</SUP> GEPP</H2><BR>\r\n"
	    + "(Gaussian Elimination with Partial Pivoting)</CENTER>\r\n"
	    + "\r\n"
	    ;

	// If there is an answer, format it.
	if (null != xmlElement)
	{
	    html = html
	    	+ "<!-- Answer from previous request. -->\r\n"
		+ "<!>\r\n"
		+ "<FONT COLOR=green>Previous Result:</FONT><BR>\r\n"
		;
	}


	int numRows = DEFAULT_Rows;
        int numCols = DEFAULT_Cols;
        double lowerBound = DEFAULT_LowerBound;
        double upperBound = DEFAULT_UpperBound;

	// A request for a matrix.
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
	    + "<INPUT TYPE=\"hidden\"\r\n"
	    + "       NAME=\"" + exRqstGepp + "/@rowcol\"\r\n"
	    + "       VALUE=\"" + numRows +','+ numCols + "\">\r\n"
	    + "<CENTER><TABLE BGCOLOR=\"#eeeeee\">\r\n"
	    ;


        if ((0 < numRows) && (0 < numCols))
        {
            double range = upperBound - lowerBound;
            java.util.Random rng = new java.util.Random();
            double element = 0.0;

            for (int r = 0; r < numRows; ++r)
            {
                html += "  <TR>\r\n";
                for (int c = 0; c < numCols; ++c)
                {
                    element = (rng.nextDouble() * range) + lowerBound;
                    element = ((double) ((long) (element * 1000))) / 1000.0;

                    html += "    <TD><INPUT TYPE=\"text\" SIZE=\"5\" VALUE=\""
			    + element + "\"\r\n"
			+ "               NAME=\"" + exRqstGepp
			    + "/Element_" + r + "_" + c + "\"></INPUT>\r\n";
                }
                html += "  </TR>\r\n";
            }
        }
	
	html = html
	    + "</TABLE>\r\n"
	    + "<BR>"
	    + "<INPUT TYPE=\"submit\"></INPUT>\r\n"
	    + "</CENTER>\r\n<BR><BR>\r\n" + "<!>\r\n"
	    + CollectorHttp.createResponseSelect(null)
	    + "<!>\r\n"
	    + "<BR>\r\n</FORM>\r\n"
	    + "<!>\r\n"
	    + "<!-- End form. -->\r\n"
	    + CollectorHttp.jicosHtmlFooter()
	    + "</BODY>\r\n"
	    + "</HTML>\r\n"
	    ;
		
	return (html);
    }

    //
    //== Inner Classes =========================================================

    /**
     *  This is a container class for the GEPP (upper, lower, and pivot).
     *
     * @author  Andy Pippin  <pippin@cs.ucsb.edu>
     */
    private class GEPPResult
    {
        public double[][]  U;
	public double[][]  L;
	public double[]  pivot;


        public GEPPResult( Object upper, Object lower, Object pivot )
	{
	    this.U = null;
	    this.L = null;
	    this.pivot = null;

logger.log( DEBUG, UPPER_TRIANGLE + "=" + upper );
	    if ( (null != upper) && (upper instanceof double[][]) )
	    {
		this.U = (double[][])upper;
	    }

logger.log( DEBUG, LOWER_TRIANGLE + "=" + lower );
	    if ( (null != lower) && (lower instanceof double[][]) )
	    {
		this.L = (double[][])lower;
	    }

logger.log( DEBUG, PIVOT_VECTOR + "=" + pivot );
	    if ( (null != pivot) && (pivot instanceof double[][]) )
	    {
		this.pivot = (double[])pivot;
	    }
	}
    }

}
