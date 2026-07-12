package com.yas.order.specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.utils.Constants;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderSpecificationAdditionalUnitTest {

    @SuppressWarnings("unchecked")
    @Test
    void withDateRange_nonNull_usesBetween() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<?> createdPath = mock(Path.class);
        Predicate betweenPred = mock(Predicate.class);

        when(root.get(Constants.Column.CREATE_ON_COLUMN)).thenReturn((Path) createdPath);
        ZonedDateTime from = ZonedDateTime.now().minusDays(1);
        ZonedDateTime to = ZonedDateTime.now();
        when(cb.between((Expression) createdPath, from, to)).thenReturn(betweenPred);

        var spec = OrderSpecification.withDateRange(from, to);
        Predicate p = spec.toPredicate((Root) root, query, cb);

        assertSame(betweenPred, p);
        verify(cb).between((Expression) createdPath, from, to);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withBillingPhoneNumber_and_country_callsLike() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<?> billingPath = mock(Path.class);
        Path<?> phonePath = mock(Path.class);
        Path<?> countryPath = mock(Path.class);
        Expression<String> loweredPhone = mock(Expression.class);
        Expression<String> loweredCountry = mock(Expression.class);
        Predicate phonePred = mock(Predicate.class);
        Predicate countryPred = mock(Predicate.class);

        when(root.get(Constants.Column.ORDER_BILLING_ADDRESS_ID_COLUMN)).thenReturn((Path) billingPath);
        when(billingPath.get(Constants.Column.ORDER_PHONE_COLUMN)).thenReturn((Path) phonePath);
        when(billingPath.get(Constants.Column.ORDER_COUNTRY_NAME_COLUMN)).thenReturn((Path) countryPath);

        when(cb.lower((Expression) phonePath)).thenReturn(loweredPhone);
        when(cb.like(loweredPhone, "%123%")).thenReturn(phonePred);
        when(cb.lower((Expression) countryPath)).thenReturn(loweredCountry);
        when(cb.like(loweredCountry, "%vn%")).thenReturn(countryPred);

        var specPhone = OrderSpecification.withBillingPhoneNumber("123");
        var p1 = specPhone.toPredicate((Root) root, query, cb);
        assertSame(phonePred, p1);

        var specCountry = OrderSpecification.withCountryName("vn");
        var p2 = specCountry.toPredicate((Root) root, query, cb);
        assertSame(countryPred, p2);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withOrderStatus_nonEmpty_usesIn() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<?> statusPath = mock(Path.class);
        Predicate inPred = mock(Predicate.class);

        when(root.get(Constants.Column.ORDER_ORDER_STATUS_COLUMN)).thenReturn((Path) statusPath);
        when(statusPath.in(List.of(OrderStatus.COMPLETED))).thenReturn(inPred);

        var spec = OrderSpecification.withOrderStatus(List.of(OrderStatus.COMPLETED));
        var p = spec.toPredicate((Root) root, query, cb);
        assertSame(inPred, p);
    }

    @SuppressWarnings("unchecked")
    @Test
    void findOrderByWithMulCriteria_fetchesWhenResultNotLong() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Fetch<?, ?> f1 = mock(Fetch.class);
        Fetch<?, ?> f2 = mock(Fetch.class);

        when(query.getResultType()).thenReturn((Class) Object.class);
        when(root.fetch(Constants.Column.ORDER_SHIPPING_ADDRESS_ID_COLUMN, jakarta.persistence.criteria.JoinType.LEFT))
            .thenReturn((Fetch) f1);
        when(root.fetch(Constants.Column.ORDER_BILLING_ADDRESS_ID_COLUMN, jakarta.persistence.criteria.JoinType.LEFT))
            .thenReturn((Fetch) f2);

        Predicate combined = mock(Predicate.class);
        doReturn(combined).when(cb).and((Predicate) any(), (Predicate) any(), (Predicate) any(), (Predicate) any(), (Predicate) any(), (Predicate) any());

        var spec = OrderSpecification.findOrderByWithMulCriteria(List.of(), null, null, null, null, null, null);
        var p = spec.toPredicate((Root) root, query, cb);

        // predicate is composed via cb.and; at minimum it should not be null
        assertNotNull(p);
    }
}
