package org.fitznima.netprog;

import gnu.getopt.Getopt;
import org.fitznima.netprog.constants.ProjectConstants;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class Client {
    public static void main(String args[]) throws SQLException {
        String dbPath = null;
        int port = 0;
        int option;
        Connection connection = null;
        Statement stmt = null;
        Socket sock;
        String input;
        BufferedReader inputReader;
        byte[] b = new byte[2000];
        int readNr;


        Getopt g = new Getopt("Client", args, "p:");
        g.setOpterr(false); // We'll do our own error handling

        while ((option = g.getopt()) != -1)
            switch (option) {
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    System.out.println("port: " + port);
                    break;
            }


        try {
            sock = new Socket(ProjectConstants.host, port);

            InputStream in = sock.getInputStream();
            OutputStream out = sock.getOutputStream();
            inputReader = new BufferedReader(new InputStreamReader(System.in));
//            if ((readNr = in.read(b)) == -1) return;
//            System.out.print(new String(b, 0, readNr, "latin1")); // Print Hello
            out.write(ProjectConstants.PROJECT_SAMPLE.getBytes()); out.flush();
            for (; ; ) {
                input = inputReader.readLine();
                out.write((input + "\n").getBytes("latin1"));
                System.out.print(input + "\n");
                out.flush();
                if ((readNr = in.read(b)) == -1){System.out.print("\nError"); break;}
                System.out.print(new String(b, 0, readNr, "latin1"));
            }

        } catch (IOException e) {
            System.out.println(e.getMessage()); //we may have to print error to log, etc
            e.printStackTrace();
        }
    }


}
