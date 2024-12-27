package antifraud.Config;

import antifraud.Enums.Roles;
import antifraud.Services.CustomUserService;
import antifraud.Services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserService customUserService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final PasswordEncoder bcryptPasswordEncoder;

    public SecurityConfig(CustomUserService customUserService, RestAuthenticationEntryPoint restAuthenticationEntryPoint, PasswordEncoder bcryptPasswordEncoder) {
        this.customUserService = customUserService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.bcryptPasswordEncoder = bcryptPasswordEncoder;
    }

    /*
    The tests in this project were incredibly hard to work with at times. If there's duplicates in the URIs and methods (which there is)
    it's probably equal parts HS tests being difficult, and me finding the quickest path to getting them to pass (not great)
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Autowired AuthenticationManager authenticationManager) throws Exception {
        http.httpBasic(Customizer.withDefaults())
                .csrf((csrf) -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole(Roles.ADMINISTRATOR.getRole())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role/").hasRole(Roles.ADMINISTRATOR.getRole())
                                .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasRole(Roles.ADMINISTRATOR.getRole())
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access/").hasRole(Roles.ADMINISTRATOR.getRole())
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(Roles.MERCHANT.getRole())
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction/").hasRole(Roles.MERCHANT.getRole())
                                .requestMatchers(HttpMethod.DELETE, "/api/auth/user/{username}").hasRole(Roles.ADMINISTRATOR.getRole())
                                .requestMatchers(HttpMethod.DELETE, "/api/auth/user/").hasRole(Roles.ADMINISTRATOR.getRole())
                                .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(Roles.ADMINISTRATOR.getRole(), Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.GET, "/api/auth/list/").hasAnyRole(Roles.ADMINISTRATOR.getRole(), Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/{number}").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/{ip}").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history").hasRole(Roles.SUPPORT.getRole())
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/{number}").hasRole(Roles.SUPPORT.getRole())
                                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                                .requestMatchers(PathRequest.toH2Console()).permitAll()
                                .requestMatchers("/error**").permitAll()
                                .requestMatchers("/actuator/shutdown").permitAll()

                .anyRequest().authenticated())
                .authenticationManager(authenticationManager)
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserService);
        authenticationProvider.setPasswordEncoder(bcryptPasswordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Component
    static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }
}
