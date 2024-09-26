package studio.studioeye.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import studio.studioeye.domain.user.dao.UserRepository;
import studio.studioeye.global.security.filter.JwtAuthenticationProcessingFilter;
import studio.studioeye.global.security.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String[] WHITE_LIST = {
            "/swagger", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/api/v1/auth/**", "/api/v1/auth/password", "/api/v1/auth/reissue",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new AuthenticationEntryPointImpl()))

                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/auth/logout").authenticated()
//                                .requestMatchers(NEED_TOKEN).hasAnyRole("ADMIN", "MEMBER")
                                .requestMatchers(WHITE_LIST).permitAll()
                                .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationProcessingFilter(), LogoutFilter.class)
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter(){
        return new JwtAuthenticationProcessingFilter(jwtTokenProvider, userRepository);
    }
}
