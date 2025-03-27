package com.astroglow.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "AUTHENTICATION")
public class AuthenticationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_biometric_id")
    private Long userBiometricId;

    @OneToOne
    @JoinColumn(name="user_id",nullable=false)
    private UserEntity user;

    public AuthenticationEntity(Long userBiometricId, UserEntity user) {
        this.userBiometricId = userBiometricId;
        this.user = user;
    }

    public AuthenticationEntity() {
    }

    public Long getUserBiometricId() {
        return userBiometricId;
    }

    public void setUserBiometricId(Long userBiometricId) {
        this.userBiometricId = userBiometricId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
