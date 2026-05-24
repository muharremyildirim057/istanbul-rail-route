package project.istanbulrailroute.application.fare.impl;

import project.istanbulrailroute.application.fare.FareStrategy;

public class DistanceBasedFareStrategy implements FareStrategy {
    private static final double BASE_FARE = 17.70;
    private static final double PER_STATION_FEE = 1.50; // 3 duraktan sonra durak başı ekstra ücret

    @Override
    public double calculateFare(int stationCount) {
        if (stationCount <= 3) {
            return BASE_FARE;
        }
        return BASE_FARE + ((stationCount - 3) * PER_STATION_FEE);
    }
}
