package com.katran.security;
import com.katran.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter  {


    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors().and() // Добавляем CORS
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/chat", "/chat/**",
                        "/forgot-password", "/forgot-password/**",
                        "/game", "/game/**",
                        "/slotGame", "/slotGame/**",
                        "/help", "/help/**",
                        "/index", "/index/**",
                        "/mainIndex", "/mainIndex/**",
                        "/profile", "/profile/**",
                        "/registration", "/registration/**",
                        "/resetpassword", "/resetpassword/**",
                        "/reset-password/validate", "/reset-password/validate/**",
                        "/reset-password/change", "/reset-password/change/**",
                        "/promocode", "/promocode/**",
                        "/send-message", "/send-message/**",
                        "/signIn", "/signIn/**",
                        "/wheel" // Разрешить доступ к WebSocket
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/mainIndex")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    // Конфигурация CORS для всех путей
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("*")); // Разрешить все домены, если нужно - укажи свой
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/resources/**","/img/**","/favicon/**","/js/**","/sound/**");  // Игнорирование ресурсов
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}