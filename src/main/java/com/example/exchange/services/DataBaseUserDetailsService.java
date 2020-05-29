package com.example.exchange.services;

import com.example.exchange.models.DataBaseUserPrincipal;
import com.example.exchange.models.entities.UserEntity;
import com.example.exchange.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DataBaseUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<UserEntity> user = userRepository.findByUserName(username);
        if (user.isPresent()) {
            return new DataBaseUserPrincipal(user.get());
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}
