package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by FitzRoi on 2/27/16.
 */
public class ProcessNewProject {
    public static String addNewProject(String message, String dbPath) {
        String messageParts[] = message.split(";");
        String project = messageParts[0].split(":")[1];
//                            System.out.print(project);
        try {
            DBManager dbManager = new DBManager(dbPath);
            Connection connection = dbManager.connectToDB();
            String sql = "INSERT INTO " + ProjectConstants.PROJECTS_TABLE +
                    " (PROJECT_NAME) VALUES('" + project + "');";

            Statement stmt = connection.createStatement();

            stmt.executeUpdate(sql);

            if (messageParts[1].contains(ProjectConstants.TASK_BEGIN)) {
                int numTasks = Integer.parseInt(messageParts[1].split(":")[1]);
//                                System.out.println("\n" + numTasks);
                for (int i = 2; i < numTasks * 3; i += 3) {
                    Task task = new Task(messageParts[i], messageParts[i + 1], messageParts[i + 2]);
//                                    tasks.add(task);
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
                return ProjectConstants.OK + ";" + messageParts[0] + "\n";

            }
        } catch (Exception ex) {

        }

        return ProjectConstants.FAIL +";" + message;

    }
}
