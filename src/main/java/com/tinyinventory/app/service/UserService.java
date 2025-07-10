package com.tinyinventory.app.service;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.tinyinventory.app.dto.*;
import com.tinyinventory.app.exceptions.*;
import com.tinyinventory.app.misc.APIKeys;
import com.tinyinventory.app.model.User;
import com.tinyinventory.app.repo.UserRepo;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.sendgrid.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

    //Uncomment Spring Security framework from pom.xml file
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // ********** Change Password in case it was forgotten *************//
    //Reset password with a random one
    //@Transactional
    public void sendPasswordResetEmail(UserResetPasswordDto userResetPasswordDto)
            throws EmailNotFoundException, MessagingException, UnsupportedEncodingException {

        //The email for which we want to reset the password
        String email = userResetPasswordDto.getEmail();

        // Check if the email exists
        if (!userRepo.existsByEmail(email)) {
            throw new EmailNotFoundException("Email not found");
        }

        //If email exists, then generate a token and send an email with it
        String token = jwtService.generatePasswordResetToken(email);
        emailService.sendRecoveryTextEmail(email, token); //Sending text email -> WORKS
        //emailService.sendRecoveryHtmlEmail(email, token); //Sending HTML email -> Woks, but sends email to spam

    }

    public void updatePassword(PasswordResetDto passwordResetDto) {
        String token = passwordResetDto.getToken();
        String password = passwordResetDto.getPassword();
        String confirmedPassword = passwordResetDto.getConfirmedPassword();

        System.out.println("Password: " + password);
        System.out.println("Confirmed password: " + confirmedPassword);

        String email = jwtService.extractUserName(token);

        if (!userRepo.existsByEmail(email)) {
            throw new EmailNotFoundException("Email not found! Possible broken token or server error.");
        }

        if (!password.equals(confirmedPassword)) {
            throw new PasswordMatchException("Passwords don't match");
        }

        if (!validatePassword(password)) {
            throw new InvalidPasswordFormatException("Password format is invalid! " +
                    "Must be at least 8 characters, include uppercase, lowercase, number, and special character.");
        }


        userRepo.updatePasswordByEmail(encoder.encode(password), email);
    }
    //*******************************************************************************************//


    public UserResponseDto saveUser(UserRegisterDto userRegisterDto) {

        if (userRepo.existsByUsername(userRegisterDto.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (userRepo.existsByEmail(userRegisterDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (!userRegisterDto.getPassword().equals(userRegisterDto.getPasswordConfirmed())) {
            throw new PasswordMatchException("Passwords don't match");
        }

        //WORKS but I opted for a simpler password format
        //This can also be done on the front-end, but for security reasons needs to be also on backend
        /*if (!validatePassword(userRegisterDto.getPassword())) {
            throw new InvalidPasswordFormatException("Password format is invalid! " +
                    "Must be at least 8 characters, include uppercase, lowercase, number, and special character.");
        }*/

        if (!validatePassword(userRegisterDto.getPassword())) {
            throw new InvalidPasswordFormatException("Password must be at least 8 characters long.");
        }

        //Create User object (Entity) from UserRegisterDto
        User user = new User();
        user.setFullName(userRegisterDto.getFirstName() + " " + userRegisterDto.getLastName());
        user.setUsername(userRegisterDto.getUsername());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(encoder.encode(userRegisterDto.getPassword()));

        //Used to encrypt the password in the database
        //user.setPassword(encoder.encode(user.getPassword()));

        return new UserResponseDto(userRepo.save(user));
    }

    //Get user data from account profile
    public UserResponseDto getUserProfileData(String username) {
        User user = userRepo.findUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));

        return new UserResponseDto(user);
    }

    //Delete User from database
    public void deleteUser(String username) {
        Optional<User> optionalUser = userRepo.findUserByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            userRepo.deleteById(user.getId());
        } else {
            throw new EntityNotFoundException("User with username " + username + " not found");
        }
    }


    //This can also be done on the front-end, but for security reasons needs to be also on backend
    public boolean validatePassword(String password) {
        //Return TRUE if:
        // 1. has at least 8 characters, at least one letter, at least one digit
        // regex: ^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$

        // 2. at least 8 characters, include uppercase, lowercase, number, and special character
        // regex: ^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,}$

        //3. Any character, but at least 8 characters
        // regex: ^\S{8,}$
        return password.matches("^\\S{8,}$");
    }

}
