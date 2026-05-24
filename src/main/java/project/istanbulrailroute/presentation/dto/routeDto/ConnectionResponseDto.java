package project.istanbulrailroute.presentation.dto.routeDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionResponseDto {
    private Long id;
    private String startStationName;
    private String targetStationName;
    private String lineName;
    private Double duration;
}
