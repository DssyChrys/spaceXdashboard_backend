package com.example.spaceXdashboard_backend.service;

import com.example.spaceXdashboard_backend.dto.ProfileResponse;
import com.example.spaceXdashboard_backend.entity.User;
import com.example.spaceXdashboard_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public String getUsernameByEmail(String email){
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("Utilisateur possédant cette email non trouvé: " + email));
    }

    public ProfileResponse getProfileByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur possédant cette email non trouvé: " + email));
        return new ProfileResponse(user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}