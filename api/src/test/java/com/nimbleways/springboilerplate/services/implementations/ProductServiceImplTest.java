package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductServiceImplTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void test_notifyDelay() {
        // GIVEN
        Product product = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.notifyDelay(product.getLeadTime(), product);

        // THEN
        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        Mockito.verify(notificationService, Mockito.atMostOnce()).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    // Tests for method handleSeasonalProduct

    @Test
    public void test_handleSeasonalProduct_whenProductExpires() {
        // GIVEN
        Product product = new Product(null, 40, 10, "SEASONAL", "STRAWBERRY", null, LocalDate.now(), LocalDate.now().plusDays(30));
        Product expectedProduct = new Product(null, 40, 0, "SEASONAL", "STRAWBERRY", null, LocalDate.now(), LocalDate.now().plusDays(30));

        // WHEN
        productService.handleSeasonalProduct(product);

        //THEN
        Mockito.verify(notificationService, Mockito.atMostOnce()).sendOutOfStockNotification("STRAWBERRY");
        ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedProduct);

    }

    @Test
    public void test_handleSeasonalProduct_whenProductNotInSeason() {
        // GIVEN
        Product product = new Product(null, 1, 10, "SEASONAL", "BERRY", null, LocalDate.now().plusDays(5), LocalDate.now().plusDays(15));

        // WHEN
        productService.handleSeasonalProduct(product);

        //THEN
        Mockito.verify(notificationService, Mockito.atMostOnce()).sendOutOfStockNotification("BERRY");
        Mockito.verify(productRepository, Mockito.never()).save(any(Product.class));
    }

    @Test
    public void test_handleSeasonalProduct_whenProductInSeason() {
        // GIVEN
        Product product = new Product(null, 1, 10, "SEASONAL", "BERRY", null, LocalDate.now().minusDays(5), LocalDate.now().plusDays(15));

        // WHEN
        productService.handleSeasonalProduct(product);

        //THEN
        Mockito.verify(notificationService, Mockito.atMostOnce()).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    // Tests for processPurchaseOrNotifyExpiry


    @Test
    void test_processPurchaseOrNotifyExpiry_whenProductIsNotExpired() {
        // GIVEN
        Product product = new Product(null, 1, 10, "EXPIRABLE", "CHEESE", LocalDate.now().plusDays(5), null, null);
        Product expectedProduct = new Product(null, 1, 9, "EXPIRABLE", "CHEESE", LocalDate.now().plusDays(5), null, null);

        // WHEN
        productService.processPurchaseOrNotifyExpiry(product);

        //THEN
        ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productRepository, Mockito.atMostOnce()).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedProduct);
    }

    @Test
    void test_processPurchaseOrNotifyExpiry_whenProductIsExpired() {
        // GIVEN
        LocalDate fiveDaysSooner = LocalDate.now().minusDays(5);
        Product product = new Product(null, 1, 10, "EXPIRABLE", "CHEESE", fiveDaysSooner, null, null);
        Product expectedProduct = new Product(null, 1, 0, "EXPIRABLE", "CHEESE", fiveDaysSooner, null, null);

        // WHEN
        productService.processPurchaseOrNotifyExpiry(product);

        //THEN
        ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(notificationService, Mockito.atMostOnce()).sendExpirationNotification("CHEESE", fiveDaysSooner);
        Mockito.verify(productRepository, Mockito.atMostOnce()).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedProduct);
    }
}