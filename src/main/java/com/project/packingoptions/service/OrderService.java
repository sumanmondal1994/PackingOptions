package com.project.packingoptions.service;

import com.project.packingoptions.dto.OrderRequest;
import com.project.packingoptions.dto.OrderResponse;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderResponse> getAllOrders();

    Optional<OrderResponse> getOrderById(Long id);

    OrderResponse createOrder(OrderRequest request);

    void deleteOrder(Long id);

}
