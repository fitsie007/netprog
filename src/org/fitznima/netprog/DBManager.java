package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Authors: Fitzroy Nembhard & Nima Agli <br>
 * Date: 3/9/2016 <br>
 * Professor Marius Silaghi <br>
 * Network Programming CSE5232 <br>
 *
 * This class provides functions for SQLite Database Management.
 */
public class DBManager {
    private String dbPath;
    private Connection connection;
    public DBManager(String dbPath){
        this.dbPath = dbPath;
    }

    /**
     * This method attempts to connect to the SQLite database selected.
     * @return a connection to the database
     */
    public Connection connectToDB(){
        connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return connection;
    }

    /**
     * This method checks if a database exists
     * @param dbName the path to the database
     * @return true if database exists, false otherwise
     */
    public boolean databaseExists(String dbName){
        File file = new File(dbName);
        return file.exists();
    }



    public String saveNewProject(Project project) {
        try {

            String projectName = project.getProjectName();
            int numTasks = project.getTasks().size();

            //use a query to check if this project already defined (if count = 0)
            int count = 0;
            String queryStr = "SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE + " WHERE PROJECT_NAME=?";
            //use preparedStatement to sanitize input strings
            PreparedStatement countQuery = connection.prepareStatement(queryStr);
            countQuery.setString(1, projectName);
            ResultSet countResultSet = countQuery.executeQuery();

            while (countResultSet.next()) {
                count = countResultSet.getInt("COUNT");

                if (count == 0) {
                    //loop and check if all date ranges valid
                    for (int i = 0, j=0; i < numTasks * 3; i += 3, j++) {
                        Task task = project.getTasks().get(j);
                        Date startTime = task.getStartTime();
                        Date endTime = task.getEndTime();

                        if (!Util.isValidDateRange(startTime, endTime))
                            return ProjectConstants.FAIL + ";";  //+ message;
                    }

                    //Now, insert this project-name into the projects table
                    queryStr = "INSERT INTO " + ProjectConstants.PROJECTS_TABLE + " (PROJECT_NAME) VALUES(?)";
                    PreparedStatement insertProjQuery = connection.prepareStatement(queryStr);
                    insertProjQuery.setString(1, projectName);

                    int rowsAffected = insertProjQuery.executeUpdate();

                    //if project inserted successfully, then attempt to insert tasks
                    if (rowsAffected > 0) {

                        //loop and insert the tasks in the tasks table
                        for (int i = 0, j=0; i < numTasks * 3; i += 3, j++) {
                            Task task = project.getTasks().get(j);
                            String taskName = task.getTaskName();
                            Date startTime = task.getStartTime();
                            Date endTime = task.getEndTime();

                            queryStr = "INSERT INTO " + ProjectConstants.TASKS_TABLE +
                                    " (TASK_NAME, START_TIME, END_TIME, PROJECT_NAME, COMPLETED_FLAG) " +
                                    "VALUES (?,?,?,?,?)";

                            PreparedStatement insertTaskQuery = connection.prepareStatement(queryStr);
                            insertTaskQuery.setString(1, taskName);
                            insertTaskQuery.setString(2, startTime.toString());
                            insertTaskQuery.setString(3, endTime.toString());
                            insertTaskQuery.setString(4, projectName);
                            insertTaskQuery.setString(5, ProjectConstants.PENDING_FLAG);

                            insertTaskQuery.executeUpdate(); //save task to database
                        }
                        connection.close();
                        return ProjectConstants.OK + ";"; //+ messageParts[0];
                    }


                }

            }
            connection.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return ProjectConstants.FAIL;
    }


    public String takeTask(Take take) {
            try {

                String project = take.getProjectName();
                int count;
                //count the list of tasks for the specified project
                String queryStr = "SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=?";

                //use preparedStatement to sanitize input strings
                PreparedStatement countQuery = connection.prepareStatement(queryStr);
                countQuery.setString(1, project);
                ResultSet countResultSet =  countQuery.executeQuery();

                while (countResultSet.next()) {

                    count = countResultSet.getInt("COUNT");
                    if (count > 0) {
                        String msg = ProjectConstants.OK + ";" + ProjectConstants.PROJECT_DEFINITION + ";" + project + ";";

                        msg += ProjectConstants.TASKS_LABEL + ":" + count + "";
                        queryStr = "SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=?";
                        PreparedStatement selectQuery = connection.prepareStatement(queryStr);
                        selectQuery.setString(1, project);

                        //get the list of tasks for the specified project
                        ResultSet taskList = selectQuery.executeQuery();
//
                        while (taskList.next()) {
                            String taskName = taskList.getString("TASK_NAME");
                            String startDate = taskList.getString("START_TIME");
                            String endDate = taskList.getString("END_TIME");
                            String ownerName = taskList.getString("OWNER_NAME");
                            String ownerIP = taskList.getString("OWNER_IP");
                            int ownerPort = taskList.getInt("OWNER_PORT");
                            String completedFlag = taskList.getString("COMPLETED_FLAG");
                            msg += ";" + taskName + ";" + startDate + ";" + endDate + ";" + ownerName + ";" + ownerIP + ";" + ownerPort + ";" + completedFlag;

                        }
                        connection.close();
                        return msg;
                    }
                }
                connection.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        return ProjectConstants.FAIL +";";
    }


    public ProjectsAnswer getProjects() {
           try {
                Statement stmt = connection.createStatement();
                ResultSet countResultSet = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE + ";");
                ProjectsAnswer projectsAnswer = null;

                while (countResultSet.next()) {
                    int count = countResultSet.getInt("COUNT");
                    if (count > 0) {
                        projectsAnswer = new ProjectsAnswer();
                        ArrayList<Project> projects = new ArrayList<>();
                        int i=0;

                        ResultSet projList = stmt.executeQuery("SELECT PROJECT_NAME FROM " + ProjectConstants.PROJECTS_TABLE + ";");
                        while (projList.next()) {
                            String projectName = projList.getString("PROJECT_NAME");
                            String queryStr = "SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=?";
                            PreparedStatement selectQuery = connection.prepareStatement(queryStr);
                            selectQuery.setString(1, projectName);
                            ResultSet taskList = selectQuery.executeQuery();
//                            ArrayList<Task> tasks = new ArrayList<>();
//
                            while (taskList.next()) {
                                boolean done = false;
                                String taskName = taskList.getString("TASK_NAME");
                                String startDate = taskList.getString("START_TIME");
                                String endDate = taskList.getString("END_TIME");
                                String ownerName = taskList.getString("OWNER_NAME");
                                String ownerIP = taskList.getString("OWNER_IP");
                                int ownerPort = taskList.getInt("OWNER_PORT");
                                String completedFlag = taskList.getString("COMPLETED_FLAG");
                                if(completedFlag.equalsIgnoreCase(ProjectConstants.COMPLETED_FLAG))
                                    done = true;
//                                tasks.add(new Task(taskName, startDate, endDate, ownerIP, ownerPort, done));
                            }

                            Project project = new Project(projectName, null);
                            projects.add(project);
                        }
                        connection.close();
                        return new ProjectsAnswer(projects);
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        return null;
    }
}
