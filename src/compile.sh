#!/bin/bash
###########################################
#Author: Fitzroy Nembhard
#Date: 2/29/2016
#Professor Marius Silaghi
#Network Programming CSE5232
##########################################

shopt -s nocasematch #force case-insensitive matches
ADDITIONAL_ARGS=""

TEMP=`getopt -o c "$@"` #use getopt to get options --> -c to clean
                        # make all if no options specified
#eval set -- "$TEMP"
while true ; do
    case "$1" in
    -c|-C) ADDITIONAL_ARGS=" clean";
     echo "Now cleaning all class files and deleting the default database"
     break;;
    esac ;
    case "$1" in
    "") ADDITIONAL_ARGS=" all";
     echo "Now compiling..."
     break;;
    esac ;
done
make $ADDITIONAL_ARGS