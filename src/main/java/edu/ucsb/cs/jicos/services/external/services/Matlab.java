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

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

final public class Matlab extends TaskExternal {

    //
    //-- Constants -------------------------------------------------------

    private static final boolean INTEGER = true;

    private static final boolean FLOATING = false;

    private static final String FORMAT_Integer = " ####0; -####0";

    private static final String FORMAT_Floating = " ##0.00; -##0.00";

    //
    //-- Variables -------------------------------------------------------

    private Vector data;

    private String command;

    private Map nameMap;

    //
    //-- Constructors ----------------------------------------------------

    public Matlab(String command)
    {
        this.initialize();

        this.command = command;
    }

    public Matlab(String command, Object data)
    {
        this.initialize();

        this.command = command;
        if (null != data)
        {
            this.data.add( data );
        }
    }

    public Matlab(String command, Object[] data)
    {
        this.initialize();

        this.command = command;
        if (null != data)
        {
            for (int d = 0; d < data.length; ++d)
            {
                this.data.add( data[d] );
            }
        }
    }

    private void initialize()
    {
        this.command = null;
        this.data = new Vector();
        this.nameMap = new HashMap();
    }

    //
    //-- Accessors -------------------------------------------------------

    public String getRawCommand()
    {
        return (this.command);
    }

    public String getCommand()
    {
        String result = null;
        boolean stop = false;

        if (null != this.command)
        {
            int first = 0, last = 0;
            result = new String();
            String preCommand = new String();
            int varIndex = 0;

            // $ --> \u2222, $$ --> \u3333
            String cmd = this.command.replace( '$', '\u2222' ).replaceAll(
                    "\u2222\u2222", "\u3333" );

            while (!stop)
            {

                if (-1 == (first = cmd.indexOf( "\u2222{", last )))
                {
                    stop = true;
                } else
                {
                    result += cmd.substring( last, first );
                    first += 2;
                    if (-1 == (last = cmd.indexOf( '}', first )))
                    {
                        stop = true;
                    } else
                    {
                        String varName = cmd.substring( first, last );
                        Object dataValue = getData( varName );
                        if (null == dataValue)
                        {
                            throw new NullPointerException( "No such data: \""
                                    + varName + '"' );
                        } else
                        {
                            boolean indexIsANumber;
                            try
                            {
                                Integer.parseInt( varName );
                                indexIsANumber = true;
                            } catch (NumberFormatException numberFormatException)
                            {
                                indexIsANumber = false;
                            }

                            if (dataValue instanceof Number)
                            {
                                result += dataValue.toString();
                            } else if (dataValue instanceof String)
                            {
                                result += dataValue;
                            } else
                            {
                                String var = null;
                                if (indexIsANumber)
                                {
                                    var = "var_" + varName;
                                } else
                                {
                                    var = varName;
                                }

                                preCommand += var + " = "
                                        + toString( dataValue ) + "; ";
                                result += var;
                            }
                        }

                        last++;
                    }
                }
            }

            // \u2222 --> \$
            result += cmd.substring( last ).replaceAll( "\u3333", "\u2222" )
                    .replace( '\u2222', '$' );
            result = preCommand + result;
        }

        return (result);
    }

    public Object[] getData()
    {
        return ((Object[]) this.data.toArray());
    }

    public Map getNameMap()
    {
        return (this.nameMap);
    }

    public void indexData( String name, Integer mapping )
    {
        this.nameMap.put( name, mapping );
    }

