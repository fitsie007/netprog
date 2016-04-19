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
 * This class processes the GET_PROJECTS command
 */

public class ProcessGetProjects {
    /**
     * This method returns the total number of projects along with a list of names of all the projects in the database.
     * @param message the GET_PROJECTS command string
     * @param dbPath the path to the database
     * @return a string specifying success (OK) if command is valid or otherwise (FAIL)
     */
    public static String getProjects(String message, String dbPath) {
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECTS_COMMAND) && messageParts.length == 1) {
            try {
                DBManager dbManager = new DBManager(dbPath);

                Connection connection = dbManager.connectToDB();
                Statement stmt = connection.createStatement();
                ResultSet countResultSet = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE + ";");

                while (countResultSet.next()) {
                    int count = countResultSet.getInt("COUNT");
                    if (count > 0) {
                        String msg = ProjectConstants.OK + ";" + ProjectConstants.PROJECTS_LABEL;
                        msg += ":" + count;


                        ResultSet projList = stmt.executeQuery("SELECT PROJECT_NAME FROM " + ProjectConstants.PROJECTS_TABLE + ";");
                        while (projList.next()) {
                            String projectName = projList.getString("PROJECT_NAME");
                            msg += ";" + projectName;
                        }
                        connection.close();
                        return msg;
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
