package project.istanbulrailroute.presentation.dto.routeDto;


import lombok.Data;

@Data
public class ConnectionRequestDto {
    private Long startStationId;
    private Long targetStationId;
    private Long lineId;
    private Double duration;
}