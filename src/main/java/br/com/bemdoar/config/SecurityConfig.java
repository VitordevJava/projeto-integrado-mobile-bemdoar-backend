package br.com.bemdoar.config;

import br.com.bemdoar.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

/** Rotas públicas explícitas; as demais exigem autenticação e autorização por método. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final String origemFrontend;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          @Value("${bemdoar.cors.origem}") String origemFrontend) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.origemFrontend = origemFrontend;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ---------- infraestrutura ----------
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ---------- login e cadastro (RF01, RF02) ----------
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/cadastro").permitAll()

                // ---------- AREAS PUBLICAS (documento, item 2.2) ----------
                .requestMatchers(HttpMethod.GET, "/api/instituicao").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categorias/ativas").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/necessidades").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/necessidades/*").permitAll()

                // ---------- todo o resto exige estar logado ----------
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // 401 = voce nao esta logado  -> o front manda para a tela de Login
                .authenticationEntryPoint((req, res, e) ->
                        responderJson(res, 401, "Voce precisa estar autenticado para acessar esta area."))
                // 403 = voce esta logado, mas nao tem permissao -> o front mostra aviso
                .accessDeniedHandler((req, res, e) ->
                        responderJson(res, 403, "Voce nao possui permissao para esta operacao."))
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void responderJson(jakarta.servlet.http.HttpServletResponse res,
                                      int status, String mensagem) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"mensagem\":\"" + mensagem + "\"}");
    }

    /** RN04 - as senhas sao gravadas com BCrypt, nunca em texto puro. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origemFrontend));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
