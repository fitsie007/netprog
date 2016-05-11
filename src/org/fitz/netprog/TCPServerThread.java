package org.fitz.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import org.fitz.netprog.asnobjects.*;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Author: Fitzroy Nembhard
 * Date: 3/18/16
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class provides functions for running a thread that listens and processes messages over TCP
 */
public class TCPServerThread extends Thread {
    private int port;
    private String dbPath;

    public TCPServerThread(int port, String dbPath) {
        this.port = port;
        this.dbPath = dbPath;
    }

    @Override
    public void run() {
        try {
            OutputStream out;
            InputStream reader;
            int maxConns = ProjectConstants.MAX_CONNECTIONS;
            ServerSocket server_sock = new ServerSocket(port, maxConns);
            System.out.print("TCP Server started on port: " + port + "\n");

            for (; ; ) {

                Socket sock = server_sock.accept();
                reader = sock.getInputStream();
                out = sock.getOutputStream();
                byte bytesRead[] = new byte[1024 * 4];

                int inputSize;
                while ((inputSize = reader.read(bytesRead)) != -1) {

                    final Decoder dec = new Decoder(bytesRead, 0, inputSize);
                    if (!dec.fetchAll(reader)) {
                        System.err.println("Error: Buffer too small or stream closed");
                        return;
                    }

                    byte requestTag = dec.getTypeByte();

                    switch (requestTag) {

                        //process Project (PROJECT_DEFINITION) command
                        case ProjectTags.TYPE_PROJECT: {
                            Project project = new Project().decode(dec);
                            String decodedMsg = ProjectConstants.PROJECT_DEFINITION_COMMAND + ";";
                            decodedMsg += project.getProjectName() + ";";
                            decodedMsg += ProjectConstants.TASKS_LABEL + ";";
                            decodedMsg += project.getTasks().size() + ";";
                            for (Task task : project.getTasks()) {
                                decodedMsg += task.getTaskName() + ";";
                                decodedMsg += Util.dbFormatDate(task.getStartTime()) + ";";
                                decodedMsg += Util.dbFormatDate(task.getEndTime()) + ";";
                            }

                            System.out.println("Decoded msg: " + decodedMsg + "\n");

                            DBManager db = new DBManager(dbPath);
                            db.connectToDB();
                            String dbResponse = db.saveNewProject(project);
                            if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                                out.write(new ProjectOK(ProjectConstants.SUCCESS_CODE).encode());
                                out.flush();
                            } else {
                                out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                                out.flush();
                            }
                        }
                        break;


                        //process GET_PROJECT command
                        case ProjectTags.TYPE_GET_PROJECT: {
                            GetProject getProject = new GetProject().decode(dec);
                            String projectName = getProject.getProjectName();
                            System.out.println("Decoded msg: " + ProjectConstants.GET_PROJECT_COMMAND + ";" + projectName + "\n");
                            DBManager db = new DBManager(dbPath);
                            db.connectToDB();
                            GetProjectResponse projectResponse = db.getProjectResponse(projectName, Util.parseIPAddress(sock.getRemoteSocketAddress()), sock.getPort());

                            if (projectResponse != null) {
                                out.write(projectResponse.encode());
                                out.flush();
                            } else {
                                out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                                out.flush();
                            }
                        }
                        break;

                        //process the GET_PROJECTS command
                        case ProjectTags.TYPE_GET_PROJECTS: {
                            System.out.println("Decoded msg: " + ProjectConstants.GET_PROJECTS_COMMAND + ";" + "\n");
                            DBManager db = new DBManager(dbPath);
                            db.connectToDB();
                            ProjectsAnswer projectsAnswer = db.getProjects();
                            if (projectsAnswer != null) {
                                out.write(projectsAnswer.encode());
                                out.flush();
                            } else {
                                out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                                out.flush();
                            }

                        }
                        break;

                        //process the TAKE command
                        case ProjectTags.TYPE_TAKE: {
                            Take take = new Take().decode(dec);
                            String decodedMsg = ProjectConstants.TAKE_COMMAND + ";";
                            decodedMsg += ProjectConstants.USER_LABEL + ";" + take.getUserName() + ";"
                                    + ProjectConstants.PROJECT_LABEL + ";" + take.getProjectName() + ";" + take.getTaskName() + "\n";
                            System.out.println("Decoded msg: " + decodedMsg);

                            DBManager db = new DBManager(dbPath);
                            db.connectToDB();
                            String dbResponse = db.takeTask(take, Util.parseIPAddress(sock.getRemoteSocketAddress()), sock.getPort());
                            if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                                out.write(new ProjectOK(ProjectConstants.SUCCESS_CODE).encode());
                                out.flush();
                            } else {
                                out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                                out.flush();
                            }

                        }
                        break;


                        //Unknown command; return FAIL
                        default: {
                            System.out.println("Unknown command received!\n");
                            out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                            out.flush();
                        }
                    }
                }
            }


        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
    }

}
