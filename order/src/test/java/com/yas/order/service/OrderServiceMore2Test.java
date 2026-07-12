package com.yas.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.utils.AuthenticationUtils;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.viewmodel.order.OrderGetVm;
import com.yas.order.viewmodel.order.PaymentOrderStatusVm;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceMore2Test {

    @Mock
    com.yas.order.repository.OrderRepository orderRepository;

    @Mock
    com.yas.order.repository.OrderItemRepository orderItemRepository;

    @Mock
    ProductService productService;

    @Mock
    CartService cartService;

    @Mock
    com.yas.order.mapper.OrderMapper orderMapper;

    @Mock
    PromotionService promotionService;

    @InjectMocks
    OrderService orderService;

    @Test
    void updateOrderPaymentStatus_setsPaid_whenCompleted() {
        Order o = Order.builder().id(101L).orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findById(101L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder().orderId(101L).paymentId(999L).paymentStatus("COMPLETED").build();
        var res = orderService.updateOrderPaymentStatus(vm);

        assertEquals(OrderStatus.PAID, o.getOrderStatus());
        assertEquals(101L, res.orderId());
    }

    @Test
    void updateOrderPaymentStatus_notCompleted_keepsStatus() {
        Order o = Order.builder().id(102L).orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findById(102L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder().orderId(102L).paymentId(5L).paymentStatus("CANCELLED").build();
        var res = orderService.updateOrderPaymentStatus(vm);

        assertNotEquals(OrderStatus.PAID, o.getOrderStatus());
        assertEquals(102L, res.orderId());
    }

    @Test
    void getLatestOrders_handlesZeroAndNonZero() {
        assertTrue(orderService.getLatestOrders(0).isEmpty());

        com.yas.order.model.OrderAddress addr = com.yas.order.model.OrderAddress.builder().id(7L).build();
        Order o = Order.builder().id(201L).totalPrice(BigDecimal.TEN).shippingAddressId(addr).billingAddressId(addr).build();
        when(orderRepository.getLatestOrders(any())).thenReturn(List.of(o));

        var list = orderService.getLatestOrders(1);
        assertFalse(list.isEmpty());
        assertEquals(201L, list.get(0).id());
    }

    @Test
    void getOrderWithItemsById_and_findOrderVmByCheckoutId() {
        com.yas.order.model.OrderAddress addr2 = com.yas.order.model.OrderAddress.builder().id(9L).build();
        Order o = Order.builder().id(301L).checkoutId("cid-301").shippingAddressId(addr2).billingAddressId(addr2).build();
        when(orderRepository.findById(301L)).thenReturn(Optional.of(o));
        when(orderItemRepository.findAllByOrderId(301L)).thenReturn(List.of(OrderItem.builder().id(1L).orderId(301L).build()));

        var vm = orderService.getOrderWithItemsById(301L);
        assertEquals(301L, vm.id());

        when(orderRepository.findByCheckoutId("cid-301")).thenReturn(Optional.of(o));
        var vm2 = orderService.findOrderVmByCheckoutId("cid-301");
        assertEquals(301L, vm2.id());
    }

    @Test
    void isOrderCompletedWithUserIdAndProductId_checksVariationsAndRepo() {
        try (MockedStatic<AuthenticationUtils> auth = Mockito.mockStatic(AuthenticationUtils.class)) {
            auth.when(AuthenticationUtils::extractUserId).thenReturn("user-123");

            when(productService.getProductVariations(55L)).thenReturn(List.of());
            when(orderRepository.findOne((org.springframework.data.jpa.domain.Specification<Order>) any()))
                .thenReturn(Optional.of(Order.builder().id(88L).build()));

            var res = orderService.isOrderCompletedWithUserIdAndProductId(55L);
            assertTrue(res.isPresent());
        }
    }
}
