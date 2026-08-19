package com.carfo.contentieux.config;

import com.carfo.contentieux.model.Role;
import com.carfo.contentieux.repository.RevokedTokenRepository;
import com.carfo.contentieux.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        // /actuator/health reste ouvert (sondes de disponibilite des orchestrateurs).
                        // Le reste de l'actuator (metriques Prometheus, env, etc.) ne doit pas etre
                        // lisible par n'importe quel compte authentifie : meme restriction que les
                        // statistiques, reservee a Chef de service et Direction Generale.
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasAnyRole(Role.chef_service.name(), Role.direction_generale.name())
                        // Generer/exporter des statistiques : reserve a Chef de service et Direction Generale
                        // (cf. diagramme de cas d'utilisation - le Juriste n'a pas ce cas d'utilisation)
                        .requestMatchers(HttpMethod.GET, "/api/statistiques/**").hasAnyRole(Role.chef_service.name(), Role.direction_generale.name())
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        // Assigner un juriste a un dossier : reserve a Chef de service (cf. diagramme de cas d'utilisation)
                        .requestMatchers(HttpMethod.POST, "/api/dossiers-juristes/**").hasRole(Role.chef_service.name())
                        .requestMatchers(HttpMethod.PUT, "/api/dossiers-juristes/**").hasRole(Role.chef_service.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/dossiers-juristes/**").hasRole(Role.chef_service.name())
                        // Gerer les comptes utilisateurs (creation/modification/suppression) : reserve
                        // a Chef de service, qui supervise l'equipe (le Juriste ne doit pas pouvoir
                        // creer ou modifier des comptes, y compris le sien).
                        .requestMatchers(HttpMethod.POST, "/api/utilisateurs/**").hasRole(Role.chef_service.name())
                        .requestMatchers(HttpMethod.PUT, "/api/utilisateurs/**").hasRole(Role.chef_service.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/utilisateurs/**").hasRole(Role.chef_service.name())
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole(Role.juriste.name())
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole(Role.juriste.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(Role.juriste.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole(Role.juriste.name())
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, RevokedTokenRepository revokedTokenRepository) {
        return new JwtAuthenticationFilter(jwtService, revokedTokenRepository);
    }

    public static class JwtAuthenticationFilter extends OncePerRequestFilter {
        public static final String CLAIMS_REQUEST_ATTRIBUTE = "jwtClaims";

        private final JwtService jwtService;
        private final RevokedTokenRepository revokedTokenRepository;

        public JwtAuthenticationFilter(JwtService jwtService, RevokedTokenRepository revokedTokenRepository) {
            this.jwtService = jwtService;
            this.revokedTokenRepository = revokedTokenRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String path = request.getRequestURI();
            if ("/api/auth/login".equals(path) || "/api/auth/login/".equals(path)
                    || path.equals("/actuator/health") || path.startsWith("/actuator/health/")) {
                filterChain.doFilter(request, response);
                return;
            }

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token manquant");
                return;
            }

            JwtService.JwtClaims claims = jwtService.validateToken(authorization.substring(7));
            if (claims == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalide");
                return;
            }

            if (revokedTokenRepository.existsByJti(claims.getJti())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoque (deconnexion effectuee)");
                return;
            }

            request.setAttribute(CLAIMS_REQUEST_ATTRIBUTE, claims);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getIdentifiant(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + claims.getRole())));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }
    }
}
