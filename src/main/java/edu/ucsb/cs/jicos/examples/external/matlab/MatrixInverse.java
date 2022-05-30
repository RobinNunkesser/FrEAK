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
 *  Demonstrates a Jicos Task invoking a non-Jicos Service, in this case
 *  Matlab.
 * 
 * @author  Andy Pippin
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
import edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab;

//import java.text.NumberFormat;
import java.text.DecimalFormat;
import org.w3c.dom.Element;

final public class MatrixInverse extends TaskMatlab {

	//
	//-- Constants -------------------------------------------------------
	
	public static final String MATRIX_NAME = "MatrixInverse";
	public static final String RESULT_NAME = "MatlabResult";

	public static final int DEFAULT_Rows = 5;
	public static final int DEFAULT_Cols = 5;
	
	public static final double DEFAULT_LowerBound = 0.0;
	public static final double DEFAULT_UpperBound = 100.0;

	private static final boolean showMatrix = true;

	//
	//-- Constructor -----------------------------------------------------
	
	public MatrixInverse() {
		super( RESULT_NAME + " = inv( " + MATRIX_NAME + " );" );
	}
    
    public MatrixInverse( double[][] matrix )
    {
        super( RESULT_NAME + " = inv( " + MATRIX_NAME + " );", null, RESULT_NAME );
        setMatrix( matrix );
    }
    


	//
	//-- Mutator ---------------------------------------------------------
	
	public void setMatrix(double[][] matrix) {
		super.addData( MATRIX_NAME, matrix );
	}

	//
	//-- Accessor --------------------------------------------------------
	
	public double[][] getMatrix() {
		return ( (double[][])super.getData( MATRIX_NAME ) );
	}

	//
	//-- Methods ---------------------------------------------------------
	
	public Object execute(Environment environment)
	{
	    if( showMatrix )
	    {
		double[][] matrix =
			    (double[][])super.getData( MATRIX_NAME );
		LogManager.getLogger().log( LogManager.DEBUG,
			"incoming:\n" + matrixToString(matrix) );
	    }

	    Object result = super.execute( environment );

	    if( (showMatrix)
	     && (null != result)
	     && (result instanceof double[][]) )
	    {
		double[][] inverse = (double[][])result;
		LogManager.getLogger().log( LogManager.DEBUG,
			"inverse:\n" + matrixToString(inverse) );
	    }

	    return( result );
	}

	//
	//== For External Requests ===========================================
	
	public String toXml(String prefix) {
		return( super.toXml( prefix ) );
	}

	
	public boolean fromXml(ExternalData externalData) {
		boolean success = false;
		
		if( null != externalData ) {
			
			String dataTypeName = externalData.getValue( "/ExternalRequest/MatrixInverse/Matrix/@dataType" );
			int dataType = TaskMatlab.getDataType( dataTypeName );
			if( TaskMatlab.DATATYPE_unknown != dataType ) {
				
				// Integer or floating point?
				boolean isInteger = false;
				switch( dataType ) {
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
				}
				
				String[] matrixRow = externalData.getArray( "/ExternalRequest/MatrixInverse/Matrix/Row" );
				if( (null == matrixRow) || (0 == matrixRow.length) ) {
					// Doh!
				} else if( isInteger ) {
					int[][] intMatrix = new int[ matrixRow.length ][];
					for( int r=0; r < matrixRow.length; ++r ) {
						String[] aRow = matrixRow[r].split( " " );
						intMatrix[r] = new int[aRow.length];
						
						for( int c=0; c < aRow.length; ++c ) {
							intMatrix[r][c] = Integer.parseInt( aRow[c] );
						}
					}
					addData( MATRIX_NAME, intMatrix );
					success = true;
					
				} else {
					double[][] dblMatrix = new double[ matrixRow.length ][];
					for( int r=0; r < matrixRow.length; ++r ) {
						String[] aRow = matrixRow[r].split( " " );
						dblMatrix[r] = new double[aRow.length];
						
						for( int c=0; c < aRow.length; ++c ) {
							dblMatrix[r][c] = Double.parseDouble( aRow[c] );
						}
					}
					addData( MATRIX_NAME, dblMatrix );
					success = true;
				}
			}
		}
		
		return( success );
	}

