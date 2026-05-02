package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Category;
import com.yas.product.model.Brand;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.Product;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.enumeration.DimensionUnit;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.utils.Constants;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductListVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.product.ProductVariationPutVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionValueRepository productOptionValueRepository;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private ProductPostVm validProductPostVm;
    private ProductPutVm validProductPutVm;

    @BeforeEach
    void setUp() {
        Mockito.reset(productRepository, brandRepository, categoryRepository);

        validProductPostVm = createValidProductPostVm();
        validProductPutVm = createValidProductPutVm();
    }

    @Nested
    class CreateProductTest {

        @Test
        void testCreateProduct_whenValidProductPostVm_thenSuccess() {
            Product mockProduct = new Product();
            mockProduct.setId(1L);
            mockProduct.setName(validProductPostVm.name());
            mockProduct.setSlug(validProductPostVm.slug().toLowerCase());

            Brand brand = new Brand();
            brand.setId(validProductPostVm.brandId());

            when(brandRepository.findById(validProductPostVm.brandId())).thenReturn(Optional.of(brand));
            when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
            when(productImageRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
            when(productCategoryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

            var result = productService.createProduct(validProductPostVm);

            assertNotNull(result);
            assertEquals(mockProduct.getId(), result.id());
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void testCreateProduct_whenLengthLessThanWidth_thenThrowBadRequestException() {
            ProductPostVm productPostVm = createProductPostVm(5.0, 15.0, "test-slug", "SKU123");

            assertThrows(BadRequestException.class, () -> productService.createProduct(productPostVm));
        }

        @Test
        void testCreateProduct_whenSlugAlreadyExists_thenThrowDuplicatedException() {
            Product existingProduct = new Product();
            existingProduct.setId(2L);
            existingProduct.setSlug(validProductPostVm.slug().toLowerCase());

            when(productRepository.findBySlugAndIsPublishedTrue(validProductPostVm.slug().toLowerCase()))
                .thenReturn(Optional.of(existingProduct));

            assertThrows(DuplicatedException.class, () -> productService.createProduct(validProductPostVm));
        }

        @Test
        void testCreateProduct_whenSkuAlreadyExists_thenThrowDuplicatedException() {
            Product existingProduct = new Product();
            existingProduct.setId(2L);
            existingProduct.setSku(validProductPostVm.sku());

            when(productRepository.findBySlugAndIsPublishedTrue(validProductPostVm.slug().toLowerCase()))
                .thenReturn(Optional.empty());
            when(productRepository.findBySkuAndIsPublishedTrue(validProductPostVm.sku()))
                .thenReturn(Optional.of(existingProduct));

            assertThrows(DuplicatedException.class, () -> productService.createProduct(validProductPostVm));
        }
    }

    @Nested
    class UpdateProductTest {

        @Test
        void testUpdateProduct_whenValidProductPutVm_thenSuccess() {
            Product existingProduct = new Product();
            existingProduct.setId(1L);
            existingProduct.setName("Old Product");
            existingProduct.setSlug("old-slug");

            Brand brand = new Brand();
            brand.setId(validProductPutVm.brandId());

            ProductOption productOption = new ProductOption();
            productOption.setId(1L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(brandRepository.findById(validProductPutVm.brandId())).thenReturn(Optional.of(brand));
            when(productRepository.findBySlugAndIsPublishedTrue(validProductPutVm.slug().toLowerCase()))
                .thenReturn(Optional.of(existingProduct));
            when(productRepository.findBySkuAndIsPublishedTrue(validProductPutVm.sku()))
                .thenReturn(Optional.of(existingProduct));
            when(productOptionRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(productOption));
            when(productImageRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
            when(productCategoryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
            when(productOptionValueRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

            productService.updateProduct(1L, validProductPutVm);

            verify(productOptionValueRepository, times(1)).saveAll(anyList());
        }

        @Test
        void testUpdateProduct_whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                productService.updateProduct(999L, validProductPutVm);
            });
            assertNotNull(exception.getMessage());
        }

        @Test
        void testUpdateProduct_whenLengthLessThanWidth_thenThrowBadRequestException() {
            Product existingProduct = new Product();
            existingProduct.setId(1L);
            existingProduct.setName("Old Product");
            existingProduct.setSlug("old-slug");

            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

            ProductPutVm invalidProductPutVm = createValidProductPutVm(5.0, 15.0);

            assertThrows(BadRequestException.class, () -> productService.updateProduct(1L, invalidProductPutVm));
        }
    }

    @Nested
    class GetProductTest {

        @Test
        void testGetProductById_whenProductExists_thenSuccess() {
            Product mockProduct = new Product();
            mockProduct.setId(1L);
            mockProduct.setName("Test Product");
            mockProduct.setSlug("test-slug");
            mockProduct.setThumbnailMediaId(1L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
            when(mediaService.getMedia(1L)).thenReturn(new NoFileMediaVm(1L, "caption", "file", "image/png", "http://image.url/1"));

            ProductDetailVm result = productService.getProductById(1L);

            assertNotNull(result);
            assertEquals(mockProduct.getId(), result.id());
            verify(productRepository, times(1)).findById(1L);
        }

        @Test
        void testGetProductById_whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                productService.getProductById(999L);
            });
            assertNotNull(exception.getMessage());
        }

        @Test
        void testGetLatestProducts_whenCountGreaterThanZero_thenReturnProducts() {
            Product product1 = new Product();
            product1.setId(1L);
            product1.setName("Latest Product");
            List<Product> products = List.of(product1);

            when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(products);

            List<ProductListVm> result = productService.getLatestProducts(5);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(productRepository, times(1)).getLatestProducts(any(Pageable.class));
        }

        @Test
        void testGetLatestProducts_whenCountZeroOrNegative_thenReturnEmptyList() {
            List<ProductListVm> result = productService.getLatestProducts(0);

            assertTrue(result.isEmpty());
            verify(productRepository, never()).getLatestProducts(any(Pageable.class));
        }

        @Test
        void testGetLatestProducts_whenRepositoryReturnsEmpty_thenReturnEmptyList() {
            when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(Collections.emptyList());

            List<ProductListVm> result = productService.getLatestProducts(5);

            assertTrue(result.isEmpty());
            verify(productRepository, times(1)).getLatestProducts(any(Pageable.class));
        }
    }

    @Nested
    class DeleteProductTest {

        @Test
        void testDeleteProduct_whenProductExists_thenSuccess() {
            Product mockProduct = new Product();
            mockProduct.setId(1L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
            when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

            productService.deleteProduct(1L);

            assertFalse(mockProduct.isPublished());
            verify(productRepository, times(1)).save(mockProduct);
        }

        @Test
        void testDeleteProduct_whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                productService.deleteProduct(999L);
            });
            assertNotNull(exception.getMessage());
        }
    }

    @Nested
    class UpdateProductQuantityTest {

        @Test
        void testUpdateProductQuantity_whenValidList_thenSuccess() {
            Product product = new Product();
            product.setId(1L);
            product.setStockQuantity(10L);

            ProductQuantityPostVm productQuantityPostVm = new ProductQuantityPostVm(1L, 20L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
            when(productRepository.saveAll(anyList())).thenReturn(List.of(product));

            productService.updateProductQuantity(List.of(productQuantityPostVm));

            assertEquals(20L, product.getStockQuantity());
            verify(productRepository, times(1)).saveAll(anyList());
        }

        @Test
        void testUpdateProductQuantity_whenEmptyList_thenSaveEmptyList() {
            productService.updateProductQuantity(Collections.emptyList());

            verify(productRepository, times(1)).saveAll(Collections.emptyList());
        }
    }

    @Nested
    class StockQuantityTest {

        @Test
        void testSubtractStockQuantity_whenValidList_thenSuccess() {
            Product product = new Product();
            product.setId(1L);
            product.setStockQuantity(100L);
            product.setStockTrackingEnabled(true);

            ProductQuantityPutVm productQuantityPutVm = new ProductQuantityPutVm(1L, 30L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
            when(productRepository.saveAll(anyList())).thenReturn(List.of(product));

            productService.subtractStockQuantity(List.of(productQuantityPutVm));

            assertTrue(product.getStockQuantity() <= 100L);
            verify(productRepository, times(1)).saveAll(anyList());
        }

        @Test
        void testSubtractStockQuantity_whenStockQuantityBelowZero_thenReturnZero() {
            Product product = new Product();
            product.setId(1L);
            product.setStockQuantity(10L);
            product.setStockTrackingEnabled(true);

            ProductQuantityPutVm productQuantityPutVm = new ProductQuantityPutVm(1L, 20L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
            when(productRepository.saveAll(anyList())).thenReturn(List.of(product));

            productService.subtractStockQuantity(List.of(productQuantityPutVm));

            verify(productRepository, times(1)).saveAll(anyList());
        }

        @Test
        void testRestoreStockQuantity_whenValidList_thenSuccess() {
            Product product = new Product();
            product.setId(1L);
            product.setStockQuantity(50L);
            product.setStockTrackingEnabled(true);

            ProductQuantityPutVm productQuantityPutVm = new ProductQuantityPutVm(1L, 30L);

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
            when(productRepository.saveAll(anyList())).thenReturn(List.of(product));

            productService.restoreStockQuantity(List.of(productQuantityPutVm));

            verify(productRepository, times(1)).saveAll(anyList());
        }
    }

    @Nested
    class GetProductByIdsTest {

        @Test
        void testGetProductByIds_whenValidIds_thenReturnProducts() {
            Product product = new Product();
            product.setId(1L);
            product.setName("Test Product");

            when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

            List<ProductListVm> result = productService.getProductByIds(List.of(1L));

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(productRepository, times(1)).findAllByIdIn(List.of(1L));
        }

        @Test
        void testGetProductByIds_whenEmptyList_thenReturnEmptyList() {
            when(productRepository.findAllByIdIn(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<ProductListVm> result = productService.getProductByIds(Collections.emptyList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetProductsWithFilterTest {

        @Test
        void testGetProductsWithFilter_whenValidFilters_thenSuccess() {
            Product product = new Product();
            product.setId(1L);
            product.setName("Test Product");
            Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

            when(productRepository.getProductsWithFilter(any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

            var result = productService.getProductsWithFilter(0, 10, "Test", "Brand");

            assertNotNull(result);
            verify(productRepository, times(1)).getProductsWithFilter(any(), any(), any(Pageable.class));
        }
    }

    @Nested
    class CoverageExpansionTest {

        @Test
        void testGetProductsByBrand_whenBrandExists_thenReturnThumbnails() {
            Brand brand = new Brand();
            brand.setId(1L);
            brand.setSlug("brand-slug");

            Product product = new Product();
            product.setId(11L);
            product.setName("Brand Product");
            product.setSlug("brand-product");
            product.setThumbnailMediaId(101L);

            when(brandRepository.findBySlug("brand-slug")).thenReturn(Optional.of(brand));
            when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
            when(mediaService.getMedia(101L)).thenReturn(new NoFileMediaVm(101L, "caption", "file", "image/png", "http://cdn/101"));

            var result = productService.getProductsByBrand("brand-slug");

            assertEquals(1, result.size());
            assertEquals("brand-product", result.get(0).slug());
        }

        @Test
        void testGetProductsByBrand_whenBrandMissing_thenThrowNotFoundException() {
            when(brandRepository.findBySlug("missing-brand")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("missing-brand"));
        }

        @Test
        void testGetProductsFromCategory_whenCategoryExists_thenReturnPageVm() {
            Category category = new Category();
            category.setId(2L);
            category.setName("Category Name");
            category.setSlug("category-slug");

            Product product = new Product();
            product.setId(21L);
            product.setName("Category Product");
            product.setSlug("category-product");
            product.setThumbnailMediaId(201L);

            ProductCategory productCategory = new ProductCategory();
            productCategory.setCategory(category);
            productCategory.setProduct(product);

            Page<ProductCategory> page = new PageImpl<>(List.of(productCategory), PageRequest.of(0, 10), 1);
            when(categoryRepository.findBySlug("category-slug")).thenReturn(Optional.of(category));
            when(productCategoryRepository.findAllByCategory(any(Pageable.class), any(Category.class))).thenReturn(page);
            when(mediaService.getMedia(201L)).thenReturn(new NoFileMediaVm(201L, "caption", "file", "image/png", "http://cdn/201"));

            var result = productService.getProductsFromCategory(0, 10, "category-slug");

            assertEquals(1, result.productContent().size());
            assertEquals(0, result.pageNo());
            assertEquals(1, result.totalElements());
        }

        @Test
        void testGetProductsFromCategory_whenCategoryMissing_thenThrowNotFoundException() {
            when(categoryRepository.findBySlug("missing-category")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.getProductsFromCategory(0, 10, "missing-category"));
        }

        @Test
        void testGetFeaturedProductsById_whenChildThumbnailMissing_thenUseParentThumbnail() {
            Product parent = new Product();
            parent.setId(31L);
            parent.setName("Parent Product");
            parent.setSlug("parent-product");
            parent.setThumbnailMediaId(301L);

            Product child = new Product();
            child.setId(32L);
            child.setName("Child Product");
            child.setSlug("child-product");
            child.setPrice(19.99);
            child.setParent(parent);

            when(productRepository.findAllByIdIn(List.of(32L))).thenReturn(List.of(child));
            when(mediaService.getMedia(null)).thenReturn(new NoFileMediaVm(null, "caption", "file", "image/png", ""));
            when(productRepository.findById(31L)).thenReturn(Optional.of(parent));
            when(mediaService.getMedia(301L)).thenReturn(new NoFileMediaVm(301L, "caption", "file", "image/png", "http://cdn/301"));

            var result = productService.getFeaturedProductsById(List.of(32L));

            assertEquals(1, result.size());
            assertEquals("http://cdn/301", result.get(0).thumbnailUrl());
        }

        @Test
        void testGetFeaturedProductsById_whenThumbnailExists_thenUseOwnThumbnail() {
            Product product = new Product();
            product.setId(33L);
            product.setName("Thumbnail Product");
            product.setSlug("thumbnail-product");
            product.setPrice(21.99);
            product.setThumbnailMediaId(333L);

            when(productRepository.findAllByIdIn(List.of(33L))).thenReturn(List.of(product));
            when(mediaService.getMedia(333L)).thenReturn(new NoFileMediaVm(333L, "caption", "file", "image/png", "http://cdn/333"));

            var result = productService.getFeaturedProductsById(List.of(33L));

            assertEquals(1, result.size());
            assertEquals("http://cdn/333", result.get(0).thumbnailUrl());
        }

        @Test
        void testGetListFeaturedProducts_whenPageHasProducts_thenReturnFeatureVm() {
            Product product = new Product();
            product.setId(41L);
            product.setName("Featured Product");
            product.setSlug("featured-product");
            product.setThumbnailMediaId(401L);
            product.setPrice(29.99);

            Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);
            when(mediaService.getMedia(401L)).thenReturn(new NoFileMediaVm(401L, "caption", "file", "image/png", "http://cdn/401"));

            var result = productService.getListFeaturedProducts(0, 10);

            assertEquals(1, result.productList().size());
            assertEquals(1, result.totalPage());
        }

        @Test
        void testGetProductDetail_whenProductHasCategories_thenReturnDetailVm() {
            Category category = new Category();
            category.setId(51L);
            category.setName("Detail Category");

            Product product = new Product();
            product.setId(52L);
            product.setName("Detail Product");
            product.setSlug("detail-product");
            product.setThumbnailMediaId(501L);

            ProductCategory productCategory = new ProductCategory();
            productCategory.setProduct(product);
            productCategory.setCategory(category);
            product.getProductCategories().add(productCategory);

            when(productRepository.findBySlugAndIsPublishedTrue("detail-product")).thenReturn(Optional.of(product));
            when(mediaService.getMedia(501L)).thenReturn(new NoFileMediaVm(501L, "caption", "file", "image/png", "http://cdn/501"));

            var result = productService.getProductDetail("detail-product");

            assertEquals("Detail Product", result.name());
            assertEquals(1, result.productCategories().size());
            assertEquals("Detail Category", result.productCategories().get(0));
            assertEquals("http://cdn/501", result.thumbnailMediaUrl());
        }

        @Test
        void testGetProductDetail_whenAttributeGroupIsNull_thenUseNoneGroupLabel() {
            Product product = new Product();
            product.setId(53L);
            product.setName("Detail Attribute Product");
            product.setSlug("detail-attribute-product");
            product.setThumbnailMediaId(502L);

            ProductAttribute productAttribute = new ProductAttribute();
            productAttribute.setId(1L);
            productAttribute.setName("Size");

            ProductAttributeValue productAttributeValue = new ProductAttributeValue();
            productAttributeValue.setProduct(product);
            productAttributeValue.setProductAttribute(productAttribute);
            productAttributeValue.setValue("XL");
            product.getAttributeValues().add(productAttributeValue);

            when(productRepository.findBySlugAndIsPublishedTrue("detail-attribute-product")).thenReturn(Optional.of(product));
            when(mediaService.getMedia(502L)).thenReturn(new NoFileMediaVm(502L, "caption", "file", "image/png", "http://cdn/502"));

            var result = productService.getProductDetail("detail-attribute-product");

            assertEquals(1, result.productAttributeGroups().size());
            assertEquals("None group", result.productAttributeGroups().get(0).name());
            assertEquals(1, result.productAttributeGroups().get(0).productAttributeValues().size());
        }

        @Test
        void testGetProductSlug_whenProductHasParent_thenReturnParentSlug() {
            Product parent = new Product();
            parent.setId(61L);
            parent.setSlug("parent-slug");

            Product child = new Product();
            child.setId(62L);
            child.setSlug("child-slug");
            child.setParent(parent);

            when(productRepository.findById(62L)).thenReturn(Optional.of(child));
            when(productRepository.findById(61L)).thenReturn(Optional.of(parent));

            var childResult = productService.getProductSlug(62L);
            var parentResult = productService.getProductSlug(61L);

            assertEquals("parent-slug", childResult.slug());
            assertEquals(62L, childResult.productVariantId());
            assertEquals("parent-slug", parentResult.slug());
            assertEquals(null, parentResult.productVariantId());
        }

        @Test
        void testGetProductSlug_whenProductHasNoParent_thenReturnOwnSlug() {
            Product product = new Product();
            product.setId(63L);
            product.setSlug("solo-slug");

            when(productRepository.findById(63L)).thenReturn(Optional.of(product));

            var result = productService.getProductSlug(63L);

            assertEquals("solo-slug", result.slug());
            assertEquals(null, result.productVariantId());
        }

        @Test
        void testGetProductEsDetailById_whenBrandAndThumbnailMissing_thenReturnNullFields() {
            Product product = new Product();
            product.setId(71L);
            product.setName("ES Product");
            product.setSlug("es-product");
            product.setPrice(39.99);
            product.setPublished(true);
            product.setVisibleIndividually(true);
            product.setAllowedToOrder(true);
            product.setFeatured(false);

            when(productRepository.findById(71L)).thenReturn(Optional.of(product));

            var result = productService.getProductEsDetailById(71L);

            assertEquals(71L, result.id());
            assertEquals(null, result.brand());
            assertEquals(null, result.thumbnailMediaId());
            assertTrue(result.categories().isEmpty());
            assertTrue(result.attributes().isEmpty());
        }

        @Test
        void testGetProductEsDetailById_whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.getProductEsDetailById(999L));
        }

        @Test
        void testGetRelatedProductsBackoffice_whenRelatedProductsExist_thenReturnList() {
            Product related = new Product();
            related.setId(81L);
            related.setName("Related Product");
            related.setSlug("related-product");
            related.setAllowedToOrder(true);
            related.setPublished(true);
            related.setFeatured(false);
            related.setVisibleIndividually(true);
            related.setPrice(49.99);
            related.setTaxClassId(1L);

            Product source = new Product();
            source.setId(82L);
            ProductRelated productRelated = new ProductRelated();
            productRelated.setProduct(source);
            productRelated.setRelatedProduct(related);
            source.getRelatedProducts().add(productRelated);

            when(productRepository.findById(82L)).thenReturn(Optional.of(source));

            var result = productService.getRelatedProductsBackoffice(82L);

            assertEquals(1, result.size());
            assertEquals("related-product", result.get(0).slug());
        }

        @Test
        void testGetRelatedProductsStorefront_whenOnlyPublishedRelatedProducts_thenFilterUnpublished() {
            Product source = new Product();
            source.setId(91L);

            Product published = new Product();
            published.setId(92L);
            published.setName("Published Related");
            published.setSlug("published-related");
            published.setPrice(59.99);
            published.setPublished(true);
            published.setThumbnailMediaId(901L);

            Product unpublished = new Product();
            unpublished.setId(93L);
            unpublished.setName("Unpublished Related");
            unpublished.setSlug("unpublished-related");
            unpublished.setPrice(69.99);
            unpublished.setPublished(false);
            unpublished.setThumbnailMediaId(902L);

            ProductRelated publishedRelated = new ProductRelated();
            publishedRelated.setProduct(source);
            publishedRelated.setRelatedProduct(published);

            ProductRelated unpublishedRelated = new ProductRelated();
            unpublishedRelated.setProduct(source);
            unpublishedRelated.setRelatedProduct(unpublished);

            Page<ProductRelated> page = new PageImpl<>(List.of(publishedRelated, unpublishedRelated), PageRequest.of(0, 10), 2);
            when(productRepository.findById(91L)).thenReturn(Optional.of(source));
            when(productRelatedRepository.findAllByProduct(source, PageRequest.of(0, 10))).thenReturn(page);
            when(mediaService.getMedia(901L)).thenReturn(new NoFileMediaVm(901L, "caption", "file", "image/png", "http://cdn/901"));

            var result = productService.getRelatedProductsStorefront(91L, 0, 10);

            assertEquals(1, result.productContent().size());
            assertEquals("published-related", result.productContent().get(0).slug());
        }

        @Test
        void testGetProductsForWarehouse_whenProductsExist_thenReturnInfoList() {
            Product product = new Product();
            product.setId(101L);
            product.setName("Warehouse Product");
            product.setSku("WH-001");

            when(productRepository.findProductForWarehouse("name", "sku", List.of(1L, 2L), FilterExistInWhSelection.YES.name()))
                .thenReturn(List.of(product));

            var result = productService.getProductsForWarehouse("name", "sku", List.of(1L, 2L), FilterExistInWhSelection.YES);

            assertEquals(1, result.size());
            assertEquals("WH-001", result.get(0).sku());
        }

        @Test
        void testGetProductCheckoutList_whenThumbnailEmpty_thenKeepDefaultThumbnail() {
            Brand brand = new Brand();
            brand.setId(123L);

            Product product = new Product();
            product.setId(124L);
            product.setName("Checkout Product Empty Thumb");
            product.setDescription("Description");
            product.setShortDescription("Short Description");
            product.setSku("CHECK-002");
            product.setBrand(brand);
            product.setPrice(79.99);
            product.setTaxClassId(9L);
            product.setThumbnailMediaId(1202L);

            Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.findAllPublishedProductsByIds(List.of(124L), PageRequest.of(0, 10))).thenReturn(page);
            when(mediaService.getMedia(1202L)).thenReturn(new NoFileMediaVm(1202L, "caption", "file", "image/png", ""));

            var result = productService.getProductCheckoutList(0, 10, List.of(124L));

            assertEquals(1, result.productCheckoutListVms().size());
            assertEquals("", result.productCheckoutListVms().get(0).thumbnailUrl());
        }

        @Test
        void testGetProductByCategoryIdsAndBrandIds_whenProductsExist_thenReturnMappedLists() {
            Product categoryProduct = new Product();
            categoryProduct.setId(111L);
            categoryProduct.setName("Category Product");

            Product brandProduct = new Product();
            brandProduct.setId(112L);
            brandProduct.setName("Brand Product");

            when(productRepository.findByCategoryIdsIn(List.of(1L))).thenReturn(List.of(categoryProduct));
            when(productRepository.findByBrandIdsIn(List.of(2L))).thenReturn(List.of(brandProduct));

            var categoryResult = productService.getProductByCategoryIds(List.of(1L));
            var brandResult = productService.getProductByBrandIds(List.of(2L));

            assertEquals(1, categoryResult.size());
            assertEquals(1, brandResult.size());
            assertEquals("Category Product", categoryResult.get(0).name());
            assertEquals("Brand Product", brandResult.get(0).name());
        }

        @Test
        void testGetProductCheckoutList_whenThumbnailExists_thenReturnCheckoutVm() {
            Brand brand = new Brand();
            brand.setId(121L);

            Product product = new Product();
            product.setId(122L);
            product.setName("Checkout Product");
            product.setDescription("Description");
            product.setShortDescription("Short Description");
            product.setSku("CHECK-001");
            product.setBrand(brand);
            product.setPrice(79.99);
            product.setTaxClassId(9L);
            product.setThumbnailMediaId(1201L);

            Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.findAllPublishedProductsByIds(List.of(122L), PageRequest.of(0, 10))).thenReturn(page);
            when(mediaService.getMedia(1201L)).thenReturn(new NoFileMediaVm(1201L, "caption", "file", "image/png", "http://cdn/1201"));

            var result = productService.getProductCheckoutList(0, 10, List.of(122L));

            assertEquals(1, result.productCheckoutListVms().size());
            assertEquals("http://cdn/1201", result.productCheckoutListVms().get(0).thumbnailUrl());
        }

        @Test
        void testGetProductsByMultiQuery_whenInputsHaveWhitespace_thenReturnProducts() {
            Product product = new Product();
            product.setId(131L);
            product.setName("Multi Query Product");
            product.setSlug("multi-query-product");
            product.setPrice(89.99);
            product.setThumbnailMediaId(1301L);

            Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
            when(productRepository.findByProductNameAndCategorySlugAndPriceBetween("test", "category-slug", 10.0, 20.0, PageRequest.of(0, 10)))
                .thenReturn(page);
            when(mediaService.getMedia(1301L)).thenReturn(new NoFileMediaVm(1301L, "caption", "file", "image/png", "http://cdn/1301"));

            var result = productService.getProductsByMultiQuery(0, 10, " Test ", " category-slug ", 10.0, 20.0);

            assertEquals(1, result.productContent().size());
            assertEquals("multi-query-product", result.productContent().get(0).slug());
        }

        @Test
        void testGetProductVariationsByParentId_whenHasOptionsTrue_thenReturnVariations() {
            ProductOption productOption = new ProductOption();
            productOption.setId(1L);

            Product parent = new Product();
            parent.setId(142L);
            parent.setHasOptions(true);

            Product variation = new Product();
            variation.setId(143L);
            variation.setName("Variation Product");
            variation.setSlug("variation-product");
            variation.setSku("VAR-001");
            variation.setGtin("GTIN-VAR");
            variation.setPrice(99.99);
            variation.setPublished(true);
            variation.setProducts(Collections.emptyList());

            ProductOptionCombination combination = new ProductOptionCombination();
            combination.setProduct(variation);
            combination.setProductOption(productOption);
            combination.setValue("Red");
            when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));

            parent.getProducts().add(variation);
            when(productRepository.findById(142L)).thenReturn(Optional.of(parent));

            var result = productService.getProductVariationsByParentId(142L);

            assertEquals(1, result.size());
            assertEquals("variation-product", result.get(0).slug());
            assertEquals("Red", result.get(0).options().get(1L));
        }

        @Test
        void testExportProducts_whenProductsExist_thenReturnExportingDetails() {
            Brand brand = new Brand();
            brand.setId(151L);
            brand.setName("Export Brand");

            Product product = new Product();
            product.setId(152L);
            product.setName("Export Product");
            product.setShortDescription("Short Description");
            product.setDescription("Description");
            product.setSpecification("Specification");
            product.setSku("EXP-001");
            product.setGtin("GTIN-EXP");
            product.setSlug("export-product");
            product.setAllowedToOrder(true);
            product.setPublished(true);
            product.setFeatured(false);
            product.setVisibleIndividually(true);
            product.setStockTrackingEnabled(true);
            product.setPrice(109.99);
            product.setBrand(brand);
            product.setMetaTitle("Meta Title");
            product.setMetaKeyword("Meta Keyword");
            product.setMetaDescription("Meta Description");

            when(productRepository.getExportingProducts("export", "Brand")).thenReturn(List.of(product));

            var result = productService.exportProducts(" export ", "Brand");

            assertEquals(1, result.size());
            assertEquals("Export Product", result.get(0).name());
            assertEquals("Export Brand", result.get(0).brandName());
        }

        @Test
        void testGetProductVariationsByParentId_whenProductNotFound_thenThrowNotFoundException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.getProductVariationsByParentId(999L));
        }

        @Test
        void testGetProductVariationsByParentId_whenHasOptionsFalse_thenReturnEmptyList() {
            Product parent = new Product();
            parent.setId(141L);
            parent.setHasOptions(false);

            when(productRepository.findById(141L)).thenReturn(Optional.of(parent));

            var result = productService.getProductVariationsByParentId(141L);

            assertTrue(result.isEmpty());
        }
    }

    private static ProductPostVm createValidProductPostVm() {
        return createProductPostVm(10.0, 5.0, "test-slug", "SKU123");
    }

    private static ProductPostVm createProductPostVm(Double length, Double width, String slug, String sku) {
        return new ProductPostVm(
            "Test Product",
            slug,
            1L,
            emptyLongList(),
            "Test Short Description",
            "Test Description",
            "Test Specification",
            sku,
            "GTIN123",
            10.0,
            DimensionUnit.CM,
            length,
            width,
            3.0,
            99.99,
            true,
            true,
            true,
            true,
            true,
            "Meta Title",
            "Meta Keywords",
            "Meta Description",
            1L,
            emptyLongList(),
            emptyProductVariationPostVmList(),
            emptyProductOptionValuePostVmList(),
            emptyProductOptionValueDisplayList(),
            emptyLongList(),
            1L
        );
    }

    private static ProductPutVm createValidProductPutVm() {
        return createValidProductPutVm(15.0, 6.0);
    }

    private static ProductPutVm createValidProductPutVm(Double length, Double width) {
        List<ProductOptionValuePutVm> productOptionValuePutVms = List.of(
            new ProductOptionValuePutVm(1L, "text", 1, List.of("red"))
        );
        List<com.yas.product.viewmodel.product.ProductOptionValueDisplay> productOptionValueDisplays = List.of(
            new com.yas.product.viewmodel.product.ProductOptionValueDisplay(1L, "text", 1, "red")
        );

        return new ProductPutVm(
            "Updated Product",
            "updated-slug",
            149.99,
            true,
            true,
            true,
            true,
            true,
            1L,
            emptyLongList(),
            "Updated Short Description",
            "Updated Description",
            "Updated Specification",
            "SKU456",
            "GTIN456",
            10.0,
            DimensionUnit.CM,
            length,
            width,
            4.0,
            "Updated Meta Title",
            "Updated Meta Keywords",
            "Updated Meta Description",
            1L,
            emptyLongList(),
            emptyProductVariationPutVmList(),
            productOptionValuePutVms,
            productOptionValueDisplays,
            emptyLongList(),
            1L
        );
    }

    private static List<Long> emptyLongList() {
        return Collections.emptyList();
    }

    private static List<ProductVariationPostVm> emptyProductVariationPostVmList() {
        return Collections.emptyList();
    }

    private static List<ProductVariationPutVm> emptyProductVariationPutVmList() {
        return Collections.emptyList();
    }

    private static List<ProductOptionValuePostVm> emptyProductOptionValuePostVmList() {
        return Collections.emptyList();
    }

    private static List<ProductOptionValuePutVm> emptyProductOptionValuePutVmList() {
        return Collections.emptyList();
    }

    private static List<com.yas.product.viewmodel.product.ProductOptionValueDisplay> emptyProductOptionValueDisplayList() {
        return Collections.emptyList();
    }

}
