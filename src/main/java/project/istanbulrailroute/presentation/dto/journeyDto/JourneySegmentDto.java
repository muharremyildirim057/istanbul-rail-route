package project.istanbulrailroute.presentation.dto.journeyDto;

import lombok.Data;
import lombok.Data;

@Data
public class JourneySegmentDto {
    private Long lineId;
    private String lineName;
    private int stationCount;
    private double segmentFare;
    private double segmentDuration;
}
