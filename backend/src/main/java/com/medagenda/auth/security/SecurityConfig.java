package com.medagenda.auth.security;

import com.medagenda.auth.TokenService;
import jakarta.servlet.DispatcherType;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.DelegatingAccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Three filter chains, one per kind of client (the first one whose matcher fits handles the request):
 * <ol>
 *   <li>{@code /api/**}: stateless, Bearer JWT, no CSRF. Used by the Angular app.</li>
 *   <li>{@code /admin/**}: session cookie + form login, CSRF on. The legacy JSP module for reception.</li>
 *   <li>everything else: closed, except Swagger and the error page.</li>
 * </ol>
 * The chain is the outer gate (public vs. authenticated). Role and ownership rules sit next
 * to each endpoint with {@code @PreAuthorize}, so they are visible where the code is.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] API_DOCS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, SecurityProblemHandler problemHandler,
                                               CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.securityMatcher("/api/**")
                // CSRF abuses cookies the browser sends automatically. The API is authenticated by a
                // header the frontend adds explicitly, so there is nothing for CSRF to exploit.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        // Needed by the public sign-up form
                        .requestMatchers(HttpMethod.GET, "/api/specialties/**", "/api/health-insurances/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler));
        return http.build();
    }

    /**
     * The JSP module is a classic server-rendered app, so it authenticates the classic way:
     * a login form creates an HTTP session and the browser sends the session cookie.
     * Because the browser attaches that cookie to every request on its own, CSRF protection
     * must stay ON here: each form carries a token that another site cannot know.
     */
    @Bean
    @Order(2)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {
        // Remembers the page that sent the user to the login form (in the session) so they return to it.
        // Since Spring Security 6.1 that redirect carries a "?continue" marker; we don't need it.
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        http.securityMatcher("/admin/**")
                .authenticationManager(authenticationManager)
                .requestCache(cache -> cache.requestCache(requestCache))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/acesso-negado", "/admin/assets/**").permitAll()
                        .anyRequest().hasAnyRole("RECEPTIONIST", "ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        // Without "true", a user sent to login from /admin/consultas returns there afterwards
                        .defaultSuccessUrl("/admin")
                        .failureUrl("/admin/login?erro"))
                // POST only (CSRF-protected): a GET link or image tag cannot log the user out
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?saiu")
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(errors -> errors.accessDeniedHandler(adminAccessDeniedHandler()));
        return http.build();
    }

    /**
     * Both cases answer 403 with a friendly page instead of a bare error:
     * a wrong role (patient or doctor logged in) and a failed CSRF check (usually an expired session).
     */
    private static AccessDeniedHandler adminAccessDeniedHandler() {
        AccessDeniedHandlerImpl wrongRole = new AccessDeniedHandlerImpl();
        wrongRole.setErrorPage("/admin/acesso-negado");
        AccessDeniedHandlerImpl invalidForm = new AccessDeniedHandlerImpl();
        invalidForm.setErrorPage("/admin/acesso-negado?motivo=csrf");

        LinkedHashMap<Class<? extends AccessDeniedException>, AccessDeniedHandler> byType = new LinkedHashMap<>();
        byType.put(CsrfException.class, invalidForm);
        return new DelegatingAccessDeniedHandler(byType, wrongRole);
    }

    @Bean
    @Order(3)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // CSRF protects endpoints that change data for a logged-in browser session. This chain
        // authenticates nobody and denies every direct request, so there is nothing to protect.
        // Leaving it on would also re-check the token on the internal forward to a JSP after a
        // POST was already refused by the admin chain, hiding that chain's friendly 403 page.
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                // Spring Security also authorizes server-side dispatches. When a controller renders a
                // JSP, MVC forwards to /WEB-INF/jsp/...; that internal hop must not hit "denyAll".
                // The original browser request was already authorized by its own chain, and
                // WEB-INF paths cannot be requested directly (the container returns 404).
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(API_DOCS).permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().denyAll());
        return http.build();
    }

    /** Maps the "role" claim (e.g. "DOCTOR") to the authority "ROLE_DOCTOR" used by hasRole(). */
    static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(TokenService.ROLE_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /** Checks e-mail + password on login. Unknown e-mails also yield BadCredentials (no user enumeration). */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
