package org.fitz.netprog.asnobjects;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/**
 * This class facilitates the encoding/decoding of
 * a message of the format:
 * Projects ::= [2] SEQUENCE {}
 * Created by FitzRoi on 4/7/16.
 */
public class GetProjects extends ASNObj {
    public GetProjects() {}

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        return enc.setASN1Type(ProjectTags.TYPE_GET_PROJECTS);
    }

    @Override
    public GetProjects decode(final Decoder dec) throws ASN1DecoderFail  {
//        if (dec.getTypeByte() != ProjectTags.TYPE_GET_PROJECTS) throw new ASN1DecoderFail("Extra objects!");
        return new GetProjects();
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new GetProjects();
    }

}
