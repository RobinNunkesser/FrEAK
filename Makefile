JAR := target/RFrEAK-0.2-8.jar

.PHONY: all build test run clean

## Default: build without tests (tests fail inside freak-rinterface library)
all: build

## Compile and package, skipping tests
build:
	mvn package -Dmaven.test.skip=true

## Run unit tests (3 tests currently fail inside freak-rinterface:0.4.0 library code)
test:
	mvn test

## Run the application
run: $(JAR)
	java -jar $(JAR)

## Clean build output
clean:
	mvn clean

$(JAR):
	$(MAKE) build
