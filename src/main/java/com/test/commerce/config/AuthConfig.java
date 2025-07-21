package com.test.commerce.config;


import com.test.commerce.model.Customer;
import com.test.commerce.model.Retailer;
import com.test.commerce.model.User;
import com.test.commerce.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AuthConfig {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthConfig.class);

    public AuthConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return email -> {
           var user =  userRepository.findByEmail(email)
                   .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                logger.info("customer");
             return customer;
            } else if (user instanceof Retailer) {
                Retailer retailer = (Retailer) user;
                logger.info("retaitomer");
                return retailer;
            }
            return  user;
        };
    }


    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }
}