package org.fitz.netprog;

import org.fitz.netprog.asnobjects.Project;
import org.fitz.netprog.constants.ProjectConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

/**
 * This class provides functionality for sending updates to registered clients
 * Created by FitzRoi on 5/5/16.
 */


public class UpdatesProvider extends Thread {

    private DBManager db;
    private DatagramSocket sock;
    private Connection connection = null;

    public UpdatesProvider(DatagramSocket sock, DBManager db){
        this.sock = sock;
        this.db = db;
    }

    @Override
    public void run() {
        deleteExpiredRegistrations();
        sendUpdates();
    }

    /**
     * This method send updates to registered clients
     * periodically. The default frequency is every minute
     */
    public void sendUpdates() {
        try {

            if(connection == null)
              connection = db.connectToDB();

            String queryStr = "SELECT * FROM " + ProjectConstants.REGISTRATION_TABLE + "";
            PreparedStatement selectQuery = connection.prepareStatement(queryStr);
            ResultSet clientList = selectQuery.executeQuery();


            while (clientList.next()) {

                String projectName = clientList.getString("PROJECT_NAME");
                String savedIP = clientList.getString("OWNER_IP");
                int savedPort = Integer.parseInt(clientList.getString("OWNER_PORT"));

                Project project = db.getProject(projectName, savedIP, savedPort);
                DatagramPacket reply;

                InetAddress IPAddress = InetAddress.getByName(Util.parseIPAddress(savedIP));

                if (project != null) {
                    try {
                        byte msg[] = project.encode();
                        reply = new DatagramPacket(msg,
                                msg.length,
                                IPAddress,
                                savedPort);
                        sock.send(reply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method deletes client registrations after a specified time
     * The default is 1 hour
     */
    public void deleteExpiredRegistrations(){
        try {
            Date now = new Date();
            Connection connection = db.connectToDB();

            String queryStr = "SELECT * FROM " + ProjectConstants.REGISTRATION_TABLE +"";
            PreparedStatement selectQuery = connection.prepareStatement(queryStr);
            ResultSet clientList = selectQuery.executeQuery();

            while (clientList.next()) {
                Date registeredTime = Util.dbFormatDateStr(clientList.getString("REGISTERED_TIME"));
                Date expirationDate = new Date(registeredTime.getTime() +
                        (ProjectConstants.HOURS_TIME_OUT * ProjectConstants.HOUR_IN_MILLISECONDS));
                String deleteQueryStr = "DELETE FROM " + ProjectConstants.REGISTRATION_TABLE + " WHERE datetime(REGISTERED_TIME) >=?";

                if(expirationDate.before(now)){
                    PreparedStatement deleteQuery = connection.prepareStatement(deleteQueryStr);
                    deleteQuery.setString(1, Util.dbFormatDate(expirationDate));
                    deleteQuery.executeUpdate();
                }

            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
