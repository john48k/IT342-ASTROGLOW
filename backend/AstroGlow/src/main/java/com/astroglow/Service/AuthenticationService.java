package com.astroglow.Service;

import com.astroglow.Entity.AuthenticationEntity;
import com.astroglow.Repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AuthenticationService {
    @Autowired
    AuthenticationRepository authenticationRepository;

    public AuthenticationService() {

    }

    public AuthenticationEntity postAuthentication(AuthenticationEntity authentication) {
        return authenticationRepository.save(authentication);
    }
    public List<AuthenticationEntity> getAllAuthentication() {
        return authenticationRepository.findAll();
    }

    @SuppressWarnings("finally")
    public AuthenticationEntity putAuthentication(int id, AuthenticationEntity newAuthentication) {
        AuthenticationEntity authentication = new AuthenticationEntity();
        try {
            authentication = authenticationRepository.findById(id).get();
            authentication.setUser(newAuthentication.getUser());
            authentication.setUserBiometricId(newAuthentication.getUserBiometricId());
        } catch (NoSuchElementException nex) {
            throw new NameNotFoundException("Authentication is " + id + " not found!");
        } finally {
            return authenticationRepository.save(authentication);
        }
    }

    public String deleteAuthentication(int id) {
        String msg="";
        if(authenticationRepository.findById(id)!=null) {
            authenticationRepository.deleteById(id);
            msg="Successfully deleted.";
        } else
            msg=id+" not found.";
        return msg;
    }
}
