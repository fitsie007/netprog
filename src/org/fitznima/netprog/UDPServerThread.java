package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNLenRuntimeException;
import net.ddp2p.ASN1.Decoder;
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
 * This class provides functions for running a thread that listens and processes messages over UDP
 */
public class UDPServerThread extends Thread {
    private DatagramSocket socket = null;
    private String dbPath;
    private int port;

    public UDPServerThread(int port, String dbPath) throws SocketException {
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
                buffer = new byte[1024 * 4];
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingPacket);

                byte data[] = incomingPacket.getData();

                Project project = null;
                Take take = null;
                Projects projects = new Projects();
                GetProject getProject = null;
                DatagramPacket reply;

                final Decoder dec = new Decoder(data);


                if (dec.contentLength() > 6) {
                    try {
                        if (dec.contentLength() > 6)
                            getProject = new GetProject().decode(dec);    //check if command is getProjects
                    } catch (final ASN1DecoderFail | ASNLenRuntimeException e1) {
                        try {
                            if (dec.contentLength() > 6)
                                take = new Take().decode(dec); //check if command is take

                        } catch (final ASN1DecoderFail | ASNLenRuntimeException e2) {
                            try {
                                if (dec.contentLength() > 6)
                                    project = new Project().decode(dec); //check if command is project (new project definition)

                            } catch (final ASN1DecoderFail | ASNLenRuntimeException e3) {
                                e3.printStackTrace();
                                System.out.println("Unknown command from client");
                                socket.close();
                                return;
                            }

                        }

                    }
                }
                if (dec.contentLength() == 6) {
                    projects = null;

                }

                // check if command was to define a new project
                if (project != null && project.getTasks() != null) {
                    System.out.println("Received project command\n");
                    System.out.println(project.getProjectName());
                    DBManager db = new DBManager(dbPath);
                    db.connectToDB();
                    String dbResponse = db.saveNewProject(project);
                    if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                        byte msg[] = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    } else {
                        byte msg[]  = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                }

                if (take != null) {
                    System.out.println("Received take command\n");
                    DBManager db = new DBManager(dbPath);
                    db.connectToDB();
                    String dbResponse = db.takeTask(take);
                    if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                        byte msg[] = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    } else {
                        byte msg[] = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                }

                if (projects == null) {
                    System.out.print("Received projects command");
                    DBManager db = new DBManager(dbPath);
                    db.connectToDB();
                    ProjectsAnswer projectsAnswer = db.getProjects();
                    if (projectsAnswer != null) {
                        byte msg[] = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    } else {
                        byte msg[] = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }

                }

                //getProject command issued
                if (getProject != null) {
                    System.out.print("GetProject command issued\n");
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }
//        socket.close();
    }
}