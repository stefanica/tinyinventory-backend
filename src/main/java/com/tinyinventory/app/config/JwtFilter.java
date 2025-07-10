package com.tinyinventory.app.config;

import com.tinyinventory.app.service.JwtService;
import com.tinyinventory.app.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired //used to get DetailsService object below
    ApplicationContext context;

    //In the request object we are receiving the token, we have to take it from there and verify it
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // *** Used mainly to reset password ***
        String requestURI = request.getRequestURI();
        // Bypass JWT validation for these endpoints
        if (requestURI.equals("/register") || requestURI.equals("/login") || requestURI.equals("/reset-password-email") ||
                requestURI.equals("/change-password")) {
            filterChain.doFilter(request, response);
            return; // Skip the JWT validation logic
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userName = null;

        // is starts with Bearer eyJhbGciO...(token)... because we are searching for Bearer
        if (authHeader != null && authHeader.startsWith("Bearer")) {
            token = authHeader.substring(7); // remove Bearer word
            userName = jwtService.extractUserName(token);
        }


        // && we want to check if is there an Authentication object already available
        //we will do this validation when the Authentication object is not there
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // *** This is where you have to validate and generate the Authentication object
            // *** Spring want an Authentication object and how we will get it is our problem
            // *** In this project we used JWT for this

            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(userName);

            if (jwtService.validateToken(token, userDetails)) {

                //This is the Authentication Token, and we need to set it to SecurityContextHolder
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }
        //since this is a filter we have to call the main servlet as well
        // and the filters in between (to forward to the next filter)
        filterChain.doFilter(request, response); //by doing this you are continue the filter chain

    }

}
