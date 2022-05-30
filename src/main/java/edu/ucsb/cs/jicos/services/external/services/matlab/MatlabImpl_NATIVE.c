//== MatlabImpl_NATIVE.c =======================================================
/*
 *  description
 *
 * @author:    Andy Pippin   <pippin@cs.ucsb.edu>
 * @version:   $LastChangeddate$   $LastChangedRevision: 269 $
 * @requires:  Java RMI, Matlab
 */

//==[ Identity ]================================================================

#define  __RM_C__
static char* srcIdentity =
    "$Id: MatlabImpl_NATIVE.c 269 2005-04-05 06:13:24Z pippin $";


//==[ Includes ]================================================================

#include  <stdint.h>
#include  <stdio.h>
#include  <stdlib.h>
#include  <sys/types.h>
#include  <engine.h>			// Matlab engine.

#include  "MatlabImpl_NATIVE.h"		// Java header.
#include  "utility.h"			// Miscellaneous functions.


//==[ Typedefs/Structures/Enums ]===============================================

//==[ Constants ]===============================================================

static jboolean const  g_hasTypeId = true;
#define  VARNAME_TypeId "valueType"
#define  VARTYPE_TypeId "I"
typedef jint TYPECAST_TypeId;
//
static jboolean const  g_hasIsArray = true;
#define  VARNAME_IsArray "isArray"
#define  VARTYPE_IsArray "Z"
typedef jboolean TYPECAST_IsArray;
//
static jboolean const  g_hasNone = false;
#define  VARNAME_None NULL
#define  VARTYPE_None NULL
typedef jint TYPE_None;
//
static jboolean const  g_hasInteger = true;
#define  VARNAME_Int  "varInt"
#define  VARTYPE_Int  "J" // long
typedef jlong TYPE_Integer;
//
static jboolean const  g_hasReal = true;
#define  VARNAME_Real "varReal"
#define  VARTYPE_Real "D" // double
typedef jint TYPE_Real;
//
static jboolean const  g_hasImaginary = false;
#define  VARNAME_Imag NULL
#define  VARTYPE_Imag NULL
typedef jint TYPE_Imaginary;  // TODO
//
static jboolean const  g_hasMatrixReal = true;
#define  VARNAME_MatrixReal "matrixReal"
#define  VARTYPE_MatrixReal "[[D" // double

/*   Atomic Types.
Signature	Java Type
---------	---------
    Z		boolean
    B		byte
    C		char
    S		short
    I		int
    J		long
    F		float
    D		double
*/

//==[ Variables ]===============================================================

Engine*   g_matlabEngine = NULL;
jboolean  g_isDebug = false;
VARIABLE* g_variableList = NULL;

static jfieldID  g_FIELDID_TypeId = NULL;
static jfieldID  g_FIELDID_IsArray = NULL;
static jfieldID  g_FIELDID_None = NULL;
static jfieldID  g_FIELDID_Int = NULL;
static jfieldID  g_FIELDID_Real = NULL;
static jfieldID  g_FIELDID_Imag = NULL;
static jfieldID  g_FIELDID_MatrixReal = NULL;



//==[ Prototypes ]==============================================================

// See utility.h

//==[ Implementation ]==========================================================

jboolean debug( int number ) {
    printf( "      ... %d\n", number ); fflush(stdout);
    return( false );
}

jboolean runTest( void ){
    double time[10] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 };
    mxArray* T = NULL;
    mxArray* D = NULL;
    int retCode;
    char buffer[1024];

    T = mxCreateDoubleMatrix( 1, 10, mxREAL );
    memcpy( (void*)mxGetPr(T), (void*)time, sizeof(time) );
    retCode = engPutVariable( g_matlabEngine, "T", T );
    printf( "engPutVariable() was %ssuccesful\n", (0==retCode) ?"": "NOT " );

    engOutputBuffer(g_matlabEngine, buffer, sizeof(buffer) );
//    retCode = engEvalString( g_matlabEngine, "D = .5 *(-9.8) *T ^2;" );
    retCode = engEvalString( g_matlabEngine, "D = 1:5" );
    printf( "engEvalString() was %ssuccesful\n", (0==retCode) ?"": "NOT " );
    printf( "buffer = \n%s", buffer+2 );

    return( false );
}

//-- JNI Methods ---------------------------------------------------------------


	JNIEXPORT jboolean JNICALL 
