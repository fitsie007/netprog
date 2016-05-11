#!/bin/bash
###########################################
#Author: Fitzroy Nembhard
#Date: 4/8/2016
#Professor Marius Silaghi
#Network Programming CSE5232
##########################################

shopt -s nocasematch #force case-insensitive matches
PROJ_PATH="org/fitz/netprog"
MAIN_PATH=.:$PROJ_PATH/:$PROJ_PATH"/"
LIB_PATH=$PROJ_PATH"/lib/*"
#LIB_PATH=$PROJ_PATH"/lib/java-getopt-1.0.14.jar:"$PROJ_PATH"/lib/sqlite-jdbc-3.8.11.2.jar"

DB_PATH=$PROJ_PATH"/data/projManagement.db"
USER_ARGS=$@
PROTOCOL="-t" #default tcp flag

if [ "$#" = 0 ]
then
	echo "Usage: client.sh <host> <port> <command> <optional -u for udp or -t for tcp> "
	exit 1
fi

if [ "$#" = 3 ] || [ "$#" = 4 ]
then
	USER_ARGS="-d "$1 #domain parameter (e.g. olin.fit.edu)
	USER_ARGS+=" -p "$2 #port parameter
fi


if [ "$4" = "-u" ] #switch to udp
then
	PROTOCOL="-u"
fi

echo "Now executing the client..."
#Execute Program
java -cp ${MAIN_PATH}:${LIB_PATH} org.fitz.netprog.RunClient ${USER_ARGS} -c "$3" ${PROTOCOL}

########THE END ###########
