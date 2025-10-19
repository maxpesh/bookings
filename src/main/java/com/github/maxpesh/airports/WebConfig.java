package com.github.maxpesh.airports;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.maxpesh.Language;
import jakarta.servlet.ServletException;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
class WebConfig {
    private static final Logger logger = Logger.getLogger(WebConfig.class.getName());

    @Bean
    RouterFunction<ServerResponse> router() {
        Repository repo = new Repository();

        return RouterFunctions.route()
                .GET("{lang}/airports/lookup/v1", WebConfig::supportLanguage, new LookupAirportHandler(repo)::handle)
                .POST("private/airports/v1", new CreateAirportHandler(repo, new AirportValidator())::handle)
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

    private static ServerResponse logStackTrace(Throwable throwable, ServerRequest request) {
        logger.log(Level.SEVERE, throwable, throwable::getMessage);
        return ServerResponse.
                status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    private static class LookupAirportHandler {
        private final Repository repo;
        private Instant lastModified = Instant.now();
        private String eTag = "deadbeef";

        public LookupAirportHandler(Repository repo) {
            this.repo = repo;
        }

        ServerResponse handle(ServerRequest request) {
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
            if (request.param("airport").isEmpty()) {
                return ServerResponse
                        .badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("Errors", List.of("Query parameter filter cannot be null or empty.")));
            }
            Language lang = Language.valueOf(request.pathVariable("lang").toUpperCase());
            String airport = request.param("airport").get();
            int limit = request.param("limit")
                    .filter(NumberUtils::isCreatable)
                    .map(Integer::parseInt)
                    .filter(v -> v >= 1 && v <= 20)
                    .orElse(20);
            List<Airport> airports = repo.getAirportsLike(airport, limit, lang);
            if (airports.isEmpty()) {
                return ServerResponse.noContent().build();
            }
            return ServerResponse.ok()
                    .headers(this::cacheControl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(airports);
        }

        private void cacheControl(HttpHeaders headers) {
            headers.setLastModified(lastModified);
            headers.setETag(eTag);
            headers.setCacheControl(CacheControl
                    .maxAge(Duration.ofHours(1)));
        }
    }

    private static class CreateAirportHandler {
        private final Repository repo;
        private final Validator validator;

        public CreateAirportHandler(Repository repo, AirportValidator validator) {
            this.repo = repo;
            this.validator = validator;
        }

        ServerResponse handle(ServerRequest request) throws ServletException, IOException {
            AirportData airport = request.body(AirportData.class);
            Errors errors = new BeanPropertyBindingResult(airport, "airport");
            validator.validate(airport, errors);
            if (errors.hasErrors()) {
                return ServerResponse.badRequest().body(errors.getAllErrors());
            }
            AirportData savedAirport = repo.saveAirport(airport);
            return ServerResponse.created(request.uriBuilder().path("/{airportCode}").build(savedAirport.code()))
                    .body(savedAirport);
        }
    }

    private static class AirportValidator implements Validator {
        @Override
        public boolean supports(Class<?> clazz) {
            return AirportData.class.equals(clazz);
        }

        @Override
        public void validate(Object obj, Errors e) {
            ValidationUtils.rejectIfEmpty(e, "code", "airport_code.required", "airport_code cannot be empty");
            ValidationUtils.rejectIfEmpty(e, "langToName", "airport_name.required", "airport_name cannot be empty");
            ValidationUtils.rejectIfEmpty(e, "langToCity", "city.required", "city cannot be empty");
            ValidationUtils.rejectIfEmpty(e, "coordinates", "coordinates.required", "coordinates cannot be empty");
            ValidationUtils.rejectIfEmpty(e, "timezone", "timezone.required", "timezone cannot be empty");
        }
    }
}
