package org.fitz.netprog.asnobjects;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.ASNObj;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import org.fitz.netprog.Util;

import java.util.Date;

/**
 * This class facilitates the encoding/decoding of
 * in ASN1 of a message based on the following format:
 * Task ::= [1] SEQUENCE {name UTF8String,
 *                          start GeneralizedTime,
 *                          end GeneralizedTime,
 *                          ip UTF8String,
 *                          port INTEGER,
 *                          done BOOLEAN}
 * Created by FitzRoi on 4/7/16.
 */
public class Task extends ASNObj {
    private String taskName;
    private Date startTime = null;
    private Date endTime = null;
    private String ip = null;
    private int port = 0;
    private boolean done = false;

    public Task() {}

    public Task(String name, String start, String end, String ip, int port, boolean done) {
        this.taskName = name;
        this.startTime = Util.getDate(start);
        this.endTime = Util.getDate(end);
        this.ip = ip;
        this.port = port;
        this.done = done;
    }

    public Task(String name, Date start, Date end, String ip, int port, boolean done) {
        this.taskName = name;
        this.startTime = start;
        this.endTime = end;
        this.ip = ip;
        this.port = port;
        this.done = done;
    }


    @Override
    public String toString() {
        return null;
    }

    @Override
    public Encoder getEncoder() {
        final Encoder enc = new Encoder().initSequence(); // creates SEQUENCE
        enc.addToSequence(new Encoder(taskName).setASN1Type(Encoder.TAG_UTF8String));
        final String stime = Encoder.getGeneralizedTime(startTime.getTime());
        final String etime = Encoder.getGeneralizedTime(startTime.getTime());
        enc.addToSequence(new Encoder(stime).setASN1Type(Encoder.TAG_GeneralizedTime));
//        System.out.print("encoded time: " +new Encoder(startTime.getTime()).setASN1Type(Encoder.TAG_GeneralizedTime));
        enc.addToSequence(new Encoder(etime).setASN1Type(Encoder.TAG_GeneralizedTime));
        enc.addToSequence(new Encoder(ip).setASN1Type(Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(port).setASN1Type(Encoder.TAG_INTEGER));
        enc.addToSequence(new Encoder(done).setASN1Type(Encoder.TAG_BOOLEAN));
        return enc.setASN1Type(ProjectTags.TYPE_TASK);
    }

    @Override
    public Task decode(final Decoder dec) throws ASN1DecoderFail {
        final Decoder decoder = dec.getContent(); // remove the SEQUENCE envelope
        final String name = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        final Date start = decoder.getFirstObject(true).getGeneralizedTimeCalender_().getTime();
        final Date end = decoder.getFirstObject(true).getGeneralizedTimeCalender_().getTime();
        final String ip = decoder.getFirstObject(true).getString(Encoder.TAG_UTF8String);
        final int port = decoder.getFirstObject(true).getInteger().intValue();
        final boolean done = decoder.getFirstObject(true).getBoolean();

//        if (dec.getTypeByte() != 0) throw new ASN1DecoderFail("Extra objects!");
        return new Task(name, start, end, ip, port, done);
    }

    /**
     * Returns a simple instance of this class for ASN1 library use.
     */
    @Override
    public ASNObj instance() throws CloneNotSupportedException {
        return new Task();
    }

    public String getTaskName() {
        return taskName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isDone() {
        return done;
    }
}
