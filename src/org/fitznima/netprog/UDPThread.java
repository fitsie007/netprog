package org.fitznima.netprog;

import org.fitznima.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 3/18/16
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class provides functions for running a thread that listens and processes messages over USP
 */
public class UDPThread extends Thread {
    private DatagramSocket socket = null;
    private String dbPath;
    private int port;

    public UDPThread(int port, String dbPath) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.dbPath = dbPath;
        this.port = port;
    }

    @Override
    public void run() {

        try {
            byte[] buffer;
            System.out.print("UDP server started on port: " + port + "\n");

            while (true) {
                buffer = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingPacket);

                String message = new String(incomingPacket.getData()).trim();
                if (!message.equals("")) {

                    //new project definition
                    if (message.contains(ProjectConstants.PROJECT_DEFINITION)) {
                        String result = ProcessNewProject.addNewProject(message, dbPath);
                        DatagramPacket reply = new DatagramPacket(result.getBytes(),
                                result.getBytes().length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                    //take project command
                    else if (message.contains(ProjectConstants.TAKE_COMMAND)) {
                        String result = ProcessTakeCommand.take(message, dbPath, incomingPacket.getAddress().getHostAddress(), incomingPacket.getPort());
                        DatagramPacket reply = new DatagramPacket(result.getBytes(),
                                result.getBytes().length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                    //get projects command
                    else if (message.contains(ProjectConstants.GET_PROJECTS_COMMAND)) {
                        String result = ProcessGetProjects.getProjects(message, dbPath);
                        DatagramPacket reply = new DatagramPacket(result.getBytes(),
                                result.getBytes().length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                    //get project command
                    else if (message.contains(ProjectConstants.GET_PROJECT_COMMAND)) {
                        String result = ProcessGetProject.getProject(message, dbPath);
                        DatagramPacket reply = new DatagramPacket(result.getBytes(),
                                result.getBytes().length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                    //unknown message
                    else {
                        String failMsg = ProjectConstants.FAIL + "; " + message + '\n';
                        DatagramPacket reply = new DatagramPacket(failMsg.getBytes(),
                                failMsg.getBytes().length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
//        socket.close();
    }
}