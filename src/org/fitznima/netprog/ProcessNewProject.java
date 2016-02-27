package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;
import java.sql.Connection;
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
    public static String addNewProject(String message, String dbPath) {
        String messageParts[] = message.split(";");
        String project = messageParts[0].split(":")[1];
        try {
            DBManager dbManager = new DBManager(dbPath);
            Connection connection = dbManager.connectToDB();
            String sql = "INSERT INTO " + ProjectConstants.PROJECTS_TABLE +
                    " (PROJECT_NAME) VALUES('" + project + "');";

            Statement stmt = connection.createStatement();

            stmt.executeUpdate(sql);

            if (messageParts[1].contains(ProjectConstants.TASK_BEGIN)) {
                int numTasks = Integer.parseInt(messageParts[1].split(":")[1]);

                for (int i = 2; i < numTasks * 3; i += 3) {
                    Task task = new Task(messageParts[i], messageParts[i + 1], messageParts[i + 2]);
                    connection = dbManager.connectToDB();
                    stmt = connection.createStatement();
                    sql = "INSERT INTO " + ProjectConstants.TASKS_TABLE +
                            " (NAME, START_TIME, END_TIME, PROJECT_NAME, COMPLETED_FLAG) " +
                            "VALUES ('" + task.getName() + "','" +
                            task.getStartTime() + "','" +
                            task.getEndTime() + "','" +
                            project + "','" + ProjectConstants.PENDING_FLAG +"');";
                    stmt.executeUpdate(sql); //save task to database
                }
                connection.close();
                return ProjectConstants.OK + ";" + messageParts[0];

            }
        } catch (Exception ex) {

        }

        return ProjectConstants.FAIL +";" + message;

    }
}
