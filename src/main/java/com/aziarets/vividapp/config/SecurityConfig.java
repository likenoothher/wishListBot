package com.aziarets.vividapp.config;

import com.aziarets.vividapp.model.BotUserRole;
import com.aziarets.vividapp.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@ComponentScan("com.aziarets.vividapp")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
//        auth.inMemoryAuthentication().withUser(User.builder()
//            .username("john_yoy")
//            .password(passwordEncoder().encode("1"))
//            .roles(BotUserRole.ADMIN.getRole())
//            .build())
//            .withUser(User.builder()
//                .username("jama_darma")
//                .password(passwordEncoder().encode("1"))
//                .roles(BotUserRole.USER.getRole())
//                .build());
    }

    @Bean
    protected DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .authorizeRequests()
                .antMatchers("/wishlist/**", "/i_present/**", "/subscribers/**", "/subscriptions/**").authenticated()
                .antMatchers("/js/**", "/css/**", "/").permitAll()
            .and()
                .formLogin()
                .defaultSuccessUrl("/wishlist")
                .permitAll()
            .and()
                .logout()
                .logoutSuccessUrl("/");
    }



}
