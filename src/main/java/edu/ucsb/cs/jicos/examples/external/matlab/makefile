FRAMEWRK=${HOME}/Classes/Jicos/framework

default: system

compile:
	@(cd ${FRAMEWRK} && ant $@)

compile-all:
	@(cd ${FRAMEWRK} && ant $@)

runtime:
	@(cd ${FRAMEWRK} && ant runtime-jar)

runtime-all:
	@(cd ${FRAMEWRK} && ant clean runtime-jar)

system:
	@(cd ${FRAMEWRK} && ant $@)

system-all:
	@(cd ${FRAMEWRK} && ant $@)

jars:
	@(cd ${FRAMEWRK} && ant system runtime-jar)

jars-all:
	@(cd ${FRAMEWRK} && ant system-all runtime-jar)

test:
	@echo "test: ....Application linux12.engr.ucsb.edu"
	@java -Djava.security.policy=$(FRAMEWRK)/policy/policy		\
	     -classpath $(FRAMEWRK)/www/jicos-system.jar		\
	     edu.ucsb.cs.jicos.examples.external.matlab.Application	\
	     linux12.engr.ucsb.edu

test-mixed:
	@echo "test: ....MixedApplication linux12.engr.ucsb.edu"
	@java -Djava.security.policy=$(FRAMEWRK)/policy/policy		 \
	     -classpath $(FRAMEWRK)/www/jicos-system.jar		 \
	     edu.ucsb.cs.jicos.examples.external.matlab.MixedApplication \
	     linux12.engr.ucsb.edu

.PHONY: default test test-mixed
.PHONY: compile compile-all runtime runtime-all system system-all jars jars-all
