package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by FitzRoi on 2/27/16.
 */
public class ProcessGetProjects {
    public static String getProjects(String message, String dbPath) {
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECTS_COMMAND) && messageParts.length == 1) {
            try {
                DBManager dbManager = new DBManager(dbPath);
                String msg = ProjectConstants.OK + ";" + ProjectConstants.PROJECTS_LABEL;
                Connection connection = dbManager.connectToDB();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE + ";");
                while (rs.next()) {
                    int count = rs.getInt("COUNT");
                    msg += ":" + count;
                }

                rs = stmt.executeQuery("SELECT PROJECT_NAME FROM " + ProjectConstants.PROJECTS_TABLE + ";");
                while (rs.next()) {
                    String projectName = rs.getString("PROJECT_NAME");
                    msg += ";" + projectName;
                }
                connection.close();
                return msg;

            } catch (Exception ex) {

            }
        }
        return ProjectConstants.FAIL;
    }

}
