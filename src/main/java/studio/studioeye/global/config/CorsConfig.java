package studio.studioeye.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
//			.allowedOrigins("*")
			.allowedOriginPatterns("*") // 쿠키를 받기 위해 기존코드(allowedOrigin) -> 새로운 코드(allowedOriginPatterns)
			.allowedMethods("*")
			.allowedHeaders("*")
			.allowCredentials(true) // 자격 증명 허용
			.maxAge(3600);
	}
}