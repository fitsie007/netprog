package org.fitz.netprog;

import gnu.getopt.Getopt;
import org.fitz.netprog.constants.ProjectConstants;

import java.net.SocketException;

/**
 * This class runs the TCP/UDP client
 * Created by FitzRoi on 4/7/16.
 */
public class RunClient {

    public static void main(String args[]) throws SocketException {
        String host = ProjectConstants.HOST;
        boolean useTCP = true;
        String command = null;
        int port = 0;
        int option;
        String response;

        //use GNU Java GetOpt to process command line options
        Getopt g = new Getopt("server", args, "p:d:ut:c:");
        g.setOpterr(false);

        while ((option = g.getopt()) != -1)
            switch (option) {
                case 'p': //port
                    port = Integer.parseInt(g.getOptarg());
                    break;
                case 'd': //domain/host
                    host = g.getOptarg();
                    break;
                case 't': //tcp flag
                    useTCP = true;
                    break;
                case 'u': //udp flag
                    useTCP = false;
                    break;
                case 'c': //string command
                   command = g.getOptarg();
                    break;
            }


        if (port < ProjectConstants.MIN_PORT || port > ProjectConstants.MAX_PORT) {
            System.err.println("Error: invalid port!");
            return;
        }

        if (command == null) {
            System.out.println("Error: Command not provided!");
            System.out.println(ProjectConstants.USAGE);
            return;
        }

        if(useTCP){
            TCPClient tcpClient = new TCPClient(host, port);
            response = tcpClient.processCommand(command);
            System.out.println(response +"\n");
        }
        else{
            UDPClient udpClient = new UDPClient(host, port);
            response = udpClient.processCommand(command);
            System.out.println(response +"\n");
        }

    }

}
