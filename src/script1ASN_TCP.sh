#!/bin/bash

HOST="localhost"
echo "Enter domain/host ip: "
read input
if [ "$input" != "" ]; then
    HOST=$input
fi

#start the server on port 3232
./run.sh 2323 &
sleep 1

#run client with example commands
./client.sh $HOST 2323 "PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;"
./client.sh $HOST 2323 "TAKE;USER:Johny;PROJECT:Exam;Buy paper"
./client.sh $HOST 2323 "GET_PROJECT;Exam"
./client.sh $HOST 2323 "GET_PROJECTS"

#kill the server process
ps -eaf | grep "org.fitz.netprog.RunClient -p 2323" | head -n+1 | awk '{ print $2 }' | xargs kill

ps -eaf | grep "org.fitz.netprog.RunServer -p 2323 -d org/fitz/netprog/data/projManagement.db" | head -n+1 | awk '{ print $2 }' | xargs kill