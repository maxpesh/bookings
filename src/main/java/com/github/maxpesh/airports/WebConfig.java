package com.github.maxpesh.airports;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.servlet.function.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
class WebConfig {
    private static final Logger logger = Logger.getLogger(WebConfig.class.getName());
    private Instant lastModified = Instant.now();
    private String eTag = "deadbeef";

    @Bean
    RouterFunction<ServerResponse> router(Repository repo) {
        return RouterFunctions.route()
                .GET("{lang}/airports/lookup", WebConfig::ifLangIsSupported, request -> {
                    if (!request.headers().header(HttpHeaders.IF_NONE_MATCH).isEmpty()) {
                        String reqETag = request.headers().header(HttpHeaders.IF_NONE_MATCH).get(0);
                        if (reqETag.equals(eTag)) {
                            return ServerResponse.status(HttpStatus.NOT_MODIFIED)
                                    .headers(this::cacheControl)
                                    .build();
                        }
                    }
                    if (!request.headers().header(HttpHeaders.IF_MODIFIED_SINCE).isEmpty()) {
                        Instant reqLastModified = Instant.parse(request.headers().header(HttpHeaders.IF_MODIFIED_SINCE).get(0));
                        if (reqLastModified.equals(lastModified)) {
                            return ServerResponse.status(HttpStatus.NOT_MODIFIED)
                                    .headers(this::cacheControl)
                                    .build();
                        }
                    }
                    if (requestIsMalformed(request)) {
                        return ServerResponse
                                .badRequest()
                                .body("Malformed request syntax. " +
                                        "Expects: airports/lookup?airport=<string>&matches=<positive int>");
                    }
                    String lang = request.pathVariable("lang");
                    String airport = request.param("airport").get();
                    int matches = Integer.parseInt(request.param("matches").get());
                    List<Airport> airports = repo.getAirportsLike(airport, matches, lang);
                    if (airports.isEmpty()) {
                        return ServerResponse.noContent().build();
                    }
                    return ServerResponse.ok()
                            .headers(this::cacheControl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(airports);
                })
                .onError(Throwable.class, WebConfig::logStackTrace)
                .build();
    }

    private static boolean ifLangIsSupported(ServerRequest request) {
        String lang = request.pathVariable("lang");
        return lang.equals("en") || lang.equals("ru");
    }

    private static boolean requestIsMalformed(ServerRequest request) {
        Optional<String> airportOpt = request.param("airport");
        Optional<String> matchesOpt = request.param("matches");
        try {
            if (airportOpt.isEmpty() || matchesOpt.isEmpty() || Integer.parseInt(matchesOpt.get()) < 1) {
                return true;
            }
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }

    private void cacheControl(HttpHeaders headers) {
        headers.setLastModified(lastModified);
        headers.setETag(eTag);
        headers.setCacheControl(CacheControl
                .maxAge(Duration.ofHours(1)));
    }

    private static ServerResponse methodNotAllowed(ServerRequest request) {
        return ServerResponse.status(HttpStatus.METHOD_NOT_ALLOWED)
                .allow(HttpMethod.GET)
                .build();
    }

    private static ServerResponse logStackTrace(Throwable throwable, ServerRequest request) {
        logger.log(Level.SEVERE, throwable, throwable::getMessage);
        return ServerResponse.
                status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