Java_edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_startMatlabEngine(
    JNIEnv* jniEnv,
    jclass  jThis,
    jstring jStartCommand
){
    // Variables
    jboolean  engineStarted = false;	// Success or failure.
    const char* startCommand = "";	// Commands to start Matlab with.
    jclass thisClass;			// this.getClass();
    jfieldID fieldId;			// JNI filed id.
    static char outputBuffer[ 8192 ];	// Result of Matlab.

    // Safety
    if( NULL == jniEnv ) {
        fprintf( stderr, "JNI Environment is NULL!\n" );
	goto exception;
    }
    if( NULL == jThis ) {
        fprintf( stderr, "jThis is NULL!\n" );
	goto exception;
    }
    if( NULL == jStartCommand ) {
        throwException( jniEnv, "Matlab start command is null" );
	goto exception;
    }

    // Get debug status.
    thisClass = (*jniEnv)->GetObjectClass( jniEnv, jThis );
    fieldId = (*jniEnv)->GetStaticFieldID( jniEnv, jThis, "isDebug", "Z" );
    g_isDebug = (*jniEnv)->GetStaticBooleanField( jniEnv, thisClass, fieldId );


    // Before the engine is even tried to be started, make sure that the 
    // memory size of a jlong is enough to hold a memory pointer.
    //
/*
    if( sizeof(VARIABLE*) > sizeof(jlong) ) {
        char errMsg[ 256 ];
	snprintf( errMsg, sizeof(errMsg),
			"Size of java long (%d) cannot hold a C address pointer (%d)",
			sizeof(jlong), sizeof(VARIABLE*) );
        throwException( jniEnv, errMsg );
	return( false );
    }
*/
    g_variableList = NULL;


    // Get any start commands for the matlab engine.
    if( NULL != jStartCommand ) {
        startCommand = (*jniEnv)->GetStringUTFChars( jniEnv, jStartCommand, 0 );
    }

    // "...and the land of the free, Start your engines!"
//    if( g_isDebug ) {
//	printf( "\nStarting matlab engine..." ); fflush( stdout );
//    }
    g_matlabEngine = engOpen( startCommand );
    engineStarted = ( NULL != g_matlabEngine );
    if( g_isDebug ) {
        if( engineStarted ) {
	    printf( ". [done]\n" );
	} else {
	    printf( ". [FAILED!]\n\a" ); // And sound the alert.
	    throwException( jniEnv, "Could not start Matlab engine" );
	    goto exception;
	}
	fflush( stdout );
    }

    // Set the output buffer.
    engOutputBuffer( g_matlabEngine, outputBuffer, sizeof(outputBuffer) );

exception:
    // Clean up and return.
    if( NULL != jStartCommand ) {
        (*jniEnv)->ReleaseStringUTFChars( jniEnv, jStartCommand, startCommand );
    }

    return( engineStarted );
}


	JNIEXPORT jboolean JNICALL
