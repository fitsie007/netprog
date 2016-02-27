package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by FitzRoi on 2/27/16.
 */
public class ProcessTakeCommand {
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

                        sql = "UPDATE " + ProjectConstants.TASKS_TABLE +
                                " SET OWNER_NAME = '" + userName + "', " +
                                " OWNER_IP = '" + IP + "'," +
                                " OWNER_PORT = " + port + ", " +
                                " COMPLETED_FLAG = '" + ProjectConstants.COMPLETED_FLAG + "' WHERE NAME='" + task + "' AND PROJECT_NAME='" + project + "';";
                        stmt.executeUpdate(sql);
                        connection.close();
                        return ProjectConstants.OK + ";" + message;

                    }


                }
            } catch (Exception ex) {

            }
        }
        return ProjectConstants.FAIL +";" + message;
    }
}
