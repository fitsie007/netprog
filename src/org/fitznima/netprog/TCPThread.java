package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 3/18/16
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class provides functions for running a thread that listens and processes messages over TCP
 */
public class TCPThread extends Thread {
    private int port;
    private int maxConns = 5;
    private String dbPath;
    public TCPThread(int port, String dbPath) {
        this.port = port;
        this.dbPath = dbPath;

    }

    @Override
    public void run() {
        try {

            BufferedWriter out;
            BufferedReader reader;
            String message = "";
            ServerSocket server_sock = new ServerSocket(port, maxConns);
            System.out.print("TCP Server started on port: " + port + "\n");

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
//                        out.write("Got UDP Data \n");
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

//                    sock.close(); //don't close the socket; let it close on error --> expect infinite messages
                }

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
    }


}
