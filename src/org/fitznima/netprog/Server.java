package org.fitznima.netprog;

import gnu.getopt.Getopt;
import org.fitznima.netprog.constants.ProjectConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class Server {

    public static void main(String args[]) {
        String dbPath = null;
        int port = 0;
        int option;
        String arg;
        Connection connection = null;
        Statement stmt = null;
        byte[] b = new byte[1000];
        int readNr;
        OutputStream out;
        BufferedReader inputReader;
        String input;

        String message ="";
        String messageParts[] = null;
        String project="";
        int numTasks = 0;
        ArrayList<Task> tasks = new ArrayList<Task>();

        Getopt g = new Getopt("server", args, "p:d:");
        g.setOpterr(false); // We'll do our own error handling

        while ((option = g.getopt()) != -1)
            switch (option) {
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    System.out.println("port: " + port);
                    break;
                case 'd':
                    dbPath = g.getOptarg();
                    System.out.println("db path: " + dbPath);
                    break;
            }

        if (dbPath != null && port != 0) { //database and tables exist
            DBManager dbManager = new DBManager(dbPath);
            try {
                if (dbManager.databaseExists(dbPath)) {

                    connection = dbManager.connectToDB();
                } else {
                    connection = dbManager.connectToDB();
                    //create tables
                    String projectTableSql = ProjectConstants.CREATE_PROJECT_TABLE_SQL;
                    String taskTableSql = ProjectConstants.CREATE_TASK_TABLE_SQL;
                    stmt = connection.createStatement();
                    stmt.executeUpdate(projectTableSql);
                    stmt.executeUpdate(taskTableSql);
                    stmt.close();
                    connection.close();

                }

                ServerSocket server_sock = new ServerSocket(port, 5);

                for (; ; ) {

                    inputReader = new BufferedReader(new InputStreamReader(System.in));
                    input = inputReader.readLine();
//                    Socket sock = server_sock.accept();
//                    out = sock.getOutputStream();//.write("Hello!\n".getBytes("latin1"));
//                    out.flush();

                    for (int k = 0; k < 1; k++) {
//                        if ((readNr = sock.getInputStream().read(b)) == -1) break;
//                        out.write(b, 0, readNr); out.flush();

//                        message = new String(input, 0, readNr, "latin1");
                        message = input;
//                        System.out.println(message);
                        messageParts = message.split(";");

                        //new project definition
                        if(messageParts[0].contains(ProjectConstants.PROJECT_DEFINITION)) {
                            project = messageParts[0].split(":")[1];
//                            System.out.print(project);
                            connection = dbManager.connectToDB();
                            String sql = "INSERT INTO " + ProjectConstants.PROJECTS_TABLE +
                                        " (PROJECT_NAME) VALUES('" + project +"');";
                            stmt = connection.createStatement();
                            stmt.executeUpdate(sql);

                            if (messageParts[1].contains(ProjectConstants.TASK_BEGIN)) {
                                numTasks = Integer.parseInt(messageParts[1].split(":")[1]);
//                                System.out.println("\n" + numTasks);
                                for (int i = 2; i < numTasks * 3; i += 3) {
                                    Task task = new Task(messageParts[i], messageParts[i + 1], messageParts[i + 2]);
//                                    tasks.add(task);
                                    connection = dbManager.connectToDB();
                                    stmt = connection.createStatement();
                                    sql = "INSERT INTO " + ProjectConstants.TASKS_TABLE +
                                            " (NAME, START_TIME, END_TIME, PROJECT_NAME, COMPLETED_FLAG) " +
                                            "VALUES ('" + task.getName() +"','" +
                                            task.getStartTime() +"','" +
                                            task.getEndTime() +"','" +
                                            project +"','Waiting');";
                                    stmt.executeUpdate(sql); //save task to database
                                }

                                //check if tasks parsed properly
                                for (Task t : tasks)
                                    t.print();

//                                out.write((ProjectConstants.OK + messageParts[0] + "\n").getBytes("latin1"));
                                System.out.println(ProjectConstants.OK +";" + messageParts[0] + "\n");
//                                out.flush();

                            }
                        }
                        //take project command
                        else if(messageParts[0].contains(ProjectConstants.TAKE_COMMAND) && messageParts.length == 4 ){
                            String userName, task, sql;
                            if(messageParts[1].contains(ProjectConstants.USER_LABEL)){
                                userName = messageParts[1].split(":")[1];
                                if(messageParts[2].contains(ProjectConstants.PROJECT_LABEL)){
                                    project = messageParts[2].split(":")[1];
                                    task = messageParts[3];
                                    connection = dbManager.connectToDB();
                                    stmt = connection.createStatement();

                                    sql = "UPDATE " + ProjectConstants.TASKS_TABLE +
                                            " SET OWNER_NAME = '" + userName +"', " +
                                            " OWNER_IP = '" +server_sock.getInetAddress().getHostAddress() + "',"+
                                            " OWNER_PORT = " +port +", " +
                                            " COMPLETED_FLAG = '" + ProjectConstants.COMPLETED_FLAG +"' WHERE NAME='" + task +"' AND PROJECT_NAME='"+project +"';";
                                    stmt.executeUpdate(sql);

                                    System.out.println(ProjectConstants.OK +";" +message);

                                }


                            }

                        }

                        //get projects command
                        else if(messageParts[0].equals(ProjectConstants.GET_PROJECTS_COMMAND) && messageParts.length == 1){

                            String msg = ProjectConstants.OK  +";" +ProjectConstants.PROJECTS_LABEL;
                            connection = dbManager.connectToDB();
                            stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery( "SELECT count(PROJECT_NAME) AS COUNT FROM " + ProjectConstants.PROJECTS_TABLE +";" );
                            while ( rs.next() ) {
                                int count = rs.getInt("COUNT");
                                msg += ":"+count;
                            }

                            rs = stmt.executeQuery( "SELECT PROJECT_NAME FROM " + ProjectConstants.PROJECTS_TABLE +";");
                            while ( rs.next() ) {
                                String projectName = rs.getString("PROJECT_NAME");
                                msg += ";"+projectName;
                            }

                            System.out.print(msg);

                        }

                        //get project command
                        else if(messageParts[0].equals(ProjectConstants.GET_PROJECT_COMMAND) && messageParts.length == 2){
                            project =messageParts[1];
                            String msg = ProjectConstants.OK  +";" + ProjectConstants.PROJECT_DEFINITION +";" +project +";";
                            int count;
                            connection = dbManager.connectToDB();
                            stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery( "SELECT count(PROJECT_NAME) AS COUNT FROM " +
                                    ProjectConstants.TASKS_TABLE +" WHERE PROJECT_NAME='" + project +"';" );
                            while ( rs.next() ) {
                                count = rs.getInt("COUNT");
                                msg += ProjectConstants.TASKS_LABEL +":"+count+"";
                            }

                            rs = stmt.executeQuery( "SELECT * FROM " + ProjectConstants.TASKS_TABLE +" WHERE PROJECT_NAME='" + project +"';");
                            while ( rs.next() ) {
                                String taskName = rs.getString("NAME");
                                String startDate = rs.getString("START_TIME");
                                String endDate = rs.getString("END_TIME");
                                String ownerName = rs.getString("OWNER_NAME");
                                String ownerIP = rs.getString("OWNER_IP");
                                int ownerPort = rs.getInt("OWNER_PORT");
                                String completedFlag = rs.getString("COMPLETED_FLAG");
                                msg +=";" +taskName +";" + startDate +";" + endDate +";" +
                                        ownerName +";" + ownerIP +";" + ownerPort +";" +completedFlag;
                            }

                            System.out.println(msg);


                        }
                        connection.close();
                    }

//                    sock.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
