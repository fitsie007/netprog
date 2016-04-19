package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class processes the GET_PROJECT command.
 */

public class ProcessGetProject {
    /**
     * This method returns the list of tasks associated with a project.
     * @param message the GET_PROJECT command string
     * @param dbPath the path to the database
     * @return a string specifying success (OK) if command is valid or otherwise (FAIL)
     */
    public static String getProject(String message, String dbPath) {
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECT_COMMAND) && messageParts.length == 2) {

            try {
                DBManager dbManager = new DBManager(dbPath);
                String project = messageParts[1];
                int count;
                Connection connection = dbManager.connectToDB();

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
        }

        return ProjectConstants.FAIL +";" + message;
    }
}