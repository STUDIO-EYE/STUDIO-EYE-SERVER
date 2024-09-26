package studio.studioeye.infrastructure.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
	info = @Info(title = "Studio_I",
		description = "Studio_I 프로모션 페이지 Api 명세서",
		version = "v1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {
	@Bean
	public GroupedOpenApi chatOpenApi() {
		String[] paths = {"/api/**"};

		SecurityScheme cookieAuthScheme = new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.COOKIE)
			.name("session_id");

		return GroupedOpenApi.builder()
			.group("Studio_I Promotion Page API v1")
			.pathsToMatch(paths)
			.build();
	}
}
