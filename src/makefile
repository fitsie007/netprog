###########################################
#Author: Fitzroy Nembhard & Nima Agli
#Date: 2/26/2016
#Professor Marius Silaghi
#Network Programming (CSE5232)
#Make file to compile project
###########################################


projpath = org/fitznima/netprog
lib = $(projpath)/lib/java-getopt-1.0.14.jar

sourcefiles = \
$(projpath)/Server.java \
$(projpath)/Client.java \
$(projpath)/main/runProjectManager.java\

classfiles  = $(sourcefiles:.java=.class)

#classfiles = Server.class Client.class projectManager.class

all: $(classfiles)

%.class: %.java
	javac -d . -classpath .:$(lib) $<
clean:
	rm -f $(projpath)/*.class $(projpath)/main/*.class
