package project.istanbulrailroute.presentation.dto.routeDto;

import lombok.Getter;
import lombok.Setter;
import project.istanbulrailroute.domain.models.enums.StationStatus;

@Getter
@Setter
public class StationResponse {
    private Long id;
    private String name;
    private StationStatus status;

    public StationResponse(Long id, String name, StationStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

}