	public Object createInput(ExternalData externalData) {
		return( super.createInput( externalData ) );
	}

	public Shared createShared(ExternalData externalData) {
		return( super.createShared( externalData ) );
	}

	public XmlDocument createResult(Object result) throws Exception {
		String xml = new String();
		
		xml += "<ExternalResponse>\r\n"
		    + "  <MatrixInverse>\r\n"
		    + super.toXml( result, "Result", "    " )
			+ "  </MatrixInverse>\r\n"
			+ "</ExternalResponse>\r\n";
		
		XmlDocument xmlResult = new XmlDocument( xml );
		return( xmlResult );
	}

	public String toHtmlString(XmlDocument xmlResult, String hostPort) {

		Element[] xmlElement = null;
		
		// Fix the hostname/port.
		if (null == hostPort)
			hostPort = "localhost";

		// Get the result if it exists.
		if (null != xmlResult) {
			xmlElement = xmlResult.getElement("/ExternalResponse/MatrixInverse/Result");
		}


		String html = "<HTML>\r\n" + "<HEAD>\r\n"
				+ "  <TITLE>Matlab&copy; Matrix Inverse</TITLE>\r\n" + "</HEAD>\r\n"
				+ "<BODY>\r\n" + CollectorHttp.jicosHtmlHeader()
				+ "<CENTER><H2>Matlab<SUP>&copy;</SUP> Matrix Inverse</H2></CENTER>\r\n" + "\r\n";

		// If there is an answer, format it.
		if (null != xmlElement) {
			html = html
				+ "<!-- Answer from previous request. -->\r\n"
				+ "<!>\r\n"
				+ "<FONT COLOR=green>Previous Result:</FONT><BR>\r\n";
		}

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
				+ "<CENTER><TABLE BGCOLOR=\"#eeeeee\">\r\n";
		

		int numRows = DEFAULT_Rows;
		int numCols = DEFAULT_Cols;
		double lowerBound = DEFAULT_LowerBound;
		double upperBound = DEFAULT_UpperBound;
		
		if( (0 < numRows) && (0 < numCols) )
		{
			double range = upperBound - lowerBound;
			java.util.Random randomNumberGenerator = new java.util.Random();
			double element = 0.0;
			
			for( int r=0; r < numRows; ++r ) {
				html += "  <TR>\r\n";
				for( int c=0; c < numCols; ++c ) {
					element = (randomNumberGenerator.nextDouble() * range) + lowerBound;
					element = ((double)((long)(element * 1000)))/1000.0;
					
					html += "    <TD><INPUT TYPE=\"text\" SIZE=\"5\" VALUE=\"" + element + "\"\r\n"
					      + "               NAME=\"" + ExternalRequest.TOP_LEVEL +"/MatrixInverse/Element_"
						  + r + "_" + c + "\"></INPUT>\r\n";
				}
				html += "  </TR>\r\n";
			}
		}

		html += "</TABLE>\r\n"
			+ "<BR>"
			+ "<INPUT TYPE=\"submit\"></INPUT>\r\n"
			+ "</CENTER>\r\n<BR><BR>\r\n" + "<!>\r\n"
			+ CollectorHttp.createResponseSelect(null) + "<!>\r\n"
			+ "<BR>\r\n</FORM>\r\n" + "<!>\r\n" + "<!-- End form. -->\r\n"
			+ CollectorHttp.jicosHtmlFooter() + "</BODY>\r\n"
			+ "</HTML>\r\n";
			
		return (html);
	}

    /**
     *  Display the contents of a matrix.
     *
     * @param matrix A double matrix.
     */
    public static String matrixToString( double[][] matrix )
    {
        String string = new String();
        final DecimalFormat formatter =
		(DecimalFormat)DecimalFormat.getInstance();
	formatter.applyPattern( "  ##0.0000; -##0.0000" );

	for( int r=0; r < matrix.length; ++r )
	{
	    string += "        | ";
	    for(int c=0; c < matrix[0].length; ++c )
	    {
		string += formatter.format( matrix[r][c] );
	    }
	    string += "|\r\n";
	}

	return( string );
    }
}
