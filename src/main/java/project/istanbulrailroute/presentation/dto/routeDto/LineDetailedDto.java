package project.istanbulrailroute.presentation.dto.routeDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LineDetailedDto {
    private Long id;
    private String name;
    private String type;
    private List<String> stations;
}
