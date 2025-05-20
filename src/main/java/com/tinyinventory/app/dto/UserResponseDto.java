package com.tinyinventory.app.dto;

import com.tinyinventory.app.model.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private int id;
    private String username;
    private String fullName;
    private String email;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
    }

}
