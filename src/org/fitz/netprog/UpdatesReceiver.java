package org.fitz.netprog;

import net.ddp2p.ASN1.ASN1DecoderFail;
import net.ddp2p.ASN1.Decoder;
import net.ddp2p.ASN1.Encoder;
import org.fitz.netprog.asnobjects.*;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * This class facilitates the receipt of updates from the server
 * Created by FitzRoi on 5/5/16.
 */
public class UpdatesReceiver extends Thread {
    private DatagramSocket sock;
    private boolean exit = false;

    public UpdatesReceiver(DatagramSocket sock) throws SocketException {
        this.sock = sock;
    }

        @Override  public void run() {

            while(!exit) {
                try {
                    byte[] buffer;
                    buffer = new byte[1024 * 4];
                    DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                    sock.receive(incomingPacket);

                    byte data[] = incomingPacket.getData();

                    final Decoder dec = new Decoder(data);
                    byte requestTag = dec.getTypeByte();

                    switch (requestTag) {

                        case ProjectTags.TYPE_PROJECT: {
                            Project project = new Project().decode(dec);
                            ArrayList<Task> tasks = project.getTasks();

                            String str = ProjectConstants.PROJECT_LABEL +
                                    ";" + project.getProjectName();
                            for (Task task : tasks) {
                                str += task.getTaskName() + ";";
                                str += Encoder.getGeneralizedTime(task.getStartTime().getTime()) + ";";
                                str += Encoder.getGeneralizedTime(task.getEndTime().getTime()) + ";";
                            }

                            System.out.println(str);
                        }

                    }

                } catch (ASN1DecoderFail | IOException asn1DecoderFail) {
                    asn1DecoderFail.printStackTrace();
                }
            }
        }

    /**
     * This method sets an exit flag so the thread can exit safely
     */
    public void exit(){
        exit = true;
    }

}
