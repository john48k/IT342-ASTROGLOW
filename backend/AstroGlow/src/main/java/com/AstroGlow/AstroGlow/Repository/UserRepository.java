package com.AstroGlow.AstroGlow.Repository;

import com.AstroGlow.AstroGlow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}