package com.example.user;

import com.example.user.model.Product;
import com.example.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Properties;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "productUrl=http://localhost:8081" }) // Sett
class UserApplicationTests {

	private  GenericContainer<?> prodcutServiceContainer;
	private  final int PORT = 8081;
	private String baseUrl;

	@Autowired
	UserService userService;

	@Value("${productUrl}")
	private String productUrl;

	@Autowired
	private ConfigurableEnvironment environment;

	@BeforeEach
     void setup(ConfigurableApplicationContext env) {
		prodcutServiceContainer = new GenericContainer<>(DockerImageName.parse("adityajavis/product-service:latest"))
				.withExposedPorts(PORT);
		prodcutServiceContainer.start();


		String host = prodcutServiceContainer.getHost();
		Integer mappedPort = prodcutServiceContainer.getMappedPort(PORT);


		baseUrl = String.format("http://%s:%d", host, mappedPort);

		MutablePropertySources propertySources = environment.getPropertySources();

		Properties props = new Properties();
		props.put("productUrl", baseUrl);

		propertySources.addFirst(new PropertiesPropertySource("dynamicProperties", props));
	}

	@Test
	void userIntegrationTest() {
		List<Product> actualProductList = userService.getProductListForUser(1);
		Product product1 = Product.builder().userId(1).name("Book").price(120.0).state(2).build();
		Product product2 = Product.builder().userId(1).name("Fruit").price(120.0).state(2).build();
		List<Product> expectedProductList = List.of(product1, product2);
		Assertions.assertThat(expectedProductList).usingElementComparatorIgnoringFields("id").containsExactlyInAnyOrderElementsOf(actualProductList);
	}

}
