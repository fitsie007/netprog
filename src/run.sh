#!/bin/bash
###########################################
#Author: Fitzroy Nembhard & Nima Agli
#Date: 2/26/2016
#Professor Marius Silaghi
#Network Programming CSE5232
##########################################

shopt -s nocasematch #force case-insensitive matches
PROJ_PATH="org/fitznima/netprog"
MAIN_PATH=.:$PROJ_PATH/:$PROJ_PATH"/"
LIB_PATH=$PROJ_PATH"/lib/java-getopt-1.0.14.jar:"$PROJ_PATH"/lib/sqlite-jdbc-3.8.11.2.jar"

DB_PATH=$PROJ_PATH"/data/projManagement.db"
USER_ARGS=""

if [ "$#" = 0 ]
then
	echo "Usage: run.sh -p <port> -d <database path>"
	echo "or run.sh <port> to use default database path ($DB_PATH)"
	exit 1
fi


if [ "$#" = 1 ] #assuming only the port specified
then
	USER_ARGS="-p "$1
	USER_ARGS+=" -d "$DB_PATH   
else
	USER_ARGS=$@ #both port and path supposedly supplied
fi

#Execute Program
java -cp $MAIN_PATH:$LIB_PATH org.fitznima.netprog.runServer $USER_ARGS

########THE END ###########
