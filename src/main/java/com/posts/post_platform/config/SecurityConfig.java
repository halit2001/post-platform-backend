package com.posts.post_platform.config;

import com.posts.post_platform.security.CustomAuthenticationManager;
import com.posts.post_platform.security.JwtAuthenticationEntryPoint;
import com.posts.post_platform.security.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements SecurityFilterChain {
   private final JwtAuthenticationEntryPoint handler;
   private final CustomAuthenticationManager customAuthenticationManager;

    @Autowired
    public SecurityConfig(JwtAuthenticationEntryPoint handler, CustomAuthenticationManager customAuthenticationManager) {
        this.handler = handler;
        this.customAuthenticationManager = customAuthenticationManager;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
       return new JwtAuthenticationFilter();
   }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception{
        return customAuthenticationManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(handler))
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/community/comments/community/**").permitAll()
                        .requestMatchers("/community/get_community/**").permitAll()
                        .requestMatchers("/community/get_community_by_name/**").permitAll()
                        .requestMatchers("/community/get_all_community_members/**").permitAll()
                        .requestMatchers("/community/find_members_count/**").permitAll()
                        .requestMatchers("/community/get_all_communities").permitAll()
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return false;
    }

    @Override
    public List<Filter> getFilters() {
        return List.of();
    }
}
