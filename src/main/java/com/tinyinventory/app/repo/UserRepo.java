package com.tinyinventory.app.repo;

import com.tinyinventory.app.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    //SELECT * FROM users WHERE username = username;  //SQL
    //@Query("SELECT u FROM users u WHERE u.username = ?1") //Spring JPA
    Optional<User> findUserByUsername(String username);

    //Verify is a username is already in the database
    //SELECT COUNT(*) > 0 FROM users WHERE username = ?1; ->SQL
    //@Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username"); ->JPA
    boolean existsByUsername(String username);

    //Check if the email is used by other users
    //SELECT COUNT(*) > 0 FROM users WHERE username = ?1;
    //@Query("SELECT COUNT(u) > 0 FROM users u WHERE u.username = :username");
    boolean existsByEmail(String email);

    //Update password in case it was forgotten
    //UPDATE Users SET password = ?1 WHERE email = ?2; //SQL
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int setPasswordByEmail(@Param("password") String password, @Param("email") String email);


}
