package com.tinyinventory.app.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
/*
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_username", columnNames = "username")
}) // Ensures username uniqueness at the database level *** Not sure if I want this
*/
@Table(name = "users") // Specifies the table name in PostgreSQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    @Column(nullable = false, unique = true)
    private Integer id; // Use Integer instead of int to handle null values before persisting

    @Column(nullable = false, unique = true) // Unique and not null
    private String username;

    //@JsonIgnore // Prevent serialization
    //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //This allows deserializing (accepting password input) but prevents it from being serialized (returned in responses). ***NOT WORKING
    @Column(nullable = false) // Ensures password is required
    private String password;

    @Column(name = "full_name") //PostgreSQL uses snake case notation
    private String fullName; // Can be nullable

    @Column(unique = true) // Ensures email uniqueness but allows null
    private String email;


}
