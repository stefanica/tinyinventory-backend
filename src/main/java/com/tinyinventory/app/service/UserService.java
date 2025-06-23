package com.tinyinventory.app.service;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.tinyinventory.app.dto.UserRegisterDto;
import com.tinyinventory.app.dto.UserResetPasswordDto;
import com.tinyinventory.app.dto.UserResponseDto;
import com.tinyinventory.app.exceptions.*;
import com.tinyinventory.app.misc.APIKeys;
import com.tinyinventory.app.model.User;
import com.tinyinventory.app.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.sendgrid.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;
    //Uncomment Spring Security framework from pom.xml file
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // ********** Change Password in case it was forgotten *************//
    //Reset password with a random one
    @Transactional
    public void resetPasswordRandom(UserResetPasswordDto userResetPasswordDto)
            throws EmailNotFoundException, IOException, IllegalStateException {
        // Check if the email exists
        if (!userRepo.existsByEmail(userResetPasswordDto.getEmail())) {
            throw new EmailNotFoundException("Email not found");
        }

        // Generate and encode a new password
        String generatedPassword = randomPassword();
        String encodedPassword = encoder.encode(generatedPassword);

        // Update the password and validate the update
        int passwordUpdated = userRepo.setPasswordByEmail(encodedPassword, userResetPasswordDto.getEmail());
        if (passwordUpdated != 1) {
            throw new IllegalStateException("Password update failed. No rows were affected.");
        }

        // Send recovery email
        try {
            sendPasswordRecoveryEmail(userResetPasswordDto.getEmail(), generatedPassword);
        } catch (IOException e) {
                throw new IOException(e.getMessage());
        }
    }

    //Generate a random password that will replace the old password and will also be sent to email
    public String randomPassword() {
        String charArr = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(charArr.charAt(random.nextInt(charArr.length())));
        }
        return password.toString();
    }

    //Send password to User email using SendGrid dependency
    public void sendPasswordRecoveryEmail(String to, String newPassword) throws IOException {
        Email from = new Email("contact@tinyinventory.com");
        String subject = "Password Recovery";
        Email recipient = new Email(to);
        Content content = new Content("text/plain", "Your new password is: " + newPassword);
        Mail mail = new Mail(from, subject, recipient, content);

        APIKeys apiKeys = new APIKeys();
        SendGrid sg = new SendGrid(apiKeys.getSendGridKey());
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sg.api(request);
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

        //This can also be done on the front-end, but for security reasons needs to be also on backend
        if (!validatePassword(userRegisterDto.getPassword())) {
            throw new InvalidPasswordFormatException("Password format is invalid");
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


    //This can also be done on the front-end, but for security reasons needs to be also on backend
    public boolean validatePassword(String password) {
        //Return TRUE if:
        // - has at least 8 characters
        // - has at least one letter
        // - has at least one digit
        return password.matches("^(?=.*[A-Za-z])(?=.*\\\\d)[A-Za-z\\\\d]{8,}$");
    }

}
