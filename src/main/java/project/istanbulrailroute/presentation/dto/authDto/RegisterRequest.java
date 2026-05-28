package project.istanbulrailroute.presentation.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 3, max = 20 , message = "Username must be between 3 and 20 characters.")
    private String username;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "Password must contain at least 1 number and 1 special character.")
    private String password;

    @NotBlank(message = "First name cannot be empty.")
    @Size(min = 3, max = 20, message = "First name must be between 3 and 20 characters.")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty.")
    @Size(min = 3, max = 20, message = "Last name must be between 3 and 20 characters.")
    private String lastName;
}