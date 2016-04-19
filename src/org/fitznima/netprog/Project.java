package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

import java.util.ArrayList;

/**
 * This class facilitates the encoding/decoding of a message using ASN1 using
 * the following format:
 Project ::= [1] SEQUENCE {name UTF8String, tasks SEQUENCE OF Task }
 * Created by FitzRoi on 4/7/16.
 */
public class Project extends ASNObj {
    private String projectName;
    private ArrayList<Task> tasks;

    public Project() {}

    public Project(String name, ArrayList<Task> tasks) {
        this.projectName = name;
        this.tasks = tasks;
    }


    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(projectName).setASN1Type(Encoder.TAG_UTF8String));
        enc.addToSequence(Encoder.getEncoder(tasks).setASN1Type(Encoder.TAG_SEQUENCE));
        return enc.setASN1Type(Encoder.TAG_SEQUENCE);
    }

    @Override
    public Project decode(final Decoder dec) throws ASN1DecoderFail {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final String name = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);

        ArrayList<Task> tasks = new ArrayList<Task>();

        try {
            tasks = decoder.getSequenceOfAL(dec.getTypeByte(), new Task());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new Project(name, tasks);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new Project();
    }

    public String getProjectName() {
        return projectName;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }
}
