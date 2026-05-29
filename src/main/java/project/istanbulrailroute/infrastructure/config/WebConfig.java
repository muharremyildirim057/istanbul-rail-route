package project.istanbulrailroute.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.istanbulrailroute.infrastructure.security.AdminAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Filtrenin HANGİ adreslerde devreye gireceğini belirliyoruz
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**") // Admin controller altındaki HER ŞEY
                .addPathPatterns("/api/stations/connections/**") // Bağlantı silme/ekleme
                // Eğer istersen GET (okuma) işlemlerini hariç tutup sadece POST/PUT/DELETE'leri koruyabilirsin.
                // Şimdilik sadece Admin uç noktalarını ve bağlantıları korumaya aldık.
                .excludePathPatterns("/api/auth/**"); // Auth işlemlerine asla filtre koyma
    }
}