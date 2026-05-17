package project.istanbulrailroute.presentation.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Kullanıcı Adı Boş Bırakılamaz.")
    @Size(min = 3, max = 20 , message = "Kullanıcı adı 3 ile 20 karakter arasında olmalıdır.")
    private String username;

    @NotBlank(message = "Şifre boş bırakılamaz.")
    @Size(min = 6, max = 20, message = "Şifre 6 ile 20 karakter arasında olmalıdır.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "Şifre en az 1 sayı ve 1 özel karakter içermelidir.")
    private String password;

    @NotBlank(message = "Ad alanı boş bırakılamaz.")
    @Size(min = 3, max = 20, message = "Ad 3 ile 20 karakter arasında olmalıdır.")
    private String firstName;

    @NotBlank(message = "Soyad alanı boş bırakılamaz.")
    @Size(min = 3, max = 20, message = "Soyad 3 ile 20 karakter arasında olmalıdır.")
    private String lastName;
}
