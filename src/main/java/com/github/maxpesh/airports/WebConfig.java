package com.github.maxpesh.airports;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.maxpesh.Language;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
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
                .GET("{lang}/airports/lookup/v1", WebConfig::supportLanguage, request -> {
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
                    Language lang = Language.valueOf(request.pathVariable("lang").toUpperCase());
                    String airport = request.param("airport").orElse("");
                    int limit = request.param("limit").map(Integer::parseInt).filter(v -> v >= 1 && v <= 10).orElse(5);
                    List<Airport> airports = repo.getAirportsLike(airport, limit, lang);
                    if (airports.isEmpty()) {
                        return ServerResponse.noContent().build();
                    }
                    return ServerResponse.ok()
                            .headers(this::cacheControl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(airports);
                })
                .POST("private/airports", request -> {
                    AirportData airport = request.body(AirportData.class);
                    if (airport.isMalformed()) {
                        return ServerResponse.badRequest().build();
                    }
                    String airportCode = repo.saveAirport(airport);
                    return ServerResponse.created(request.uriBuilder().path("/{airportCode}").build(airportCode))
                            .build();
                })
                .onError(Throwable.class, WebConfig::logStackTrace)
                .build();
    }

    @Bean
    MappingJackson2HttpMessageConverter httpJsonMsgConverter() {
        ObjectMapper objMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
        return new MappingJackson2HttpMessageConverter(objMapper);
    }

    private static boolean supportLanguage(ServerRequest request) {
        String lang = request.pathVariable("lang");
        return lang.equals("en") || lang.equals("ru");
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
