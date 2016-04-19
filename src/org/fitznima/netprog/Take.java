package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/**
 * This class facilitates the encoding/decoding of a message using ASN1
 * based on the following format:
 Take ::= [5] SEQUENCE {user UTF8String, project UTF8String, task UTF8String}
 * Created by FitzRoi on 4/7/16.
 */
public class Take extends ASNObj {
    private String userName;
    private String projectName;
    private String taskName;


    public Take() {}

    public Take(String userName, String projectName, String taskName) {
        this.userName = userName;
        this.projectName = projectName;
        this.taskName = taskName;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(userName).setASN1Type(Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(projectName).setASN1Type(Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(taskName).setASN1Type(Encoder.TAG_UTF8String));
        return enc.setASN1Type(Encoder.TAG_SEQUENCE);
    }

    @Override
    public Take decode(final Decoder dec) throws ASN1DecoderFail  {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final String userName = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        final String projectName = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        final String taskName = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new Take(userName, projectName, taskName);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new Take();
    }

    public String getUserName() {
        return userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getTaskName() {
        return taskName;
    }
}