Java_edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_evalString(
    JNIEnv* jniEnv,
    jclass  jThis,
    jstring jCommand,
    jstring jResultVarName,
    jobject jResultObject
){
    jboolean success = false;
    const char* command = NULL;
    const char* varName = NULL;
    mxArray* mxResult = NULL;
    jclass thisClass = NULL;
    jclass resultClass = NULL;

    if( g_isDebug ) {
        printf( "DEBUG [%s:%d] evalString() - enter.", __FILE__, __LINE__ );
	fflush( stdout );
    }

    if( NULL == jThis ) {
        goto exception;
    }
    if( NULL == g_matlabEngine ) {
	throwException( jniEnv, "Matlab is not started" );
        goto exception;
    }
    if( NULL == jCommand ) {
	throwException( jniEnv, "Matlab command is null" );
        goto exception;
    }
    if( NULL == jResultVarName ) {
	throwException( jniEnv, "result variable name is null" );
        goto exception;
    }

    thisClass = (*jniEnv)->GetObjectClass( jniEnv, jThis );
    resultClass = (*jniEnv)->GetObjectClass( jniEnv, jResultObject );

    // Get the field ids.
    //
    if( g_hasTypeId ) {
	g_FIELDID_TypeId = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_TypeId, VARTYPE_TypeId );
    }
    if( g_hasIsArray ) {
	g_FIELDID_IsArray = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_IsArray, VARTYPE_IsArray );
    }
    if( g_hasNone ) {
	g_FIELDID_None = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_None, VARTYPE_None );
    }
    if( g_hasInteger ) {
	g_FIELDID_Int = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_Int, VARTYPE_Int );
    }
    if( g_hasReal ) {
	g_FIELDID_Real = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_Real, VARTYPE_Real );
    }
    if( g_hasImaginary ) {
	g_FIELDID_Imag = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_Imag, VARTYPE_Imag );
    }
    if( g_hasMatrixReal ) {
	g_FIELDID_MatrixReal = (*jniEnv)->GetFieldID( 
			jniEnv, resultClass, VARNAME_MatrixReal, VARTYPE_MatrixReal );
    }


    // Get the command and result variable name.
    command = (*jniEnv)->GetStringUTFChars( jniEnv, jCommand, 0 );
    varName = (*jniEnv)->GetStringUTFChars( jniEnv, jResultVarName, 0 );

    // Evaluate the string.
    if( g_isDebug ) {
        printf( "DEBUG [%s:%d] evalString()'Invoking command...\n",
			    __FILE__, __LINE__ ); fflush( stdout );
    }
    engEvalString( g_matlabEngine, command );
    if( g_isDebug ) {
        printf( "DEBUG [%s:%d]  ... getting result.\n", __FILE__, __LINE__ );
	fflush( stdout );
    }
    mxResult = engGetVariable( g_matlabEngine, varName );
    if( g_isDebug ) {
        printf( "DEBUG [%s:%d]  ... saving answer:", __FILE__, __LINE__ );
	fflush( stdout );
    }
    success = saveAnswer( jniEnv, jResultObject, mxResult );
    if( g_isDebug ) {
        printf( "  [%s]\n", (success) ? "done" : "ERROR" );
        printf( "DEBUG [%s:%d]  ... closing engine.\n", __FILE__, __LINE__ );
	fflush( stdout );
    }
    engEvalString( g_matlabEngine, "close;" );


exception:
    if( NULL != jCommand ) {
	(*jniEnv)->ReleaseStringUTFChars( jniEnv, jCommand, command );
    }
    if( NULL != jResultVarName ) {
	(*jniEnv)->ReleaseStringUTFChars( jniEnv, jResultVarName, varName );
    }

    if( g_isDebug ) {
        printf( "DEBUG [%s:%d] evalString() - exiting with \"%s\"\n",
			__FILE__, __LINE__, (success) ? "true" : "false" );
	fflush( stdout );
    }

    return( success );
}


	JNIEXPORT jboolean JNICALL 
