package project.istanbulrailroute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"project.istanbulrailroute"})
@EnableJpaRepositories(basePackages = {"project.istanbulrailroute.infrastructure"})
@SpringBootApplication
public class IstanbulRailRouteApplication {

    public static void main(String[] args) {
        SpringApplication.run(IstanbulRailRouteApplication.class, args);
    }

}
