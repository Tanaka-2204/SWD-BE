package com.example.demo.config; // Đảm bảo đúng package

import org.springframework.beans.factory.annotation.Value;
import com.example.demo.repository.StudentRepository; // <<< THÊM
import com.example.demo.entity.Student; // <<< THÊM
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter; // Thêm import
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority; // Thêm import
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Thêm import
import org.springframework.security.oauth2.jwt.Jwt; // Thêm import
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter; // Thêm import
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter; // Thêm import
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection; // Thêm import
import java.util.Collections; // Thêm import
import java.util.List; // Thêm import
import java.util.stream.Collectors; // Thêm import
import java.util.stream.Stream; // Thêm import

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize
public class SecurityConfig {

    // Inject JWKS URI from application.properties
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
    private final StudentRepository studentRepository;
    private final CognitoGroupsConverter cognitoGroupsConverter;

    public SecurityConfig(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
        this.cognitoGroupsConverter = new CognitoGroupsConverter();
    }
    
    // --- Security Filter Chain Configuration ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/api/v1/test/login"
                ).permitAll()
                .anyRequest().authenticated()
            )
            // === CẬP NHẬT Ở ĐÂY ===
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    // Sử dụng custom converter để đọc role từ Cognito groups
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            // ======================
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // --- JWT Decoder Bean --- (Giữ nguyên)
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    // --- CORS Configuration Bean --- (Giữ nguyên)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000", "*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // =======================================================
    // == THÊM BEAN VÀ LỚP HELPER CHO VIỆC ĐỌC ROLE TỪ JWT ==
    // =======================================================
    /**
     * Creates a custom JwtAuthenticationConverter to extract roles/authorities
     * from the 'cognito:groups' claim in the Cognito JWT.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CognitoGroupsConverter());
        return converter;
    }

    /**
     * Helper class to convert Cognito groups claim into Spring Security GrantedAuthority objects.
     */
    static class CognitoGroupsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        private static final String COGNITO_GROUPS_CLAIM = "cognito:groups";
        private static final String ROLE_PREFIX = "ROLE_";

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);
            List<String> groups = jwt.getClaimAsStringList(COGNITO_GROUPS_CLAIM);

            if (groups == null || groups.isEmpty()) {
                return defaultAuthorities != null ? defaultAuthorities : Collections.emptyList();
            }

            List<GrantedAuthority> groupAuthorities = groups.stream()
                    .map(groupName -> new SimpleGrantedAuthority(ROLE_PREFIX + groupName.toUpperCase()))
                    .collect(Collectors.toList());

            if (defaultAuthorities != null) {
                return Stream.concat(defaultAuthorities.stream(), groupAuthorities.stream())
                             .collect(Collectors.toSet());
            } else {
                return groupAuthorities;
            }
        }
    }
    // =======================================================
}