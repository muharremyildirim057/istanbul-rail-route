package project.istanbulrailroute.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import project.istanbulrailroute.domain.models.Passenger;
import project.istanbulrailroute.infrastructure.UserRepository;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public AdminAuthInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Tarayıcıların gönderdiği ön kontrol (CORS) isteklerini atla
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 1. Frontend'den gelen istekte "X-Username" başlığı var mı?
        String username = request.getHeader("X-Username");
        if (username == null || username.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Kimlik bilgisi bulunamadi.");
            return false; // İsteği Controller'a gitmeden kes!
        }

        // 2. Veritabanından kullanıcıyı bul
        Passenger user = userRepository.findByUsername(username).orElse(null);

        // 3. Kullanıcı var mı ve Rolü ADMIN mi?
        if (user == null || !"ADMIN".equals(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: Bu islem icin Admin yetkiniz yok.");
            return false; // İsteği kes!
        }

        // Her şey yolundaysa Controller'a (örneğin StationAdminController) geçmesine izin ver
        return true;
    }
}