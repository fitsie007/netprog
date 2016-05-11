package org.fitz.netprog;

import org.fitz.netprog.asnobjects.*;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Author: Fitzroy Nembhard <br>
 * Date: 3/9/2016 <br>
 * Professor Marius Silaghi <br>
 * Network Programming CSE5232 <br>
 *
 * This class provides functions for SQLite Database Management.
 */
public class DBManager {
    private String dbPath;
    private Connection connection;

    public DBManager(){}
    public DBManager(String dbPath) {
        this.dbPath = dbPath;
    }

    /**
     * This method attempts to connect to the SQLite database selected.
     *
     * @return a connection to the database
     */
    public Connection connectToDB() {
        connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return connection;
    }

    /**
     * This method checks if a database exists
     *
     * @param dbName the path to the database
     * @return true if database exists, false otherwise
     */
    public boolean databaseExists(String dbName) {
        File file = new File(dbName);
        return file.exists();
    }

    public String getDbPath() {
        return dbPath;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * This method saves a new project and its task information to the database
     * @param project the ASN1 project object from which to extract information
     * @return PASS or FAIL response
     */
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
                    for (int i = 0, j = 0; i < numTasks * 3; i += 3, j++) {
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
                        for (int i = 0, j = 0; i < numTasks * 3; i += 3, j++) {
                            Task task = project.getTasks().get(j);
                            String taskName = task.getTaskName();
                            Date startTime = task.getStartTime();
                            Date endTime = task.getEndTime();

                            queryStr = "INSERT INTO " + ProjectConstants.TASKS_TABLE +
                                    " (TASK_NAME, START_TIME, END_TIME, PROJECT_NAME, COMPLETED_FLAG) " +
                                    "VALUES (?,?,?,?,?)";

                            PreparedStatement insertTaskQuery = connection.prepareStatement(queryStr);
                            insertTaskQuery.setString(1, taskName);
                            insertTaskQuery.setString(2, Util.dbFormatDate(startTime));
                            insertTaskQuery.setString(3, Util.dbFormatDate(endTime));
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


    /**
     * This method assigns a task to a user
     * by updating the related fields in the database
     * @param take the ASN1 Take object
     * @return a PASS or FAIL response
     */
    public String takeTask(Take take, String ip, int port) {
        try {

            String projectName = take.getProjectName();
            String taskName = take.getTaskName();
            int count;
            //count the list of tasks for the specified project
            String queryStr = "SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=?";

            //use preparedStatement to sanitize input strings
            PreparedStatement countQuery = connection.prepareStatement(queryStr);
            countQuery.setString(1, projectName);
            ResultSet countResultSet = countQuery.executeQuery();

            while (countResultSet.next()) {

                count = countResultSet.getInt("COUNT");
                if (count > 0) {
                    String msg = ProjectConstants.OK + ";" + ProjectConstants.PROJECT_DEFINITION_COMMAND + ";" + projectName + ";";

                    msg += ProjectConstants.TASKS_LABEL + ":" + count + "";
                    queryStr = "SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=? AND TASK_NAME=?";
                    PreparedStatement selectQuery = connection.prepareStatement(queryStr);
                    selectQuery.setString(1, projectName);
                    selectQuery.setString(2, taskName);

                    //get the list of tasks for the specified project
                    ResultSet taskList = selectQuery.executeQuery();

//
                    while (taskList.next()) {
                        queryStr = "UPDATE " + ProjectConstants.TASKS_TABLE + " SET OWNER_IP=?, OWNER_NAME=?, OWNER_PORT=?  WHERE PROJECT_NAME=? AND TASK_NAME=?";
                        PreparedStatement updateQuery = connection.prepareStatement(queryStr);
                        updateQuery.setString(1, ip);
                        updateQuery.setString(2, take.getUserName());
                        updateQuery.setString(3, port +"");
                        updateQuery.setString(4, projectName);
                        updateQuery.setString(5, taskName);
                        updateQuery.executeUpdate();
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

        return ProjectConstants.FAIL + ";";
    }


    /**
     * This method retrieves a list of projects from the database
     * @return an ASN1 ProjectAnswer object
     */
    public ProjectsAnswer getProjects() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet countResultSet = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE + ";");

            while (countResultSet.next()) {
                int count = countResultSet.getInt("COUNT");
                if (count > 0) {
                    ArrayList<Project> projects = new ArrayList<>();

                    ResultSet projList = stmt.executeQuery("SELECT PROJECT_NAME FROM " + ProjectConstants.PROJECTS_TABLE + ";");
                    while (projList.next()) {
                        String projectName = projList.getString("PROJECT_NAME");
                        String queryStr = "SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=?";
                        PreparedStatement selectQuery = connection.prepareStatement(queryStr);
                        selectQuery.setString(1, projectName);
                            ResultSet taskList = selectQuery.executeQuery();
                        ArrayList<Task> tasks = new ArrayList<>();

                            while (taskList.next()) {
                                boolean done = false;
                                String taskName = taskList.getString("TASK_NAME");
                                String startDate = taskList.getString("START_TIME");
                                String endDate = taskList.getString("END_TIME");
                                String ownerIP = taskList.getString("OWNER_IP");
                                int ownerPort = taskList.getInt("OWNER_PORT");
                                String completedFlag = taskList.getString("COMPLETED_FLAG");
                                if(completedFlag.equalsIgnoreCase(ProjectConstants.COMPLETED_FLAG))
                                    done = true;
                                tasks.add(new Task(taskName, Util.parseDateStr(startDate), Util.parseDateStr(endDate), ownerIP, ownerPort, done));
                            }

                        Project project = new Project(projectName, tasks);
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


    /**
     * This method retrieves a project from the database
     * @param projectName the name of the project
     * @param ip the ip of the client assigned to the project tasks
     * @param port the port of the client
     * @return a project
     */
    public Project getProject(String projectName, String ip, int port) {
        try {
            String queryStr = "SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE OWNER_IP=? AND OWNER_PORT=? AND PROJECT_NAME=?";
            PreparedStatement selectQuery = connection.prepareStatement(queryStr);
            selectQuery.setString(1, ip);
            selectQuery.setString(2, port + "");
            selectQuery.setString(3, projectName);
            ResultSet taskList = selectQuery.executeQuery();
            ArrayList<Task> tasks = new ArrayList<>();


            while (taskList.next()) {
                boolean done = false;
                String taskName = taskList.getString("TASK_NAME");
                String startDate = taskList.getString("START_TIME");
                String endDate = taskList.getString("END_TIME");
                String ownerIP = taskList.getString("OWNER_IP");
                int ownerPort = taskList.getInt("OWNER_PORT");
                String completedFlag = taskList.getString("COMPLETED_FLAG");
                if (completedFlag.equalsIgnoreCase(ProjectConstants.COMPLETED_FLAG))
                    done = true;
                tasks.add(new Task(taskName, Util.parseDateStr(startDate), Util.parseDateStr(endDate), ownerIP, ownerPort, done));
            }

            Project project = new Project(projectName, tasks);

            connection.close();
            return project;
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        return null;
    }


    /**
     * This method prepares a GetProjectResponse object as a
     * response to a GetProject request
     * @param projectName the name of the project
     * @param ip the IP address of the client
     * @param port the port on which the client connected
     * @return an ASN1 GetProjectResponse object
     */
    public GetProjectResponse getProjectResponse(String projectName, String ip, int port) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet countResultSet = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE + ";");
            GetProjectResponse projectResponse = null;
            while (countResultSet.next()) {
                int count = countResultSet.getInt("COUNT");
                if (count > 0) {
                    int taskCount = 0;
                    String studentName = "";
                    String status = "";
                    String queryStr = "SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=?";
                    PreparedStatement selectQuery = connection.prepareStatement(queryStr);
                    selectQuery.setString(1, projectName);
                    ResultSet taskList = selectQuery.executeQuery();
                    ArrayList<Task> tasks = new ArrayList<>();

                    while (taskList.next()) {
                        taskCount++;
                        boolean done = false;

                        String taskName = taskList.getString("TASK_NAME");
                        String startDateStr = taskList.getString("START_TIME");
                        String endDateStr = taskList.getString("END_TIME");
                        String ownerName = taskList.getString("OWNER_NAME");
                        String ownerIP = taskList.getString("OWNER_IP");
                        int ownerPort = taskList.getInt("OWNER_PORT");
                        status = taskList.getString("COMPLETED_FLAG");

                        if (status.equals(ProjectConstants.COMPLETED_FLAG))
                            done = true;
                        tasks.add(new Task(taskName, Util.dbFormatDateStr(startDateStr), Util.dbFormatDateStr(endDateStr), ownerIP, ownerPort, done));

                        if (ownerIP!=null && ownerName!=null && ownerIP.equals(ip))
                            studentName = ownerName;

                    }
                    projectResponse = new GetProjectResponse(status, studentName, taskCount, new Project(projectName, tasks));
                }
            }

            connection.close();
            return projectResponse;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method registers new client/user to a project
     * @param projectName the project to which the user must be registered
     * @param ip the IP address of the client
     * @param port the port of the client
     * @return a PASS or FAIL message
     */
    public String registerUser(String projectName, String ip, int port) {
        try {
            Date now = new Date();
            String queryStr = "SELECT * FROM " + ProjectConstants.PROJECTS_TABLE + " WHERE PROJECT_NAME=?";
            PreparedStatement selectQuery = connection.prepareStatement(queryStr);
            selectQuery.setString(1, projectName);
            ResultSet projectList = selectQuery.executeQuery();

            //make sure project exists
            if (projectList.next()) {

                queryStr = "INSERT INTO " + ProjectConstants.REGISTRATION_TABLE +
                        " (PROJECT_NAME, REGISTERED_TIME, OWNER_IP, OWNER_PORT) " +
                        "VALUES (?,?,?,?)";

                PreparedStatement registerQuery = connection.prepareStatement(queryStr);
                registerQuery.setString(1, projectName);
                registerQuery.setString(2, Util.dbFormatDate(now));
                registerQuery.setString(3, ip);
                registerQuery.setString(4, port + "");

                registerQuery.executeUpdate(); //save registration data to database
            }
            connection.close();
            return ProjectConstants.OK + ";";

        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        return ProjectConstants.FAIL;
    }

    /**
     * This method unsubscribes a user from receiving updates
     * @param projectName
     * @param ip
     * @param port
     * @return
     */
    public String unsubscribeUser(String projectName, String ip, int port) {
        try {
            String deleteQueryStr = "DELETE FROM " + ProjectConstants.REGISTRATION_TABLE + " WHERE PROJECT_NAME=? AND OWNER_IP=? AND OWNER_PORT=?";

                PreparedStatement unsubscribeQuery = connection.prepareStatement(deleteQueryStr);
                unsubscribeQuery.setString(1, projectName);
                unsubscribeQuery.setString(2, ip);
                unsubscribeQuery.setString(3, port + "");

                unsubscribeQuery.executeUpdate(); //remove registration from database

            connection.close();
            return ProjectConstants.OK + ";";

        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        return ProjectConstants.FAIL;
    }

    public boolean clientsRegistered(){
        try {
            String queryStr = "SELECT * FROM " + ProjectConstants.REGISTRATION_TABLE +"";
            PreparedStatement selectQuery = connection.prepareStatement(queryStr);
            ResultSet projectList = selectQuery.executeQuery();

            if (projectList.next()) {
                connection.close();
                return true;
            }


        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        return false;
    }
}
