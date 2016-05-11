package org.fitz.netprog.asnobjects;

import net.ddp2p.ASN1.Encoder;

/**
 * Created by FitzRoi on 4/26/16.
 * ASN1 Bit positions: [8][7] [6]  [5][4][3][2][1]
 *                     CLASS  P/C  TAG    NUMBER   (P/C : Primitive/Constructed)
 */
public class ProjectTags {

    //cannot use buildASN1byteType since I'm using switch statement to check types
//    public static final byte TYPE_TASK = Encoder.buildASN1byteType(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte)1);
    public static final byte TYPE_PROJECT_OK          = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 0;
    public static final byte TYPE_TASK                = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 1;
    public static final byte TYPE_PROJECT             = TYPE_TASK;
    public static final byte TYPE_GET_PROJECTS        = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 2;
    public static final byte TYPE_PROJECTS_ANSWER     = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 3;
    public static final byte TYPE_GET_PROJECT         = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 4;
    public static final byte TYPE_TAKE                = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 5;
    public static final byte TYPE_GET_PROJECT_RESPONSE= (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 8;
    public static final byte TYPE_REGISTER            = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 13;
    public static final byte TYPE_LEAVE               = (Encoder.CLASS_APPLICATION<<6) + (Encoder.PC_CONSTRUCTED<<5) + 14;
}
