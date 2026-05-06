package com.yas.product.viewmodel.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yas.product.model.Brand;
import com.yas.product.model.Product;
import org.junit.jupiter.api.Test;

class ProductViewModelTest {

    @Test
    void productListVm_usesParentIdWhenPresent() {
        Product parent = new Product();
        parent.setId(1L);

        Product child = new Product();
        child.setId(2L);
        child.setName("Child");
        child.setSlug("child");
        child.setAllowedToOrder(true);
        child.setPublished(true);
        child.setFeatured(false);
        child.setVisibleIndividually(true);
        child.setParent(parent);

        ProductListVm vm = ProductListVm.fromModel(child);

        assertEquals(1L, vm.parentId());
    }

    @Test
    void productCheckoutListVm_usesBrandAndParent() {
        Brand brand = new Brand();
        brand.setId(5L);

        Product parent = new Product();
        parent.setId(7L);

        Product product = new Product();
        product.setId(8L);
        product.setName("Product");
        product.setDescription("Desc");
        product.setShortDescription("Short");
        product.setSku("SKU");
        product.setBrand(brand);
        product.setParent(parent);

        ProductCheckoutListVm vm = ProductCheckoutListVm.fromModel(product);

        assertEquals(5L, vm.brandId());
        assertEquals(7L, vm.parentId());
        assertEquals("", vm.thumbnailUrl());
    }
}
