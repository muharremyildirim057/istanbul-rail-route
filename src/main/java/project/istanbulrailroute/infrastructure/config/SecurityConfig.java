package project.istanbulrailroute.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Şifreleri BCrypt algoritması ile hashlemek için kullanacağız
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Postman'den veya arayüzden POST/PUT istekleri atabilmen için CSRF korumasını kapatıyoruz
                .csrf(csrf -> csrf.disable())

                // Bütün isteklere şimdilik izin veriyoruz (login ekranı sormayacak)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // O ekranda gördüğün tarayıcının varsayılan popup'ını kapatıyoruz
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
