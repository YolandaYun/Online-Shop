package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.InventoryResponse;
import com.microservice.orderservice.dto.OrderRequest;
import com.microservice.orderservice.dto.OrderRequestItem;
import com.microservice.orderservice.model.Order;
import com.microservice.orderservice.model.OrderItem;
import com.microservice.orderservice.repository.OrderRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderItem> orderItems = orderRequest.getOrderRequestItemList()
                .stream()
                .map(this::mapOrderRequestToOrder)
                .toList();
        order.setOrderItemsList(orderItems);

        List<String> skuCodes = orderItems.stream()
                .map(OrderItem::getSkuCode)
                .toList();

        // communicate with inventory service to check if in stock
        InventoryResponse[] inventoryResponseArr = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder
                        .queryParam("skuCodes", skuCodes)
                        .build())
                .retrieve() //  initiates the HTTP request to URI and returns a ResponseSpec object
                .bodyToMono(InventoryResponse[].class) //asynchronous deserialization
                .block(); // convert to synchronous;
                // wait for HTTP response before proceeding further to a non-reactive part of code.
        boolean allProductsInStock = Arrays.stream(inventoryResponseArr).allMatch(InventoryResponse::isInStock);

        if (allProductsInStock){
            orderRepository.save(order);
            log.info("successfully placed order");
        }
        else {
            throw new IllegalArgumentException("At least one product is not in stock.");
        }

    }

    private OrderItem mapOrderRequestToOrder(OrderRequestItem orderRequestItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setPrice(orderRequestItem.getPrice());
        orderItem.setQuantity(orderRequestItem.getQuantity());
        orderItem.setSkuCode(orderRequestItem.getSkuCode());
        return orderItem;
    }
}
