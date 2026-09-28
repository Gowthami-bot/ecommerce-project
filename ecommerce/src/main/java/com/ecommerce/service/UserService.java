package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.PasswordHasher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    public int createUser(User user) {
        user.setPassword(passwordHasher.hash(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean accountExists(String username, String email) {
        return userRepository.existsByUsernameOrEmail(username, email);
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase());
        if (user == null || !passwordHasher.matches(password, user.getPassword())) {
            return null;
        }
        if (passwordHasher.needsUpgrade(user.getPassword())) {
            userRepository.updatePassword(user.getId(), passwordHasher.hash(password));
        }
        return user;
    }

    public void upgradeLegacyPasswords() {
        for (User user : userRepository.findLegacyPasswordUsers()) {
            userRepository.updatePassword(user.getId(), passwordHasher.hash(user.getPassword()));
        }
    }
}