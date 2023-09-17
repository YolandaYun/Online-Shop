package com.microservice.productservice;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microservice.productservice.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.productservice.dto.ProductRequest;
import com.microservice.productservice.respository.ProductRepository;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;
	
    

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
		// for test, instead of local db, docker container is used
        dymDynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
	@Test
	void testCreateProduct() {
		ProductRequest productRequest = ProductRequest.builder()
                .name("carpet")
                .description("a fluffy one")
                .price(BigDecimal.valueOf(100))
                .build();
		String requestBody = "";
		try {
		requestBody = objectMapper.writeValueAsString(productRequest);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		};

		// MockMvc is a mocked servlet environment that we can use to test our HTTP controller endpoints
		// /without the need to launch our embedded servlet container.
		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
							.contentType(MediaType.APPLICATION_JSON)
							.content(requestBody))
							.andExpect(status().isCreated());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		Product product = Product.builder()
//				.name("carpet")
//				.price(BigDecimal.valueOf(100))
//				.description("a fluffy one")
//				.build();
//		List<Product> myList = new ArrayList<Product>();
//		myList.add(product);

		Assertions.assertEquals(1, productRepository.findAll().size());


	}

}
