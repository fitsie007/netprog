package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import org.fitznima.netprog.constants.ProjectConstants;

import java.util.ArrayList;

/**
 * Created by FitzRoi on 4/9/16.
 */
public class EncoderTester {
    public static void main(String args[]) {

      Take take = encodeNewTake("TAKE;USER:Johny;PROJECT:Exam;Buy paper");
        Project project = encodeNewProject("PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;", "127.0.0.1", 2323);

        byte[] testbytes = take.encode();
        Decoder testdec = new Decoder(testbytes);

        byte[] bytes = project.encode();
        testdec = new Decoder(bytes);

        try {
//            Take output = new Take().decode(testdec);
//            System.out.println(output.getProjectName());

            Project project1 = new Project().decode(testdec);
            System.out.println(project1.getProjectName());
            ArrayList<Task> tasks = project1.getTasks();
            for(Task task:tasks)
                System.out.print(task.getTaskName() +"\n");
        } catch (ASN1DecoderFail e) {
            e.printStackTrace();
        }

    }


    public static Project encodeNewProject(String message, String ip, int port) {
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


    public static Take encodeNewTake(String message) {
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
}
