package org.fitznima.netprog;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by FitzRoi on 2/26/16.
 */
public class DBManager {
    private String dbPath;
    public DBManager(String dbPath){
        this.dbPath = dbPath;
    }

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