Java_edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_putVariable(
    JNIEnv* jniEnv,
    jclass  jThis,
    jstring jVarName,
    jobjectArray jMatrix
){
    // Variables
    jboolean  success =false;	// Success of insert.
    mxArray*  matrix =NULL;	// The Matlab array.
    size_t    dataSize;		// Size of memory to allocate.
    jdouble*  input =NULL;	// Input data.
    jdoubleArray  oneDim =NULL;	// A row of doubles.
    jclass thisClass =NULL;	// "this"'s class.
    jfieldID fieldId = NULL;	// The id of a variable.
    const char*  varName;	// The name of the variable.
    register int  numRows;	// Number of rows.
    register int  numCols;	// Number of columns
    register int  r, c;		// Loop variables (row and column).


    // In debug mode?
    if( g_isDebug ) {
	printf( "DEBUG [%s:%d] putVariable(): Entering...\n",
						    __FILE__, __LINE__ );
	fflush( stdout );
    }

    // Safety dance.
    if( NULL == jVarName )
    {
	throwException( jniEnv, "variable name cannot be null" );
        return( false );
	// *** DOES NOT CONTINUE ***
    }
    else if( NULL == jMatrix )
    {
	throwException( jniEnv, "matrix cannot be null" );
        return( false );
	// *** DOES NOT CONTINUE ***
    }

    // Get the variable name.
    varName = (*jniEnv)->GetStringUTFChars( jniEnv, jVarName, 0 );
    if( g_isDebug ) {
	printf( "DEBUG [%s:%d]  ...putVariable()'varName=\"%s\"\n",
		__FILE__, __LINE__, varName );
	fflush( stdout );
    }

    // Get the dimensions of the array.
    numRows = (*jniEnv)->GetArrayLength( jniEnv, jMatrix );
    oneDim = (jdoubleArray)((*jniEnv)->GetObjectArrayElement( jniEnv, jMatrix, 0 ));
    numCols = (*jniEnv)->GetArrayLength( jniEnv, oneDim );

    if( g_isDebug ) {
	printf( "DEBUG [%s:%d]  ...putVariable()'(rows,cols)=(%d,%d)\n",
		__FILE__, __LINE__, numRows, numCols );
	fflush( stdout );
    }


    // Allocate the memory
//char debug[ 1024 ];
//char tmp[ 32 ];
    if( NULL != (matrix = mxCreateDoubleMatrix( numRows, numCols, mxREAL )) )
    {
	double*  dPtr = mxGetPr( matrix );
//printf( "DEBUG [%s:%d]  ...putVariable()'matrix:\n", __FILE__, __LINE__ ); fflush( stdout );
	for( r=0; r<numRows; ++r )
	{
//strcpy( debug, "     " );
	    oneDim = (jdoubleArray)(*jniEnv)->GetObjectArrayElement( jniEnv, jMatrix, r );
	    if( NULL != oneDim )
	    {
		input = (*jniEnv)->GetDoubleArrayElements( jniEnv, oneDim, 0 );
		if( NULL != input )
		{
		    for( c=0; c<numCols; ++c, ++input, ++dPtr )
		    {
			*dPtr = (double)*input;
//snprintf( tmp, sizeof(tmp), "%5.3lf ", *dPtr );
//strncat( debug, tmp, sizeof(debug) );
		    }
//printf( "%s\n", debug );
		}
	    }
	}

	addVariable( matrix );
	engPutVariable( g_matlabEngine, varName, matrix );

	//if( NULL != matrix ) mxDestroyArray( matrix );

	success = true;
    }

    if( NULL != jVarName ) {
        (*jniEnv)->ReleaseStringUTFChars( jniEnv, jVarName, varName );
    }
    if( g_isDebug ) {
	printf( "DEBUG [%s:%d] putVariable(): exiting with \"%s\"\n",
		__FILE__, __LINE__, (success) ? "true" : "false" );
	fflush( stdout );
    }

    return( success );
}

/*
 * Class:     edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl
 * Method:    getVariable
 * Signature: (Ljava/lang/String;Ledu/ucsb/cs/jicos/services/external/services/matlab/MatlabImpl$ResultObject;)Z
 */
	JNIEXPORT jboolean JNICALL
Java_edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_getVariable(
    JNIEnv*  jniEnv,
    jclass   jThis,
    jstring  jVarName,
    jobject  jResultObject
){
    jboolean     success = false;	// Result.
    const char*  varName = NULL;	// The name of the variable.
    mxArray*     mxResult = NULL;	// The value.

    // In debug mode?
    if( g_isDebug ) {
	printf( "DEBUG [%s:%d] getVariable(): Entering...\n",
						    __FILE__, __LINE__ );
	fflush( stdout );
    }

    // Safety dance.
    if( NULL == jVarName )
    {
	throwException( jniEnv, "variable name cannot be null" );
        return( false );
	// *** DOES NOT CONTINUE ***
    }
    else if( NULL == jResultObject )
    {
	throwException( jniEnv, "ResultObject cannot be null" );
        return( false );
	// *** DOES NOT CONTINUE ***
    }

    // Get the variable name.
    varName = (*jniEnv)->GetStringUTFChars( jniEnv, jVarName, 0 );
    if( g_isDebug ) {
	printf( "DEBUG [%s:%d]  ...getVariable()'varName=\"%s\"\n",
		__FILE__, __LINE__, varName );
	fflush( stdout );
    }

    // Save the variable.
    if( NULL != mxResult )
    {
	if( g_isDebug ) {
	    printf( "DEBUG [%s:%d]  ...getVariable(): saving answer...\n",
							__FILE__, __LINE__ );
	    fflush( stdout );
	}
	success = saveAnswer( jniEnv, jResultObject, mxResult );
    }

    // In debug mode?
    if( g_isDebug ) {
	printf( "DEBUG [%s:%d] getVariable() = %s: Exiting...\n",
			    __FILE__, __LINE__, (success) ? "true" : "false" );
	fflush( stdout );
    }

    return( success );
}


