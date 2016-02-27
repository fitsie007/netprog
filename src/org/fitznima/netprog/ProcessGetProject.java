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
 * This class processes the GET_PROJECT command
 */

public class ProcessGetProject {
    public static String getProject(String message, String dbPath) {
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECT_COMMAND) && messageParts.length == 2) {

            try {
                DBManager dbManager = new DBManager(dbPath);
                String project = messageParts[1];
                String msg = ProjectConstants.OK + ";" + ProjectConstants.PROJECT_DEFINITION + ";" + project + ";";
                int count;
                Connection connection = dbManager.connectToDB();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " +
                        ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME='" + project + "';");
                while (rs.next()) {
                    count = rs.getInt("COUNT");
                    msg += ProjectConstants.TASKS_LABEL + ":" + count + "";
                }

                rs = stmt.executeQuery("SELECT * FROM " + ProjectConstants.TASKS_TABLE + " WHERE PROJECT_NAME='" + project + "';");
                while (rs.next()) {
                    String taskName = rs.getString("NAME");
                    String startDate = rs.getString("START_TIME");
                    String endDate = rs.getString("END_TIME");
                    String ownerName = rs.getString("OWNER_NAME");
                    String ownerIP = rs.getString("OWNER_IP");
                    int ownerPort = rs.getInt("OWNER_PORT");
                    String completedFlag = rs.getString("COMPLETED_FLAG");
                    msg += ";" + taskName + ";" + startDate + ";" + endDate + ";" +
                            ownerName + ";" + ownerIP + ";" + ownerPort + ";" + completedFlag;

                }
                return msg;
            } catch (Exception ex) {

            }
        }
        return ProjectConstants.FAIL +";" + message;
    }
}