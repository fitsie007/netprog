#!/bin/bash

#start the server on port 3232
./run.sh 2323 &
sleep 1

#Inform user of example, which will create a sample project with two tasks
echo "PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-30:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;"

#use the nc tool as client to communicate with the server over UDP
nc -u localhost 2323 <<EOF
PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-30:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;
EOF

#kill the server process
ps -eaf | grep "org.fitznima.netprog.RunServer -p 3232 -d org/fitznima/netprog/data/projManagement.db" | head -n+1 | awk '{ print $2 }' | xargs kill
