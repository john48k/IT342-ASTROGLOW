package com.astroglow.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "AUTHENTICATION")
public class AuthenticationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_biometric_id")
    private Long userBiometricId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
