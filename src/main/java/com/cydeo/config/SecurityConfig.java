package com.cydeo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

        List<UserDetails> userList = new ArrayList<>();

        User user1 = new User("mike", passwordEncoder.encode("password"),
                List.of(new SimpleGrantedAuthority("ROLE_Admin")));
        User user2 = new User("Ozzy", passwordEncoder.encode("password"),
                List.of(new SimpleGrantedAuthority("ROLE_Manager")));

        userList.add(user1);
        userList.add(user2);

        return new InMemoryUserDetailsManager(userList);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user/**").hasRole("Admin")
                        .requestMatchers("/project/**").hasRole("Manager")
                        .requestMatchers("/task/**").hasRole("Manager")
                        .requestMatchers("/task/employee/**").hasRole("Employee")
                        .requestMatchers(
                                "/",
                                "/login",
                                "/fragments/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated())
//                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/welcome")
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .build();
    }

    /// .loginPage tells Spring Security to use our own page as the login form instead of the default one
    /// if the login is successful, Spring Security will redirect the user to the welcome page




}
