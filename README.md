# netprog
#### Network Programming (CSE5232)
#### Florida Institute of Technology
##### Professor Marius Silaghi
##### Spring 2016
##### Author: Fitz Roi
<hr>

netprog contains two projects:  
1. A simple email client ([See it here](../src/org/fitz/netprog/email)) that uses openssl to connect to an IMAP server and
   download all emails to the local computer
2. A client/server that sends and receives commands encoded in ASN1. ([See it here](..netprog/tree/master/src/org/fitz/netprog))


Divided in five (5) milestones, this project features a client/server that stores in a sqlite database the set of tasks associated with projects. 
Each task is associated with a start and an end time as well as a responsible person. 
The server marks as expired the tasks immediately after they were completed. 
The code presented here corresponds to milstone 5 of the project.
The client/server receives commands over UDP/TCP as ANS1 encoded bytes.

The server works with the following commands:
* PROJECT       --> defines a new project and saves the data in a database
* TAKE          --> allows a user to mark a task as completed
* PROJECTS      --> asks the server for a list of projects
* PROJECT       --> retrieves data regarding a project
* REGISTER      --> registers a client to receive periodic updates regarding a project
* LEAVE         --> unsubscribes a client from receiving updates

The ANS1 message formats are as follows:  
  Task ::= [1] SEQUENCE {name UTF8String, start GeneralizedTime, end GeneralizedTime, ip UTF8String OPTIONAL, port INTEGER OPTIONAL, done BOOLEAN OPTIONAL}  
  Project ::= [1] SEQUENCE {name UTF8String, tasks SEQUENCE OF Task }  
  ProjectOK ::= [0] SEQUENCE {code INTEGER, project Project OPTIONAL}  
  Projects ::= [2] SEQUENCE {} --- SEQUENCE OF INTEGER  
  ProjectsAnswer ::= [3] SEQUENCE OF Project  
  GetProject ::= [4] SEQUENCE {name UTF8String}  
  GetProjectResponse ::= [8] SEQUENCE { status UTF8String, studentname UTF8String, taskcount INTEGER, project Project}  
  Take ::= [5] SEQUENCE {user UTF8String, project UTF8String, task UTF8String}  
  UTF8String ::= [UNIVERSAL 12] IMPLICIT OCTET STRING  
  PrintableString ::= [UNIVERSAL 19] IMPLICIT OCTET STRING  
  Register ::= [13] SEQUENCE {project UTF8String}  
  Leave ::= [14] Register  

### Message examples
* **"PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;"**
To define the project "Exam", composed of two tasks. Each task is given as a sequence of three components (name, start time, end  time).
The first task is "Buy paper" starting on **"2015-03-12:18h30m00s001Z"**, i.e., 12/03/2015 at 18h30 and 1 millisecond, GMT, and ending on **"2015-03-15:18h30m00s001Z"**, i.e., 15/03/2015 at 18h30 and 1 millisecond, GMT. The second task is "Write exam" starting on **"2015-03-15:18h30m00s001Z"**, i.e., 15/03/2015 at 18h30 and 1 millisecond, GMT, and ending on **"2015-03-15:20h30m00s001Z"**, i.e., 15/03/2015 at 20h30 and 1 millisecond, GMT.  
    
* On success, the server answers with **"OK;PROJECT_DEFINITION:Exam"**, i.e. by prepending "OK;" to the first component of the received
  message. With TCP, the string is terminated either by new line (\n) or by the closing of the stream. Any other answer from the       server will be considered a failure.  
  On any failure the server returns "FAIL;..." where the remaining components after FAIL list the exact received request being answered.
* **"TAKE;USER:Johny;PROJECT:Exam;Buy paper"**
  To declare that the user Johny commits to acomplish the task "Buy Paper" for the Project "Exam".  
  The server replies on success with: "OK;TAKE;USER:Johny;PROJECT:Exam;Buy paper"  
* **"GET_PROJECTS"**
  To ask the server the list all the projects.  
  On success the server answers with: "OK;PROJECTS:2;Exam;Enigma", i.e., with "OK" followed by a component specifying the number of
  projects (2), followed by the list of the project names.
* **"GET_PROJECT;Exam"**
  To retrieve the whole set of tasks relevant for the project "Exam"  
  If the project is found, the server answers with a string **"OK;PROJECT_DEFINITION:Exam;TASKS:2;Buy   paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Johny;10.0.0.1;2501;Done;Write   exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;2016-03-18:18h30m00s001Z;2016-03-15:20h30m00s001Z;Mary;10.0.0.2;2505;Waiting"**,
  i.e. similar to the message defining the project, but each task gets 4 extra attributes (name owner, IP owner, port owner,
  flag completed). The flag for completed task is one of: Done,Waiting. We assume that each task undertaken by somebody was accomplish at the corresponding end time. The name, IP and poer of the takers are recorded from TAKE commands.

### Compiling and Executing
To compile the project, run *./compile.sh*  
To clean the class files, run *./compile.sh -c*  
To run the server, run *./run.sh 2323*, where 2323 represents a port

The following are test examples for the client:    
./client.sh 192.168.2.48 2323 "PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;"  
./client.sh 192.168.2.48 2323 "TAKE;USER:Johny;PROJECT:Exam;Buy paper"  
./client.sh 192.168.2.48 2323 "GET_PROJECT;Exam"  
./client.sh 192.168.2.48 2323 "GET_PROJECTS"  
./client.sh 192.168.2.48 2323 "REGISTER;Exam"  
./client.sh 192.168.2.48 2323 "LEAVE;Exam"  

<hr>


