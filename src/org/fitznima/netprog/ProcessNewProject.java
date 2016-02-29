package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class processes a new project and stores the data in a database
 */

public class ProcessNewProject {
    /**
     * This method adds a new project and a list of tasks to the database
     * @param message the PROJECT_DEFINITION command string
     * @param dbPath the path to the database
     * @return a string specifying success (OK) if command is valid and successful or otherwise (FAIL)
     */
    public static String addNewProject(String message, String dbPath) {
        String messageParts[] = message.split(";");
        String project = messageParts[0].split(":")[1];

        try {

            if (messageParts[1].contains(ProjectConstants.TASK_BEGIN)) {
                int numTasks = Integer.parseInt(messageParts[1].split(":")[1]);
                int expectedLength = 2 + (numTasks * 3);

                //check if project definition has expected number of parameters
                if(messageParts.length == expectedLength) {
                    DBManager dbManager = new DBManager(dbPath);
                    Connection connection = dbManager.connectToDB();


                    //use a query to check if this project already defined (if count = 0)
                    Statement stmt = connection.createStatement();
                    int count = 0;
                    ResultSet rs = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " +
                            ProjectConstants.PROJECTS_TABLE + " WHERE PROJECT_NAME='" +project +"';");
                    while (rs.next()) {
                        count = rs.getInt("COUNT");
                    }


                    if(count == 0) {
                        //loop and check if all date ranges valid
                        for (int i = 2; i < numTasks * 3; i += 3) {
                            String startTime = messageParts[i + 1];
                            String endTime = messageParts[i + 2];

                            if (!Util.isValidDateRange(startTime, endTime))
                                return ProjectConstants.FAIL + ";" + message;
                        }

                        //Now, insert this project-name into the projects table
                        String sql = "INSERT INTO " + ProjectConstants.PROJECTS_TABLE +
                                " (PROJECT_NAME) VALUES('" + project + "');";
                        stmt = connection.createStatement();
                        stmt.executeUpdate(sql);

                        //loop and insert the tasks in the tasks table
                        for (int i = 2; i < numTasks * 3; i += 3) {
                            String taskName = messageParts[i];
                            String startTime = messageParts[i + 1];
                            String endTime = messageParts[i + 2];

                            connection = dbManager.connectToDB();
                            stmt = connection.createStatement();
                            sql = "INSERT INTO " + ProjectConstants.TASKS_TABLE +
                                    " (NAME, START_TIME, END_TIME, PROJECT_NAME, COMPLETED_FLAG) " +
                                    "VALUES ('" + taskName + "','" +
                                    startTime + "','" +
                                    endTime + "','" +
                                    project + "','" + ProjectConstants.PENDING_FLAG + "');";
                            stmt.executeUpdate(sql); //save task to database
                        }

                        connection.close();
                        return ProjectConstants.OK + ";" + messageParts[0];
                    }


                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return ProjectConstants.FAIL +";" + message;

    }
}
