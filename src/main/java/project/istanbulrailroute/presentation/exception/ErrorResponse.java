package project.istanbulrailroute.presentation.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class ErrorResponse {
    // Getter ve Setter Metotları
    private LocalDateTime timestamp;
    private int status;
    private List<String> errors; // Frontend'in döne döne okuyabileceği hata listesi

    public ErrorResponse(int status, List<String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.errors = errors;
    }

}
