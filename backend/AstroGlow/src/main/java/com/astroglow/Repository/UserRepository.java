package com.astroglow.Repository;


import com.astroglow.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    UserEntity findByUserName(String userName);
    @Query("SELECT u FROM UserEntity u WHERE u.userEmail = :email")
    UserEntity findByUserEmail(@Param("email") String email);
}
