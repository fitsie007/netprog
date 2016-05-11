package org.fitz.netprog.constants;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class stores the messages and constants used in the program
 */

public class ProjectConstants {
    public static String HOST="localhost";
    public static String CREATE_PROJECT_TABLE_SQL="CREATE TABLE PROJECTS " +
                                            "(PROJECT_NAME        TEXT PRIMARY KEY  NOT NULL)";

    public static String CREATE_TASK_TABLE_SQL="CREATE TABLE TASKS " +
                                        "(TASK_ID               INTEGER PRIMARY KEY NOT NULL," +
                                        " TASK_NAME             TEXT                NOT NULL," +
                                        " START_TIME            TEXT                NOT NULL," +
                                        " END_TIME              TEXT                NOT NULL,"+
                                        " PROJECT_NAME          TEXT                NOT NULL,"+
                                        " OWNER_NAME            TEXT,"+
                                        " OWNER_IP              TEXT,"+
                                        " OWNER_PORT            INT,"+
                                        " COMPLETED_FLAG        TEXT)";

    public static String CREATE_REGISTRATION_TABLE_SQL="CREATE TABLE REGISTRATION " +
            "(CLIENT_ID             INTEGER PRIMARY KEY NOT NULL," +
            " PROJECT_NAME          TEXT                NOT NULL," +
            " REGISTERED_TIME       TEXT                NOT NULL," +
            " OWNER_IP              TEXT,"+
            " OWNER_PORT            INT)";

    public static String PROJECT_SAMPLE ="PROJECT_DEFINITION_COMMAND:Exam;TASKS:2;" +
            "Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;" +
            "Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;";

    public static String SAMPLE_TAKE ="TAKE;USER:Johny;PROJECT:Exam;Buy paper";

    public static String SAMPLE_GET_PROJECTS = "GET_PROJECTS";

    public static String SAMPLE_GET_PROJECT = "GET_PROJECT;Exam";

    public static String SAMPLE_REGISTER = "REGISTER;Exam";

    public static String SAMPLE_LEAVE = "LEAVE;Exam";

    public static String PROJECT_DEFINITION_COMMAND = "PROJECT_DEFINITION";

    public static String TASK_BEGIN = "TASKS:";

    public static String TAKE_COMMAND = "TAKE";

    public static String GET_PROJECTS_COMMAND = "GET_PROJECTS";

    public static String GET_PROJECT_COMMAND ="GET_PROJECT";

    public static String LEAVE_COMMAND = "LEAVE";

    public static String REGISTER_COMMAND = "REGISTER";

    public static String OK ="OK";

    public static String PROJECTS_TABLE = "PROJECTS";

    public static String PROJECTS_LABEL = "PROJECTS";

    public static String TASKS_TABLE = "TASKS";

    public static String TASKS_LABEL = "TASKS";

    public static String REGISTRATION_TABLE = "REGISTRATION";

    public static String USER_LABEL = "USER";

    public static String PROJECT_LABEL = "PROJECT";

    public static String PROJECT_RESPONSE_LABEL = "GET_PROJECT_RESPONSE";

    public static String COMPLETED_FLAG = "Done";

    public static String PENDING_FLAG = "Waiting";

    public static String FAIL = "FAIL";

    public static String USAGE = "Usage: Client -t|-u -p port -c command";

    public static int MAX_PORT = 65535;

    public static String DEFAULT_SERVER = "localhost";

    public static String DEFAULT_PORT = "2323";

    public static int MIN_PORT = 1;

    public static int MAX_CONNECTIONS = 5;

    public static int SUCCESS_CODE = 0;

    public static int FAIL_CODE = -1;

    public static final long HOUR_IN_MILLISECONDS = 60L * 60L * 1000L;

    public static long HOURS_TIME_OUT = 1L;


}
