package org.fitz.netprog.asnobjects;

import net.ddp2p.ASN1.*;

/** This class processes getproject commands of the following ASN1 format:
 * GetProjectResponse ::= [8] SEQUENCE {
 *                                      status UTF8String,
 *                                      studentname UTF8String,
 *                                      taskcount INTEGER,
 *                                      project Project}
 * Created by FitzRoi on 4/7/16.
 */
public class GetProjectResponse extends ASNObj {
    private String status;
    private String studentName;
    private int taskCount;
    private Project project;


    public GetProjectResponse() {}

    public GetProjectResponse(String status, String studentName, int taskCount, Project project) {
        this.status = status;
        this.studentName = studentName;
        this.taskCount = taskCount;
        this.project = project;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(status).setASN1Type(Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(studentName).setASN1Type(Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(taskCount).setASN1Type(Encoder.TAG_INTEGER));
        enc.addToSequence(project.getEncoder()).setASN1Type(ProjectTags.TYPE_PROJECT);
//        enc.addToSequence(new Encoder(project.encode()).setASN1Type(ProjectTags.TYPE_PROJECT));
        return enc.setASN1Type(ProjectTags.TYPE_GET_PROJECT_RESPONSE);
    }

    @Override
    public GetProjectResponse decode(final Decoder dec) throws ASN1DecoderFail {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final String status = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        final String studentName = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        final int taskCount = decoder.getFirstObject(true).getInteger(Encoder.TAG_INTEGER).intValue();
        Decoder d_t;
        Project proj = new Project();
        d_t = decoder.getFirstObject(true, ProjectTags.TYPE_PROJECT);
        try {
            proj = (Project) ((ASNObjArrayable) new Project()).instance().decode(d_t);

//            ArrayList<Task> tasks = new ArrayList<Task>();
//            tasks = d_t.getSequenceOfAL(ProjectTags.TYPE_TASK, new Task());
//
//            System.out.print("Encoded tasks size:" + tasks.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");

        return new GetProjectResponse(status, studentName, taskCount, proj);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new GetProjectResponse();
    }

    public String getStatus() {
        return status;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public Project getProject() {
        return project;
    }
}
