//~~ utility.c ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
 *  Miscellaneous functions.
 *
 * @author:    Andy Pippin   <pippin@cs.ucsb.edu>
 * @version:   $Date$   $Revision$
 * @requires:   
 */

//==[ Identity ]================================================================

#define  __UTILITY_C__
static char* srcIdentity = 
    "$Id$";


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

#define MATLAB_EXCEPTION "edu/ucsb/cs/jicos/services/external/matlab/MatlabException"

//==[ Variables ]===============================================================

//==[ Prototypes ]==============================================================

//------------------------------------------------------------------------------
/*
 *  description
 *
 * @param   arg   description
 * @param   arg   description
 * @return  returns
 */


//==[ Implementation ]==========================================================

	jboolean
saveAnswer(
    JNIEnv*  jniEnv,
    jobject  jResultObject,
    mxArray* mxResult
){
    jboolean    success =true;
    double*     data =NULL;
//    jclass      class =NULL;
    jfieldID    valueType;
    jfieldID    fieldId;

    if( NULL == g_matlabEngine ) {
	if( g_isDebug ) printf( "\n\nERROR: matlabEngine is NULL\n\n" );
	success = false;
	goto exception; // Really should throw exception.
    }
    if( NULL == jniEnv ) {
	if( g_isDebug ) printf( "\n\nERROR: jniEnv is NULL\n\n" );
	success = false;
	goto exception; // Really should throw exception.
    }
    if( NULL == jResultObject ) {
	if( g_isDebug ) printf( "\n\nERROR: jResultObject is NULL\n\n" );
	success = false;
	goto exception; // Really should throw exception.
    }


    // Get the data from the computation result.
    if( NULL != mxResult ) {
//	if( g_isDebug ) {
//	    printf( ". [done]\nGetting data..." );
//	    fflush( stdout );
//	}

    }

    // Flush.
//    if( g_isDebug ) {
//	printf( ". [done]\n" );
//	fflush( stdout );
//    }


    // If we got a NULL
    if( NULL == mxResult ) {
	setTypeId( jniEnv, jResultObject, JAVATYPE_Null );

    } else {

	switch( mxGetClassID( mxResult ) )
	{
	    case  mxDOUBLE_CLASS:
		setTypeId( jniEnv, jResultObject, JAVATYPE_Double );
//		realValue( jniEnv, jResultObject, (jdouble)(*(jdouble*)data) );
		saveRealValue( jniEnv, jResultObject, mxResult );
		break;

	    case  mxINT32_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_Long );
		intValue( jniEnv, jResultObject, (jlong)(*(int32_t*)data) );
		break;

	    // More popular above this line.

	    case  mxCHAR_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_Char );
		intValue( jniEnv, jResultObject, (jlong)(*(char*)data) );
		break;

	    case  mxSINGLE_CLASS:
		setTypeId( jniEnv, jResultObject, JAVATYPE_Float );
		realValue( jniEnv, jResultObject, (jdouble)(*(float*)data) );
		break;

	    case  mxINT8_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_Char );
		intValue( jniEnv, jResultObject, (jlong)(*(int8_t*)data) );
		break;

	    case  mxUINT8_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_UChar );
		intValue( jniEnv, jResultObject, (jlong)(*(uint8_t*)data) );
		break;

	    case  mxINT16_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_Short );
		intValue( jniEnv, jResultObject, (jlong)(*(int16_t*)data) );
		break;

	    case  mxUINT16_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_UShort );
		intValue( jniEnv, jResultObject, (jlong)(*(uint16_t*)data) );
		break;

	    case  mxUINT32_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_ULong );
		intValue( jniEnv, jResultObject, (jlong)(*(uint32_t*)data) );
		break;

	    case  mxINT64_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_LLong );
		intValue( jniEnv, jResultObject, (jlong)(*(int64_t*)data) );
		break;

	    case  mxUINT64_CLASS:
		setTypeId(jniEnv, jResultObject, JAVATYPE_ULLong );
		intValue( jniEnv, jResultObject, (jlong)(*(uint64_t*)data) );
		break;

	    case  mxUNKNOWN_CLASS:
	    default:
		setTypeId(jniEnv, jResultObject, JAVATYPE_Unknown );
		break;
	}
    }

exception:
    return( success );
}


//------------------------------------------------------------------------------

	void
throwException(
    JNIEnv* jniEnv,
    const char*  msg
){
    jclass matExcClass;

    if( NULL != jniEnv ) {

        matExcClass = (*jniEnv)->FindClass( jniEnv, MATLAB_EXCEPTION );
	if( NULL != matExcClass ) {
	    if( NULL == msg ) {
	        msg = "no message";
	    }
	    (*jniEnv)->ThrowNew( jniEnv, matExcClass, msg );
	}
    }
}


//------------------------------------------------------------------------------

	extern void
clearVariables(
    void
){
    VARIABLE* head;
    VARIABLE* scout;
    VARIABLE* nuke;

    if( NULL != g_variableList ) {
        scout = head = g_variableList->next;
        do {
	    nuke = scout;
	    scout = scout->next;

	    mxDestroyArray( nuke->matrix );
	    free( nuke );

	} while( scout != head );

	g_variableList = NULL;
    }

    return;
}


//------------------------------------------------------------------------------

	extern jboolean
addVariable(
    mxArray* variable
){
    jboolean result = false;
    VARIABLE* new = NULL;

    if( NULL != (new = (VARIABLE*)malloc( sizeof(VARIABLE) )) ) {
        new->matrix = variable;

	// First one?
	if( NULL == g_variableList ) {
	    new->next = new;
	    g_variableList = new;

	// Nope, so just append it.
	} else {
	    new->next = g_variableList->next;
	    g_variableList = new;
	}
    }

    return( result );
}


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
