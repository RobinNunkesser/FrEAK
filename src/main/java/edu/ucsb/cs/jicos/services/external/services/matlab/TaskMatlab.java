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
 * Demonstrates a non-Java client invoking a non-Java Jicos Task, in this case,
 * a Matlab command.
 * 
 * @author Pete Cappello
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services.matlab;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.services.TaskExternal;
import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TaskMatlab extends TaskExternal {

    //
    //-- Constants -------------------------------------------------------

    private static final boolean INTEGER = true;

    private static final boolean FLOATING = false;

    private static final String FORMAT_Integer = " ####0; -####0";

    private static final String FORMAT_Floating = " ##0.00; -##0.00";

    // Data types
    /** Unknown data type. */
    public static final int DATATYPE_unknown = 0;

    /** 8-bit (signed) integer data type. */
    public static final int DATATYPE_Int08 = 1;

    /** 16-bit (signed) integer data type. */
    public static final int DATATYPE_Int16 = 2;

    /** 32-bit (signed) integer data type. */
    public static final int DATATYPE_Int32 = 3;

    /** 64-bit (signed) integer data type. */
    public static final int DATATYPE_Int64 = 4;

    /** floating point data type. */
    public static final int DATATYPE_Float = 5;

    /** double (floating point) data type. */
    public static final int DATATYPE_Double = 6;

    //
    //-- Variables -------------------------------------------------------

    private String command;

    private Map dataMap;

    private Object matlabResult;

    private String resultName;

    //
    //-- Constructors ----------------------------------------------------

    /**
     * Default, no argument, constructor.
     */
    public TaskMatlab()
    {
        super( TaskMatlab.class );
        this.initialize();
    }

    /**
     * Create a Matlab task with the specified command.
     * 
     * @param command
     *            Matlab command.
     */
    public TaskMatlab(String command)
    {
        super( TaskMatlab.class );

        this.initialize();
        this.command = command;
    }

    /**
     * Create a complete Matlab task.
     * 
     * @param command
     *            The matlab command to evaluate.
     * @param dataMap
     *            The supporting data for the command.
     * @param resultName
     *            The name of the variable containing the result.
     */
    public TaskMatlab(String command, Map dataMap, String resultName)
    {
        super( TaskMatlab.class );

        this.initialize();
        this.command = command;
        this.resultName = resultName;
        if (null != dataMap)
        {
            this.dataMap.putAll( dataMap );
        }
    }

    // Iniitialize member variables.
    private void initialize()
    {
        this.command = null;
        this.dataMap = new HashMap();
        this.matlabResult = null;
    }

    //
    //-- Accessors -------------------------------------------------------

    /**
     * Return the name of the command.
     */
    public String getCommand()
    {
        return (this.command);
    }

    /**
     * Return the map of data (name/value pairs).
     */
    public Map getDataMap()
    {
        return (this.dataMap);
    }

    /**
     * Return the data type enumerator for the given name.
     * 
     * @param typeName
     *            The name of the type.
     */
    public static int getDataType( String typeName )
    {
        int dataType = DATATYPE_unknown;

        String type = null;
        if (null != typeName)
        {
            type = typeName.toLowerCase();
        }

        if (null == type)
        {
            dataType = DATATYPE_unknown;
        }
        else if (type.equals( "double" ))
        {
            dataType = DATATYPE_Double;
        }
        else if (type.equals( "int" ) || type.equals( "long" )
                || type.equals( "int32" ) || type.equals( "int32_t" ))
        {
            dataType = DATATYPE_Int32;
        }
        else if (type.equals( "short" ) || type.equals( "int16" )
                || type.equals( "int16_t" ))
        {
            dataType = DATATYPE_Int16;
        }
        else if (type.equals( "char" ) || type.equals( "byte" )
                || type.equals( "int8" ) || type.equals( "int08" )
                || type.equals( "int8_t" ) || type.equals( "int08_t" ))
        {
            dataType = DATATYPE_Int08;
        }
        else if (type.equals( "float" ))
        {
            dataType = DATATYPE_Float;
        }
        else if (type.equals( "int64" ) || type.equals( "int64_t" )
                || type.equals( "longlong" ) || type.equals( "long long" ))
        {
            dataType = DATATYPE_Int64;
        }

        return (dataType);
    }

    /**
     * Return the name of the variable containing the result.
     */
    public String getResultName()
    {
        return (this.resultName);
    }

    /**
     * Get the name of the typeId enumerator.
     * 
     * @param typeId
     *            The data type enumerator.
     */
    public static String getTypeName( int typeId )
    {
        String typeName = null;

        switch (typeId)
        {
            case DATATYPE_Int08:
                typeName = "int08";
                break;
            case DATATYPE_Int16:
                typeName = "int16";
                break;
            case DATATYPE_Int32:
                typeName = "int32";
                break;
            case DATATYPE_Int64:
                typeName = "int64";
                break;
            case DATATYPE_Float:
                typeName = "float";
                break;
            case DATATYPE_Double:
                typeName = "double";
                break;
        }

        return (typeName);
    }

    /**
     * Return the data in a <I>n </I>x2 array of data.
     * 
     * The first element of each row is the name and the second element is the
     * obect.
     */
    public Object[][] getData()
    {
        Object[][] data = null;

        if (null != this.dataMap)
        {
            int dataMap_size = this.dataMap.size();
            data = new Object[dataMap_size][2];

            Iterator keySet = this.dataMap.keySet().iterator();
            int dataItem = 0;

            while (keySet.hasNext())
            {
                String key = (String) keySet.next();
                data[dataItem][0] = key;
                data[dataItem][1] = this.dataMap.get( key );

                dataItem++;
            }
        }

        return (data);
    }

    /**
     * Get a particular data item by name.
     * 
     * @param name
     *            The name of the data item, <CODE>null</CODE> for the whole
     *            thing.
     * @return The specified data item, or <CODE>null</CODE> if not found.
     */
    public Object getData( String name )
    {
        Object dataItem = null;

        if (null == name)
        {
            dataItem = (Object) this.dataMap;

        } else
        {
            dataItem = this.dataMap.get( name );

        }

        return (dataItem);
    }

    /**
     * Return the result of the matlab computation.
     */
    public Object getMatlabResult()
    {
        return (this.matlabResult);
    }

    /**
     * Override the default method to create a Java string
     * (class[member,[...]]).
     */
    public String toString()
    {
        String[] className = getClass().getName().replace( '.', '\u2222' )
                .split( "\u2222" );
        String result = className[className.length - 1] + "[command=";

        if (null != this.command)
        {
            result += '"' + this.command + '"';
        }

        result += ",dataMap=";
        if (0 != this.dataMap.size())
        {
            result += this.dataMap.toString();
        }
        result += "]";

        return (result);
    }

    /**
     * Convert an object to a string (depending on it's type).
     * 
     * Possible objects:
     * <OL>
     * <LI>java.lang.Number</LI>
     * <LI>int[]</LI>
     * <LI>double[]</LI>
     * <LI>java.lang.Number[]</LI>
     * <LI>int[][]</LI>
     * <LI>double[][]</LI>
     * <LI>java.lang.Number[][]</LI>
     * </OL>
     * 
     * @param object
     *            Some object.
     */
    public String toString( Object object )
    {
        String result = null;

        if (null == object)
        {

            // Scalar - object floating point.
        } else if (object instanceof Number)
        {
            result = String.valueOf( object );

            // Vector - atomic integers
        } else if ((object instanceof short[]) || (object instanceof int[])
                || (object instanceof long[]))
        {
            result = toString_VectorInt( object );

            // Vector - atomic floating point.
        } else if ((object instanceof float[]) || (object instanceof double[]))
        {
            result = toString_VectorFloat( object );

            // Vector - object integer.
        } else if ((object instanceof Short[]) || (object instanceof Integer[])
                || (object instanceof Long[]))
        {
            result = toString_Vector( (Number[]) object, INTEGER );

            // Vector - object floating point.
        } else if (object instanceof Number[])
        {
            result = toString_Vector( (Number[]) object, FLOATING );

            // Matrix - atomic integers
        } else if ((object instanceof short[][]) || (object instanceof int[][])
                || (object instanceof long[][]))
        {
            result = toString_MatrixInt( object );

            // Matrix - atomic floating point
        } else if ((object instanceof float[][])
                || (object instanceof double[][]))
        {
            result = toString_MatrixFloat( object );

            // Matrix - object integers
        } else if ((object instanceof Short[][])
                || (object instanceof Integer[][])
                || (object instanceof Long[][]))
        {
            result = toString_Matrix( (Number[][]) object, INTEGER );

            // Matrix - oject floating point
        } else if (object instanceof Number[][])
        {
            result = toString_Matrix( (Number[][]) object, FLOATING );

        }

        return (result);
    }

    /**
     * Convert the given <CODE>object</CODE> to an XML string with the element
     * <CODE>name</CODE> given and precede each line of text with the given
     * <CODE>prefix</CODE>.
     * 
     * @param object
     *            The object to convert.
     * @param name
     *            The name of the element.
     * @param prefix
     *            Precede each line of text with this string.
     */
    public String toXml( Object object, String name, String prefix )
    {
        String xml = new String();

        if (null == object)
        {
            if (null != prefix)
            {
                xml += prefix;
            }

            if (null != name)
            {
                xml += "<" + name + "/>\r\n";
            } else
            {
                xml += "<Null/>\r\n";
            }

        } else
        {

            // Integral scalar
            if ((object instanceof Short) || (object instanceof Integer)
                    || (object instanceof Long))
            {
                xml = toXml_Scalar( (Number) object, name, INTEGER, prefix );

                // Flating point scalar
            } else if (object instanceof Number)
            {
                xml = toXml_Scalar( (Number) object, name, FLOATING, prefix );

                // String
            } else if (object instanceof String)
            {
                xml = toXml_String( (String) object, name, prefix );

                // Vector - atomic integers
            } else if ((object instanceof short[]) || (object instanceof int[])
                    || (object instanceof long[])
                    || (object instanceof float[])
                    || (object instanceof double[])
                    || (object instanceof Number[]))
            {
                xml = toXml_Vector( object, name, prefix );

                // Matrix - atomic integers
            } else if ((object instanceof short[][])
                    || (object instanceof int[][])
                    || (object instanceof long[][])
                    || (object instanceof float[][])
                    || (object instanceof double[][])
                    || (object instanceof Number[][]))
            {
                xml = toXml_Matrix( object, name, prefix );

            }
        }

        return (xml);
    }

    //
    //-- Mutators --------------------------------------------------------

    /**
     * Set the command to be perfoirmed by matlab.
     * 
     * @param command
     *            Matlab command to invoke.
     */
    public void setCommand( String command )
    {
        this.command = command;
    }

    /**
     * Set the name of the variable containing the result at the end of the
     * computation.
     * 
     * @param resultName
     *            The name of the variable.
     */
    public void setResultName( String resultName )
    {
        this.resultName = resultName;
    }

    /**
     * Set the contents of the data map to the contents of the given map.
     * 
     * @param dataMap
     *            A map of variable name/value pairs.
     */
    public void setData( Map dataMap )
    {

        this.dataMap.clear();

        if (null != dataMap)
        {
            this.dataMap.putAll( dataMap );
        }

        return;
    }

    /**
     * Add an object to the data map of this task.
     * 
     * @param name
     *            The name of this variable.
     * @param value
     *            The value of this variable.
     */
    public void addData( String name, Object value )
    {
        if (null != name)
        {
            this.dataMap.put( name, value );
        }

        return;
    }

    /**
     * Add the contents of a map to the contents of the data map.
     * 
     * @param dataMap
     *            Additional contents.
     */
    public void addData( Map dataMap )
    {
        if (null != dataMap)
        {
            this.dataMap.putAll( dataMap );
        }

        return;
    }

    /**
     * Save the result of the computation.
     * 
     * @param matlabResult
     *            The result of the matlab result.
     */
    public void setMatlabResult( Object matlabResult )
    {
        this.matlabResult = matlabResult;
    }

    //
    //-- Perform ----------------------------------------------------

    /**
     * Get the Matlab engine and invoke a command on it.
     * 
     * @param environment
     *            The Jicos environment of this task.
     */
    public Object execute( Environment environment )
    {
        Object result = null;

        //do something matlab specific.
        if (null != this.command)
        {
            Matlab matlab = (Matlab) environment.getProxyServiceExternal();

            if (null == matlab)
            {
                LogManager.getLogger().log( LogManager.WARNING,
                        "matlab proxyServiceExternal is NULL!" );
		throw new RuntimeException( 
                        "matlab proxyServiceExternal is NULL!" );
            }
	    else
            {
                try
                {
                    result = matlab.evaluate( command, resultName, dataMap );
                }
		catch (MatlabException matlabException)
                {
                    result = null;
                }
            }
        }
LogManager.getLogger().log( LogManager.DEBUG, "returning type: \"" + result.getClass().getName() );
        return ((Object) result);
    }

    //
    //-- For ExternalRequests ------------------------------------------------

    /**
     * Create XML out of this object.
     * 
     * @param prefix
     *            Precede each line of output with this.
     */
    public String toXml( String prefix )
    {
        String xml = new String();
        String pre = (null == prefix) ? "" : prefix;

        String[] classArray = getClass().getName().replace( '.', '\u2345' )
                .split( "\u2345" );
        String tag = classArray[classArray.length - 1];

        xml += pre + "<" + tag + ">\r\n";

        if (null == command)
        {
            xml += pre + "  <Command/>\r\n";
        } else
        {
            xml += pre + "  <Command>" + command + "</Command>\r\n";
        }

        int dataMap_size = this.dataMap.size();
        if (0 == dataMap_size)
        {
            xml += pre + "  <Data/>\r\n";
        } else
        {
            xml += pre + "  <Data items=\"" + dataMap_size + "\">\r\n";

            Iterator iterator = this.dataMap.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                Object val = this.dataMap.get( key );
                xml += toXml( val, key, pre + "    " );
            }

            xml += pre + "  </Data>\r\n";
        }

        if (null == matlabResult)
        {
            xml += pre + "  <MatlabResult/>\r\n";
        } else
        {
            xml += pre + "  <MatlabResult>\r\n";
            xml += toXml( this.matlabResult, null, pre + "    " );
            xml += pre + "  </MatlabResult>\r\n";
        }

        xml += pre + "</" + tag + ">\r\n";

        return (xml);
    }

    /**
     * Populate the variables with data from the external data (should be
     * overridden).
     * 
     * @return <CODE>false</CODE>
     */
    public boolean fromXml( ExternalData externalData )
    {
        boolean success = false;
        return (success);
    }

    /**
     * Create the input variable with data from the external data (should be
     * overridden).
     * 
     * @return <CODE>null</CODE>
     */
    public Object createInput( ExternalData externalData )
    {
        return ((Object) null);
    }

    /**
     * Create the shared object with data from the external data (should be
     * overridden).
     * 
     * @return <CODE>false</CODE>
     */
    public Shared createShared( ExternalData externalData )
    {
        return ((Shared) null);
    }

    /**
     * Create an XML document from the result (should be overridden).
     * 
     * @return <CODE>&gt;?xml version="1.0"?&lt;</CODE>
     */
    public XmlDocument createResult( Object result ) throws Exception
    {
        return (new XmlDocument( this.toXml( "" ) ));
    }

    /**
     * Create an HTML document from the result (should be overridden).
     */
    public String toHtmlString( XmlDocument xmlResult, String hostPort )
    {
        return (xmlResult.toHtmlString());
    }

    //
    //------------------------------------------------------------------------
    //-- Convert particular objects to a string ------------------------------
    //------------------------------------------------------------------------

    /**
     * Convert a java.lang.Number to a string using a number formatter.
     * 
     * @param scalar
     *            The number to format.
     * @param isInteger
     *            Are the numbers integers?
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_Scalar( Number scalar, boolean isInteger )
    {
        String result = null;
        java.text.NumberFormat formatter = java.text.DecimalFormat
                .getInstance();

        if (isInteger)
        {
            ((java.text.DecimalFormat) formatter)
		    .applyPattern( FORMAT_Integer );
        }
	else
        {
            ((java.text.DecimalFormat) formatter)
                    .applyPattern( FORMAT_Floating );
        }

	result += formatter.format( scalar.longValue() );
        return (result);
    }

    /**
     * Convert an array of java.lang.Numbers to a string.
     * 
     * @param vector
     *            The array of numbers.
     * @param isInteger
     *            Are the numbers integers?
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_Vector( Number[] vector, boolean isInteger )
    {
        String result = new String();

        int numRows = vector.length;

        if (0 < numRows)
        {

            java.text.NumberFormat formatter = java.text.DecimalFormat
                    .getInstance();
            if (isInteger)
            {
                ((java.text.DecimalFormat) formatter)
                        .applyPattern( FORMAT_Integer );
            } else
            {
                ((java.text.DecimalFormat) formatter)
                        .applyPattern( FORMAT_Floating );
            }

            result += "(";
            if (isInteger)
            {
                for (int row = 0; row < numRows; ++row)
                {
                    if (0 != row)
                        result += ',';
                    result += formatter.format( vector[row].longValue() );
                }
            } else
            {
                for (int row = 0; row < numRows; ++row)
                {
                    if (0 != row)
                        result += ',';
                    result += formatter.format( vector[row].doubleValue() );
                }
            }
            result += " )";
        }

        return (result);
    }

    /**
     * Convert a vector of integers (short, int, or long) to a string.
     * 
     * @param vector
     *            The array of numbers.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_VectorInt( Object vector )
    {
        final int TYPE_unknown = 0;
        final int TYPE_Short = 1;
        final int TYPE_Int = 2;
        final int TYPE_Long = 3;

        String result = new String();
        int typeId = TYPE_unknown;
        int numRows = 0;

        if (vector instanceof short[])
        {
            typeId = TYPE_Short;
            numRows = ((short[]) vector).length;

        } else if (vector instanceof int[])
        {
            typeId = TYPE_Int;
            numRows = ((int[]) vector).length;

        } else if (vector instanceof long[])
        {
            typeId = TYPE_Long;
            numRows = ((long[]) vector).length;
        }

        if (0 < numRows)
        {

            java.text.NumberFormat formatter = java.text.DecimalFormat
                    .getInstance();
            ((java.text.DecimalFormat) formatter).applyPattern( FORMAT_Integer );

            result += "(";

            // Java 5 takes care of this....
            switch (typeId)
            {
                case TYPE_Short:
                    short[] shortVector = (short[]) vector;
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                            result += ',';
                        result += formatter.format( shortVector[row] );
                    }
                    break;
                case TYPE_Int:
                    int[] intVector = (int[]) vector;
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                            result += ',';
                        result += formatter.format( intVector[row] );
                    }
                    break;
                case TYPE_Long:
                    long[] longVector = (long[]) vector;
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                            result += ',';
                        result += formatter.format( longVector[row] );
                    }
                    break;
            }
            result += " )";
        }

        return (result);
    }

    /**
     * Convert a vector of floating points (float, or double) to a string.
     * 
     * @param vector
     *            The array of floating point numbers.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_VectorFloat( Object vector )
    {
        final int TYPE_unknown = 0;
        final int TYPE_Float = 1;
        final int TYPE_Double = 2;

        String result = new String();
        int typeId = TYPE_unknown;
        int numRows = 0;

        if (vector instanceof float[])
        {
            typeId = TYPE_Float;
            numRows = ((float[]) vector).length;

        } else if (vector instanceof double[])
        {
            typeId = TYPE_Double;
            numRows = ((double[]) vector).length;

        }

        if (0 < numRows)
        {

            java.text.NumberFormat formatter = java.text.DecimalFormat
                    .getInstance();
            ((java.text.DecimalFormat) formatter)
                    .applyPattern( FORMAT_Floating );

            result += "(";

            // Java 5 takes care of this....
            switch (typeId)
            {
                case TYPE_Float:
                    float[] floatVector = (float[]) vector;
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                            result += ',';
                        result += formatter.format( floatVector[row] );
                    }
                    break;
                case TYPE_Double:
                    double[] doubleVector = (double[]) vector;
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                            result += ',';
                        result += formatter.format( doubleVector[row] );
                    }
                    break;
            }
            result += " )";
        }

        return (result);
    }

    /**
     * Convert a matrix of java.lang.Number to a string.
     * 
     * @param matrix
     *            The array of array of numbers.
     * @param isInteger
     *            Are the numbers integers?
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_Matrix( Number[][] matrix, boolean isInteger )
    {
        String result = new String();

        int numRows;
        int numCols;

        if (0 < (numRows = matrix.length))
        {

            java.text.NumberFormat formatter = java.text.DecimalFormat
                    .getInstance();
            if (isInteger)
            {
                ((java.text.DecimalFormat) formatter)
                        .applyPattern( FORMAT_Integer );
            } else
            {
                ((java.text.DecimalFormat) formatter)
                        .applyPattern( FORMAT_Floating );
            }

            result += "( ";
            if (isInteger)
            {
                for (int row = 0; row < numRows; ++row)
                {
                    numCols = matrix[row].length;
                    if (0 != row)
                        result += ',';
                    result += "(";
                    for (int col = 0; col < numCols; ++col)
                    {
                        if (0 != col)
                            result += ',';
                        result += formatter.format( matrix[row][col]
                                .longValue() );
                    }
                    result += " )";
                }
            } else
            {
                for (int row = 0; row < numRows; ++row)
                {
                    numCols = matrix[row].length;
                    if (0 != row)
                        result += ',';
                    result += "(";
                    for (int col = 0; col < numCols; ++col)
                    {
                        if (0 != col)
                            result += ',';
                        result += formatter.format( matrix[row][col]
                                .doubleValue() );
                    }
                    result += " )";
                }
            }

            result += " )";
        }

        return (result);
    }

    /**
     * Convert a matrix of integers (short, int, or long) to a string.
     * 
     * @param matrix
     *            The array of array of numbers.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_MatrixInt( Object matrix )
    {
        final int TYPE_unknown = 0;
        final int TYPE_Short = 1;
        final int TYPE_Int = 2;
        final int TYPE_Long = 3;

        String result = new String();
        int typeId = TYPE_unknown;
        int numRows = 0;
        int numCols = 0;

        if (matrix instanceof short[][])
        {
            typeId = TYPE_Short;
            numRows = ((short[][]) matrix).length;

        } else if (matrix instanceof int[][])
        {
            typeId = TYPE_Int;
            numRows = ((int[][]) matrix).length;

        } else if (matrix instanceof long[][])
        {
            typeId = TYPE_Long;
            numRows = ((long[][]) matrix).length;
        }

        if (0 < numRows)
        {

            java.text.NumberFormat formatter = java.text.DecimalFormat
                    .getInstance();
            ((java.text.DecimalFormat) formatter).applyPattern( FORMAT_Integer );

            result += "( ";

            switch (typeId)
            {
                case TYPE_Short:
                    short[][] shortMatrix = (short[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = shortMatrix[row].length;
                        if (0 != row)
                            result += ',';
                        result += "(";
                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                                result += ',';
                            result += formatter.format( shortMatrix[row][col] );
                        }
                        result += " )";
                    }
                    break;
                case TYPE_Int:
                    int[][] intMatrix = (int[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = intMatrix[row].length;
                        if (0 != row)
                            result += ',';
                        result += "(";
                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                                result += ',';
                            result += formatter.format( intMatrix[row][col] );
                        }
                        result += " )";
                    }
                    break;
                case TYPE_Long:
                    long[][] longMatrix = (long[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = longMatrix[row].length;
                        if (0 != row)
                            result += ',';
                        result += "(";
                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                                result += ',';
                            result += formatter.format( longMatrix[row][col] );
                        }
                        result += " )";
                    }
                    break;
            }
            result += " )";
        }

        return (result);
    }

    /**
     * Convert a vector of floating point numbers (float, or double) to a
     * string.
     * 
     * @param matrix
     *            The array of array of numbers.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toString()
     */
    protected String toString_MatrixFloat( Object matrix )
    {
        final int TYPE_unknown = 0;
        final int TYPE_Float = 1;
        final int TYPE_Double = 2;

        String result = new String();
        int typeId = TYPE_unknown;
        int numRows = 0;
        int numCols = 0;

        if (matrix instanceof float[][])
        {
            typeId = TYPE_Float;
            numRows = ((float[][]) matrix).length;

        } else if (matrix instanceof double[][])
        {
            typeId = TYPE_Double;
            numRows = ((double[][]) matrix).length;

        }

        if (0 < numRows)
        {

            java.text.NumberFormat formatter = java.text.DecimalFormat
                    .getInstance();
            ((java.text.DecimalFormat) formatter)
                    .applyPattern( FORMAT_Floating );

            result += "( ";

            switch (typeId)
            {
                case TYPE_Float:
                    float[][] floatMatrix = (float[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = floatMatrix[row].length;
                        if (0 != row)
                            result += ',';
                        result += "(";
                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                                result += ',';
                            result += formatter.format( floatMatrix[row][col] );
                        }
                        result += " )";
                    }
                    break;
                case TYPE_Double:
                    double[][] doubleMatrix = (double[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = doubleMatrix[row].length;
                        if (0 != row)
                            result += ',';
                        result += "(";
                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                                result += ',';
                            result += formatter.format( doubleMatrix[row][col] );
                        }
                        result += " )";
                    }
                    break;
            }
            result += " )";
        }

        return (result);
    }

    /**
     * Convert a String to an XML element.
     * 
     * @param string
     *            The string to convert.
     * @param name
     *            The name of the string element.
     * @param prefix
     *            Preced each line with this text.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toXml(Object,String,String)
     */
    protected String toXml_String( String string, String name, String prefix )
    {
        String xml = null;
        String tag = ((null == name) ? "String" : name);
        String pre = ((null == prefix) ? "" : prefix);

        xml = pre + "<" + tag + " objType=\"string\"";

        if (null == string)
        {
            xml += "/>\r\n";
        } else
        {
            xml += ">" + string + "</" + tag + ">\r\n";
        }

        return (xml);
    }

    /**
     * Convert a java.lang.Number to an XML element.
     * 
     * @param scalar
     *            The number to convert.
     * @param name
     *            The name of the string element.
     * @param isInteger
     *            Is the number an integer or floating point?
     * @param prefix
     *            Preced each line with this text.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toXml(Object,String,String)
     */
    protected String toXml_Scalar( Number scalar, String name,
            boolean isInteger, String prefix )
    {
        String xml = null;
        String tag = ((null == name) ? "Scalar" : name);
        String pre = ((null == prefix) ? "" : prefix);

        xml = pre + "<" + tag + " objType=\"scalar\"";

        if (isInteger)
        {
            xml += " dataType=\"int32_t\"";
        } else
        {
            xml += " dataType=\"double\"";
        }

        if (null == scalar)
        {
            xml += "/>\r\n";
        } else
        {
            xml += ">" + scalar.toString() + "</" + tag + ">\r\n";
        }

        return (xml);
    }

    /**
     * Convert a vector (1-dimensional array) to an XML element.
     * 
     * @param vector
     *            The vector to convert.
     * @param name
     *            The name of the string element.
     * @param prefix
     *            Preced each line with this text.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toXml(Object,String,String)
     */
    protected String toXml_Vector( Object vector, String name, String prefix )
    {
        final int TYPE_unknown = 0;
        final int TYPE_AtomicShort = 1;
        final int TYPE_AtomicInt = 2;
        final int TYPE_AtomicLong = 3;
        final int TYPE_AtomicFloat = 4;
        final int TYPE_AtomicDouble = 5;
        final int TYPE_ObjectShort = 6;
        final int TYPE_ObjectInt = 7;
        final int TYPE_ObjectLong = 8;
        final int TYPE_ObjectFloat = 9;
        final int TYPE_ObjectDouble = 10;

        String xml = null;
        String tag = ((null == name) ? "Vector" : name);
        String pre = ((null == prefix) ? "" : prefix);
        int typeId = TYPE_unknown;
        int numRows = 0;

        xml = pre + "<" + tag + " objType=\"vector\"";

        if (vector instanceof short[])
        {
            typeId = TYPE_AtomicShort;
            xml += " dataType=\"int16_t\"";
            numRows = ((short[]) vector).length;

        } else if (vector instanceof int[])
        {
            typeId = TYPE_AtomicInt;
            xml += " dataType=\"int32_t\"";
            numRows = ((int[]) vector).length;

        } else if (vector instanceof long[])
        {
            typeId = TYPE_AtomicLong;
            xml += " dataType=\"int32_t\"";
            numRows = ((long[]) vector).length;

        } else if (vector instanceof float[])
        {
            typeId = TYPE_AtomicFloat;
            xml += " dataType=\"float\"";
            numRows = ((float[]) vector).length;

        } else if (vector instanceof double[])
        {
            typeId = TYPE_AtomicDouble;
            xml += " dataType=\"double\"";
            numRows = ((double[]) vector).length;

        } else if (vector instanceof Short[])
        {
            typeId = TYPE_ObjectShort;
            xml += " dataType=\"int16_t\"";
            numRows = ((Short[]) vector).length;

        } else if (vector instanceof Integer[])
        {
            typeId = TYPE_ObjectInt;
            xml += " dataType=\"int32_t\"";
            numRows = ((Integer[]) vector).length;

        } else if (vector instanceof Long[])
        {
            typeId = TYPE_ObjectLong;
            xml += " dataType=\"int32_t\"";
            numRows = ((Long[]) vector).length;

        } else if (vector instanceof Float[])
        {
            typeId = TYPE_ObjectFloat;
            xml += " dataType=\"float\"";
            numRows = ((Float[]) vector).length;

        } else if (vector instanceof Double[])
        {
            typeId = TYPE_ObjectDouble;
            xml += " dataType=\"double\"";
            numRows = ((Double[]) vector).length;
        }

        if (0 == numRows)
        {
            xml += "/>\r\n";
        } else
        {

            // Java 5 takes care of this....
            switch (typeId)
            {
                case TYPE_AtomicShort:
                    short[] shortVector = (short[]) vector;
                    xml += " size=\"" + shortVector.length + "\">";
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                        {
                            xml += ' ';
                        }
                        xml += String.valueOf( shortVector[row] );
                    }
                    break;

                case TYPE_AtomicInt:
                    int[] intVector = (int[]) vector;
                    xml += " size=\"" + intVector.length + "\">";
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                        {
                            xml += ' ';
                        }
                        xml += String.valueOf( intVector[row] );
                    }
                    break;

                case TYPE_AtomicLong:
                    long[] longVector = (long[]) vector;
                    xml += " size=\"" + longVector.length + "\">";
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                        {
                            xml += ' ';
                        }
                        xml += String.valueOf( longVector[row] );
                    }
                    break;

                case TYPE_AtomicFloat:
                    float[] floatVector = (float[]) vector;
                    xml += " size=\"" + floatVector.length + "\">";
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                        {
                            xml += ' ';
                        }
                        xml += String.valueOf( floatVector[row] );
                    }
                    break;

                case TYPE_AtomicDouble:
                    double[] doubleVector = (double[]) vector;
                    xml += " size=\"" + doubleVector.length + "\">";
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                        {
                            xml += ' ';
                        }
                        xml += String.valueOf( doubleVector[row] );
                    }
                    break;

                case TYPE_ObjectShort:
                case TYPE_ObjectInt:
                case TYPE_ObjectLong:
                case TYPE_ObjectFloat:
                case TYPE_ObjectDouble:
                    Number[] numVector = (Number[]) vector;
                    xml += " size=\"" + numVector.length + "\">";
                    for (int row = 0; row < numRows; ++row)
                    {
                        if (0 != row)
                        {
                            xml += ' ';
                        }
                        xml += numVector[row].toString();
                    }
                    break;
            }
            xml += "</" + tag + ">\r\n";
        }

        return (xml);
    }

    /**
     * Convert a matrix to an XML element.
     *  
     * @param matrix    The matrix to convert.
     * @param name    The name of the string element.
     * @param prefix  Preced each line with this text.
     * @see edu.ucsb.cs.jicos.services.external.services.matlab.TaskMatlab#toXml(Object,String,String)
     */
    protected String toXml_Matrix( Object matrix, String name, String prefix )
    {
        final int TYPE_unknown = 0;
        final int TYPE_AtomicShort = 1;
        final int TYPE_AtomicInt = 2;
        final int TYPE_AtomicLong = 3;
        final int TYPE_AtomicFloat = 4;
        final int TYPE_AtomicDouble = 5;
        final int TYPE_ObjectShort = 6;
        final int TYPE_ObjectInt = 7;
        final int TYPE_ObjectLong = 8;
        final int TYPE_ObjectFloat = 9;
        final int TYPE_ObjectDouble = 10;

        String xml = null;
        String tag = ((null == name) ? "Matrix" : name);
        String pre = ((null == prefix) ? "" : prefix);
        int typeId = TYPE_unknown;
        int numRows = 0;

        // Start the result.
        xml = pre + "<" + tag + " objType=\"matrix\"";

        // Determine the data type.
        if (matrix instanceof short[][])
        {
            typeId = TYPE_AtomicShort;
            xml += " dataType=\"int16_t\"";
            numRows = ((short[][]) matrix).length;

        } else if (matrix instanceof int[][])
        {
            typeId = TYPE_AtomicInt;
            xml += " dataType=\"int32_t\"";
            numRows = ((int[][]) matrix).length;

        } else if (matrix instanceof long[][])
        {
            typeId = TYPE_AtomicLong;
            xml += " dataType=\"int32_t\"";
            numRows = ((long[][]) matrix).length;

        } else if (matrix instanceof float[][])
        {
            typeId = TYPE_AtomicFloat;
            xml += " dataType=\"float\"";
            numRows = ((float[][]) matrix).length;

        } else if (matrix instanceof double[][])
        {
            typeId = TYPE_AtomicDouble;
            xml += " dataType=\"double\"";
            numRows = ((double[][]) matrix).length;

        } else if (matrix instanceof Short[][])
        {
            typeId = TYPE_ObjectShort;
            xml += " dataType=\"int16_t\"";
            numRows = ((Short[][]) matrix).length;

        } else if (matrix instanceof Integer[][])
        {
            typeId = TYPE_ObjectInt;
            xml += " dataType=\"int32_t\"";
            numRows = ((Integer[][]) matrix).length;

        } else if (matrix instanceof Long[][])
        {
            typeId = TYPE_ObjectLong;
            xml += " dataType=\"int32_t\"";
            numRows = ((Long[][]) matrix).length;

        } else if (matrix instanceof Float[][])
        {
            typeId = TYPE_ObjectFloat;
            xml += " dataType=\"float\"";
            numRows = ((Float[][]) matrix).length;

        } else if (matrix instanceof Double[][])
        {
            typeId = TYPE_ObjectDouble;
            xml += " dataType=\"double\"";
            numRows = ((Double[][]) matrix).length;
        }

        if (0 == numRows)
        {
            xml += "/>\r\n";

        } else
        {

            xml += ">\r\n";
            int numCols;

            // Java 5 takes care of this....
            switch (typeId)
            {
                case TYPE_AtomicShort:
                    short[][] shortMatrix = (short[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = shortMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += String.valueOf( shortMatrix[row][col] );
                        }
                        xml += "</Row>\r\n";
                    }
                    break;

                case TYPE_AtomicInt:
                    int[][] intMatrix = (int[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = intMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += String.valueOf( intMatrix[row][col] );
                        }
                        xml += "</Row>\r\n";
                    }
                    break;

                case TYPE_AtomicLong:
                    long[][] longMatrix = (long[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = longMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += String.valueOf( longMatrix[row][col] );
                        }
                        xml += "</Row>\r\n";
                    }
                    break;

                case TYPE_AtomicFloat:
                    float[][] floatMatrix = (float[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = floatMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += String.valueOf( floatMatrix[row][col] );
                        }
                        xml += "</Row>\r\n";
                    }
                    break;

                case TYPE_AtomicDouble:
                    double[][] doubleMatrix = (double[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = doubleMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += String.valueOf( doubleMatrix[row][col] );
                        }
                        xml += "</Row>\r\n";
                    }
                    break;

                case TYPE_ObjectShort:
                case TYPE_ObjectInt:
                case TYPE_ObjectLong:
                    Number[][] numIMatrix = (Number[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = numIMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += numIMatrix[row][col].toString();
                        }
                        xml += "</Row>\r\n";
                    }
                    break;

                case TYPE_ObjectFloat:
                case TYPE_ObjectDouble:
                    Number[][] numFMatrix = (Number[][]) matrix;
                    for (int row = 0; row < numRows; ++row)
                    {
                        numCols = numFMatrix[row].length;
                        xml += prefix + "  <Row row=\"" + row + "\" cols=\""
                                + numCols + "\">";

                        for (int col = 0; col < numCols; ++col)
                        {
                            if (0 != col)
                            {
                                xml += ' ';
                            }
                            xml += numFMatrix[row][col].toString();
                        }
                        xml += "</Row>\r\n";
                    }
                    break;
            }
            xml += prefix + "</" + tag + ">\r\n";
        }

        return (xml);
    }

}
