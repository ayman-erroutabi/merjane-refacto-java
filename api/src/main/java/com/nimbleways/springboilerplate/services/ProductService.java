package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {
    void handleSeasonalProduct(Product p);

    void notifyDelay(int leadTime, Product p);

    void processPurchaseOrNotifyExpiry(Product p);
}
