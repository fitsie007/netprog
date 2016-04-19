###########################################
#Author: Fitzroy Nembhard & Nima Agli
#Date: 2/26/2016
#Professor Marius Silaghi
#Network Programming (CSE5232)
#Makefile to compile project
###########################################


projpath = org/fitznima/netprog
lib = $(projpath)/lib/*

sourcefiles = \
$(projpath)/RunServer.java \
$(projpath)/RunClient.java \


classfiles  = $(sourcefiles:.java=.class)

#classfiles = Server.class

all: $(classfiles)

%.class: %.java
	javac -d . -classpath .:$(lib) $<
clean:
	rm -f $(projpath)/*.class $(projpath)/constants/*.class $(projpath)/data/*.db
