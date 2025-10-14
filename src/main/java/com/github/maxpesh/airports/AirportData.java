package com.github.maxpesh.airports;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.maxpesh.Language;

import java.time.ZoneId;
import java.util.HashMap;

record AirportData(@JsonAlias("airport_code") String code,
                   @JsonAlias("airport_name") HashMap<Language, String> langToName,
                   @JsonAlias("city") HashMap<Language, String> langToCity,
                   Point coordinates, ZoneId timezone) {
    boolean isMalformed() {
        return langToName == null ||
                langToCity == null ||
                coordinates == null ||
                timezone == null ||
                langToName.size() < Language.values().length ||
                langToCity.size() < Language.values().length;
    }
}

// View of the AirportData returned to the user
record Airport(String code, String name, String city, Point coordinates, ZoneId timezone) {
}

record Point(double x, double y) {
}