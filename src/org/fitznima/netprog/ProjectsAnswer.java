package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

import java.util.ArrayList;

/**
 * This class facilitates the encoding/decoding of a message using
 * ASN1 based on the following format:
 * ProjectsAnswer ::= [3] SEQUENCE OF Project
 * Created by FitzRoi on 4/7/16.
 */
public class ProjectsAnswer extends ASNObj {
    private ArrayList<Project> projects;

    public ProjectsAnswer() {}

    public ProjectsAnswer(ArrayList<Project> projects) {
        this.projects = projects;
    }


    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(Encoder.getEncoder(projects).setASN1Type(Encoder.TAG_SEQUENCE));
        return enc.setASN1Type(Encoder.TAG_SEQUENCE);
    }

    @Override
    public ProjectsAnswer decode(final Decoder dec) throws ASN1DecoderFail {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        try {
            projects = decoder.getSequenceOfAL(dec.getTypeByte(), new Project());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new ProjectsAnswer(projects);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new ProjectsAnswer();
    }

    public ArrayList<Project> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<Project> projects) {
        this.projects = projects;
    }


}
