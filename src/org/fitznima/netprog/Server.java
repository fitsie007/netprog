package org.fitznima.netprog;

import gnu.getopt.Getopt;
import org.fitznima.netprog.constants.ProjectConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.sql.Connection;
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

//                        out.write((ProjectConstants.OK + messageParts[0] + "\n").getBytes("latin1"));

                        //new project definition
                        if (message.contains(ProjectConstants.PROJECT_DEFINITION)){
                            String result = ProcessNewProject.addNewProject(message, dbPath);
                            System.out.println(result);
                        }
//
                        //take project command
                        else if (message.contains(ProjectConstants.TAKE_COMMAND)){
                            String result = ProcessTakeCommand.take(message, dbPath, server_sock.getInetAddress().getHostAddress(), port);
                            System.out.println(result);
                        }

                        //get projects command
                        else if(message.contains(ProjectConstants.GET_PROJECTS_COMMAND)){
                            String msg = ProcessGetProjects.getProjects(message, dbPath);
                            System.out.println(msg);
                        }

                        //get project command
                        else if(message.contains(ProjectConstants.GET_PROJECT_COMMAND)){
                            String msg = ProcessGetProject.getProject(message,dbPath);
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
