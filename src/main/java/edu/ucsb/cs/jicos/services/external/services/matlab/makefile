#***************************************************************************#
#                                                                           #
#        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          #
#                                                                           #
#    Permission is hereby granted, free of charge, to any person obtaining  #
#  a copy of this software and associated documentation files (the          #
#  "Software"), to deal in the Software without restriction, including      #
#  without limitation the rights to use, copy, modify, merge, publish,      #
#  distribute, sublicense, and/or sell copies of the Software, and to       #
#  permit persons to whom the Software is furnished to do so, subject to    #
#  the following conditions:                                                #
#                                                                           #
#    The above copyright notice and this permission notice shall be         #
#  included in all copies or substantial portions of the Software.          #
#                                                                           #
#    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        #
#  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       #
#  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   #
#  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     #
#  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     #
#  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        #
#  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   #
#                                                                           #
#***************************************************************************#

#
# makefile
#
#	Makefile for Malab JNI code.
#
# Created on December 23, 2004
#


#--[ Programs and Flags ]-------------------------------------------------------

# Locations of critical files.
MATLAB_HOME = /eci/matlab
JAVA_HOME = /usr/java

# What type of machine is this.
HOST_System = $(shell uname -s | tr 'A-Z' 'a-z' 2>/dev/null)
HOST_Processor = $(shell uname -p | tr 'A-Z' 'a-z' 2>/dev/null)

# -- Determined values.
FRAMEWRK=$(shell cd ../../../../../../../../.. && pwd)
SOURCE	=$(FRAMEWRK)/source
BUILD	=$(FRAMEWRK)/build/classes
SLASH	=$(shell pwd | sed -e "s,$(SOURCE),," -e 's,^.,,')
DOTPATH	=$(shell echo $(SLASH) | sed -e 's,/,.,g')
NATIVE	=$(FRAMEWRK)/lib/external/linux-x86
JNI	=MatlabImpl_NATIVE

# -- Java values.
POLICY	=-Djava.security.policy=$(FRAMEWRK)/policy/policy
HOSTCONF=-Djicos.services.host.matlab.config=$(FRAMEWRK)/$(SLASH)/hostmatlab.properties
LOGCONF	=-Djicos.log.level=fine -Djicos.log.services.external.level=info
#JARFILES=$(FRAMEWRK)/lib/jicos-system.jar:$(FRAMEWRK)/lib/junit.jar
JARFILES=$(FRAMEWRK)/lib/jicos-system.jar


# -- Initial flag values.
CFLAGS	=-I$(MATLAB_HOME)/extern/include -I$(JAVA_HOME)/include 
LDFLAGS	=
LIBS	=-leng -lmx -lm

# -- Host specific values
ifeq ($(HOST_System),sunos)
    ifeq ($(HOST_Processor),sparc)
	CFLAGS += -I$(JAVA_HOME)/include/solaris
	LDFLAGS+= -L$(MATLAB_HOME)/bin/sol2
	#
	JAVA_LIB=$(JAVA_HOME)/jre/lib/sparc
	MATLAB_LIB=$(MATLAB_HOME)/bin/sol2
    endif
endif

ifeq ($(HOST_System),linux)
    CFLAGS += -I$(JAVA_HOME)/include/linux
    LDFLAGS+= -L$(JAVA_HOME)/jre/lib/i386
    LDFLAGS+= -L$(MATLAB_HOME)/bin/glnx86
    #
    JAVA_LIB=$(JAVA_HOME)/jre/lib/i386
    MATLAB_LIB=$(MATLAB_HOME)/bin/glnx86
endif

# -- Wrap evything up.
JAVA_LIBRARY_PATH=$(JAVA_LIB):$(MATLAB_LIB):$(NATIVE)
COMPILE.cc = gcc $(CFLAGS)
LINK.cc = gcc $(CFLAGS) $(LDFLAGS)


#--[ Targets ]------------------------------------------------------------------

DEFAULT=jni
default: $(DEFAULT)

# -- JNI library

javah: java
	javah -classpath $(BUILD) -o $(JNI).h $(DOTPATH).MatlabImpl

JNI_SRC	=$(JNI).c utility.c
JNI_OBJ	=$(JNI_SRC:%.c=$(NATIVE)/%.o)
JNI_LIB	=$(NATIVE)/lib$(JNI).so

