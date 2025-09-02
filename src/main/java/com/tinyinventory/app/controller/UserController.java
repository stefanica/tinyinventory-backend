package com.tinyinventory.app.controller;

import com.tinyinventory.app.dto.*;
import com.tinyinventory.app.exceptions.*;
import com.tinyinventory.app.model.User;
import com.tinyinventory.app.service.EmailService;
import com.tinyinventory.app.service.JwtService;
import com.tinyinventory.app.service.MyUserDetailsService;
import com.tinyinventory.app.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

//Used to allow access from React/Vite. It may be changed or commented in production
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = {
        "http://localhost:5173", // for development
        "http://tinyinventory.com",
        "https://tinyinventory.com",
        "https://www.tinyinventory.com"
})
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserLoginDto userLoginDto) {
        //How do you check if the username and password are correct?
        // In Spring Security we have something called User Password Authentication Token

        //Authentication is the object that holds the data, but first you have to put the data of user into it
        //When you send a request, it goes to Authentication Manager, from that it uses a Authentication provider
        //In our case we are using DaoAuthenticationProvider and this uses the object of Authentication
        //But first I want an Authentication Manager and for that we create a new @Bean in SecurityConfig
        //Before we generate a token we want to see if the username and password is correct (the 2 lines below)
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword()));

            // *** Authentication Token from above (username, password) is different from JwtToken from below (digital signature)

            //If the username and password are correct then we can generate a JwtToken to be saved in the browser
            if (authentication.isAuthenticated()) {
                //the returned JwtToken should be saved in the in browser local memory (cookie)
                // and passed along with every client request except for login and register
                //return jwtService.generateToken(userLoginDto.getUsername());
                try {
                    String token = jwtService.generateToken(userLoginDto.getUsername());
                    //System.out.println(token);
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(Map.of("token", token));
                } catch (Exception e) {
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Server error: Login token failed to generate"));
                }
            } else {
                //return "Login Failed";
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Login Failed. Bad Username or Password"));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server Connection Error: " + e.getMessage()));
        }
        //***Original was without the try-catch blocks and ResponseEntity***
        /* //Returns a String Token
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        } else {
            return "Login Failed";
        }
        */
    }


    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> saveUser(@RequestBody UserRegisterDto userRegisterDto) {
       //try {
           UserResponseDto savedUser = userService.saveUser(userRegisterDto);

           Map<String, Object> response = new HashMap<>();
           response.put("username", savedUser.getUsername());
           response.put("fullName", savedUser.getFullName());
           response.put("email", savedUser.getEmail());
           response.put("message", "User registered successfully.");

           if (savedUser.getUsername() != null && savedUser.getEmail() != null) {
               emailService.sendRegistrationEmail(savedUser.getEmail(), savedUser.getUsername());
           }

           return ResponseEntity
                   .status(HttpStatus.CREATED)
                   .body(response);

    }

    /***************************************** Password Forgot / Change ******************************************/
    @PostMapping("/reset-password-email")
    public ResponseEntity<Map<String, Object>> sendResetPasswordLink(@RequestBody UserResetPasswordDto userResetPasswordDto) {
        try {
            userService.sendPasswordResetEmail(userResetPasswordDto);
           /* return ResponseEntity.ok().body(Map.of("message", "An email with a random " +
                        "generated password was send to your email: " + userResetPasswordDto.getEmail()));*/
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("message", "An email with a link " +
                            "to reset your password was send to your mail address: " + userResetPasswordDto.getEmail()));
        } catch (EmailNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "The email you have entered was not found in the database"));
        } catch (MessagingException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send email: " + e.getMessage() + " Please try again."));
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage() + " Please try again."));
        }
    }


    //Method name used if we want to send the token as a Path variable
    /*@PutMapping("/change-password/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable String token, @RequestBody PasswordResetDto request) {*/

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody PasswordResetDto request) {
        try {
           userService.updatePassword(request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("message", "Password changed successfully."));
        } catch (EmailNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (PasswordMatchException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (InvalidPasswordFormatException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage())); //gets the message from the UserService (exception creation)
        } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", e.getMessage()));
            }
        }
    /********************************************************************************************/



    //Return user information (Full Name, Username, Email)
    @GetMapping("/user")
    public ResponseEntity<UserResponseDto> getUserProfileData(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        String username = userDetails.getUsername();
        try {
            UserResponseDto userResponseDto = userService.getUserProfileData(username);
            if (userResponseDto != null) {
                return ResponseEntity.ok(userResponseDto);
            } else {
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build();
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    //Return user information (Full Name, Username, Email)
    @DeleteMapping("/close-account")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserLoginDto userLoginDto) {
        //Checks if the token from frontend is available
        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        //Checks is the username and password are correct
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword()));
            if (authentication.isAuthenticated()) {
                String username = userLoginDto.getUsername();
                //DELETE user
                userService.deleteUser(username);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .build();
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .build();
            }

        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server Connection Error: " + e.getMessage()));
        }
    }



    //RECOMMENDED: Check to see if token is valid, using Bearer Token method (in React);
    @GetMapping("/check-token")
    public ResponseEntity<Map<String, Object>> isTokenValid(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Session Expired"));
        }

        //return ResponseEntity.ok(userDetails.getUsername());
        //System.out.println("Check if valid: " + userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Token Valid"));
    }

    //NOT Recommended: Check to see if token is valid using token in Request Body (request from React)
    @GetMapping("/check-credentials")
    public ResponseEntity<Map<String, Object>> checkTokenValidation(@RequestBody TokenDto tokenDto) {

        try {
            //get username from token
            String username = jwtService.extractUserName(tokenDto.getToken());
            //get userDetails by checking the database
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);

            boolean isTokenValid = jwtService.validateToken(tokenDto.getToken(), userDetails);

            if (isTokenValid) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of("message", "OK"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Session Expired"));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server Connection Error. Try again!"));
        }
    }

}
