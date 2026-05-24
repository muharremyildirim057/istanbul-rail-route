package project.istanbulrailroute.application.management;

import project.istanbulrailroute.domain.models.Station;

public interface StationObserver {
    void onStationStatusChanged(Station station);
}
