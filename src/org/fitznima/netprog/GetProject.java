package org.fitznima.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;

/** This class processes getproject commands of the following ASN1 format:
 * GetProject ::= [4] SEQUENCE {name UTF8String}
 * Created by FitzRoi on 4/7/16.
 */
public class GetProject extends ASNObj {
    String projectName;


    public GetProject() {}

    public GetProject(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(projectName).setASN1Type(Encoder.TAG_UTF8String));
        return enc.setASN1Type(Encoder.TAG_SEQUENCE);
    }

    @Override
    public GetProject decode(final Decoder dec) throws ASN1DecoderFail  {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final String projectName = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new GetProject(projectName);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new GetProject();
    }

}
