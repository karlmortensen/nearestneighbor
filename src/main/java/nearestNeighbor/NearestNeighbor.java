package nearestNeighbor;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NearestNeighbor {

    

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException {
        Class.forName("org.sqlite.JDBC");
        // readCsvAndCreateDatabase(GoogleApiKey.getApiKey(), "shortData.csv", "database.db");
        // readCsvAndCreateDatabase(GoogleApiKey.getApiKey(), "data.csv", "database.db");
        findAllDistancesAndStoreInDatabase(38.805853, -77.176633, "test3.db");
        //System.out.println("Distance is: " + getDistanceBetween(33.23, -97.41, 33.234, -97.2109, GoogleApiKey.getApiKey()));
    }

    public static void readCsvAndCreateDatabase(String apiKey, String csvFilePathAndName, String databasePathAndName) {
        try {
            File file = new File(databasePathAndName);
            file.delete();
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file.toString())) {
                Statement statement = conn.createStatement();
                String sql = "CREATE TABLE members (memberId INTEGER PRIMARY KEY NOT NULL, household TEXT NOT NULL, address TEXT NOT NULL, city TEXT NOT NULL, zip TEXT NOT NULL, latitude TEXT, longitude TEXT)";
                statement.executeUpdate(sql);
                sql = "CREATE TABLE distances (distanceId, INTEGER PRIMARY KEY NOT NULL, memberID NOT NULL, remoteMemberId NOT NULL, crowDistance INTEGER, driveDistance INTEGER)";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(sql);

                    Reader in = new FileReader(csvFilePathAndName);
                    Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader("Head of House and Spouse", "Address - Street 1", "Address - City", "Address - Postal Code").parse(in);
                    // Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
                    boolean skip = true;
                    for (CSVRecord record : records) {
                        if (skip) {
                            skip = false;
                            continue;
                        }
                        String address = record.get("Address - Street 1") + "','" + record.get("Address - City") + "','" + record.get("Address - Postal Code");
                        //KDM LatLng latlng = getLatLngFromAddress(address, apiKey);
                        LatLng latlng = new LatLng(34.2, -77.8);
                        sql = "INSERT INTO members (household, address, city, zip, latitude, longitude) VALUES ('"
                                + record.get("Head of House and Spouse") + "','" + address + "'," + latlng.lat + "," + latlng.lng + ")";
                        st.executeUpdate(sql);
                    }
                }
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(NearestNeighbor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void findAllDistancesAndStoreInDatabase(double myLat, double myLong, String databasePathAndName) {
        GeoApiContext context = new GeoApiContext().setApiKey(GoogleApiKey.getApiKey());
        List<String> startingLocations = new ArrayList<>();
        List<String> endingLocations = new ArrayList<>();

        File file = new File(databasePathAndName);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file.toString())) {
            Statement statement = conn.createStatement();
            String sql = "SELECT memberId, latitude, longitude FROM members ORDER BY memberId";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                startingLocations.add(myLat + "," + myLong);
                endingLocations.add(rs.getDouble(2) + "," + rs.getDouble(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(NearestNeighbor.class.getName()).log(Level.SEVERE, null, ex);
        }

     /*
        for (String s : endingLocations) {
            System.out.println("asdf: " + s);
        }
     */

 /*
        try {
            DistanceMatrix dmar = DistanceMatrixApi.getDistanceMatrix(context,
                    startingLocations.toArray(new String[startingLocations.size()]),
                    endingLocations.toArray(new String[endingLocations.size()])).await();
            for (com.google.maps.model.DistanceMatrixElement d : dmar.rows[0].elements) {
                System.out.println("d=" + d.distance.humanReadable);
            }

    } catch (Exception ex) {
            Logger.getLogger(gradle1.class.getName()).log(Level.SEVERE, null, ex);
        }
        //dmar.arrivalTime(arrivalTime)

     /*
        for (String s : endingLocations) {
            System.out.println("asdf: " + s);
        }
     */

 /*
        try {
            DistanceMatrix dmar = DistanceMatrixApi.getDistanceMatrix(context,
                    startingLocations.toArray(new String[startingLocations.size()]),
                    endingLocations.toArray(new String[endingLocations.size()])).await();
            for (com.google.maps.model.DistanceMatrixElement d : dmar.rows[0].elements) {
                System.out.println("d=" + d.distance.humanReadable);
            }

    } catch (Exception ex) {
            Logger.getLogger(gradle1.class.getName()).log(Level.SEVERE, null, ex);
        }
        //dmar.arrivalTime(arrivalTime)
         */
    }

    public static double getDistanceBetween(double startLat, double startLong, double endLat, double endLong, String apiKey) {
        GeoApiContext context = new GeoApiContext().setApiKey(apiKey);
        String[] starting = {startLat + "," + startLong};
        String[] ending = {endLat + "," + endLong};
        String[] starting2 = {startLat + "," + startLong, "43.2,-91.0"};
        String[] ending2 = {endLat + "," + endLong, "42.1, -88.2"};

        try {
            DistanceMatrix dmar = DistanceMatrixApi.getDistanceMatrix(context, starting, ending).await();
            System.out.println("dmar=" + dmar.toString());
            dmar = DistanceMatrixApi.getDistanceMatrix(context, starting2, ending2).await();
            System.out.println("dmar=" + dmar.toString());
        } catch (Exception ex) {
            Logger.getLogger(NearestNeighbor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //dmar.arrivalTime(arrivalTime)
        return 3.4;
    }

    public static LatLng getLatLngFromAddress(String address, String apiKey) {
        GeoApiContext context = new GeoApiContext().setApiKey(apiKey);
        LatLng latLng = null;
        GeocodingResult[] results;
        try {
            results = GeocodingApi.geocode(context, address).await();
            latLng = results[0].geometry.location;
            // System.out.println(results[0].formattedAddress);
        } catch (Exception ex) {
            Logger.getLogger(NearestNeighbor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return latLng;
    }
}
