package project.istanbulrailroute.presentation.dto.authDto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
