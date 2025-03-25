package com.astroglow.Service;



import com.astroglow.Entity.UserEntity;
import com.astroglow.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public UserService() {
        super();
    }

    public UserEntity postUser(UserEntity user) {
        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }


    @SuppressWarnings("finally")
    public UserEntity putUser(int userId, UserEntity newUserDetails) {
        UserEntity user = new UserEntity();

        try {
            user = userRepository.findById(userId).get();

            user.setUserName(newUserDetails.getUserName());
            user.setUserEmail(newUserDetails.getUserEmail());
            user.setUserPassword(newUserDetails.getUserPassword());

        } catch(NoSuchElementException nex) {
            throw new NameNotFoundException("User " + userId + " not found!");
        }
        finally {
            return userRepository.save(user);
        }
    }

    public String deleteUser(int id) {
        String msg= "";
        if(userRepository.findById(id)!=null) {
            userRepository.deleteById(id);
            msg="User successfully deleted.";
        } else
            msg=id + " not found.";
        return msg;
    }

    // Signup Method
    public UserEntity registerUser(UserEntity user) {
        // Check if the user already exists
        if (userRepository.findByUserEmail(user.getUserEmail()) != null) {
            throw new IllegalArgumentException("User already exists with this email.");
        }
        return userRepository.save(user); // Save user without encoding the password
    }

    // Login Method
    public UserEntity loginUser(String userEmail, String password) {
        UserEntity user = userRepository.findByUserEmail(userEmail);
        if (user != null && user.getUserPassword().equals(password)) {
            return user; // Successful login
        }
        return null; // Login failed
    }
}
