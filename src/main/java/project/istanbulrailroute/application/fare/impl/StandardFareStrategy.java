package project.istanbulrailroute.application.fare.impl;

import project.istanbulrailroute.application.fare.FareStrategy;

public class StandardFareStrategy implements FareStrategy {
    private static final double BASE_FARE = 17.70;

    @Override
    public double calculateFare(int stationCount) {
        return BASE_FARE;
    }
}
