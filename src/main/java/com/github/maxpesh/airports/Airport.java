package com.github.maxpesh.airports;

import java.time.ZoneId;

record Airport(String code, String name, String city, Point coordinates, ZoneId timezone) {
}

record Point(double x, double y) {
}
