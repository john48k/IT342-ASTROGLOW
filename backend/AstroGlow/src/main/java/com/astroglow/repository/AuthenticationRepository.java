package com.astroglow.repository;

import com.astroglow.entity.AuthenticationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationRepository extends JpaRepository<AuthenticationEntity, Integer> {

}
