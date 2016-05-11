package org.fitz.netprog.asnobjects;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/**
 * This class facilitates the encoding/decoding of a
 * message using ASN1 of the format:
 * Register ::= [13] SEQUENCE {project UTF8String}
 * Created by FitzRoi on 4/7/16.
 */
public class Register extends ASNObj {
    private String project;


    public Register() {}

    public Register(String project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(project).setASN1Type(Encoder.TAG_UTF8String));
        return enc.setASN1Type(ProjectTags.TYPE_REGISTER);
    }

    @Override
    public Register decode(final Decoder dec) throws ASN1DecoderFail  {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final String project = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new Register(project);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new Register();
    }


    public String getProject() {
        return project;
    }
}
