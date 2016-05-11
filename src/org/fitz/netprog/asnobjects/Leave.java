package org.fitz.netprog.asnobjects;

import net.ddp2p.ASN1.*;

/**
 * This class facilitates the encoding/decoding of a
 * message using ASN1 of the format:
 * Leave ::= [14] Register
 * Created by FitzRoi on 4/7/16.
 */
public class Leave extends ASNObj {
    private Register register;

    public Leave() {}

    public Leave(Register register) {
        this.register = register;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(register.getEncoder().setASN1Type(ProjectTags.TYPE_REGISTER));
        return enc.setASN1Type(ProjectTags.TYPE_LEAVE);
    }

    @Override
    public Leave decode(final Decoder dec) throws ASN1DecoderFail  {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        Decoder d_t;
        Register register = new Register();

        d_t = decoder.getFirstObject(true, ProjectTags.TYPE_PROJECT);
        try {
            register = (Register) ((ASNObjArrayable)new Project()).instance().decode(d_t);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new Leave(register);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new Leave();
    }


    public Register getRegister() {
        return register;
    }
}
