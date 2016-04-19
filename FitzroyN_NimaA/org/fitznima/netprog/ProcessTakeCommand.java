package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class processes the TAKE command, which allows a user to complete a project.
 */

public class ProcessTakeCommand {
    /**
     * This method assigns a task to the selected user and updates the status of the task in the database.
     * @param message the TAKE command string
     * @param dbPath the path to the database
     * @param IP the IP address of the user
     * @param port the port on which the user connected
     * @return an OK message if action was successful or a FAIL message if action failed.
     */
    public static String take(String message, String dbPath, String IP, int port) {
        String messageParts[] = message.split(";");
        if (messageParts[0].contains(ProjectConstants.TAKE_COMMAND) && messageParts.length == 4) {
            String userName, task, sql;
            try {
                DBManager dbManager = new DBManager(dbPath);
                if (messageParts[1].contains(ProjectConstants.USER_LABEL)) {
                    userName = messageParts[1].split(":")[1];
                    if (messageParts[2].contains(ProjectConstants.PROJECT_LABEL)) {
                        String project = messageParts[2].split(":")[1];
                        task = messageParts[3];
                        Connection connection = dbManager.connectToDB();
                        String endDateStr;

                        //Use preparedstatement to sanitize input strings
                        String queryStr = "SELECT END_TIME FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME=? AND TASK_NAME=?";
                        PreparedStatement selectQuery = connection.prepareStatement(queryStr);
                        selectQuery.setString(1, project);
                        selectQuery.setString(2, task);

                        ResultSet taskResultSet = selectQuery.executeQuery();

                        //get project end-date from database
                        while (taskResultSet.next()) {
                            endDateStr = taskResultSet.getString("END_TIME");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            Date endDate = df.parse(Util.formatDate(endDateStr));
                            Date now = new Date();

                            //make sure project not expired (ie, time now is after completion)
                            if (now.after(endDate)) {

                                //use preparedStatement to sanitize input strings
                                queryStr = "UPDATE " + ProjectConstants.TASKS_TABLE +
                                        " SET OWNER_NAME=?, OWNER_IP=?, OWNER_PORT=? COMPLETED_FLAG=? WHERE TASK_NAME=? AND PROJECT_NAME=?";

                                PreparedStatement updateProjQuery = connection.prepareStatement(queryStr);
                                updateProjQuery.setString(1, userName);
                                updateProjQuery.setString(2, IP);
                                updateProjQuery.setInt(3, port);
                                updateProjQuery.setString(4, ProjectConstants.COMPLETED_FLAG);
                                updateProjQuery.setString(5, task);
                                updateProjQuery.setString(6, project);

                                int rowsAffected = updateProjQuery.executeUpdate();

                                connection.close();

                                if (rowsAffected > 0)
                                    return ProjectConstants.OK + ";" + message;
                            }
                        }
                        connection.close();
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return ProjectConstants.FAIL + ";" + message;
    }
}
