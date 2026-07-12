package com.yas.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.order.model.Order;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.viewmodel.order.OrderGetVm;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class OrderServiceMoreTest {

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
    void findOrderByCheckoutId_success_returnsOrder() {
        Order o = Order.builder().id(11L).checkoutId("cid-1").totalPrice(BigDecimal.ONE).build();
        when(orderRepository.findByCheckoutId("cid-1")).thenReturn(Optional.of(o));

        var got = orderService.findOrderByCheckoutId("cid-1");
        assertNotNull(got);
        assertEquals(11L, got.getId());
    }

    @Test
    void acceptOrder_setsAccepted() {
        Order o = Order.builder().id(22L).orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findById(22L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.acceptOrder(22L);

        assertEquals(OrderStatus.ACCEPTED, o.getOrderStatus());
        verify(orderRepository).save(o);
    }

    @Test
    void rejectOrder_setsRejectAndReason() {
        Order o = Order.builder().id(33L).orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findById(33L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.rejectOrder(33L, "bad stock");

        assertEquals(OrderStatus.REJECT, o.getOrderStatus());
        assertEquals("bad stock", o.getRejectReason());
        verify(orderRepository).save(o);
    }

    @Test
    void getMyOrders_returnsMappedList() {
        try (MockedStatic<com.yas.commonlibrary.utils.AuthenticationUtils> auth = Mockito.mockStatic(com.yas.commonlibrary.utils.AuthenticationUtils.class)) {
            auth.when(com.yas.commonlibrary.utils.AuthenticationUtils::extractUserId).thenReturn("user-x");

            Order o = Order.builder().id(44L).totalPrice(BigDecimal.TEN).build();
            when(orderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(o));

            var list = orderService.getMyOrders("", null);
            assertNotNull(list);
            assertFalse(list.isEmpty());
            OrderGetVm vm = list.get(0);
            assertEquals(44L, vm.id());
        }
    }
}
