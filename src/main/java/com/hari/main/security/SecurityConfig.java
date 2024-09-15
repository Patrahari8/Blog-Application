package com.hari.main.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.hari.main.config.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(configurer ->
                        configurer
                                .requestMatchers("/signup", "/register", "/login", "/resources/**", "/css/**").permitAll()
                                .requestMatchers("/post", "/post/**", "/", "/comment/**","/post/search", "/post/filterBy", "/post/sortByDate").permitAll()
                                .requestMatchers("/post/create", "/post/*/edit", "/post/*/delete", "/post/*/update",
                                        "/comment/*/edit/*", "/comment/{id}", "/comment/*/delete/*").hasAnyRole("AUTHOR", "ADMIN")
                                .requestMatchers("/**").hasRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .formLogin(form ->
                        form
                                .loginPage("/login")
                                .loginProcessingUrl("/authenticateTheUser")
                                .defaultSuccessUrl("/post", true)
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }


}


