package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;
import org.springframework.stereotype.Service;

public interface ProductService {

    void saveProduct(Product p);
    void handleSeasonalProduct(Product p);

    void notifyDelay(int leadTime, Product p);

    void processPurchaseOrNotifyExpiry(Product p);
}
