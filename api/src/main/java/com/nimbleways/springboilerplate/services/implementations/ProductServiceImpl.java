package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository pr;

    @Autowired
    NotificationService ns;

    /**
     * Sends a notification with leadTime for a certain product
     *
     * @param leadTime the number of days needed for restocking
     * @param p        the product in question
     */
    @Override
    public void notifyDelay(int leadTime, Product p) {
        ns.sendDelayNotification(leadTime, p.getName());
    }

    @Override
    public void saveProduct(Product p) {
        pr.save(p);
    }

    /**
     * Attempts to consume (sell) one unit of the given product.
     * <p>
     * If the product has available stock and its expiry date is after today, this method decrements
     * the available quantity by 1 and persists the updated product.
     * Otherwise, it sends an expiration notification, sets the available quantity to 0, and persists
     * the updated product.
     * </p>
     *
     * @param p the product to validate and update; must not be {@code null}
     * @throws NullPointerException if {@code p} is {@code null} (or if its expiry date is {@code null})
     */
    @Override
    public void handleSeasonalProduct(Product p) {
        final LocalDate now = LocalDate.now();
        if (now.plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            saveProduct(p);
        } else if (p.getSeasonStartDate().isAfter(now)) {
            ns.sendOutOfStockNotification(p.getName());
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }



    /**
     * Decrements available stock by 1 if the product is in stock and not expired; otherwise sends an
     * expiration notification, sets availability to 0, and saves the product.
     *
     * @param p the product to update
     */
    @Override
    public void processPurchaseOrNotifyExpiry(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            saveProduct(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            saveProduct(p);
        }
    }
}