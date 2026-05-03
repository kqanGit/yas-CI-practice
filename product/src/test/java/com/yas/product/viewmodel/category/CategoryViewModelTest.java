package com.yas.product.viewmodel.category;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yas.product.model.Category;
import org.junit.jupiter.api.Test;

class CategoryViewModelTest {

    @Test
    void categoryGetVm_parentIdDefaultsToMinusOne() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Root");
        category.setSlug("root");

        CategoryGetVm vm = CategoryGetVm.fromModel(category);

        assertEquals(-1L, vm.parentId());
    }

    @Test
    void categoryGetDetailVm_parentIdUsesParentOrZero() {
        Category parent = new Category();
        parent.setId(99L);

        Category child = new Category();
        child.setId(100L);
        child.setName("Child");
        child.setSlug("child");
        child.setParent(parent);

        CategoryGetDetailVm withParent = CategoryGetDetailVm.fromModel(child);
        assertEquals(99L, withParent.parentId());

        Category root = new Category();
        root.setId(101L);
        root.setName("Root");
        root.setSlug("root");

        CategoryGetDetailVm noParent = CategoryGetDetailVm.fromModel(root);
        assertEquals(0L, noParent.parentId());
    }
}
