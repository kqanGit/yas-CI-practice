package com.yas.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.viewmodel.order.PaymentOrderStatusVm;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    com.yas.order.repository.OrderRepository orderRepository;

    @Mock
    com.yas.order.repository.OrderItemRepository orderItemRepository;

    @Mock
    ProductService productService;

    @Mock
    CartService cartService;

    @Mock
    OrderMapper orderMapper;

    @Mock
    PromotionService promotionService;

    @InjectMocks
    OrderService orderService;

    @Test
    void getLatestOrders_countZero_returnsEmpty() {
        List<com.yas.order.model.Order> res = orderService.getLatestOrders(0);
        assertTrue(res.isEmpty());
    }

    @Test
    void getLatestOrders_withData_returnsMappedList() {
        Order o = Order.builder().id(7L).totalPrice(BigDecimal.TEN).build();
        when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(List.of(o));

        var list = orderService.getLatestOrders(1);
        assertEquals(1, list.size());
        assertEquals(o.getId(), list.get(0).id());
    }

    @Test
    void updateOrderPaymentStatus_orderNotFound_throws() {
        when(orderRepository.findById(100L)).thenReturn(java.util.Optional.empty());
        PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder().orderId(100L).paymentId(10L).paymentStatus("COMPLETED").build();
        assertThrows(NotFoundException.class, () -> orderService.updateOrderPaymentStatus(vm));
    }

    @Test
    void updateOrderPaymentStatus_completed_setsPaid() {
        Order order = Order.builder().id(200L).orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findById(200L)).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrderStatusVm vm = PaymentOrderStatusVm.builder().orderId(200L).paymentId(55L).paymentStatus("COMPLETED").build();
        var result = orderService.updateOrderPaymentStatus(vm);

        assertEquals("PAID", result.orderStatus());
        assertEquals(55L, result.paymentId());
    }

    @Test
    void getOrderWithItemsById_notFound_throws() {
        when(orderRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrderWithItemsById(999L));
    }

    @Test
    void findOrderByCheckoutId_notFound_throws() {
        when(orderRepository.findByCheckoutId("nope")).thenReturn(java.util.Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.findOrderByCheckoutId("nope"));
    }
}
