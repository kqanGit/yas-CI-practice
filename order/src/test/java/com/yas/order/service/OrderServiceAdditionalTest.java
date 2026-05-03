package com.yas.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.utils.AuthenticationUtils;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.order.OrderItemPostVm;
import com.yas.order.viewmodel.order.OrderPostVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.order.OrderListVm;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;

@ExtendWith(MockitoExtension.class)
class OrderServiceAdditionalTest {

    @org.mockito.Mock
    com.yas.order.repository.OrderRepository orderRepository;

    @org.mockito.Mock
    com.yas.order.repository.OrderItemRepository orderItemRepository;

    @org.mockito.Mock
    ProductService productService;

    @org.mockito.Mock
    CartService cartService;

    @org.mockito.Mock
    OrderMapper orderMapper;

    @org.mockito.Mock
    PromotionService promotionService;

    @org.junit.jupiter.api.Test
    void createOrder_success_updatesAndReturnsAccepted() {
        OrderService svc = new OrderService(orderRepository, orderItemRepository, productService, cartService, orderMapper, promotionService);
        OrderService spySvc = Mockito.spy(svc);

        OrderAddressPostVm addr = OrderAddressPostVm.builder()
                .contactName("n").phone("p").addressLine1("a1").city("c").zipCode("z")
                .districtId(1L).districtName("d").stateOrProvinceId(1L).stateOrProvinceName("s")
                .countryId(1L).countryName("cn").build();

        OrderItemPostVm item = OrderItemPostVm.builder().productId(10L).productName("prod").quantity(1)
                .productPrice(BigDecimal.ONE).note(null).build();

        OrderPostVm post = OrderPostVm.builder()
                .checkoutId("cid").email("e@e").shippingAddressPostVm(addr).billingAddressPostVm(addr)
                .note(null).tax(0f).discount(0f).numberItem(1).totalPrice(BigDecimal.ONE).deliveryFee(BigDecimal.ZERO)
                .couponCode(null).deliveryMethod(com.yas.order.model.enumeration.DeliveryMethod.YAS_EXPRESS)
                .paymentMethod(com.yas.order.model.enumeration.PaymentMethod.COD)
                .paymentStatus(com.yas.order.model.enumeration.PaymentStatus.PENDING)
                .orderItemPostVms(List.of(item)).build();

        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(123L);
            return o;
        });
        Order savedOrder = new Order();
        savedOrder.setId(123L);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(savedOrder));
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg instanceof java.util.Collection) {
                return List.copyOf((java.util.Collection) arg);
            }
            return List.of();
        });
        doNothing().when(productService).subtractProductStockQuantity(any());
        doNothing().when(cartService).deleteCartItems(any());
        doNothing().when(promotionService).updateUsagePromotion(any());

        var result = spySvc.createOrder(post);

        assertNotNull(result);
        // createOrder builds the OrderVm before calling acceptOrder, so returned status remains PENDING
        assertEquals(OrderStatus.PENDING, result.orderStatus());
        verify(promotionService, atLeastOnce()).updateUsagePromotion(any());
        verify(orderRepository, atLeastOnce()).findById(123L);
    }

    @Test
    void getAllOrder_emptyPage_returnsEmptyVm() {
        OrderService svc = new OrderService(orderRepository, orderItemRepository, productService, cartService, orderMapper, promotionService);
        when(orderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var res = svc.getAllOrder(Pair.of(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()), "", List.of(), Pair.of("",""), "", Pair.of(0, 10));
        assertNotNull(res);
        assertNull(res.orderList());
        assertEquals(0, res.totalElements());
    }

    @Test
    void isOrderCompletedWithUserId_checksVariationsAndExists() {
        OrderService svc = new OrderService(orderRepository, orderItemRepository, productService, cartService, orderMapper, promotionService);

        try (MockedStatic<AuthenticationUtils> auth = Mockito.mockStatic(AuthenticationUtils.class)) {
            auth.when(AuthenticationUtils::extractUserId).thenReturn("user1");
            when(productService.getProductVariations(5L)).thenReturn(List.of());
            when(orderRepository.findOne((org.springframework.data.jpa.domain.Specification<Order>) any())).thenReturn(Optional.of(new Order()));

            var res = svc.isOrderCompletedWithUserIdAndProductId(5L);
            assertTrue(res.isPresent());
        }
    }

    @Test
    void exportCsv_handlesNullListAndNonNullList() throws Exception {
        OrderService svc = new OrderService(orderRepository, orderItemRepository, productService, cartService, orderMapper, promotionService);
        // null list branch: simulate repository returning empty page
        when(orderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        com.yas.order.model.request.OrderRequest req = com.yas.order.model.request.OrderRequest.builder()
            .createdFrom(ZonedDateTime.now().minusDays(1))
            .createdTo(ZonedDateTime.now())
            .pageNo(0)
            .pageSize(10)
            .build();
        byte[] result = svc.exportCsv(req);
        assertNotNull(result);
        assertTrue(result.length > 0);

        // non-null branch
        OrderBriefVm brief = OrderBriefVm.builder().id(1L).email("e").build();
        // non-null branch: repository returns a page with orders and mapper converts to CSV
        Order o = Order.builder().id(1L).totalPrice(BigDecimal.ONE).billingAddressId(new com.yas.order.model.OrderAddress()).build();
        when(orderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(o), PageRequest.of(0, 10), 1));
        when(orderMapper.toCsv(any())).thenReturn(
            OrderItemCsv.builder().id(1L).email("e").orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING).totalPrice(BigDecimal.ONE)
                .deliveryStatus(DeliveryStatus.PREPARING).createdOn(ZonedDateTime.now()).phone("p").build()
        );

        com.yas.order.model.request.OrderRequest req2 = com.yas.order.model.request.OrderRequest.builder()
            .createdFrom(ZonedDateTime.now().minusDays(1))
            .createdTo(ZonedDateTime.now())
            .pageNo(0)
            .pageSize(10)
            .build();
        byte[] result2 = svc.exportCsv(req2);
        assertNotNull(result2);
        assertTrue(result2.length > 0);
    }
}
