package com.example.demo.config;

import com.example.demo.repository.StudentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter; 
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority; 
import org.springframework.security.core.authority.SimpleGrantedAuthority; 
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter; 
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter; 
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.Collection; 
import java.util.Collections; 
import java.util.List; 
import java.util.stream.Collectors; 
import java.util.stream.Stream; 

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize
public class SecurityConfig {

    // Inject JWKS URI from application.properties
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
                    "/api/v1/test/login",
                    
                    // <<< SỬA LẠI CÁC DÒNG NÀY >>>
                    "/api/v1/event-categories",
                    "/api/v1/event-categories/*",
                    "/api/v1/products", 
                    "/api/v1/products/*"
                ).permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/v1/universities").permitAll()
                .anyRequest().authenticated() // Bắt buộc xác thực cho TẤT CẢ các request còn lại
            )
            // === CẬP NHẬT Ở ĐÂY ===
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // Sử dụng custom converter để đọc role từ Cognito groups
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            // ======================
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // --- CORS Configuration Bean --- (Giữ nguyên)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList( "*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
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