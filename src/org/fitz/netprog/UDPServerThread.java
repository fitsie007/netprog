package org.fitz.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import org.fitz.netprog.asnobjects.*;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Fitzroy Nembhard
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
            UpdatesProvider regUpdatesProvider = null;

            while (true) {
                buffer = new byte[1024 * 4];
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingPacket);

                byte data[] = incomingPacket.getData();


                DatagramPacket reply;

                final Decoder dec = new Decoder(data);
                byte  requestTag = dec.getTypeByte();

                switch (requestTag){
                    //process Project (PROJECT_DEFINITION_COMMAND) command
                    case ProjectTags.TYPE_PROJECT: {
                        Project project = new Project().decode(dec);
                        String decodedMsg = ProjectConstants.PROJECT_DEFINITION_COMMAND + ";";
                        decodedMsg += project.getProjectName() + ";";
                        decodedMsg += ProjectConstants.TASKS_LABEL + ";";
                        decodedMsg += project.getTasks().size() + ";";
                        for (Task task : project.getTasks()) {
                            decodedMsg += task.getTaskName() + ";";
                            decodedMsg += Encoder.getGeneralizedTime(task.getStartTime().getTime()) + ";";
                            decodedMsg += Encoder.getGeneralizedTime(task.getEndTime().getTime()) + ";";
                        }

                        System.out.println(decodedMsg + "\n");

                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();
                        String dbResponse = db.saveNewProject(project);
                        byte msg[];

                        if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                            msg = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);

                        } else {
                            msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        }
                    }
                    break;


                    //process GET_PROJECT command
                    case ProjectTags.TYPE_GET_PROJECT: {
                        GetProject getProject = new GetProject().decode(dec);
                        String projectName = getProject.getProjectName();
                        System.out.println(ProjectConstants.GET_PROJECT_COMMAND +";" +projectName + "\n");

                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();
                        GetProjectResponse projectResponse = db.getProjectResponse(projectName, Util.parseIPAddress(incomingPacket.getAddress().toString()), socket.getPort());
                        byte msg[];
                        if (projectResponse != null) {
                            msg = projectResponse.encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        } else {
                            msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        }
                    }
                    break;

                    //process the GET_PROJECTS command
                    case ProjectTags.TYPE_GET_PROJECTS: {
                        System.out.println(ProjectConstants.GET_PROJECTS_COMMAND +";" + "\n");
                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();
                        byte msg[];
                        ProjectsAnswer projectsAnswer = db.getProjects();
                        if (projectsAnswer != null) {
                            msg = projectsAnswer.encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        } else {
                            msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        }

                    }
                    break;

                    //process the TAKE command
                    case ProjectTags.TYPE_TAKE:{
                        Take take = new Take().decode(dec);
                        String decodedMsg = ProjectConstants.TAKE_COMMAND + ";";
                        decodedMsg += ProjectConstants.USER_LABEL + ";" + take.getUserName() + ";"
                                + ProjectConstants.PROJECT_LABEL + ";" + take.getProjectName() + ";" + take.getTaskName();
                        System.out.println(decodedMsg);

                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();
                        byte msg[];
                        String dbResponse = db.takeTask(take, incomingPacket.getAddress().toString(),
                                incomingPacket.getPort());
                        if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                            msg = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        } else {
                            msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        }

                    }
                    break;

                    case ProjectTags.TYPE_REGISTER: {
                        if (regUpdatesProvider == null) {
                            regUpdatesProvider = new UpdatesProvider(socket, new DBManager(dbPath));
                            initialzeScheduledUpdates(regUpdatesProvider);
                        }

                        Register register = new Register().decode(dec);
                        String decodedMsg = ProjectConstants.REGISTER_COMMAND + ";";
                        String project = register.getProject();
                        decodedMsg += project;

                        System.out.println("Decoded msg: " + decodedMsg);
                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();
                        byte msg[];
                        //register user by storing registration data in database
                        String dbResponse = db.registerUser(register.getProject(), incomingPacket.getAddress().toString(), incomingPacket.getPort());

                        if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                            msg = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        } else {
                            msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);

                        }


                    }
                    break;

                    case ProjectTags.TYPE_LEAVE: {
                        Leave leave = new Leave().decode(dec);
                        String decodedMsg = ProjectConstants.REGISTER_COMMAND + ";";
                        String project = leave.getRegister().getProject();
                        decodedMsg += project;

                        System.out.println("Decoded msg: " + decodedMsg);
                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();

                        byte msg[];
                        //register user by storing registration data in database
                        String dbResponse = db.unsubscribeUser(project, incomingPacket.getAddress().toString(), incomingPacket.getPort());

                        if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                            msg = new ProjectOK(ProjectConstants.SUCCESS_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        } else {
                            msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                            reply = new DatagramPacket(msg,
                                    msg.length,
                                    incomingPacket.getAddress(),
                                    incomingPacket.getPort());
                            socket.send(reply);
                        }


                        }
                    break;

                    //Unknown command; return FAIL
                    default: {
                        byte msg[];
                        msg = new ProjectOK(ProjectConstants.FAIL_CODE).encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                incomingPacket.getAddress(),
                                incomingPacket.getPort());
                        socket.send(reply);
                    }
                }
            }


        } catch (IOException | ASN1DecoderFail e) {
            e.printStackTrace();
        }
//        socket.close();
    }

    public void initialzeScheduledUpdates(Thread regUpdater){
        final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(regUpdater, 0, 1, TimeUnit.MINUTES);
    }
}