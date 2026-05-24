package project.istanbulrailroute.presentation.dto.routeDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LineAdminResponseDto {
    private Long id;
    private String name;
    private String type;
}
