package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.services.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class OrderServiceImplTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;


    @Test
    public void test_purchaseExpirableProduct_success() {
        // GIVEN
        LocalDate fiveDaysLater = LocalDate.now().plusDays(5);
        Product product = new Product(null, 15, 1, "EXPIRABLE", "CHEESE", fiveDaysLater, null, null);
        Product expectedProduct = new Product(null, 15, 0, "EXPIRABLE", "CHEESE", fiveDaysLater, null, null);

        // WHEN
        orderService.purchaseExpirableProduct(product);

        //THEN
        ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productService, Mockito.never()).processPurchaseOrNotifyExpiry(any(Product.class));
        Mockito.verify(productService, Mockito.atMostOnce()).saveProduct(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedProduct);
    }


    @ParameterizedTest
    @CsvSource({"0, 5", "1, 0"})
    // Testing both when  : not available but before expire date, available but after expire date
    public void test_purchaseExpirableProduct_fail(int available, int daysAfter) {
        // GIVEN
        LocalDate fiveDaysLater = LocalDate.now().plusDays(daysAfter);
        Product product = new Product(null, 15, available, "EXPIRABLE", "CHEESE", fiveDaysLater, null, null);

        // WHEN
        orderService.purchaseExpirableProduct(product);

        //THEN
        Mockito.verify(productService, Mockito.atMostOnce()).processPurchaseOrNotifyExpiry(any(Product.class));
    }


    @Test
    public void test_purchaseNormalProduct_success() {
        // GIVEN
        Product product = new Product(null, 15, 1, "NORMAL", "CHEESE", null, null, null);
        Product expectedProduct = new Product(null, 15, 0, "NORMAL", "CHEESE", null, null, null);

        // WHEN
        orderService.purchaseNormalProduct(product);

        //THEN
        ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productService, Mockito.never()).notifyDelay(anyInt(), any(Product.class));
        Mockito.verify(productService, Mockito.atMostOnce()).saveProduct(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedProduct);
    }

    @ParameterizedTest
    @CsvSource({"true, 1", "false, 0"})
    public void test_purchaseNormalProduct_fail(boolean shouldNotify, int leadTime) {
        // GIVEN
        Product product = new Product(null, leadTime, 0, "NORMAL", "CHEESE", null, null, null);

        // WHEN
        orderService.purchaseNormalProduct(product);

        //THEN
        ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productService, shouldNotify ? Mockito.atMostOnce() : Mockito.never()).notifyDelay(eq(leadTime), any(Product.class));
        Mockito.verify(productService, Mockito.never()).saveProduct(argumentCaptor.capture());
    }

    // TO BE CONTINUED ... its 22:00 game is game

}