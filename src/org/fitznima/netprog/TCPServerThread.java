package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNLenRuntimeException;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import org.fitznima.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            String message = "";
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

                    Project project = null;
                    Take take = null;
                    Projects projects = new Projects();
                    GetProject getProject = null;

                    final Decoder dec = new Decoder(bytesRead, 0, inputSize);

                        //try to decode the message by checking which format message follows
                    if(dec.contentLength() > 6) {
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
                                    sock.close();
                                    return;
                                }

                            }

                        }
                    }
                   if(dec.contentLength() == 6){
                        projects = null;

                    }

//                    else{
//                        projects = null; //getProjects command issued
//                    }

                    // check if command was to define a new project
                    if (project != null && project.getTasks() != null) {

                        String decodedMsg = ProjectConstants.PROJECT_DEFINITION + ";";
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
                        if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                            out.write(new ProjectOK(ProjectConstants.SUCCESS_CODE).encode());
                            out.flush();
                        } else {
                            out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                            out.flush();
                        }

                    }

                    //if take command issued
                    if (take != null) {
                        String decodedMsg = ProjectConstants.TAKE_COMMAND + ";";
                        decodedMsg += ProjectConstants.USER_LABEL + ";" + take.getUserName() + ";"
                                + ProjectConstants.PROJECT_LABEL + ";" + take.getProjectName() + ";" + take.getTaskName();
                        System.out.println(decodedMsg);

                        DBManager db = new DBManager(dbPath);
                        db.connectToDB();
                        String dbResponse = db.takeTask(take);
                        if (!dbResponse.equalsIgnoreCase(ProjectConstants.FAIL)) {
                            out.write(new ProjectOK(ProjectConstants.SUCCESS_CODE).encode());
                            out.flush();
                        } else {
                            out.write(new ProjectOK(ProjectConstants.FAIL_CODE).encode());
                            out.flush();
                        }

                    }

                    //if getProjects command issued
                    if (projects == null) {
                        System.out.println(ProjectConstants.GET_PROJECTS_COMMAND +";" + "\n");
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

                    //getProject command issued
                    if (getProject != null) {
                        System.out.print("GetProject command issued\n");
                    }

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
