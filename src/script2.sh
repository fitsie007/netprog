#!/bin/bash

#start the server on port 3232
./run.sh 3232 &
sleep 1

#Inform user of example, which will assign a task to a user
echo "TAKE;USER:Johny;PROJECT:Exam;Buy paper"

#use the sock tool as client to communicate with the server
sock :3232 <<EOF
TAKE;USER:Johny;PROJECT:Exam;Buy paper
EOF

#kill the server process
ps -eaf | grep "org.fitznima.netprog.runServer -p 3232 -d org/fitznima/netprog/data/projManagement.db" | head -n+1 | awk '{ print $2 }' | xargs kill



