package com.example.login_server.login.repository;

import com.example.login_server.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query(value = "update USER u set u.password = :password where u.username = :username" , nativeQuery = true)
    void updateUserByPasswordForUsername(User user);

}

