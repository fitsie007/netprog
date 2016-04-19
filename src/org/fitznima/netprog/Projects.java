package org.fitznima.netprog;

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
public class Projects extends ASNObj {
    public Projects() {}

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        return enc.setASN1Type(Encoder.TAG_SEQUENCE);
    }

    @Override
    public Projects decode(final Decoder dec) throws ASN1DecoderFail  {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new Projects();
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new Projects();
    }

}
