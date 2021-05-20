package com.berthold.covidinfo;

import android.util.Log;

import com.berthold.covidinfo.ui.home.CovidSearchResultData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CovidDataBase {

    public static final int ERROR = 0;                // Index in 'result' for 'error' text
    public static final int ERROR_DESCRIPTION = 1;    // Index in 'result' for description of error

    public static void make(String name) throws Exception {

        // DB- Conection
        String DB_DRIVER = "org.h2.Driver";
        String DB_CONNECTION = "jdbc:h2:";
        String DB_USER = "";
        String DB_PASSWORD = "";

        // Create database or get existing...
        Connection conn;
        try {
            conn = getDataBase(DB_DRIVER, DB_CONNECTION + name, DB_USER, DB_PASSWORD);
        } catch (SQLException se) {
            System.out.println("Error while creating database:" + DB_CONNECTION);
            System.out.println(se);
            return;
        }

        // Creates the tables
        try {
            Statement stmt = null;
            stmt = conn.createStatement();

            // Covid data table
            stmt.executeUpdate("create table"
                    + "	covid"
                    + " (key1 identity,"
                    + "datasetID char(255),"
                    + "bundesland char(255),"
                    + "bez char(255),"
                    + "name char(255),"
                    + "lastUpdate char(255),"
                    + "beenhere boolean,"
                    + "casesPer100K float)");

            // Create sample entry's
            stmt.executeUpdate("insert into covid "
                    + "(name,bundesland,bez) "
                    + "values ('Landau','Reinland-Pfalz','-')");

        } catch (SQLException se) {
            Log.v("DBMAKE", se.toString());
            return;
        }

        // Close database
        conn.close();
        Log.i("DBMAKE", "Closed connection");
    }

    /**
     * Opens an existing database. If no database found, create it.
     *
     * @param DB_DRIVER
     * @param DB_CONNECTION
     * @param DB_USER
     * @param DB_PASSWORD
     * @return
     * @throws Exception
     */
    public static Connection getDataBase(String DB_DRIVER, String DB_CONNECTION, String DB_USER, String DB_PASSWORD) throws Exception {
        Class.forName(DB_DRIVER);
        Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        conn.setAutoCommit(true);
        return (conn);
    }

    //
    //
    // Database generic methods.
    //
    //
    /**
     * This handles a generic sql- request.
     *
     * @param sqlString The sql syntax.
     * @param conn      The database connection.
     * @return A string buffer containing the results. Fields are comma separated, rows are separated by #.
     * If no result could be found the string 'empty' is returned. In case of an error the string 'error' is returned.
     */
    public static StringBuffer sqlRequest(String sqlString, Connection conn) {
        StringBuffer result = new StringBuffer();

        try {
            PreparedStatement selectPreparedStatement = null;
            selectPreparedStatement = conn.prepareStatement(sqlString);
            ResultSet rs = selectPreparedStatement.executeQuery();

            ResultSetMetaData md = rs.getMetaData();
            int maxcol = md.getColumnCount();

            // Check if result is empty
            if (rs.isBeforeFirst()) {
                // Get entries
                while (rs.next()) {
                    for (int coll = 1; coll <= maxcol; coll++) {
                        if (coll != maxcol)
                            result.append(rs.getString(coll) + ","); // Column are comma separated
                        else result.append(rs.getString(coll));
                    }
                    result.append("#");    // This is the end of the row
                }
            } else
                // There was no first result, therefore there was no rs.beforeFirst
                result.append("empty");
        } catch (SQLException e) {
            result.append("error");
            Log.v("DBMAKE", e.toString());
        }
        return result;
    }

    /**
     * Check if an entry already exists (case sensitive)
     *
     * @param table  Name of table
     * @param column Name of column to be checked
     * @param value  Value of column
     * @return true if the value is already a member of the Database
     * <p>
     * todo: Add case insensitive
     */

    public static Boolean entryDoesExist(String table, String column, String value, Connection conn) {
        StringBuffer rs = sqlRequest("select " + column + " from " + table + " where " + column + "='" + value + "'", conn);

        if (rs.toString().replace("#", "").trim().equals(value)) return true;

        return false;
    }

    //
    //
    // App specific methods
    //
    //
    /**
     * Checks if an entry for an given date already exists.
     *
     * Name, state,county are the unique identifiers. An entry exists if the
     * date of the new entry equals the date of an entry already saved inside the database.
     *
     * @param name          Name of the town.
     * @param bundesland    State.
     * @param bez           County.
     * @param date          Date the dataset was updated.
     * @param conn          The database.
     * @return true if the entry exists, false if not.
     */
    public static Boolean covidDataForThisDateExists (String name,String bundesland,String bez,String date,Connection conn){

        try {
            PreparedStatement selectPreparedStatement = null;
            selectPreparedStatement = conn.prepareStatement("select name,bundesland,bez,lastUpdate from covid where name='"+name+"' and bundesland='"+bundesland+"' and bez='"+bez+"' and lastUpdate='"+date+"'");
            ResultSet rs = selectPreparedStatement.executeQuery();

            // Check if result is empty
            if (rs.isBeforeFirst()) {
                // Get entries
                while (rs.next()) {
                    Log.v ("--DBMAKE",rs.getString("name")+" "+rs.getString("bundesland"));
                }
            } else{
                return false; // No entry found
            }

        } catch (SQLException e) {
            Log.v("DBMAKE", e.toString());
        }
        return true; // Entry was found
    }

    /**
     * Gets all entries matching the given unique identifiers (Name, state,county).
     *
     * @param name          Town.
     * @param bundesland    State.
     * @param bez           County.
     * @param conn          The database.
     *
     * @return A list of {@link CovidSearchResultData} objects.
     */
    public static List<CovidSearchResultData> getEntry(String name, String bundesland, String bez, Connection conn){

        List<CovidSearchResultData> searchResultList=new ArrayList<>();

        try {
            PreparedStatement selectPreparedStatement = null;
            selectPreparedStatement = conn.prepareStatement("select name,bundesland,bez,lastUpdate,casesPer100K,beenhere from covid where name='" + name + "' and bundesland='" + bundesland + "' and bez='" + bez + "'");
            ResultSet rs = selectPreparedStatement.executeQuery();

            // Check if result is empty
            if (rs.isBeforeFirst()) {
                // Get entries
                while (rs.next()) {
                    String fname=rs.getString("name");
                    String fbundesland=rs.getString("bundesland");
                    String fbez=rs.getString("bez");
                    float casesPer100K=rs.getFloat("casesPer100K");
                    String fdate=rs.getString("lastUpdate");

                    CovidSearchResultData entry=new CovidSearchResultData("-",fbundesland,fname,fbez,casesPer100K,fdate);

                    boolean beenHere=rs.getBoolean("beenhere");
                    entry.setBeenHere(beenHere);

                    searchResultList.add(entry);

                    Log.v ("DBMAKE",rs.getString("name")+" "+rs.getString("bundesland")+" "+rs.getString("bez")+"  "+rs.getFloat("casesPer100K"));
                }
            } else{
                // No matching entry found....
                return null;
            }

        } catch (SQLException e){
            Log.v("DBMAKE", e.toString());
        }
        return searchResultList;
    }

    /**
     * Adds an entry into the database.
     *
     * @param name       Town.
     * @param bundesland State.
     * @param bez        County.
     * @param cases100K  Cases.
     * @param date       The date at which this data was updated.
     * @param conn       The database connection.
     */
    public static void insert(String name, String bundesland, String bez, float cases100K, String date, boolean beenHere, Connection conn) {

        try {
            Statement statement = conn.createStatement();

            statement.executeUpdate("insert into covid "
                    + "(name,bundesland,bez,casesPer100K,lastUpdate,beenhere) "
                    + "values ('" + name + "','" + bundesland + "','" + bez + "'," + cases100K + ",'" + date + "',"+beenHere+")");

        } catch (SQLException e) {
            Log.v("DBMAKE", e.toString());
        }
    }
}
