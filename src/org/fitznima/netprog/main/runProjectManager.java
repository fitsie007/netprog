package org.fitznima.netprog.main;

import gnu.getopt.Getopt;
import org.fitznima.netprog.DBManager;
import org.fitznima.netprog.constants.ProjectConstants;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by FitzRoi on 2/26/16.
 */
public class runProjectManager {

    public static void main(String args[]) throws SQLException {
        String dbPath=null;
        int port=0;
        int option;
        String arg;
        Connection connection = null;
        Statement stmt = null;

        Getopt g = new Getopt("server", args, "p:d:");
        g.setOpterr(false); // We'll do our own error handling

        while ((option = g.getopt()) != -1)
            switch (option) {
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    System.out.println("port: " + port);
                    break;
                case 'd':
                    dbPath = g.getOptarg();
                    System.out.println("db path: " + dbPath);
                    break;
            }

        if(dbPath!=null && port!=0){
            DBManager dbManager = new DBManager(dbPath);
            if(dbManager.databaseExists(dbPath)){

                connection = dbManager.connectToDB();
            }
            else{
                connection = dbManager.connectToDB();
                //create table
                String createTableSql = ProjectConstants.createTableSQL;
                stmt = connection.createStatement();
                stmt.executeUpdate(createTableSql);
                stmt.close();
                connection.close();

            }
        }
    }


    public void startServer(int port, String dbPath){
        org.fitznima.netprog.Server.runServer(port, dbPath);
    }

    public void startClient(int port){
        org.fitznima.netprog.Client.runClient(port);
    }


}
