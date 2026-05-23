package project.istanbulrailroute.applications.fare.impl;

import project.istanbulrailroute.applications.fare.FareStrategy;

public class StandardFareStrategy implements FareStrategy {
    private static final double BASE_FARE = 17.70;

    @Override
    public double calculateFare(int stationCount) {
        return BASE_FARE;
    }
}
