package org.fitznima.netprog.constants;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class ProjectConstants {
    public static String createTableSQL="CREATE TABLE PROJECTS " +
                                        "(TASKID        INT PRIMARY KEY     NOT NULL," +
                                        " NAME           TEXT    NOT NULL, " +
                                        " START_TIME     TEXT    NOT NULL, " +
                                        " END_TIME       TEXT)";
}
