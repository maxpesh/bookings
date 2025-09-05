fos.Router.setData({
    "base_url": "",
    "routes": {
        "dbr_mainpage_get_inspiration_prices_ajax": {
            "tokens": [
                ["text", "\/get-inspiration-prices"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_flights_search_query": {
            "tokens": [
                ["text", "\/flights\/select\/"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_new_flights_search_query": {
            "tokens": [
                ["text", "\/flights\/search\/"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_flights_search_nearest_airports_json": {
            "tokens": [
                ["text", "\/flights\/nearest-airports"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_deals_airline": {
            "tokens": [
                ["variable", "\/", "[^\/]++", "airlineSlug"],
                ["variable", "\/", "[0-9A-Za-z]+", "airlineCode"],
                ["text", "\/deals\/al"]
            ],
            "defaults": {
                "airlineSlug": null
            },
            "requirements": {
                "airlineCode": "[0-9A-Za-z]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_deals_list_specify": {
            "tokens": [
                ["variable", "\/", "[^\\\/]+", "slug"],
                ["variable", "\/", "[a-zA-Z0-9]+", "toCode"],
                ["variable", "\/", "(?:0|cs|co|ci|ap)", "toType"],
                ["variable", "\/", "[a-zA-Z0-9]+", "fromCode"],
                ["variable", "\/", "(?:0|cs|co|ci|ap)", "fromType"],
                ["text", "\/deals"]
            ],
            "defaults": {
                "slug": null
            },
            "requirements": {
                "fromType": "(0|cs|co|ci|ap)",
                "toType": "(0|cs|co|ci|ap)",
                "fromCode": "[a-zA-Z0-9]+",
                "toCode": "[a-zA-Z0-9]+",
                "slug": "[^\\\/]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_old_airline_national_redirection": {
            "tokens": [
                ["variable", "\/", "[A-Za-z]{2}", "countryCode"],
                ["text", "\/airlines\/national"]
            ],
            "defaults": [],
            "requirements": {
                "countryCode": "[A-Za-z]{2}"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_airline_national": {
            "tokens": [
                ["variable", "\/", "[0-9]+", "p"],
                ["variable", "\/", "[^\/]++", "countrySlug"],
                ["variable", "\/", "[A-Za-z]{2}", "countryCode"],
                ["text", "\/airlines\/national\/co"]
            ],
            "defaults": {
                "countrySlug": null,
                "p": 1
            },
            "requirements": {
                "countryCode": "[A-Za-z]{2}",
                "p": "[0-9]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_airline_airline": {
            "tokens": [
                ["variable", "\/", "[^\/]++", "airlineSlug"],
                ["variable", "\/", "[0-9A-Za-z-]{2,3}", "airlineCode"],
                ["variable", "\/", "AL|al", "_landingpage_type"],
                ["text", "\/airlines"]
            ],
            "defaults": {
                "airlineSlug": null,
                "_landingpage_type": "al"
            },
            "requirements": {
                "airlineCode": "[0-9A-Za-z-]{2,3}",
                "_landingpage_type": "AL|al",
                "p": "[0-9]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_airline_search_hints_ajax": {
            "tokens": [
                ["variable", "\/", "[0-9A-Za-z- ]+", "searchPhrase"],
                ["text", "\/airlines\/searchHints"]
            ],
            "defaults": [],
            "requirements": {
                "searchPhrase": "[0-9A-Za-z- ]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_airport_country_list_json": {
            "tokens": [
                ["text", "\/airports\/co.json"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_airport_country_json": {
            "tokens": [
                ["text", ".json"],
                ["variable", "\/", "[0-9A-Za-z-]+", "countryCode"],
                ["text", "\/airports\/co"]
            ],
            "defaults": [],
            "requirements": {
                "countryCode": "[0-9A-Za-z-]+",
                "countrySlug": "[0-9A-Za-z-]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_airport_airport": {
            "tokens": [
                ["variable", "\/", "[0-9A-Za-z-]+", "slug"],
                ["variable", "\/", "[0-9A-Za-z-]+", "code"],
                ["text", "\/airports\/ap"]
            ],
            "defaults": {
                "slug": null
            },
            "requirements": {
                "code": "[0-9A-Za-z-]+",
                "slug": "[0-9A-Za-z-]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_reviews_airline": {
            "tokens": [
                ["variable", "\/", "[0-9]+", "p"],
                ["variable", "\/", "[0-9A-Za-z-]+", "slug"],
                ["variable", "\/", "[0-9A-Za-z-]{2,3}", "code"],
                ["text", "\/reviews\/al"]
            ],
            "defaults": {
                "slug": null,
                "p": 1
            },
            "requirements": {
                "code": "[0-9A-Za-z-]{2,3}",
                "slug": "[0-9A-Za-z-]+",
                "p": "[0-9]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_reviews_airline_search_hints_ajax": {
            "tokens": [
                ["variable", "\/", "[0-9A-Za-z- ]+", "searchPhrase"],
                ["text", "\/reviews\/search-hints\/airlines"]
            ],
            "defaults": [],
            "requirements": {
                "searchPhrase": "[0-9A-Za-z- ]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_reviews_airport": {
            "tokens": [
                ["variable", "\/", "[0-9]+", "p"],
                ["variable", "\/", "[0-9A-Za-z-]+", "slug"],
                ["variable", "\/", "[0-9A-Za-z-]+", "code"],
                ["text", "\/reviews\/ap"]
            ],
            "defaults": {
                "slug": null,
                "p": 1
            },
            "requirements": {
                "code": "[0-9A-Za-z-]+",
                "slug": "[0-9A-Za-z-]+",
                "p": "[0-9]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_landing_pages_reviews_general": {
            "tokens": [
                ["variable", "\/", "[0-9]+", "p"],
                ["text", "\/reviews"]
            ],
            "defaults": {
                "p": 1
            },
            "requirements": {
                "p": "[0-9]+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_rest_v1_0_flight_stat_arrival": {
            "tokens": [
                ["text", "\/flightstat\/arrival"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_rest_v1_0_flight_stat_departure": {
            "tokens": [
                ["text", "\/flightstat\/departure"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_rest_v1_0_flight_stat_arrival_from_cache": {
            "tokens": [
                ["text", "\/flightstat\/arrival_from_cache"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_rest_v1_0_flight_stat_departure_from_cache": {
            "tokens": [
                ["text", "\/flightstat\/departure_from_cache"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "esky_hotels_landings_new": {
            "tokens": [
                ["text", "\/hot\/details"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "esky_hotels_search_results_new": {
            "tokens": [
                ["text", "\/hot\/search"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "esky_hotels_langing_page_new": {
            "tokens": [
                ["variable", "\/", ".+", "pathToNewLanding"],
                ["text", "\/hot"]
            ],
            "defaults": [],
            "requirements": {
                "pathToNewLanding": ".+"
            },
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_guide_search_hints": {
            "tokens": [
                ["text", "\/travel-guide-search-hints"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        },
        "dbr_guide_search": {
            "tokens": [
                ["text", "\/travel-guide-search"]
            ],
            "defaults": [],
            "requirements": [],
            "hosttokens": [],
            "methods": [],
            "schemes": []
        }
    },
    "prefix": "",
    "host": "",
    "port": "",
    "scheme": null,
    "locale": null
});