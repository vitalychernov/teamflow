package com.teamflow.config;

import com.teamflow.security.CustomUserDetailsService;
import com.teamflow.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for JWT-based stateless auth.
 *
 * Key concepts:
 * - STATELESS session: no cookies, no server-side session, every request
 *   must carry a JWT token
 * - CSRF disabled: CSRF attacks rely on cookies/sessions — irrelevant for
 *   stateless JWT APIs
 * - CORS configured: allows the React frontend to call the API from a
 *   different origin (Vercel domain)
 *
 * Spring Boot 3.x / Security 6.x style:
 * - No more WebSecurityConfigurerAdapter (removed)
 * - SecurityFilterChain @Bean instead
 * - @EnableMethodSecurity instead of @EnableGlobalMethodSecurity
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS — allow frontend origin
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session — Spring Security never creates an HttpSession
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                    // Auth endpoints — public
                    .requestMatchers("/api/auth/**").permitAll()
                    // Swagger UI — public
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**"
                    ).permitAll()
                    // Admin endpoints — ADMIN role only
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // OPTIONS preflight requests — always permit (for CORS)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Everything else — must be authenticated
                    .anyRequest().authenticated()
            )

            // Wire up our AuthenticationProvider (uses UserDetailsService + BCrypt)
            .authenticationProvider(authenticationProvider())

            // Add JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationProvider: wires together UserDetailsService and PasswordEncoder.
     * Spring Security uses this to verify credentials during login.
     * DaoAuthenticationProvider = Database Authentication Object Provider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager: the entry point for authentication.
     * We inject this into AuthService to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder.
     * BCrypt automatically salts and hashes passwords.
     * Cost factor defaults to 10 — good balance of security vs speed.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS configuration.
     * Allows the React frontend (Vercel + localhost) to call the API.
     *
     * Production note: replace "*" with your actual Vercel domain:
     * e.g., "https://teamflow.vercel.app"
     * allowedOriginPatterns("*") is safe here (unlike allowedOrigins("*"))
     * because it works with allowCredentials(true).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
