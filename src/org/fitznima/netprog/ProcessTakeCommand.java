package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
                        Statement stmt = connection.createStatement();

                        String endDateStr;


                        ResultSet rs = stmt.executeQuery("SELECT END_TIME FROM " +
                                ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME='" + project +"' AND TASK_NAME='" +task +"';");

                        //get project end-date from database
                        while (rs.next()) {
                            endDateStr = rs.getString("END_TIME");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            Date endDate = df.parse(Util.formatDate(endDateStr));
                            Date now = new Date();

                            //make sure project not expired
                            if (now.equals(endDate)) {
                                sql = "UPDATE " + ProjectConstants.TASKS_TABLE +
                                        " SET OWNER_NAME = '" + userName + "', " +
                                        " OWNER_IP = '" + IP + "'," +
                                        " OWNER_PORT = " + port + ", " +
                                        " COMPLETED_FLAG = '" + ProjectConstants.COMPLETED_FLAG + "' WHERE TASK_NAME='" + task + "' AND PROJECT_NAME='" + project + "';";
                                stmt.executeUpdate(sql);
                                connection.close();
                                return ProjectConstants.OK + ";" + message;
                            }
                        }

                    }


                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return ProjectConstants.FAIL +";" + message;
    }
}
