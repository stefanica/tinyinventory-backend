package com.tinyinventory.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//If the user forgot his password, it has to send the email to backend
//Used to send user email from front-end to backend
//Backend will then send an Jwt Token to used to reset the password

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResetPasswordDto {
    private String email;

}
