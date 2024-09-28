package com.example.user.service;

import com.example.user.model.Product;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
}
