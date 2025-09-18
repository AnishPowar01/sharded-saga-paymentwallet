package com.anish.wallet.shardedsagawallet.controllers;

import com.anish.wallet.shardedsagawallet.entity.User;
import com.anish.wallet.shardedsagawallet.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<User>> getUserDetailsByName(@PathVariable String name) {
        return ResponseEntity.ok(userService.getUserDetails(name));
    }


}