$(NATIVE)/lib$(JNI).so: $(JNI_OBJ)
	@mkdir -p $(NATIVE)
	@echo "Linking:   .../$(shell basename $@)"
	@$(LINK.cc) -shared -fPIC -o $(JNI_LIB) $(JNI_OBJ) $(LIBS)

$(NATIVE)/%.o: %.c
	@echo "Compiling: $<"
	@mkdir -p $(NATIVE)
	@$(COMPILE.cc) -fPIC -o $@ -c $<

$(NATIVE)/$(JNI).o: $(JNI).c $(JNI).h
$(NATIVE)/insert.o: insert.c insert.h

jni: cleanoops $(JNI_LIB)

$(NATIVE)/$(JNI).o: $(JNI).c utility.h
$(NATIVE)/insert.o: insert.c insert.h utility.h
$(NATIVE)/utility.o: utility.c utility.h

java:
	@(cd $(FRAMEWRK) && ant -f build.xml)

jar:
	@(cd $(FRAMEWRK) && ant -f build.xml system)

jar-all:
	@(cd $(FRAMEWRK) && ant -f build.xml system-all)


#--[ Miscellaneous Targets ]----------------------------------------------------

.PHONY:	java jni javah jar  all rebuild help clean mrproper test hsp host info

all: jar jni

clean: cleanoops
	@rm -f $(JNI_LIB) $(JNI_OBJ) core.[0-9]* core hs_err_pid[0-9]*.log o
mrproper: clean
	@(cd $(FRAMEWRK) && ant -f build.xml clean)

cleanoops:
	@rm -f core.[0-9]* core hs_err_pid[0-9]*.log

rebuild: mrproper jni
	@(cd $(FRAMEWRK) && ant -f build.xml system-all)

#test: java jni
test: java jni
	@echo "Launching MatlabImplTest..."
	@echo
	@echo
	java -Djava.library.path=$(JAVA_LIBRARY_PATH) \
	    -classpath $(BUILD):$(FRAMEWRK)/lib/junit.jar \
	    $(DOTPATH).MatlabImplTest

hsp: jar
	#@echo "Launching HSP..."
	(echo "" && java $(POLICY) $(LOGCONF) -ea -classpath $(JARFILES) \
	    edu.ucsb.cs.jicos.services.Hsp localhost 0) &

host: jar
	java $(POLICY) $(HOSTCONF) $(LOGCONF) -ea -classpath $(JARFILES) \
	    edu.ucsb.cs.jicos.services.external.HostMatlab localhost

help:
	@echo
	@echo "Available targets:"
	@echo "  <default>   = $(DEFAULT)"
	@echo "  all         = Make both the Java and JNI library."
	@echo "  clean       = Clean up C object files."
	@echo "  help        = This message."
	@echo "  host        = Start a HostMatlab using a TS/HSP on localhost."
	@echo "  hsp         = Start up an HSP with 1 TS and 0 hosts."
	@echo "  info        = Display various information."
	@echo "  jar         = Build the Jicos jar file."
	@echo "  java        = Build the java files."
	@echo "  javah       = Build the JNI header file."
	@echo "  jni         = Build the JNI (C) library files"
	@echo "  mrproper    = Clean up C and _*ALL*_ Java files."
	@echo ""

info:
	@echo ""
	@echo "MATLAB_HOME = \"${MATLAB_HOME}\""
	@echo "JAVA_HOME = \"${JAVA_HOME}\""
	@echo "HOST_System = \"${HOST_System}\""
	@echo "HOST_Processor = \"${HOST_Processor}\""
	@echo "FRAMEWRK = \"${FRAMEWRK}\""
	@echo "SOURCE = \"${SOURCE}\""
	@echo "BUILD = \"${BUILD}\""
	@echo "SLASH = \"${SLASH}\""
	@echo "DOTPATH = \"${DOTPATH}\""
	@echo "NATIVE = \"${NATIVE}\""
	@echo "JNI = \"${JNI}\""
	@echo ""
	@echo "JNI library located at: \"${NATIVE}/lib${JNI}.so\""
	@echo ""
	@ls $(FRAMEWRK)
	@echo ""
