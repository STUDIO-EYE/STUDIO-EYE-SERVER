package studio.studioeye.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import studio.studioeye.domain.user.dao.UserRepository;
import studio.studioeye.domain.user.domain.User;
import studio.studioeye.global.security.UserDetailsImpl;
import studio.studioeye.global.security.jwt.JwtTokenProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// JWT Token을 이용한 인증 필터
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtTokenProvider.extractAccessToken(request)
                .filter(jwtTokenProvider::isTokenValid)
                .ifPresent(accessToken -> jwtTokenProvider.extractUserId(accessToken)
                        .ifPresent(userId -> userRepository.findById(userId)
                                .ifPresent(user -> saveAuthentication(user)
                                )
                        )
                );
        filterChain.doFilter(request, response);
    }

    private void saveAuthentication(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .authorities(authorities)
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();//5
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}