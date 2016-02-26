package org.fitznima.netprog;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class Client {
    public static void runClient(int port) {
        Socket sock;
        String input;
        BufferedReader reader;
        BufferedWriter out;

        try {
            ServerSocket server_sock = new ServerSocket(port, 5);
            for (; ; ) {
                sock = server_sock.accept();
                reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), "latin1"));
                out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "latin1"));
                out.write("Hello!\n");
                out.flush();
                for (int k = 0; k < 3; k++) {
                    if ((input = reader.readLine()) == null)
                        break;
                    out.write(input + "\n");
                    out.flush();
                }
                sock.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage()); //we may have to print error to log, etc
            e.printStackTrace();
        }
    }

}
