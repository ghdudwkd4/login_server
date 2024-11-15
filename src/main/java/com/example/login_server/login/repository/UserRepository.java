package com.example.login_server.login.repository;

import com.example.login_server.login.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);

    @Query(nativeQuery = true , value = "SELECT * FROM USER WHERE userId NOT IN ( :userId )")
    List<User> findNotInUserId(String userId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true , value = "UPDATE USER u SET u.password = :password WHERE u.username = :username")
    void updateUserByPassword(@Param("username") String username, @Param("password") String password) throws Exception;

    @Modifying
    @Transactional
    @Query(nativeQuery = true , value = "UPDATE USER u SET u.state = :state WHERE u.username = :username")
    void updateUserByState(User user) throws Exception;
}