/*
 * Class:     edu_ucsb_cs_jicos_services_external_matlab_MatlabImpl
 * Method:    removeVariables
 * Signature: ()V
 */
	JNIEXPORT void JNICALL
Java_edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_removeVariables(
    JNIEnv* jniEnv,
    jclass  jThis
){
    clearVariables();
}


	JNIEXPORT void JNICALL
Java_edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_stopMatlabEngine(
    JNIEnv* jniEnv,
    jclass  jThis
){
    if( NULL != g_matlabEngine ) {
        engClose( g_matlabEngine );
    }
    return;
}



//-- Local methods -------------------------------------------------------------

	void
setTypeId(
    JNIEnv* jniEnv,
    jobject jResultObject,
    int   javaTypeCode
){
    if( g_hasTypeId && (NULL != jniEnv) && (NULL != jResultObject) ) {
	TYPECAST_TypeId val = (TYPECAST_TypeId)javaTypeCode;
	(*jniEnv)->SetIntField( jniEnv, jResultObject, g_FIELDID_TypeId, val );
    }
}

	void
setIsArray(
    JNIEnv* jniEnv,
    jobject jResultObject,
    jboolean  isArray
){
    if( g_hasTypeId && (NULL != jniEnv) && (NULL != jResultObject) ) {
	TYPECAST_IsArray val = (TYPECAST_IsArray)isArray;
	(*jniEnv)->SetBooleanField( jniEnv, jResultObject, g_FIELDID_IsArray, val );
    }
}

	void
intValue(
    JNIEnv* jniEnv,
    jobject jResultObject,
    jlong   val
){
    if( g_hasInteger && (NULL != jniEnv) && (NULL != jResultObject) ) {
	(*jniEnv)->SetLongField( jniEnv, jResultObject, g_FIELDID_Int, val );
    }
}

	void
realValue(
    JNIEnv* jniEnv,
    jobject jResultObject,
    jdouble val
){
    if( g_hasReal && (NULL != jniEnv) && (NULL != jResultObject) ) {
	(*jniEnv)->SetDoubleField( jniEnv, jResultObject, g_FIELDID_Real, val );
    }
}

	void
realMatrix(
    JNIEnv* jniEnv,
    jobject jResultObject,
    jobject matrix
){
    if( g_hasReal && (NULL != jniEnv) && (NULL != jResultObject) ) {
	(*jniEnv)->SetObjectField( jniEnv, jResultObject, g_FIELDID_MatrixReal, matrix );
    }
}


	void
saveRealValue(
    JNIEnv* jniEnv,
    jobject jResultObject,
    mxArray* mxResult
){
    register int  numRows = 1;
    register int  numCols = 1;
    register int  r = 0;
    double*       data = NULL;
    int           numDim = 0;
    const int*    dim = NULL;


    numDim = mxGetNumberOfDimensions( mxResult );
    dim = mxGetDimensions( (const mxArray*)mxResult );
    data = mxGetPr( mxResult );

    if( 2 == numDim ) {
	numRows = *(dim++);
	numCols = *(dim++);

	if( (1 == numRows) && (1 == numCols) ) {
	    realValue( jniEnv, jResultObject, (jdouble)(*(jdouble*)data) );

	} else {
	    jdoubleArray row = (jdoubleArray)(*jniEnv)->NewDoubleArray(
					jniEnv, numRows );
	    jclass rowClass = (*jniEnv)->GetObjectClass( jniEnv, row );
	    jobjectArray matrix = (jobjectArray)(*jniEnv)->NewObjectArray(
					jniEnv, numRows, rowClass, 0 );

	    for( r = 0; r < numRows; ++r ) {
		row = (jdoubleArray)(*jniEnv)->NewDoubleArray(
					jniEnv, numCols );
		(*jniEnv)->SetDoubleArrayRegion(
					jniEnv, row, (jsize)0, numCols, data );
		(*jniEnv)->SetObjectArrayElement(
					jniEnv, matrix, r, row );

		data += numCols; // next row
	    }

	    setTypeId( jniEnv, jResultObject, JAVATYPE_Double );
	    setIsArray( jniEnv, jResultObject, true );
	    realMatrix( jniEnv, jResultObject, matrix );
	}

    } else {
	throwException( jniEnv, "Currently does not support <2 dimensions" );

    }
}


//==============================================================================
