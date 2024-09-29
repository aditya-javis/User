package com.example.user.service;

import com.example.user.model.Product;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Value("${productUrl}")
    private String productUrl;

    @Autowired
    private ConfigurableEnvironment environment;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public List<Product> getProductListForUser(Integer id) {
        Product product1 = Product.builder().userId(id).name("Book").price(120.0).state(0).build();
        Product product2 = Product.builder().userId(id).name("Fruit").price(120.0).state(0).build();
        List<Product> productList = List.of(product1, product2);

        RestTemplate restTemplate = new RestTemplate();
        String url = environment.getProperty("productUrl") + "/update-product";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<List<Product>> requestEntity = new HttpEntity<>(productList, headers);

        ResponseEntity<List<Product>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<Product>>() {}
        );

        return response.getBody();
    }

    public void updateUsers(int state) {
        List<User> userList = userRepository.findAll();
        userList.forEach(item -> item.setState(state));
        userRepository.saveAll(userList);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void createUsers(Integer count) {
        Long a  = System.currentTimeMillis();
        List<User> userList = new ArrayList<>();
        for(int i =0; i< count; i++) {
            User user = new User();
            user.setName("Aditya" + i);
            user.setEmail("Temp" + i);
            user.setState(0);
            userList.add(user);
        }

        userRepository.saveAll(userList);

        Long b = System.currentTimeMillis();
        System.out.println(b-a);
    }

    public void testOptimisticLocking(Long id, Integer state) {
        Optional<User> userOpt = userRepository.findById(id);
        if(userOpt.isPresent()) {
            User user = userOpt.get();
            CompletableFuture<Void> task1 = updateUserInThread1Async(user, state);
            CompletableFuture<Void> task2 = updateUserInThread2Async(user, state);
            CompletableFuture.allOf(task1, task2).join();
        }
    }

    @Async
    @Transactional(rollbackOn = OptimisticLockException.class)
    public CompletableFuture<Void> updateUserInThread1Async(User user, Integer state) {
        System.out.println("Inside Transaction 1");
        return CompletableFuture.runAsync(() -> {
            user.setState(state);
            user.setName("Thread1_User");
            userRepository.save(user);
        });
    }

    @Async
    @Transactional(rollbackOn = OptimisticLockException.class)
    public CompletableFuture<Void> updateUserInThread2Async(User user, Integer state) {
        System.out.println("Inside Transaction 2");
        return CompletableFuture.runAsync(() -> {
            user.setState(state);
            user.setName("Thread2_User");
            userRepository.save(user);
        });
    }

}
