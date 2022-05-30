//~~ utility.h ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
 *  Miscellaneous functions.
 *
 * @author:    Andy Pippin   <pippin@cs.ucsb.edu>
 * @version:   $Date$   $Revision$
 * @requires:   
 */

//==[ Identity ]================================================================

#ifndef  __UTILITY_H__
#define  __UTILITY_H__

#ifdef __UTILITY_C__
static char* hdrIdentity =
    "$Id$";
#endif


//==[ Includes ]================================================================

#include  "MatlabImpl_NATIVE.h"		// Java header.


//==[ Typedefs/Structures/Enums ]===============================================

enum _datatype_z {
    DATATYPE_IntMatrix,
    DATATYPE_IntVector,
    DATATYPE_IntScalar,
    DATATYPE_LongMatrix,
    DATATYPE_LongVector,
    DATATYPE_LongScalar,
    DATATYPE_FloatMatrix,
    DATATYPE_FloatVector,
    DATATYPE_FloatScalar,
    DATATYPE_DoubleMatrix,
    DATATYPE_DoubleVector,
    DATATYPE_DoubleScalar,

    DATATYPE_unknown =-1
};


typedef struct _variable_z
{
    mxArray*  matrix;
    struct _variable_z*  next;
}
  VARIABLE;


//==[ Constants ]===============================================================

#ifndef false
#   define false ((jboolean)(0))
#   define true ((jboolean)(~0))
#endif

#define  JAVATYPE_Unknown  ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Unknown)
#define  JAVATYPE_Char     ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Char)
#define  JAVATYPE_UChar    ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_UChar)
#define  JAVATYPE_Byte     ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Byte)
#define  JAVATYPE_Short    ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Short)
#define  JAVATYPE_UShort   ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_UShort)
#define  JAVATYPE_Int      ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Int)
#define  JAVATYPE_UInt     ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_UInt)
#define  JAVATYPE_Long     ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Long)
#define  JAVATYPE_ULong    ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_ULong)
#define  JAVATYPE_LLong    ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_LLong)
#define  JAVATYPE_ULLong   ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_ULLong)
#define  JAVATYPE_Boolean  ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Boolean)
#define  JAVATYPE_Double   ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Double)
#define  JAVATYPE_Float    ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Float)
#define  JAVATYPE_Null     ((int)edu_ucsb_cs_jicos_services_external_services_matlab_MatlabImpl_TYPE_Null)


//==[ Variables ]===============================================================

// Instanciated in MatlabImpl_NATIVE.c
extern Engine*   g_matlabEngine;
extern jboolean  g_isDebug;
extern VARIABLE* g_variableList;


//==[ Prototypes ]==============================================================

//------------------------------------------------------------------------------
/*
 *  Save the answer into the result object.
 *
 * @param   jniEnv         JNI Environment
 * @param   jResultObject  Java result object
 * @param   mxResult       Matlab result
 * @return  Success (true) or failure (false).
 */
	extern jboolean
saveAnswer(
    JNIEnv*  jniEnv,
    jobject  jResultObject,
    mxArray* mxResult
);


//------------------------------------------------------------------------------
/*
 *  Create and throw a MatlabException
 *
 * @param   jniEnv   JNI Environment
 * @param   msg      The detail message.
 */
	extern void
throwException(
    JNIEnv* jniEnv,
    const char*  msg
);


//------------------------------------------------------------------------------
/*
 *  description
 *
 * @param   arg   description
 * @param   arg   description
 * @return  returns
 */
	extern void
clearVariables(
    void
);


//------------------------------------------------------------------------------
/*
 *  description
 *
 * @param   arg   description
 * @param   arg   description
 * @return  returns
 */
	extern jboolean
addVariable(
    mxArray* variable
);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#endif // __UTILITY_H__
