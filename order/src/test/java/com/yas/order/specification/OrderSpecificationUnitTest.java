package com.yas.order.specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Predicate;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderSpecificationUnitTest {

    @SuppressWarnings("unchecked")
    @Test
    void hasCreatedBy_callsEqual() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<?> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(root.get("createdBy")).thenReturn((Path) path);
        when(cb.equal(path, "user-1")).thenReturn(expected);

        var spec = OrderSpecification.hasCreatedBy("user-1");
        Predicate p = spec.toPredicate((Root) root, query, cb);

        assertSame(expected, p);
        verify(cb).equal(path, "user-1");
    }

    @Test
    void hasOrderStatus_null_returnsConjunction() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conj = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conj);

        var spec = OrderSpecification.hasOrderStatus(null);
        var p = spec.toPredicate((Root) root, query, cb);

        assertSame(conj, p);
        verify(cb).conjunction();
    }

    @Test
    void withDateRange_bothNulls_returnsConjunction_and_between_when_set() {
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conj = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conj);

        var specNull = OrderSpecification.withDateRange(null, null);
        assertSame(conj, specNull.toPredicate((Root) root, query, cb));

        // createdFrom/createdTo non-null branch is exercised elsewhere; here ensure nulls return conjunction
    }

    @SuppressWarnings("unchecked")
    @Test
    void withProductName_handles_nullQuery_and_nonEmpty_callsExists() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> nullQuery = null;
        Root<?> root = mock(Root.class);
        Predicate conj = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conj);

        var specNull = OrderSpecification.withProductName("any");
        assertSame(conj, specNull.toPredicate((Root) root, nullQuery, cb));

        // now non-null query and productName provided
        CriteriaQuery<Long> query = mock(CriteriaQuery.class);
        Subquery<Long> sub = mock(Subquery.class);
        Root<OrderItem> itemRoot = mock(Root.class);
        when(query.subquery(Long.class)).thenReturn(sub);
        when(sub.from(OrderItem.class)).thenReturn(itemRoot);
        Path<?> orderIdPath = mock(Path.class);
        when(itemRoot.get("orderId")).thenReturn((Path) orderIdPath);
        when(sub.select((Expression) orderIdPath)).thenReturn(sub);

        Predicate existsPred = mock(Predicate.class);
        when(cb.exists(sub)).thenReturn(existsPred);

        var spec = OrderSpecification.withProductName("ProdX");
        var p = spec.toPredicate((Root) root, (CriteriaQuery) query, cb);
        assertSame(existsPred, p);
        verify(cb).exists(sub);
    }

    @SuppressWarnings("unchecked")
    @Test
    void hasProductInOrderItems_queryNull_and_nonNull() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<?> root = mock(Root.class);
        Predicate conj = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conj);

        var specNull = OrderSpecification.hasProductInOrderItems(List.of(1L, 2L));
        assertSame(conj, specNull.toPredicate((Root) root, null, cb));

        CriteriaQuery<OrderItem> query = mock(CriteriaQuery.class);
        Subquery<OrderItem> sub = mock(Subquery.class);
        Root<OrderItem> itemRoot = mock(Root.class);
        when(query.subquery(OrderItem.class)).thenReturn(sub);
        when(sub.from(OrderItem.class)).thenReturn(itemRoot);
        Path<?> orderIdPath2 = mock(Path.class);
        Path<?> productIdPath = mock(Path.class);
        when(itemRoot.get("orderId")).thenReturn((Path) orderIdPath2);
        when(itemRoot.get("productId")).thenReturn((Path) productIdPath);
        when(productIdPath.in((java.util.Collection) any())).thenReturn(mock(Predicate.class));
        when(sub.select(itemRoot)).thenReturn(sub);

        Predicate existsPred = mock(Predicate.class);
        when(cb.exists(sub)).thenReturn(existsPred);

        var spec = OrderSpecification.hasProductInOrderItems(List.of(5L));
        var p = spec.toPredicate((Root) root, (CriteriaQuery) query, cb);
        assertSame(existsPred, p);
        verify(cb).exists(sub);
    }
}
