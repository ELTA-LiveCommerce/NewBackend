package kr.elta.backend.configuration;
import kr.elta.backend.configuration.JwtAuthenticationFilter;
import kr.elta.backend.configuration.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@Slf4j
public class SpringSecurity {
    private final JwtUtil jwtUtil;

    public SpringSecurity(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // AuthenticationManager 빈 노출
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 시큐리티 필터 체인
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager
    ) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtUtil);

        http
                .csrf(cs -> cs.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // 인증 없이 접근 허용할 경로들
                                .requestMatchers(
                                        "/auth/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                // seller 경로는 SELLER 또는 ADMIN 권한 필요
                                .requestMatchers("/seller/**")
                                .hasAnyAuthority("SELLER", "ADMIN")
                                .requestMatchers("/admin/**")
                                .hasAnyAuthority("ADMIN")
                                // 나머지는 모두 인증 필요
                                .anyRequest().authenticated()
                )
                .authenticationManager(authManager)
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}