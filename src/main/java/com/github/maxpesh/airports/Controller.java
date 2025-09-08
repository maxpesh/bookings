package com.github.maxpesh.airports;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/airports")
class Controller {
    private final Repository repo;

    Controller(Repository repo) {
        this.repo = repo;
    }

    @GetMapping("/lookup")
    List<Airport> airportByPattern(@RequestParam("airport") String airport, @RequestParam("matches") int matches) {
        return repo.getAirportsLike(airport, matches);
    }
}
