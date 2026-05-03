package com.yas.order.viewmodel.order;

import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderVmTest {

    @Test
    void fromModel_mapsAllFields() {
        OrderAddress ship = OrderAddress.builder().id(10L).contactName("Shipper").build();
        OrderAddress bill = OrderAddress.builder().id(11L).contactName("Biller").build();

        OrderItem item = OrderItem.builder()
                .id(5L)
                .productId(100L)
                .productName("Product A")
                .quantity(2)
                .productPrice(BigDecimal.valueOf(199.99))
                .note("note")
                .orderId(1L)
                .build();

        Set<OrderItem> items = new HashSet<>();
        items.add(item);

        Order order = Order.builder()
                .id(1L)
                .email("test@example.com")
                .shippingAddressId(ship)
                .billingAddressId(bill)
                .note("note")
                .tax(1.5f)
                .discount(2.0f)
                .numberItem(3)
                .totalPrice(BigDecimal.valueOf(399.98))
                .couponCode("CODE123")
                .orderStatus(OrderStatus.PENDING)
                .deliveryFee(BigDecimal.valueOf(5))
            .deliveryMethod(DeliveryMethod.YAS_EXPRESS)
                .deliveryStatus(DeliveryStatus.PREPARING)
                .paymentStatus(PaymentStatus.PENDING)
                .checkoutId("chk-1")
                .build();

        OrderVm vm = OrderVm.fromModel(order, items);

        assertEquals(order.getId(), vm.id());
        assertEquals(order.getEmail(), vm.email());
        assertNotNull(vm.shippingAddressVm());
        assertNotNull(vm.billingAddressVm());
        assertEquals(order.getNote(), vm.note());
        assertEquals(order.getNumberItem(), vm.numberItem());
        assertEquals(order.getTotalPrice(), vm.totalPrice());
        assertEquals(order.getOrderStatus(), vm.orderStatus());
        assertEquals(order.getCheckoutId(), vm.checkoutId());
        assertNotNull(vm.orderItemVms());
        assertEquals(1, vm.orderItemVms().size());
    }
}
