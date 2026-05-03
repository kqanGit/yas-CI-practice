package com.yas.order.viewmodel.order;

import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderBriefVmTest {

    @Test
    void fromModel_mapsFields() {
        OrderAddress bill = OrderAddress.builder().id(2L).contactName("Bill").build();
        Order order = Order.builder()
                .id(11L)
                .email("a@b.c")
                .billingAddressId(bill)
                .totalPrice(BigDecimal.valueOf(100))
                .orderStatus(OrderStatus.PAID)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .deliveryStatus(DeliveryStatus.READY)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        OrderBriefVm vm = OrderBriefVm.fromModel(order);

        assertEquals(order.getId(), vm.id());
        assertEquals(order.getEmail(), vm.email());
        assertNotNull(vm.billingAddressVm());
        assertEquals(order.getTotalPrice(), vm.totalPrice());
        assertEquals(order.getOrderStatus(), vm.orderStatus());
    }
}
