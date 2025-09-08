package com.github.maxpesh.airports;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.geometric.PGpoint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@org.springframework.stereotype.Repository
class Repository implements AutoCloseable {
    private final HikariDataSource dataSource;
    private final Properties properties;

    Repository() {
        properties = readProperties();
        var conf = new HikariConfig();
        conf.setJdbcUrl(properties.getProperty("url"));
        conf.setUsername(properties.getProperty("username"));
        conf.setPassword(properties.getProperty("password"));
        dataSource = new HikariDataSource(conf);
    }

    List<Airport> getAirportsLike(String airportName, int limit) {
        var airports = new ArrayList<Airport>();
        try (var conn = getConnection()) {
            try (var stmt = conn.prepareStatement("""
                    select airport_code, airport_name, city, coordinates, timezone
                    from airports
                    where airport_name ilike '%%%s%%' -- airportName
                    limit %d -- limit
                    """.formatted(airportName, limit))) {
                var rs = stmt.executeQuery();
                printWarnings(stmt.getWarnings(), rs.getWarnings());
                while (rs.next()) {
                    var code = rs.getString("airport_code");
                    var name = rs.getString("airport_name");
                    var city = rs.getString("city");
                    var coordinates = (PGpoint) rs.getObject("coordinates");
                    var point = new Point(coordinates.x, coordinates.y);
                    var timezone = rs.getString("timezone");
                    var airport = new Airport(code, name, city, point, ZoneId.of(timezone));
                    airports.add(airport);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return airports;
    }

    private Connection getConnection() {
        try {
            var conn = dataSource.getConnection();
            printWarnings(conn.getWarnings());
            conn.setAutoCommit(false);
            try (var stmt = conn.prepareStatement("set bookings.lang=%s"
                    .formatted(properties.getProperty("bookings.lang")))) {
                stmt.executeUpdate();
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    private static Properties readProperties() {
        var props = new Properties();
        var dbPropsUrl = Thread.currentThread().getContextClassLoader().getResource("database.properties");
        if (dbPropsUrl == null) {
            throw new RuntimeException("File database.properties should be present in the classpath");
        }
        try {
            try (var in = new FileInputStream(dbPropsUrl.getPath())) {
                props.load(in);
            }
        } catch (FileNotFoundException e) {
            assert false; // Should never fall here
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    @Override
    public void close() {
        dataSource.close();
    }

    public static void printWarnings(SQLWarning... warnings) {
        if (warnings != null) {
            for (var warning : warnings) {
                if (warning != null) {
                    System.out.println("\n---Warning---\n");
                }
                while (warning != null) {
                    System.out.println("Message: " + warning.getMessage());
                    System.out.println("SQLState: " + warning.getSQLState());
                    System.out.print("Vendor error code: ");
                    System.out.println(warning.getErrorCode());
                    System.out.println();
                    warning = warning.getNextWarning();
                }
            }
        }
    }
}
