package com.github.maxpesh.airports;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.PGStatement;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.geometric.PGpoint;
import org.postgresql.jdbc.PreferQueryMode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@org.springframework.stereotype.Repository
class Repository implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(Repository.class.getName());
    private final HikariDataSource dataSource;

    Repository() {
        Properties properties = readProperties();
        var pgDataSource = new PGSimpleDataSource();
        pgDataSource.setURL(properties.getProperty("url"));
        pgDataSource.setUser(properties.getProperty("username"));
        pgDataSource.setPassword(properties.getProperty("password"));
        pgDataSource.setApplicationName("bookings");
        pgDataSource.setPrepareThreshold(5); // default
        pgDataSource.setPreferQueryMode(PreferQueryMode.EXTENDED_FOR_PREPARED);
        var conf = new HikariConfig();
        conf.setDataSource(pgDataSource);
        conf.setAutoCommit(false);
        conf.setMaximumPoolSize(10);
        conf.setConnectionTimeout(SECONDS.toMillis(30)); // default
        conf.setIdleTimeout(MINUTES.toMillis(10)); // default
        conf.setKeepaliveTime(MINUTES.toMillis(2)); // default
        conf.setMaxLifetime(MINUTES.toMillis(30)); // default
        conf.setInitializationFailTimeout(1); // fail fast
        conf.setValidationTimeout(SECONDS.toMillis(5)); // default
        dataSource = new HikariDataSource(conf);
    }

    List<Airport> getAirportsLike(String airportName, int limit) {
        var airports = new ArrayList<Airport>();
        try (var conn = dataSource.getConnection()) {
            try (var stmt = conn.createStatement()) {
                stmt.executeUpdate("set plan_cache_mode = 'force_generic_plan'");
                logWarnings(conn.getWarnings(), stmt.getWarnings());
            }
            try (var stmt = conn.prepareStatement("""
                    select airport_code, airport_name, city, coordinates, timezone
                    from airports
                    where airport_code ilike ? or airport_name ilike ? or city ilike ?
                    limit ?
                    """)) {
                var pgStmt = stmt.unwrap(PGStatement.class);
                pgStmt.setPrepareThreshold(1); // prepare on the server immediately
                stmt.setString(1, "%" + airportName + "%");
                stmt.setString(2, "%" + airportName + "%");
                stmt.setString(3, "%" + airportName + "%");
                stmt.setInt(4, limit);
                var rs = stmt.executeQuery();
                logWarnings(conn.getWarnings(), stmt.getWarnings(), rs.getWarnings());
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
            throw new RuntimeException(e);
        }
        return airports;
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

    public static void logWarnings(SQLWarning... warnings) {
        if (warnings != null) {
            for (var warning : warnings) {
                while (warning != null) {
                    logger.warning("Message: %s, SQLState: %s, Vendor error code: %d");
                    warning = warning.getNextWarning();
                }
            }
        }
    }
}
