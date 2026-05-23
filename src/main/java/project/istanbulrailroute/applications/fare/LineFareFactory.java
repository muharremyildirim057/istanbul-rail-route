package project.istanbulrailroute.applications.fare;

import org.springframework.stereotype.Component;
import project.istanbulrailroute.applications.fare.impl.DistanceBasedFareStrategy;
import project.istanbulrailroute.applications.fare.impl.StandardFareStrategy;
import project.istanbulrailroute.domain.models.enums.LineType;

@Component
public class LineFareFactory {
    public FareStrategy getFareStrategy(LineType type){
            switch (type) {
                case BANLIYO:
                    // Eğer hat Marmaray ise mesafeye dayalı stratejiyi ver
                    return new DistanceBasedFareStrategy();
                case METRO:
                case TRAMVAY:
                case NOSTALJIK_TRAMVAY:
                case METROBUS:
                default:
                    // Diğer tüm hatlarda standart stratejiyi ver
                    return new StandardFareStrategy();
            }
    }
}
