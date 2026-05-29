package project.istanbulrailroute.presentation.dto.paymentDto;

import lombok.Data;
import project.istanbulrailroute.presentation.dto.journeyDto.JourneySegmentDto;

import java.util.List;

@Data
public class PayJourneyRequest {
    private Long passengerId;
    private double totalFare;
}
