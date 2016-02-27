package org.fitznima.netprog.constants;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class stores the messages and constants used in the program
 */

public class ProjectConstants {
    public static String host="localhost";
    public static String CREATE_PROJECT_TABLE_SQL="CREATE TABLE PROJECTS " +
                                            "(PROJECT_NAME        TEXT PRIMARY KEY  NOT NULL)";

    public static String CREATE_TASK_TABLE_SQL="CREATE TABLE TASKS " +
                                        "(TASK_ID               INTEGER PRIMARY KEY     NOT NULL," +
                                        " NAME                  TEXT                NOT NULL, " +
                                        " START_TIME            TEXT                NOT NULL, " +
                                        " END_TIME              TEXT                NOT NULL,"+
                                        " PERSON_RESPONSIBLE    TEXT, " +
                                        " PROJECT_NAME          TEXT                NOT NULL, "+
                                        " OWNER_NAME            TEXT,"+
                                        " OWNER_IP              TEXT,"+
                                        " OWNER_PORT            INT,"+
                                        " COMPLETED_FLAG        TEXT)";

    public static String PROJECT_SAMPLE ="PROJECT_DEFINITION:Exam;TASKS:2;" +
            "Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;" +
            "Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;";

    public static String PROJECT_DEFINITION = "PROJECT_DEFINITION";

    public static String TASK_BEGIN="TASKS:";

    public static String TAKE_COMMAND ="TAKE";

    public static String GET_PROJECTS_COMMAND ="GET_PROJECTS";

    public static String GET_PROJECT_COMMAND ="GET_PROJECT";

    public static String OK ="OK";

    public static String PROJECTS_TABLE = "PROJECTS";

    public static String PROJECTS_LABEL = "PROJECTS";

    public static String TASKS_TABLE = "TASKS";

    public static String TASKS_LABEL = "TASKS";

    public static String USER_LABEL ="USER";

    public static String PROJECT_LABEL = "PROJECT";

    public static String COMPLETED_FLAG ="Done";

    public static String PENDING_FLAG ="Waiting";

    public static String FAIL ="FAIL";

}
