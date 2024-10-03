package studio.studioeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudioEyeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudioEyeApplication.class, args);
    }

}
