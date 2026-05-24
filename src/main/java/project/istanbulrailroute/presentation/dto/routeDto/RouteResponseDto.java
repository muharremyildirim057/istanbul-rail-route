package project.istanbulrailroute.presentation.dto.routeDto;

import lombok.Data;
import project.istanbulrailroute.presentation.dto.journeyDto.JourneySegmentDto;

import java.util.List;

@Data
public class RouteResponseDto {
    private List<StationResponse> stations;
    private List<JourneySegmentDto> segments;
    private double totalFare;
    private double totalDuration;
}
