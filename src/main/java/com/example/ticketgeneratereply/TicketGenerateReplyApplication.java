package com.example.ticketgeneratereply;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@SpringBootApplication
@EnableResilientMethods // Replaced @EnableRetry
public class TicketGenerateReplyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketGenerateReplyApplication.class, args);
    }
}

@Configuration
@EnableWebSecurity
class SecurityConfiguration {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure CSRF protection to be fully htmx-compatible using a Cookie engine
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/public/seed-knowledge")
                )
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to assets, static layouts, or webhook endpoints
                        .requestMatchers("/css/**", "/js/**", "/webhooks/**", "/index.html", "/static/**").permitAll()
                        // Secure your core management dashboard endpoints
                        .requestMatchers("/dashboard/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );
        return http.build();
    }
}
