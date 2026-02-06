package com.nimbleways.springboilerplate.services;


import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;

import java.util.Optional;

public interface OrderService {

    Long processOrder(Long orderId);

    Optional<Order> findOrderById(Long id);

    void purchaseSeasonalProduct(Product p);

    void purchaseNormalProduct(Product p);

    void purchaseExpirableProduct(Product p);
}
