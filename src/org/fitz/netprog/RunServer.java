package org.fitz.netprog;

import gnu.getopt.Getopt;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Author: Fitzroy Nembhard
 * Date: 3/18/16
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This is the main class for running the server.
 */
public class RunServer {
    public static void main(String args[]) {
        String dbPath = null;
        int port = 0;
        int option;
        Connection connection;
        Statement stmt;
        BufferedWriter out;
        BufferedReader reader;

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
                //check if database and tables exist
                if (!dbManager.databaseExists(dbPath)) {
                    connection = dbManager.connectToDB();

                    //create tables if they do not exist
                    String projectTableSql = ProjectConstants.CREATE_PROJECT_TABLE_SQL;
                    String taskTableSql = ProjectConstants.CREATE_TASK_TABLE_SQL;
                    String registrationTableSql = ProjectConstants.CREATE_REGISTRATION_TABLE_SQL;
                    stmt = connection.createStatement();
                    stmt.executeUpdate(projectTableSql);
                    stmt.executeUpdate(taskTableSql);
                    stmt.executeUpdate(registrationTableSql);
                    stmt.close();
                    connection.close();
                }

                //Initialize and start the TCP and UDP threads
                TCPServerThread tcpThread = new TCPServerThread(port, dbPath);
                UDPServerThread udpThread = new UDPServerThread(port, dbPath);

                tcpThread.start();
                udpThread.start();

                tcpThread.join();
                udpThread.join();


            } catch (SQLException | SocketException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
