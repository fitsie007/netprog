package org.fitznima.netprog;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class Server {

    public static void runServer(int port, String dbPath) {
        byte[] b = new byte[100];
        int readNr;
        OutputStream out;
        try {
            ServerSocket server_sock = new ServerSocket(port, 5);
            for (; ; ) {
                Socket sock = server_sock.accept();
                (out = sock.getOutputStream()).write("Hello!\n".getBytes("latin1"));
                out.flush();

                for (int k = 0; k < 3; k++) {
                    if ((readNr = sock.getInputStream().read(b)) == -1) break;
                    out.write(b, 0, readNr);
                    out.flush();
                }
                sock.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
