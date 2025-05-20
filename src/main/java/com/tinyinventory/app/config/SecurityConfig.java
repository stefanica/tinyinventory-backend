package com.tinyinventory.app.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //This is an interface and needs implementation, done in service package
    //@Autowired will inject an MyUserDetailsService object from service package
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;


    // It Authenticates user using a database table through a UserDetailsService object
    @Bean
    public AuthenticationProvider authProvider() {
        //I want to say: I want to connect to the Database and we need a DaoAuthenticationProvider
        //because this is your database access
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        //the process to connect and check with the database taken care by UserDetailsService
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    //This AuthManager Bean is used in the UserController Login method
    //It uses the AuthenticationProvider to authenticate a User
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // If we want to change the default Spring Security configuration
    // we have to return object of SecurityFilterChain
    // Spring Security behind the scenes works with securityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ***The Lambda configuration, used in production***
        http.csrf(customizer -> customizer.disable()); //disabling csrf

        // enable authorisation; for every request we are saying authenticate
        http.authorizeHttpRequests((request -> request
                //once a user logs in I want to create a new token
                .requestMatchers("/register", "/login", "/reset-password") //add this request path (url)
                .permitAll() // only for "register" and "login" requests path to permit to be accessed
                .anyRequest().authenticated()));

        //IF we have STATELESS we have to disable the form login
        //http.formLogin(Customizer.withDefaults());// if it is not implemented we have to provide a basic form for login

        http.httpBasic(Customizer.withDefaults()); // for the security

        //If we don't want it to maintain session
        //every time you send a request you will get a new session
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //Here we place our own JwtFilter before the Spring Custom One (UsernamePass....)
        //JwtFilter is used to do all your checks (validating the token)
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    //************* WHY to use JWT ***************
    /*
     AuthenticationProvider and AuthenticationManager to authenticate users
     —this typically means you’re using session-based authentication (e.g., cookies or in-memory sessions).
     However, if you want to move to a token-based stateless authentication model
     (especially useful for REST APIs), JWT comes into play.


     1. (Session-based Authentication):
        - After a user logs in, their credentials are verified and stored in an HTTP session.
        - Each request checks the session for user details.
     2. JWT Setup (Token-based Authentication):
        - Upon successful login, a JWT is issued containing the user’s information (e.g., username, roles).
        - This JWT is sent by the client in the Authorization: Bearer <token> header with each request.
        - The server does not maintain any session or user state—just verifies the token.
        - Benefit: Ideal for scalable microservices, APIs, and SPAs (React, Angular) where session storage is impractical.


       WHERE to store the JWT on client side:
       1. JWT stored in the browser's localStorage
          localStorage.setItem("jwtToken", yourToken);
          To retrieve: const token = localStorage.getItem("jwtToken");
          Disadvantages: Vulnerable to XSS (Cross-Site Scripting) attacks—if malicious scripts run on your site, they can steal the token.

       2. HTTP-Only Cookies (Most Secure Approach)
          JWT is stored in an HTTP-only cookie—a cookie that cannot be accessed via JavaScript (mitigating XSS risks).
          Example (Server-side in Spring Boot):
          ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true) // Prevent JavaScript access
                .secure(true)  // Use HTTPS
                .path("/")     // Cookie available to entire site
                .maxAge(Duration.ofHours(1)) // Token expiration
                .build();
          response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

          XSS = (Cross-Site Scripting)
          Advantages:
            Protected from XSS (since JavaScript cannot access HTTP-only cookies).
            Automatically sent with every request to the same domain (no need to manually attach the JWT).
          Disadvantages:
            Requires more setup (especially for cross-domain handling via CORS).
            CSRF protection may still be needed if using cookies for stateful sessions.

     */





}
