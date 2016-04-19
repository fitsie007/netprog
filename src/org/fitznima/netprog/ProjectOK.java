package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/**
 * This class facilitates the encoding/decoding of a
 * message using ASN1 of the format:
 * ProjectOK ::= [0] SEQUENCE {code INTEGER}
 * Created by FitzRoi on 4/7/16.
 */
public class ProjectOK extends ASNObj {
    private int code = -1;


    public ProjectOK() {}

    public ProjectOK(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(code).setASN1Type(Encoder.TAG_INTEGER));
        return enc.setASN1Type(Encoder.TAG_SEQUENCE);
    }

    @Override
    public ProjectOK decode(final Decoder dec) throws ASN1DecoderFail  {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final int code = decoder.getFirstObject(true).getInteger(Encoder.TAG_INTEGER).intValue();
//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new ProjectOK(code);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new ProjectOK();
    }


    public int getCode() {
        return code;
    }
}
