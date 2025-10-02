package com.github.maxpesh.airports;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Configuration
class WebConfig {
    private Instant lastModified = Instant.now();
    private String eTag = "deadbeef";

    @Bean
    RouterFunction<ServerResponse> router(Repository repo) {
        return RouterFunctions.route()
                .filter(this::ifNoneMatch)
                .filter(this::ifNotModifiedSince)
                .GET("/airports/lookup", request -> {
                    if (requestIsMalformed(request)) {
                        return ServerResponse
                                .badRequest()
                                .headers(this::setCommonHeaders)
                                .body("Malformed request syntax. " +
                                        "Expects: airports/lookup?airport=<string>&matches=<positive int>");
                    }
                    String airport = request.param("airport").get();
                    int matches = Integer.parseInt(request.param("matches").get());
                    return ServerResponse.ok()
                            .headers(this::setCommonHeaders)
                            .body(repo.getAirportsLike(airport, matches));
                })
                .build();
    }

    private ServerResponse ifNoneMatch(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        List<String> values = request.headers().header(HttpHeaders.IF_NONE_MATCH);
        if (values.isEmpty()) {
            return next.handle(request);
        }
        String eTag = values.get(0);
        if (eTag.equals(this.eTag)) {
            return ServerResponse.status(HttpStatus.NOT_MODIFIED)
                    .headers(this::setCommonHeaders)
                    .build();
        } else {
            return next.handle(request);
        }
    }

    private ServerResponse ifNotModifiedSince(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        List<String> values = request.headers().header(HttpHeaders.IF_MODIFIED_SINCE);
        if (values.isEmpty()) {
            return next.handle(request);
        }
        String ifModifiedSince = values.get(0);
        if (Instant.parse(ifModifiedSince).equals(lastModified)) {
            return ServerResponse.status(HttpStatus.NOT_MODIFIED)
                    .headers(this::setCommonHeaders)
                    .build();
        } else {
            return next.handle(request);
        }
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

    private void setCommonHeaders(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setLastModified(lastModified);
        headers.setETag(eTag);
    }
}
