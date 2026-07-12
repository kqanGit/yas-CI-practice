package com.yas.order.viewmodel.order;

import com.yas.order.model.OrderItem;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemVmTest {

    @Test
    void fromModel_mapsFields() {
        OrderItem item = OrderItem.builder()
                .id(9L)
                .productId(123L)
                .productName("Name")
                .quantity(4)
                .productPrice(BigDecimal.valueOf(12.5))
                .note("n")
                .discountAmount(BigDecimal.valueOf(1))
                .taxAmount(BigDecimal.valueOf(0.5))
                .taxPercent(BigDecimal.valueOf(2))
                .orderId(77L)
                .build();

        OrderItemVm vm = OrderItemVm.fromModel(item);

        assertEquals(item.getId(), vm.id());
        assertEquals(item.getProductId(), vm.productId());
        assertEquals(item.getProductName(), vm.productName());
        assertEquals(item.getQuantity(), vm.quantity());
        assertEquals(item.getProductPrice(), vm.productPrice());
        assertEquals(item.getNote(), vm.note());
        assertEquals(item.getOrderId(), vm.orderId());
    }
}
