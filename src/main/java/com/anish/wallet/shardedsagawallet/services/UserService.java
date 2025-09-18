package com.anish.wallet.shardedsagawallet.services;

import com.anish.wallet.shardedsagawallet.entity.User;
import com.anish.wallet.shardedsagawallet.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User createUser(User user)
    {
        log.info("Created user with email {}", user.getEmail());
        User createdUser = userRepository.save(user);
        log.info("user created with id {} in database shardwallet{1}", createdUser.getId(), (createdUser.getId() % 2 + 1));

        return createdUser;
    }

    public User getUserDetails(Long id)
    {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Id Not Found"));
    }

    public List<User> getUserDetails(String name) {
        List<User> users = userRepository.findByName(name);
        if (users.isEmpty()) {
            throw new RuntimeException("No user found with name: " + name);
        }
        return users;
    }
}
