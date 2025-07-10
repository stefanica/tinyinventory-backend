package com.tinyinventory.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendRegistrationEmail(String toEmail, String username) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("accounts@tinyinventory.com");  // must match verified sender in Brevo
        message.setTo(toEmail);
        message.setSubject("TinyInventory Registration");
        message.setText(" \n Welcome to TinyInventory! \n\n" +
                " This message was sent as a registration process to tinyinventory.com" + "\n\n" +
                " Your login username is: " + username + "\n\n" +
                " You can use the link below to log into your account: \n" +
                " https://tinyinventory.com/login " + "\n\n\n\n" +
                " For terms and conditions you can check: https://tinyinventory.com/terms" + "\n" +
                " If you haven't registered to TinyInventory, then someone else has entered your email, most likely by mistake." + "\n" +
                " If this email was wrongfully sent, contact us at: contact@tinyinventory.com. \n" +
                " Otherwise do not reply to this email! Thank you!" + "\n");

        mailSender.send(message);
    }

    public void sendRecoveryTextEmail(String toEmail, String token) {

            //UserService userService = new UserService();
            String localTokenURL = "http://localhost:5173/change-password/" + token; //For localhost
            String webTokenURL = "https://tinyinventory.com/change-password/" + token; //For web

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("accounts@tinyinventory.com");  // must match verified sender in Brevo
            message.setTo(toEmail);
            message.setSubject("TinyInventory Password Reset");
            message.setText(" \n This is the link to change your TinyInventory account password: \n\n" +
                    localTokenURL + "\n\n" +
                    " Follow the link or copy and paste it in your browser. It will take you to the page where you can change " +
                    "your password for your tinyinventory.com account." + "\n\n" +
                    " The link will work for 24 hours." + "\n\n" +
                    " After you changed the password, go to the Login page to see if it worked. " + "\n\n\n" +
                    " If you have not requested a password change, then this was sent by mistake." + "\n" +
                    " Do not reply to this email! Thank you!" + "\n");

            mailSender.send(message);
    }

    //Works, BUT sends email to spam
    public void sendRecoveryHtmlEmail(String toEmail, String token) throws MessagingException, UnsupportedEncodingException {

        String localTokenURL = "http://localhost:5173/change-password/" + token; //For localhost
        String webTokenURL = "https://tinyinventory.com/change-password/" + token; //For web

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress("accounts@tinyinventory.com", "TinyInventory"));
        helper.setTo(toEmail);
        helper.setSubject("TinyInventory Password Reset");

        String htmlContent = "<p>This is your link to change your TinyInventory account password:</p>" +
                "<p><a href=\"" + localTokenURL + "\">Click here to change your tinyinventory.com password</a></p>" +
                //"<p>Or copy and paste this link into your browser:<br>" +
                //webTokenURL + "</p>" +
                "<p>After resetting your password, you can log back into your account.</p>" +
                "<p>If you did not request a password reset, please ignore this email.<br>" +
                "Do not reply to this email. Thank you!</p>";

        helper.setText(htmlContent, true);  // true = enable HTML

        mailSender.send(message);
    }








    //Generate a random password that will replace the old password and will also be sent to email
    /***** NOT USED -> Using Jwt instead  *****/
    /*public String randomPassword() {
        String lowCaseCharArr = "abcdefghijklmnopqrstuvwxyz";
        String uppCaseCharArr = "ABCDEFGHIJKLMNOPQRSTUVEXYZ";
        String numbers = "0123456789";
        String specialChar = "<>,.?!@#$%^&*()-+={}[]";

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        //append a Capital letter character
        password.append(uppCaseCharArr.charAt(random.nextInt(uppCaseCharArr.length())));
        //append a lower case character
        for (int i = 0; i < 4; i++) {
            password.append(lowCaseCharArr.charAt(random.nextInt(lowCaseCharArr.length())));
        }
        //append a number
        for (int i = 0; i < 4; i++) {
            password.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        //append a special character
        password.append(specialChar.charAt(random.nextInt(specialChar.length())));

        return password.toString();
    }*/


}
