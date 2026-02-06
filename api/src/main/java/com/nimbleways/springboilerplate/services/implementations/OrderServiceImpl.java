package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository or;

    private final ProductService ps;

    @Override
    public Long processOrder(Long orderId) {
        Order order = findOrderById(orderId).orElseThrow();
        Set<Product> products = order.getItems();
        for (Product p : products) {
            switch (p.getType()) {
                case "NORMAL" -> purchaseNormalProduct(p);
                case "SEASONAL" -> purchaseSeasonalProduct(p);
                case "EXPIRABLE" -> purchaseExpirableProduct(p);
                default -> throw new IllegalArgumentException("Unknown product type: " + p.getType());
            }
        }
        return order.getId();
    }

    @Override
    public void purchaseExpirableProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            ps.saveProduct(p);
        } else {
            ps.processPurchaseOrNotifyExpiry(p);
        }
    }

    @Override
    public void purchaseNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            ps.saveProduct(p);
        } else {
            int leadTime = p.getLeadTime();
            if (leadTime > 0) {
                ps.notifyDelay(leadTime, p);
            }
        }
    }

    @Override
    public void purchaseSeasonalProduct(Product p) {

        if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate())
                && p.getAvailable() > 0)) {
            p.setAvailable(p.getAvailable() - 1);
            ps.saveProduct(p);
        } else {
            ps.handleSeasonalProduct(p);
        }
    }

    @Override
    public Optional<Order> findOrderById(Long id) {
        return or.findById(id);
    }
}