    public void indexData( String name, int mapping )
    {
        this.nameMap.put( name, new Integer( mapping ) );
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
            return ((Object) this.data.toArray());

        } else
        {
            int index = -1;
            Object nameMapIndex = this.nameMap.get( name );

            // Found entry in the name map.
            //
            if (null != nameMapIndex)
            {
                try
                {
                    // Index into the data.
                    if (nameMapIndex instanceof Number)
                    {
                        index = ((Number) nameMapIndex).intValue();
                        if ((0 <= index) && (index < this.data.size()))
                        {
                            dataItem = this.data.get( index );
                        }
                        // name value is a string: convert to integer.
                    } else if (nameMapIndex instanceof String)
                    {
                        index = Integer.parseInt( (String) nameMapIndex );
                        if ((0 <= index) && (index < this.data.size()))
                        {
                            dataItem = this.data.get( index );
                        }
                    }
                } catch (Exception anyException)
                {
                }

                // nameMapIndex is null, go straight to the data.
            } else
            {
                try
                {
                    index = Integer.parseInt( name );
                    if ((0 <= index) && (index < this.data.size()))
                    {
                        dataItem = this.data.get( index );
                    }
                } catch (Exception anyException)
                {
                }
            }
        }

        return (dataItem);
    }

    public String toString()
    {
        String[] className = getClass().getName().replace( '.', '\u2222' )
                .split( "\u2222" );
        String result = className[className.length - 1] + "[command=";

        if (null != this.command)
        {
            result += '"' + this.command + '"';
        }
        result += ",data.length=" + this.data.size();
        result += "]";

        return (result);
    }

    public String toString( Object object )
    {
        String result = null;

        if (null == object)
        {

            // Scalar - object integer.
        } else if ((object instanceof Short) || (object instanceof Integer)
                || (object instanceof Long))
        {
            result = toString_Scalar( (Number) object, INTEGER );

            // Scalar - object floating point.
        } else if (object instanceof Number)
        {
            result = toString_Scalar( (Number) object, FLOATING );

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

    private String toString_Scalar( Number scalar, boolean isInteger )
    {
        String result = null;
        java.text.NumberFormat formatter = java.text.DecimalFormat
                .getInstance();
        if (isInteger)
        {
            ((java.text.DecimalFormat) formatter).applyPattern( FORMAT_Integer );
        } else
        {
            ((java.text.DecimalFormat) formatter)
                    .applyPattern( FORMAT_Floating );
        }
        return (result);
    }

    private String toString_Vector( Number[] vector, boolean isInteger )
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

    private String toString_VectorInt( Object vector )
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

    private String toString_VectorFloat( Object vector )
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
            numRows = ((short[]) vector).length;

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

    private String toString_Matrix( Number[][] matrix, boolean isInteger )
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

    private String toString_MatrixInt( Object matrix )
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

    private String toString_MatrixFloat( Object matrix )
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

    private String toXml( Object object, String name, int index, String prefix )
    {
        String xml = new String();

        if (null == object)
        {
            xml = "<Object/>\r\n";

            // Integral scalar
        } else if ((object instanceof Short) || (object instanceof Integer)
                || (object instanceof Long))
        {
            xml = toXml_Scalar( (Number) object, name, index, INTEGER, prefix );

            // Flating point scalar
        } else if (object instanceof Number)
        {
            xml = toXml_Scalar( (Number) object, name, index, FLOATING, prefix );

            // String
        } else if (object instanceof String)
        {
            xml = toXml_String( (String) object, name, index, prefix );

            // Vector
        } else if ((object instanceof short[]) || (object instanceof int[])
                || (object instanceof long[]) || (object instanceof Short[])
                || (object instanceof Integer[]) || (object instanceof Long[]))
        {
            xml = toXml_Vector( (Number[]) object, name, index, INTEGER, prefix );
        } else if ((object instanceof Number[]) || (object instanceof float[])
                || (object instanceof double[]))
        {
            xml = toXml_Vector( (Number[]) object, name, index, FLOATING,
                    prefix );

            // Matrix
        } else if ((object instanceof short[][]) || (object instanceof int[][])
                || (object instanceof long[][])
                || (object instanceof Short[][])
                || (object instanceof Integer[][])
                || (object instanceof Long[][]))
        {
            xml = toXml_Matrix( (Number[][]) object, name, index, INTEGER,
                    prefix );
        } else if ((object instanceof Number[][])
                || (object instanceof float[][])
                || (object instanceof double[][]))
        {
            xml = toXml_Matrix( (Number[][]) object, name, index, FLOATING,
                    prefix );
        }

        return (xml);
    }

    private String toXml_String( String string, String name, int index,
            String prefix )
    {
        String xml = null;

        xml = prefix + "<String index=\"" + index + '"';
        if (null != name)
        {
            xml += " name=\"" + name + '"';
        }

        if (null == string)
        {
            xml += "/>\r\n";
        } else
        {
            xml += ">" + string + "</String>\r\n";
        }

        return (xml);
    }

    private String toXml_Scalar( Number scalar, String name, int index,
            boolean isInteger, String prefix )
    {
        String xml = new String();
        int numRows;

        xml = prefix + "<Scalar index=\"" + index + '"';
        if (null != name)
        {
            xml += " name=\"" + name + '"';
        }

        if (isInteger)
        {
            xml += " type=\"Integer\"";
        } else
        {
            xml += " type=\"Double\"";
        }

        if (null == scalar)
        {
            xml += "/>\r\n";
        } else
        {
            xml += ">" + scalar.toString() + "</Scalar>\r\n";
        }

        return (xml);
    }

    private String toXml_Vector( Number[] vector, String name, int index,
            boolean isInteger, String prefix )
    {
        String xml = new String();
        int numRows;

        xml = prefix + "<Vector index=\"" + index + '"';
        if (null != name)
        {
            xml += " name=\"" + name + '"';
        }

        if (isInteger)
        {
            xml += " type=\"Integer\"";
        } else
        {
            xml += " type=\"Double\"";
        }

        if ((null == vector) || (0 == (numRows = vector.length)))
        {
            xml += "/>\r\n";
        } else
        {

            xml += " size=\"" + numRows + "\">";

            for (int row = 0; row < numRows; ++row)
            {
                if (0 != row)
                    xml += ',';
                if (isInteger)
                {
                    xml += vector[row].longValue();
                } else
                {
                    xml += vector[row].doubleValue();
                }
            }
            xml += "</Vector>\r\n";
        }

        return (xml);
    }

    private String toXml_Matrix( Number[][] matrix, String name, int index,
            boolean isInteger, String prefix )
    {
        String xml = new String();
        int numRows;
        int numCols;

        xml = prefix + "<Matrix index=\"" + index + '"';
        if (null != name)
        {
            xml += " name=\"" + name + '"';
        }

        if (isInteger)
        {
            xml += " type=\"Integer\"";
        } else
        {
            xml += " type=\"Double\"";
        }

        if ((null == matrix) || (0 == (numRows = matrix.length)))
        {
            xml += "/>";
        } else
        {
            xml += " rows=\"" + numRows + "\">\r\n";

            for (int row = 0; row < numRows; ++row)
            {
                numCols = matrix[row].length;
                xml += prefix + "  <Row row=\"" + row + "\" cols=\"" + numCols
                        + "\">";

                for (int col = 0; col < numCols; ++col)
                {
                    if (0 != col)
                        xml += ',';
                    if (isInteger)
                    {
                        xml += matrix[row][col].longValue();
                    } else
                    {
                        xml += matrix[row][col].doubleValue();
                    }
                }
                xml += "</Row>\r\n";
            }
            xml += prefix + "</Matrix>\r\n";
        }

        return (xml);
    }

    //
    //-- Mutators --------------------------------------------------------

    public void setCommand( String command )
    {
        this.command = command;
    }

    public void setData( Object[] object )
    {

        this.data.clear();

        if (null != object)
        {
            for (int o = 0; o < object.length; ++o)
            {
                this.data.add( object[o] );
            }
        }

        return;
    }

    public void setData( Object object )
    {

        this.data.clear();

        if (null != object)
        {
            this.data.add( object );
        }

        return;
    }

    public void setNameMap( Map nameMap )
    {
        if (null != nameMap)
        {
            this.nameMap.clear();
            this.nameMap.putAll( nameMap );
        }
    }

    public void addData( int index, Object object )
    {
        try
        {
            int dataIndex = this.data.size();
            this.data.add( dataIndex, object );
            this.nameMap.put( new Integer( index ), new Integer( dataIndex ) );
        } catch (Exception ignore)
        {
        }
    }

    public void addData( String index, Object object )
    {
        if (null != index)
        {
            try
            {
                int dataIndex = this.data.size();
                this.data.add( dataIndex, object );
                this.nameMap.put( index, new Integer( dataIndex ) );
            } catch (Exception ignore)
            {
            }
        }
    }

    public int addData( Object object )
    {
        int index = this.data.size();
        this.data.add( index, object );

        return (index);
    }

    //
    //-- Perform ----------------------------------------------------

    public Object execute( Environment environment )
    {
        Object result = null;

        //do something matlab specific.
        if (null != this.command)
        {
        }

        return ((Object) result);
    }

    //~~ For ExternalRequests ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public Matlab()
    {
        this.initialize();
    }

    // Java class --> XML
    public String toXml( String prefix )
    {
        String xml = new String();

        xml += prefix + "<Matlab>\r\n";

        if (null == command)
        {
            xml += prefix + "  <Command/>\r\n";
        } else
        {
            xml += prefix + "  <Command>" + command + "</Command>\r\n";
        }

        if (0 == this.data.size())
        {
            xml += prefix + "  <Data/>\r\n";
        } else
        {
            int dataSize = this.data.size();

            // Build reverse index.
            Map reverseMap = new HashMap();
            java.util.Iterator iterator = this.nameMap.keySet().iterator();
            while (iterator.hasNext())
            {
                Object key = iterator.next();
                if ((key instanceof String) || (key instanceof Number))
                {
                    Integer val = (Integer) this.nameMap.get( key );
                    reverseMap.put( val, key.toString() );
                }
            }

            xml += prefix + "  <Data items=\"" + dataSize + "\">\r\n";
            for (int item = 0; item < dataSize; ++item)
            {
                String name = (String) reverseMap.get( new Integer( item ) );
                xml += toXml( this.data.get( item ), name, item, prefix
                        + "    " );
            }
            xml += prefix + "  </Data>\r\n";
        }
        xml += prefix + "</Matlab>\r\n";

        return (xml);
    }

    public boolean fromXml( ExternalData externalData )
    {
        boolean success = false;
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
        return (new XmlDocument( xml ));
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
                strFib = null;
        }

        /*
         int number = this.n;
         if (0 >= number)
         number = 8;
         */

        String html = "<HTML>\r\n" + "<HEAD>\r\n"
                + "  <TITLE>Fibonacci Solver</TITLE>\r\n" + "</HEAD>\r\n"
                + "<BODY>\r\n" + CollectorHttp.jicosHtmlHeader()
                + "<CENTER><H2>Fibonacci Solver</H2></CENTER>\r\n" + "\r\n";

        if (null != strFib)
        {
            html = html + "<!-- Answer from previous request. -->\r\n"
                    + "<!>\r\n"
                    + "<FONT COLOR=green>Previous Result:</FONT><BR>\r\n"
                    + "<BLOCKQUOTE>\r\n" + "<TABLE>\r\n" + "  <TR>\r\n"
                    + "    <TD VALIGN=\"center\">Fibonacci( <B>" // TODO + this.n
                    + "</B> )&nbsp;=&nbsp;</TD>\r\n"
                    + "    <TD VALIGN=\"center\"><FONT COLOR=blue SIZE=\"+2\">"
                    + strFib + "</FONT></TD>\r\n" + "  </TR>\r\n"
                    + "</TABLE>\r\n" + "</BLOCKQUOTE>\r\n" + "<BR>\r\n";
        }

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
                // TODO + number
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