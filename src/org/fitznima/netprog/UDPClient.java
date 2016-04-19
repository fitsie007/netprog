package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import org.fitznima.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * This class creates a UDP client
 * Created by FitzRoi on 4/7/16.
 */
public class UDPClient {
    private String host;
    private int port;
    private DatagramSocket sock;
    public UDPClient(String host, int port) throws SocketException {
        this.host = host;
        this.port = port;
        this.sock = new DatagramSocket(port);
    }

    public String processCommand(String command){
        try{

            InetAddress IPAddress = sock.getInetAddress();
            InputStream in;
            ProjectOK ok = null;
            DatagramPacket reply;

            if (command.contains(ProjectConstants.PROJECT_DEFINITION)) {
                byte msg[] = encodeNewProject(command, IPAddress.getHostAddress()).encode();
                reply = new DatagramPacket(msg,
                        msg.length,
                        IPAddress,
                        port);
                sock.send(reply);

            }

            if (command.contains(ProjectConstants.TAKE_COMMAND)) {
                byte msg[] = encodeNewTake(command).encode();
                reply = new DatagramPacket(msg,
                        msg.length,
                        IPAddress,
                        port);
                sock.send(reply);
            }

            if (command.contains(ProjectConstants.GET_PROJECTS_COMMAND)) {
                byte msg[] = getProjects(command).encode();
                reply = new DatagramPacket(msg,
                        msg.length,
                        IPAddress,
                        port);
                sock.send(reply);
            }

            if (command.contains(ProjectConstants.GET_PROJECT_COMMAND)) {
                byte msg[] = encodeNewGetProject(command).encode();
                reply = new DatagramPacket(msg,
                        msg.length,
                        IPAddress,
                        port);
                sock.send(reply);
            }

            byte[] buffer;
            while (true) {
                buffer = new byte[1024 * 4];
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                sock.receive(incomingPacket);

                byte data[] = incomingPacket.getData();

                final Decoder dec = new Decoder(data);
                ProjectsAnswer projectsAnswer = null;

                try {
                    ok = new ProjectOK().decode(dec);
                } catch (final ASN1DecoderFail e) {
                    try {
                        projectsAnswer = new ProjectsAnswer().decode(dec);
                    } catch (final Exception e1) {
                        e1.printStackTrace();
                    }
                }

                if (ok == null && projectsAnswer == null) {
                    sock.close();
                    return "Unknown command from server";
                }

                if (ok != null) {
                    if (ok.getCode() == ProjectConstants.SUCCESS_CODE)
                        return ProjectConstants.OK + ";" + command;
                    else
                        return ProjectConstants.FAIL + ";" + command;
                }
                if(projectsAnswer != null){
                    ArrayList<Project> projects = projectsAnswer.getProjects();

                    final StringBuilder str = new StringBuilder(ProjectConstants.OK +";" +ProjectConstants.PROJECTS_LABEL+
                            ";" + projects.size());
                    for(Project project: projects){
                        str.append(";" + project.getProjectName());
                    }
                    return str.toString();
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public Project encodeNewProject(String message, String ip) {
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


    public Take encodeNewTake(String message) {
        String userName = null, projectName = null, taskName= null;
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

    public GetProject encodeNewGetProject(String message) {
        String projectName = null;
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECT_COMMAND) && messageParts.length == 2) {
            projectName = messageParts[1];
            return new GetProject(projectName);
        }
        return null;
    }

    public Projects getProjects(String message) {
        String messageParts[] = message.split(";");

        if (messageParts[0].equals(ProjectConstants.GET_PROJECTS_COMMAND) && messageParts.length == 1) {
            return new Projects();
        }
        return null;
    }
}
