package com.github.maxpesh.airports;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
class WebSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authConf -> {
                    authConf.requestMatchers("*/airports/lookup/v1").permitAll();
                    authConf.requestMatchers("/error").permitAll();
                    authConf.requestMatchers("/favicon.ico").permitAll();
                    authConf.anyRequest().authenticated();
                })
                .httpBasic(basicConf -> basicConf
                        .realmName("private realm")
                        .securityContextRepository(new NullSecurityContextRepository()))
                .authenticationManager(authenticationManager())
                .requestCache(RequestCacheConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    private AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(authProvider);
    }

    public UserDetailsService userDetailsService() {
        UserDetails testUser = User.builder()
                .passwordEncoder(s -> passwordEncoder().encode(s))
                .username("test")
                .password("password")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(testUser);
    }

    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return true;
            }
        };
    }
}
