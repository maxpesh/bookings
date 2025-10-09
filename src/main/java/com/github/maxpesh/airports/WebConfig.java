package com.github.maxpesh.airports;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
                .GET("{lang}/airports/lookup", WebConfig::supportLanguage, request -> {
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
                    if (!parametersAreValid(request)) {
                        return ServerResponse.badRequest().build();
                    }
                    String lang = request.pathVariable("lang");
                    String airport = request.param("airport").orElse("");
                    int limit = Integer.parseInt(request.param("limit").orElse("5"));
                    List<Airport> airports = repo.getAirportsLike(airport, limit, lang);
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

    private static boolean supportLanguage(ServerRequest request) {
        String lang = request.pathVariable("lang");
        return lang.equals("en") || lang.equals("ru");
    }

    private static boolean parametersAreValid(ServerRequest request) {
        int limit = Integer.parseInt(request.param("limit").orElse("3"));
        return limit >= 1 && limit <= 25;
    }

    private void cacheControl(HttpHeaders headers) {
        headers.setLastModified(lastModified);
        headers.setETag(eTag);
        headers.setCacheControl(CacheControl
                .maxAge(Duration.ofHours(1)));
    }

    private static ServerResponse logStackTrace(Throwable throwable, ServerRequest request) {
        logger.log(Level.SEVERE, throwable, throwable::getMessage);
        return ServerResponse.
                status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
