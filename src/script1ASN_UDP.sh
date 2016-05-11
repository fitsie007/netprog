#!/bin/bash

#start the server on port 3232
echo "Enter domain/host ip: "
read input
if [ "$input" != "" ]; then
    HOST=$input
fi

./run.sh 2323 &
sleep 1
HOST="localhost"

#run client with example commands
./client.sh $HOST 2323 "PROJECT_DEFINITION_COMMAND:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;" -u
./client.sh $HOST 2323 "TAKE;USER:Johny;PROJECT:Exam;Buy paper" -u
./client.sh $HOST 2323 "GET_PROJECT;Exam" -u
./client.sh $HOST 2323 "GET_PROJECTS" -u
./client.sh $HOST 2323 "REGISTER;Exam" -u

#kill the server process
#ps -eaf | grep "org.fitz.netprog.RunClient -p 2323 -d org/fitz/netprog/data/projManagement.db" | head -n+1 | awk '{ print $2 }' | xargs kill

ps -eaf | grep "org.fitz.netprog.RunClient" | head -n+1 | awk '{ print $2 }' | xargs kill

ps -eaf | grep "org.fitz.netprog.RunServer -p 2323 -d org/fit/netprog/data/projManagement.db" | head -n+1 | awk '{ print $2 }' | xargs kill
