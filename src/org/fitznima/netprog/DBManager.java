package org.fitznima.netprog;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Authors: Fitzroy Nembhard & Nima Agli
 * Date: 2/26/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class provides SQLite Database Management
 */
public class DBManager {
    private String dbPath;

    public DBManager(String dbPath){
        this.dbPath = dbPath;
    }

    /**
     * This method attempts to connect to the database selected
     * @return a connection to the database
     */
    public Connection connectToDB(){
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return c;
    }

    public boolean databaseExists(String dbName){
        File file = new File(dbName);
        return file.exists();
    }
}
