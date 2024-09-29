package com.example.user.controller;

import com.example.user.model.User;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping("/create")
    public User updateUserState(@RequestBody User user) {
        return userService.save(user);
    }

    @PostMapping("/{count}")
    public void createUser(@PathVariable Integer count) {
        userService.createUsers(count);
    }


    @PostMapping("/update/{state}")
    public void updateUserState(@PathVariable Integer state) {
        Long a = System.currentTimeMillis();
        userService.updateUsers(state);
        Long b = System.currentTimeMillis();
        System.out.println(b-a);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userService.findById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @DeleteMapping("/delete")
    public void deleteALl() {
        userService.deleteAll();
    }

    @PostMapping("/test/{id}/{state}")
    public void testOptimisticLocking(@PathVariable Long id , @PathVariable Integer state) {
        userService.testOptimisticLocking(id, state);
    }
}
