package com.astroglow.Service;



import com.astroglow.Entity.UserEntity;
import com.astroglow.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public UserService() {
        super();
    }

    public UserEntity post(UserEntity user) {
        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
