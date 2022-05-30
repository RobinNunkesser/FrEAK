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
 * This implements the Matlab interface.
 * 
 * This is the interface to the C API of Matlab. By using JNI, the C API methods
 * are called by Java functions. which is the preferred way to call Matlab (R14)
 * from Java, as all of their Java APIs are "subject to change".
 * 
 * Created on December 21, 2004, 3:08 PM
 * 
 * @author Pete Cappello
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services.matlab;

import edu.ucsb.cs.jicos.services.external.services.ProxyServiceExternal;
import edu.ucsb.cs.jicos.foundation.LogManager;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class MatlabImpl extends ProxyServiceExternal implements Matlab
{
    // Make sure JNI and Java agree on what is what.

    /** An unknown data type. */
    public static final int TYPE_Unknown = 0;

    /** A character (int8_t) data type. */
    public static final int TYPE_Char = 1;

    /** A unsigned character (uint8_t) data type. */
    public static final int TYPE_UChar = 2;

    /** A byte data (uint8_t) type. */
    public static final int TYPE_Byte = 3;

    /** A short int data (int16_t) type. */
    public static final int TYPE_Short = 4;

    /** A unsigned short data (int16_t) type. */
    public static final int TYPE_UShort = 5;

    /** A int data type. */
    public static final int TYPE_Int = 6;

    /** A unsigned int data type. */
    public static final int TYPE_UInt = 7;

    /** A long data (int32_t) type. */
    public static final int TYPE_Long = 8;

    /** A unsigned long data (uint32_t) type. */
    public static final int TYPE_ULong = 9;

    /** A long long data (int64_t) type. */
    public static final int TYPE_LLong = 10;

    /** A unsigned long long data (uint64_t) type. */
    public static final int TYPE_ULLong = 11;

    /** A boolean data type. */
    public static final int TYPE_Boolean = 12;

    /** A double data type. */
    public static final int TYPE_Double = 13;

    /** A float data type. */
    public static final int TYPE_Float = 14;

    /** A null(Java)/NULL(C) value. */
    public static final int TYPE_Null = 15;

    /** Is matlab available on this host. */
    public static final boolean isAvailable = isMatlabAvailable();

    /** The default command used to start the Matlab engine with. */
    public static final String DEFAULT_StartCommand = "";

    /** The default debug mode value. */
    public static final String DEFAULT_DebugStatus = "false"; // "true";

    /** The default start matlab engine value. */
    public static final boolean DEFAULT_StartEngine = true;

    /** The name of the JNI library. */
    private static final String NATIVE_LibraryName = "MatlabImpl_NATIVE";

    /** Base name of properties for this class. */
    public static final String PROPERTY_Base = "jicos.services.external.matlab";


    private static final Logger logger = LogManager.getLogger();
    private static final Level INFO = LogManager.INFO;
    private static final Level DEBUG = LogManager.DEBUG;

    //
    //-- Variables --------------------------------------------------------

    /** The current debug value. */
    public static boolean isDebug = false;

    /* The list of variables on the native side. */
    //private long variableList;
    // This causes problems on the native side.
    //
    //-- Constructors -----------------------------------------------------
    // If this is running on the matlab host, then load the JNI code.
    static
    {
        if (isAvailable)
        {
            System.loadLibrary( "MatlabImpl_NATIVE" );
        }
    }

    /**
     * Create a Matlab engine with the default starting arguments.
     * 
     * @throws MatlabProxyNotFoundException
     *             If the Matlab engine is not available on this machine.
     * @throws MatlabException
     *             If there was an exception whle starting the engine.
     */
    public MatlabImpl() throws MatlabProxyNotFoundException, MatlabException
    {
        String start = System.getProperty( PROPERTY_Base + ".startCommand" );

        Boolean debug = null;
        String propDebug = System.getProperty( PROPERTY_Base + ".debug" );
        if (null != propDebug)
        {
            char charDebug = propDebug.toLowerCase().charAt( 0 );

            switch (charDebug)
            {
                case 'y':
                case 't':
                case '1':
                    debug = Boolean.TRUE;
                    break;

                case 'n':
                case 'f':
                case '0':
                    debug = Boolean.FALSE;
                    break;
            }
        }

        initialize( start, debug );
    }

    /**
     * Create a Matlab engine with the given start arguments and debug mode.
     * 
     * @param startCommand
     *            Command to start Matlab engine with.
     * @param debugOn
     *            Turn on debugging output (especially within the JNI code).
     * @throws MatlabProxyNotFoundException
     *             If the Matlab engine is not available on this machine.
     * @throws MatlabException
     *             If there was an exception whle starting the engine.
     */
    public MatlabImpl(String startCommand, Boolean debugOn)
            throws MatlabProxyNotFoundException, MatlabException
    {
        this.initialize( startCommand, debugOn );
    }

    // Start up the engine.
    private void initialize( String startCommand, Boolean debugOn )
            throws MatlabProxyNotFoundException, MatlabException
    {

        // First check if it is even here.
        if (!isAvailable)
        {
            throw new MatlabProxyNotFoundException(
                    "Matlab is not available on this machine." );
        }

        // Fix parameters.
        String start = startCommand;
        if (null == start)
        {
            start = DEFAULT_StartCommand;
        }

        if (null == debugOn)
        {
            isDebug = Boolean.getBoolean( DEFAULT_DebugStatus );
        }
        else
        {
            isDebug = debugOn.booleanValue();
        }

	logger.log( DEBUG, "Starting engine...." );
        if (!startMatlabEngine( start ))
        {
            throw new MatlabProxyNotFoundException( "Couldn't start engine" );
        }
    }

    /**
     * Shut down the matlab engine.
     */
    public void close() throws MatlabException
    {
        stopMatlabEngine();
    }

    /**
     * Evaluate a Matlab command - helper method: automatically flush vars.
     * 
     * @param command
     *            The command to evaluate.
     * @param resultName
     *            The variable with the answer (null implies "JICOS")
     * @param nameValueMap
     *            A collection of name/value pairs.
     * @return The result of the computation.
     * @throws NumberFormatException
     *             If one of the data types is unknown.
     * @throws MatlabException
     *             On error in the engine.
     */
    public Object evaluate( String command, String resultName,
	    Map nameValueMap ) throws NumberFormatException, MatlabException
    {
       return( evaluate( command, resultName, nameValueMap, true ) );
    }

    /**
     * Evaluate a Matlab command.
     * 
     * @param command
     *            The command to evaluate.
     * @param resultName
     *            The variable with the answer (null implies "JICOS")
     * @param nameValueMap
     *            A collection of name/value pairs.
     * @param flushVariables
     *            Automatically remove variables when done?
     * @return The result of the computation.
     * @throws NumberFormatException
     *             If one of the data types is unknown.
     * @throws MatlabException
     *             On error in the engine.
     */
    public Object evaluate( String command, String resultName, Map nameValueMap, boolean flushVariables )
            throws NumberFormatException, MatlabException
    {
        Object answer = null;

        assert null != command;
        assert null != resultName;

        if (null != nameValueMap)
        {
            Iterator iterator = nameValueMap.keySet().iterator();
            while (iterator.hasNext())
            {
                String name = (String) iterator.next();
                Object value = nameValueMap.get( name );
                try
                {
                    long start = (new Date()).getTime();

                    double[][] matrix = convertValue( value );
                    putVariable( name, matrix );

                    long finis = (new Date()).getTime();
                    logger.log( INFO, "Loading \"" + name + "\" took "
			+ String.valueOf( finis - start ) + "ms to input." );
                }
                catch (NumberFormatException numberFormatException)
                {
                    String msg = "Variable \"" + name + "\": "
                            + numberFormatException.getMessage();
                    throw new NumberFormatException( msg );
                }
            }
        }

        ResultObject resultObject = new ResultObject();
	logger.log( DEBUG, "calling evalString()" );

	long start = (new Date()).getTime();
	boolean evalResult = evalString( command, resultName, resultObject );
	long finis = (new Date()).getTime();

        if (evalResult)
        {
            answer = resultObject.getResult();
        }
        if (isDebug)
        {
            logger.log( DEBUG, "returning: " + answer );
        }

        // Garbage collection over on the native side.
        removeVariables();

        return (answer);
    }

    /**
     *  Retrieve a variable from the Matlab engine.
     * 
     * @param varName  The name of the variable to retrieve.
     * @return The variable, or <CODE>null</CODE> if not available.
     * @throws MatlabException
     *             On error in the engine.
     */
    public Object getVariable( String varName ) throws MatlabException
    {
        return( null );
    }


    /**
     * Convert the data object to a double[][], which is what Matlab wants.
     * 
     * @param value
     *            The value object
     * @throws NumberFormatException
     *             If value is an unknown type.
     */
    private double[][] convertValue( Object value )
    {
        double[][] doubleMatrix = null;
        int rows = -1;
        int cols = -1;

        if (value instanceof double[][])
        {
            doubleMatrix = (double[][]) value;

            // double[] and Number
        }
        else if (value instanceof double[])
        {
            doubleMatrix = new double[1][];
            doubleMatrix[0] = (double[]) value;
        }
        else if (value instanceof Number)
        {
            doubleMatrix = new double[1][1];
            doubleMatrix[0][0] = ((Number) value).doubleValue();

            // int arrays.
        }
        else if (value instanceof int[][])
        {
            int[][] intMatrix = (int[][]) value;
            if ((0 < (rows = intMatrix.length))
                    && (0 < (cols = intMatrix[0].length)))
            {
                doubleMatrix = new double[rows][cols];
                for (int r = 0; r < rows; ++r)
                {
                    for (int c = 0; c < cols; ++c)
                    {
                        doubleMatrix[r][c] = (double) intMatrix[r][c];
                    }
                }
            }
        }
        else if (value instanceof int[])
        {
            int[] intVector = (int[]) value;
            if (0 < (rows = intVector.length))
            {
                doubleMatrix = new double[1][cols];
                for (int r = 0; r < rows; ++r)
                {
                    doubleMatrix[1][r] = (double) intVector[r];
                }
            }

            // long arrays.
        }
        else if (value instanceof long[][])
        {
            long[][] longMatrix = (long[][]) value;
            if ((0 < (rows = longMatrix.length))
                    && (0 < (cols = longMatrix[0].length)))
            {
                doubleMatrix = new double[rows][cols];
                for (int r = 0; r < rows; ++r)
                {
                    for (int c = 0; c < cols; ++c)
                    {
                        doubleMatrix[r][c] = (double) longMatrix[r][c];
                    }
                }
            }
        }
        else if (value instanceof long[])
        {
            long[] longVector = (long[]) value;
            if (0 < (rows = longVector.length))
            {
                doubleMatrix = new double[1][cols];
                for (int r = 0; r < rows; ++r)
                {
                    doubleMatrix[1][r] = (double) longVector[r];
                }
            }

            // short arrays.
        }
        else if (value instanceof short[][])
        {
            short[][] shortMatrix = (short[][]) value;
            if ((0 < (rows = shortMatrix.length))
                    && (0 < (cols = shortMatrix[0].length)))
            {
                doubleMatrix = new double[rows][cols];
                for (int r = 0; r < rows; ++r)
                {
                    for (int c = 0; c < cols; ++c)
                    {
                        doubleMatrix[r][c] = (double) shortMatrix[r][c];
                    }
                }
            }
        }
        else if (value instanceof short[])
        {
            short[] shortVector = (short[]) value;
            if (0 < (rows = shortVector.length))
            {
                doubleMatrix = new double[1][cols];
                for (int r = 0; r < rows; ++r)
                {
                    doubleMatrix[1][r] = (double) shortVector[r];
                }
            }

            // float arrays.
        }
        else if (value instanceof float[][])
        {
            float[][] floatMatrix = (float[][]) value;
            if ((0 < (rows = floatMatrix.length))
                    && (0 < (cols = floatMatrix[0].length)))
            {
                doubleMatrix = new double[rows][cols];
                for (int r = 0; r < rows; ++r)
                {
                    for (int c = 0; c < cols; ++c)
                    {
                        doubleMatrix[r][c] = (double) floatMatrix[r][c];
                    }
                }
            }
        }
        else if (value instanceof float[])
        {
            float[] floatVector = (float[]) value;
            if (0 < (rows = floatVector.length))
            {
                doubleMatrix = new double[1][cols];
                for (int r = 0; r < rows; ++r)
                {
                    doubleMatrix[1][r] = (double) floatVector[r];
                }
            }

            // What the $)(&)*& is this?!?
        }
        else
        {
            throw new NumberFormatException( "unknown data type: "
                    + value.getClass().getName() );
        }

        return (doubleMatrix);
    }

    /**
     * Is this the Matlab host?
     * 
     * If the engine is to be started, tries to locate the JNI and Matlab
     * libraries.
     * 
     * @return Matlab is availabel and to be started (<CODE>true</CODE>), or
     *         not (<CODE>false</CODE>).
     */
    private static boolean isMatlabAvailable()
    {
        boolean matlabIsAvailable = false;

        boolean startEngine = false;
        boolean isCorrectOs = false;
        boolean jniLibraryExists = false;
        String jniLibrary = null;
        boolean matlabLibraryExists = false;
        String matlabLibrary = null;
        String libExt = null;

        String os_name = System.getProperty( "os.name", "" ).toLowerCase();

        String startEngineString = (String) System.getProperty( PROPERTY_Base
                + ".startEngine", String.valueOf( DEFAULT_StartEngine ) );
        if (null == startEngineString)
            startEngine = DEFAULT_StartEngine;
        else if (startEngineString.equalsIgnoreCase( "yes" ))
        {
            startEngine = true;
        }
        else if (startEngineString.equalsIgnoreCase( "no" ))
        {
            startEngine = false;
        }
        else
        {
            startEngine = Boolean.valueOf( startEngineString ).booleanValue();
        }

        // If on a linux box....
        if (startEngine && "linux".equals( os_name ))
        {
            isCorrectOs = true;
            jniLibrary = File.separator + "lib" + NATIVE_LibraryName + ".so";
            matlabLibrary = File.separator + "libeng.so";

        } // Anything else, bail.

        if (startEngine && isCorrectOs)
        {

            String searchPath = System.getProperty( "java.library.path", "" );
            String[] element = searchPath.split( File.pathSeparator );

            for (int e = 0; (e < element.length) && !matlabIsAvailable; ++e)
            {

                // Is it a directory
                File dir = new File( element[e] );
                if (dir.isDirectory())
                {

                    if (doesExist( element[e] + jniLibrary ))
                    {
                        jniLibraryExists = true;
                    }
                    if (doesExist( element[e] + matlabLibrary ))
                    {
                        matlabLibraryExists = true;
                    }
                }
                matlabIsAvailable = (jniLibraryExists && matlabLibraryExists);
            }
        }

        return (matlabIsAvailable);
    }

    private static boolean doesExist( String fileName )
    {
        boolean doesExist = false;

        File file = new File( fileName );
        doesExist = file.exists();

        return (doesExist);
    }

    //
    //-- Native Methods ------------------------------------------------------

    /**
     * Start the matlab engine.
     * 
     * @param  args   Engine starup commands
     * @throws MatlabException On error in the engine.
     * @throws MatlabProxyNotFoundException No matlab engine available.
     * @throws MatlabException On error in the engine.
     */
    private static native boolean startMatlabEngine( String args )
            throws MatlabException, MatlabProxyNotFoundException;

    /**
     * Evaluate a Matlab command.
     * 
     * @param  command       The matlab command.
     * @param  resultVarName The name of the variable to get the answer.
     * @param  resultObject  The Java-side result.
     * @throws MatlabException On error in the engine.
     */
    private static native boolean evalString( String command,
            String resultVarName, ResultObject resultObject )
            throws MatlabException;

    /**
     * Insert a variable into the matlab engine.
     * 
     * @param name The name of the variable
     * @param matrix The value of the variable.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     * @throws MatlabException  On error in the engine.
     */
    private static native boolean putVariable( String name,
	    double[][] matrix ) throws MatlabException;

    /**
     * Get a variable from the matlab engine.
     * 
     * @param name The name of the variable
     * @param resultObject The value of the variable.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     * @throws MatlabException  On error in the engine.
     */
    private static native boolean getVariable( String name,
	    ResultObject resultObject ) throws MatlabException;

    /**
     * Do the memory management over on the native side.
     * 
     * @throws MatlabException On error in the engine.
     */
    private static native void removeVariables() throws MatlabException;

    /**
     * Stop the matlab engine.
     * 
     * @throws MatlabException
     *             On error in the engine.
     */
    private static native void stopMatlabEngine() throws MatlabException;

    //
    //-- Inner Classes -------------------------------------------------------

    /**
     * The result of a Matlab computation.
     * 
     * @author pippin
     */
    class ResultObject
    {

        //
        //-- Variables -------------------------------------------------

        private int valueType; // The type of answer.

        private boolean isArray; // Is this an array?

        private int rowSize; // If an array, this is the number of rows.

        private int colSize; // If an array, this is the number of rows.

        private long varInt; // Store all integer numbers here.

        private double varReal; // Store all floating point numbers here.

        private double[][] matrixReal;

        //
        //-- Constructors ----------------------------------------------

        ResultObject()
        {
            valueType = TYPE_Unknown;
            isArray = false;
            rowSize = 0;
            colSize = 0;
        }

        //
        //-- Constructors ----------------------------------------------

        /**
         * Get the type id.
         */
        int getTypeId()
        {
            return (this.valueType);
        }

        /**
         * Get the name of the type.
         */
        String getTypeName()
        {
            String result = null;

            switch (this.valueType)
            {
                case TYPE_Boolean:
                    result = "TYPE_Boolean";
                    break;
                case TYPE_Byte:
                    result = "TYPE_Byte";
                    break;
                case TYPE_Char:
                    result = "TYPE_Char";
                    break;
                case TYPE_Double:
                    result = "TYPE_Double";
                    break;
                case TYPE_Float:
                    result = "TYPE_Float";
                    break;
                case TYPE_Int:
                    result = "TYPE_Int";
                    break;
                case TYPE_LLong:
                    result = "TYPE_LLong";
                    break;
                case TYPE_Long:
                    result = "TYPE_Long";
                    break;
                case TYPE_Null:
                    result = "TYPE_Null";
                    break;
                case TYPE_Short:
                    result = "TYPE_Short";
                    break;
                case TYPE_UChar:
                    result = "TYPE_UChar";
                    break;
                case TYPE_UInt:
                    result = "TYPE_UInt";
                    break;
                case TYPE_ULLong:
                    result = "TYPE_ULLong";
                    break;
                case TYPE_ULong:
                    result = "TYPE_ULong";
                    break;
                case TYPE_UShort:
                    result = "TYPE_UShort";
                    break;
                default:
                case TYPE_Unknown:
                    result = "TYPE_Unknown";
                    break;
            }

            return (result);
        }

        boolean isArray()
        {
            return (this.isArray);
        }

        int getRowSize()
        {
            return (this.rowSize);
        }

        int getColSize()
        {
            return (this.colSize);
        }

        Object getResult()
        {
            Object result = null;
            if (this.isArray)
            {
                // System.out.println( "DEBUG MatlabImpl.java'getResult() --
                // Result is an array." );
                if (TYPE_Double == this.valueType)
                {
                    result = matrixReal;
                }
            }
            else
            {

                switch (this.valueType)
                {
                    case TYPE_Boolean:
                        if (0 == this.varInt)
                        {
                            result = new Boolean( false );
                        }
                        else
                        {
                            result = new Boolean( true );
                        }
                        break;

                    case TYPE_Byte:
                        result = new Byte( (byte) this.varInt );
                        break;

                    case TYPE_Char:
                        result = new Character( (char) this.varInt );
                        break;

                    case TYPE_Double:
                        result = new Double( this.varReal );
                        break;

                    case TYPE_Float:
                        result = new Float( (float) this.varReal );
                        break;

                    case TYPE_Int:
                        result = new Integer( (int) this.varInt );
                        break;

                    case TYPE_LLong:
                        result = new Long( this.varInt );
                        break;

                    case TYPE_Long:
                        result = new Long( this.varInt );
                        break;

                    case TYPE_Short:
                        result = new Short( (short) this.varInt );
                        break;

                    case TYPE_UChar:
                        result = new Character( (char) this.varInt );
                        break;

                    case TYPE_UInt:
                        result = new Long( this.varInt );
                        break;

                    case TYPE_ULLong:
                        result = new Long( this.varInt );
                        break;

                    case TYPE_ULong:
                        result = new Long( this.varInt );
                        break;

                    case TYPE_UShort:
                        result = new Long( this.varInt );
                        break;

                    case TYPE_Null:
                    case TYPE_Unknown:
                    default:
                        break;
                }

            }

            // System.out.println( "DEBUG MatlabImpl.java'getResult() -- result: " + result + " (" + result.getClass().getName() + ")" );
            return (result);
        }
    }

}
