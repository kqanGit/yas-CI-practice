package com.yas.product.viewmodel.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductVariationVmTest {

    @Test
    void productVariationPostVm_idIsNull() {
        ProductVariationPostVm vm = new ProductVariationPostVm(
                "Name",
                "slug",
                "sku",
                "gtin",
                10.0,
                1L,
                List.of(2L, 3L),
                Map.of(5L, "red")
        );

        assertNull(vm.id());
    }

    @Test
    void productVariationPutVm_keepsId() {
        ProductVariationPutVm vm = new ProductVariationPutVm(
                9L,
                "Name",
                "slug",
                "sku",
                "gtin",
                12.0,
                2L,
                List.of(),
                Map.of()
        );

        assertEquals(9L, vm.id());
    }
}
