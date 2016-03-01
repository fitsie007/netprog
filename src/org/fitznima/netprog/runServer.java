package org.fitznima.netprog;

import gnu.getopt.Getopt;
import org.fitznima.netprog.constants.ProjectConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This is the main class for running the server.
 */
public class runServer {

    public static void main(String args[]) {
        String dbPath = null;
        int port = 0;
        int maxConns = 5;
        int option;
        Connection connection = null;
        Statement stmt = null;
        BufferedWriter out;
        BufferedReader reader;
        String message = "";

        //use GNU Java GetOpt to process command line options
        Getopt g = new Getopt("server", args, "p:d:");
        g.setOpterr(false);

        while ((option = g.getopt()) != -1)
            switch (option) {
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    break;
                case 'd':
                    dbPath = g.getOptarg();
                    break;
            }

        //if database path and port provided
        if (dbPath != null && port != 0) {
            DBManager dbManager = new DBManager(dbPath);
            try {
                if (!dbManager.databaseExists(dbPath)) { //check if database and tables exist
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

                ServerSocket server_sock = new ServerSocket(port, maxConns);
                System.out.print("Server started on port: " + port);

                for (; ; ) {

                    Socket sock = server_sock.accept();
                    reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), "latin1"));
                    out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "latin1"));

                    while ((message = reader.readLine()) != null) {


                        //new project definition
                        if (message.contains(ProjectConstants.PROJECT_DEFINITION)) {
                            String result = ProcessNewProject.addNewProject(message, dbPath);
                            out.write(result + "\n");
                            out.flush();
                        }
//
                        //take project command
                        else if (message.contains(ProjectConstants.TAKE_COMMAND)) {
                            String result = ProcessTakeCommand.take(message, dbPath, server_sock.getInetAddress().getHostAddress(), server_sock.getLocalPort());
                            out.write(result + "\n");
                            out.flush();
                        }

                        //get projects command
                        else if (message.contains(ProjectConstants.GET_PROJECTS_COMMAND)) {
                            String result = ProcessGetProjects.getProjects(message, dbPath);
                            out.write(result + "\n");
                            out.flush();
                        }

                        //get project command
                        else if (message.contains(ProjectConstants.GET_PROJECT_COMMAND)) {
                            String result = ProcessGetProject.getProject(message, dbPath);
                            out.write(result + '\n');
                            out.flush();
                        } else {
                            out.write(ProjectConstants.FAIL + "; " + message + '\n');
                            out.flush();
                        }

//                    sock.close(); //don't close the socket--> expect infinite messages
                    }

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
