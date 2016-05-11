package org.fitz.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import org.fitz.netprog.asnobjects.*;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class runs a TCP client
 * Created by FitzRoi on 4/7/16.
 */
public class TCPClient {
    private String host;
    private int port;
    public TCPClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     * This method processes the command submitted by the user
     * @param command a string making a request from the server
     * @return
     */
    public String processCommand(String command){
        Socket sock;
        try{
            sock = new Socket(host, port);
            InputStream reader = sock.getInputStream();
            OutputStream out = sock.getOutputStream();



            //process a PROJECT_DEFINITION_COMMAND command
            if (command.contains(ProjectConstants.PROJECT_DEFINITION_COMMAND)) {
                out.write(project(command, Util.parseIPAddress(sock.getRemoteSocketAddress()), sock.getPort()).encode());
                out.flush();
            }

            //process a TAKE command
            if (command.contains(ProjectConstants.TAKE_COMMAND)) {
                out.write(take(command).encode());
                out.flush();
            }

            //process a GET_PROJECTS command
            if (command.contains(ProjectConstants.GET_PROJECTS_COMMAND)) {
                GetProjects projects = getProjects(command);
                if(projects!= null) {
                    out.write(projects.encode());
                    out.flush();
                }
            }

            //process a GET_PROJECT command
            if (command.contains(ProjectConstants.GET_PROJECT_COMMAND)) {
                GetProject getProject = getProject(command);
                if(getProject != null) {
                    out.write(getProject.encode());
                    out.flush();
                }
            }

            //process responses from server
            byte bytesRead[] = new byte[1024 * 4];

            int inputSize;
            while ((inputSize = reader.read(bytesRead)) != -1) {

                final Decoder dec = new Decoder(bytesRead, 0, inputSize);

                if(! dec.fetchAll(reader)){
                    System.err.println("Error: Buffer too small or stream closed");
                    return ProjectConstants.FAIL;
                }

                byte  requestTag = dec.getTypeByte();

                switch (requestTag) {

                    case ProjectTags.TYPE_PROJECT_OK: {
                        ProjectOK ok  = new ProjectOK().decode(dec);
                        if (ok.getCode() == ProjectConstants.SUCCESS_CODE)
                            return ProjectConstants.OK + ";" + command;
                        else
                            return ProjectConstants.FAIL + ";" + command;
                    }

                    case ProjectTags.TYPE_PROJECTS_ANSWER:{
                       ProjectsAnswer projectsAnswer = new ProjectsAnswer().decode(dec);
                        ArrayList<Project> projects = projectsAnswer.getProjects();

                        String str = ProjectConstants.OK+
                                ";" +ProjectConstants.PROJECTS_LABEL+
                                ";" + projects.size();
                        for(Project project: projects){
                            str +=";" + project.getProjectName();
                        }
                        return str;
                    }

                    case ProjectTags.TYPE_GET_PROJECT_RESPONSE:{
                        GetProjectResponse projectResponse = new GetProjectResponse().decode(dec);
                        Project project = projectResponse.getProject();

                        return ProjectConstants.OK + ";" + ProjectConstants.PROJECT_RESPONSE_LABEL+
                                ";" + projectResponse.getStatus() +
                                ";" + projectResponse.getStudentName() +
                                ";" + project.getProjectName();
                    }

                    case ProjectTags.TYPE_PROJECT:{
                        Project project = new Project().decode(dec);
                        ArrayList<Task> tasks = project.getTasks();

                        String str = ProjectConstants.PROJECT_LABEL+
                                ";" + project.getProjectName();
                        for(Task task : tasks){
                            str += task.getTaskName() + ";";
                            str += Encoder.getGeneralizedTime(task.getStartTime().getTime()) + ";";
                            str += Encoder.getGeneralizedTime(task.getEndTime().getTime()) + ";";
                        }

                        return str;
                    }


                    default:
                        return "Unknown command from server";
                }
            }

        }catch(IOException | ASN1DecoderFail e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method parses a PROJECT_DEFINITION_COMMAND string
     * and creates a Project object
     * @param message the command string from the user
     * @param ip the ip to use for the tasks within the project
     * @return a new ANS1 Project object
     */
    public Project project(String message, String ip, int port) {
        ArrayList<Task> tasks = null;
        String messageParts[] = message.split(";");
        String projectName = messageParts[0].split(":")[1];

            if (messageParts[1].contains(ProjectConstants.TASK_BEGIN)) {
                int numTasks = Integer.parseInt(messageParts[1].split(":")[1]);
                tasks = new ArrayList<>();
                int expectedLength = 2 + (numTasks * 3);

                //check if project definition has expected number of parameters
                if (messageParts.length == expectedLength) {
                    for (int i = 2, j=0; i < numTasks * 3; i += 3, j++) {
                        String taskName = messageParts[i];
                        String startTime = messageParts[i + 1];
                        String endTime = messageParts[i + 2];
                        Task task = new Task(taskName, startTime, endTime, ip, port, false);
                        task.encode();
                        tasks.add(task);
                    }
                }
        }
        return new Project(projectName, tasks);
    }

    /**
     * This method parses a TAKE command
     * and creates a Take object
     * @param message the command string from the user
     * @return a new ASN1 Take object
     */
    public Take take(String message) {
        String userName, projectName, taskName;
        String messageParts[] = message.split(";");
        if (messageParts[0].contains(ProjectConstants.TAKE_COMMAND) && messageParts.length == 4) {
                if (messageParts[1].contains(ProjectConstants.USER_LABEL)) {
                    userName = messageParts[1].split(":")[1];
                    if (messageParts[2].contains(ProjectConstants.PROJECT_LABEL)) {
                        projectName = messageParts[2].split(":")[1];
                        taskName = messageParts[3];
                        return new Take(userName, projectName, taskName);
                    }
                }

        }
        return null;
    }

    /**
     * This method parses a GET_PROJECT command
     * and creates a GetProject object
     * @param message the command string from the user
     * @return a new ASN1 GetProject object
     */
    public GetProject getProject(String message) {
        String projectName;
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECT_COMMAND) && messageParts.length == 2) {
            projectName = messageParts[1];
            return new GetProject(projectName);
        }
        return null;
    }

    /**
     * This method parses a GET_PROJECTS command
     * and creates a GetProjects object
     * @param message the command string from the user
     * @return a new ASN1 GetProjects object
     */
    public GetProjects getProjects(String message) {
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECTS_COMMAND) && messageParts.length == 1) {
            return new GetProjects();
        }
        return null;
    }
}
