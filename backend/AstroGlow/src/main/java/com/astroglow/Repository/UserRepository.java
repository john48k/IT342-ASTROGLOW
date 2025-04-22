package com.astroglow.Repository;

import com.astroglow.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByUserName(String userName);
    UserEntity findByOauthId(String oauthId);
    UserEntity findByUserEmail(String userEmail);
}
