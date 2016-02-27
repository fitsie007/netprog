# netprog
### Network Programming (CSE5232) Project
This project features a server that stores in a sqlite database the set of tasks associated with projects. 
Each task is associated with a start and an end time, as well as a responsible person. 
The server marks as expired the tasks immediately after they were completed. 
It receives commands over TCP as strings, recognizing the commands below, 
where the date and description are separated with a ";" (no other strings may contain semicolumns ";")

The server works with the following commands:
* PROJECT_DEFINITION  --> defines a new project and saves the data in a database
* TAKE                --> allows a user to mark a task as completed
* GET_PROJECTS        --> asks the server for a list of projects
* GET_PROJECT         --> retrieves data regarding a project

