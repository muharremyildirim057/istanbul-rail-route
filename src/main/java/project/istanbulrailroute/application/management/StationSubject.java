package project.istanbulrailroute.application.management;

import project.istanbulrailroute.domain.models.Station;

public interface StationSubject {
    void registerObserver(StationObserver observer);

    void removeObserver(StationObserver observer);

    void notifyObservers(Station station);
}
