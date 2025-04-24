package com.astroglow.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "AUTHENTICATION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuthenticationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_biometric_id")
    private int userBiometricId;

    @Column(name = "biometric_enabled", nullable = false, columnDefinition = "boolean default false")
    private boolean biometricEnabled = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonIgnoreProperties("authentication")
    private UserEntity user;

    public AuthenticationEntity(int userBiometricId, UserEntity user) {
        this.userBiometricId = userBiometricId;
        this.user = user;
        this.biometricEnabled = false;
    }

    public AuthenticationEntity() {
        this.biometricEnabled = false;
    }

    public int getUserBiometricId() {
        return userBiometricId;
    }

    public void setUserBiometricId(int userBiometricId) {
        this.userBiometricId = userBiometricId;
    }

    public boolean isBiometricEnabled() {
        return biometricEnabled;
    }

    public void setBiometricEnabled(boolean biometricEnabled) {
        this.biometricEnabled = biometricEnabled;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
