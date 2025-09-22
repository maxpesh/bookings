package com.github.maxpesh.airports;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
class WebConfig {
    @Bean
    RouterFunction<ServerResponse> router(Repository repo) {
        return RouterFunctions.route()
                .path("/airports", builder -> builder
                        .GET("/lookup", request -> {
                            if (requestIsMalformed(request)) {
                                return ServerResponse
                                        .badRequest()
                                        .body("Malformed request syntax. Expects: airports/lookup?airport=<string>&matches=<positive int>");
                            }
                            var airport = request.param("airport").get();
                            var matches = Integer.parseInt(request.param("matches").get());
                            return ServerResponse
                                    .ok()
                                    .body(repo.getAirportsLike(airport, matches));
                        })
                )
                .build();
    }

    private boolean requestIsMalformed(ServerRequest request) {
        var airportOpt = request.param("airport");
        var matchesOpt = request.param("matches");

        try {
            if (airportOpt.isEmpty() || matchesOpt.isEmpty() || Integer.parseInt(matchesOpt.get()) < 1) {
                return true;
            }
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }
}
