package com.one.core.application.controller.tenant.products;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.application.dto.tenant.product.ProductFilterDTO;
import com.one.core.application.dto.tenant.product.ProductPackagingDTO;
import com.one.core.application.dto.tenant.product.ProductRecipeDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.domain.service.tenant.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/products")
@PreAuthorize("hasRole('TENANT_USER') or hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN')")
@Tag(name = "Products", description = "Manage products. Quantities are stored in base units (grams, milliliters, units) and normalized automatically.")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageableResponse<ProductDTO>> getAllProducts(
            ProductFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ProductDTO> productPage = productService.getAllProducts(filterDTO, pageable);
        return ResponseEntity.ok(new PageableResponse<>(productPage));
    }

    @Operation(
            summary = "List all PACKAGING products (no pagination)",
            description = "Devuelve todos los productos de tipo PACKAGING. Por defecto solo activos."
    )
    @GetMapping("/packaging/available")
    public ResponseEntity<List<ProductDTO>> getAllPackaging(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(productService.getAllPackaging(activeOnly));
    }

    @Operation(
            summary = "List all PHYSICAL_GOOD products (no pagination)",
            description = "Devuelve todos los productos de tipo PHYSICAL_GOOD. Por defecto solo activos."
    )
    @GetMapping("/physicalgood/available")
    public ResponseEntity<List<ProductDTO>> getAllPhysicalGoods(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(productService.getAllPhysicalGoods(activeOnly));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @Operation(summary = "Create product", description = "Quantities are stored in base units (grams, milliliters, units) and normalized automatically.")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @Operation(summary = "Update product", description = "Quantities are stored in base units (grams, milliliters, units) and normalized automatically.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{mainProductId}/recipe")
    public ResponseEntity<List<ProductRecipeDTO>> setOrUpdateRecipe(
            @PathVariable Long mainProductId,
            @Valid @RequestBody List<ProductRecipeDTO> recipeItems) {
        List<ProductRecipeDTO> updatedRecipe = productService.setOrUpdateRecipe(mainProductId, recipeItems);
        return ResponseEntity.ok(updatedRecipe);
    }

    @GetMapping("/{mainProductId}/recipe-items")
    public ResponseEntity<List<ProductRecipeDTO>> getRecipeItems(@PathVariable Long mainProductId) {
        List<ProductRecipeDTO> recipe = productService.getRecipeItems(mainProductId);
        return ResponseEntity.ok(recipe);
    }

    @DeleteMapping("/recipe-items/{recipeItemId}")
    public ResponseEntity<Void> removeRecipeItem(@PathVariable Long recipeItemId) {
        productService.removeRecipeItem(recipeItemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{mainProductId}/packaging")
    public ResponseEntity<List<ProductPackagingDTO>> setOrUpdatePackaging(
            @PathVariable Long mainProductId,
            @Valid @RequestBody List<ProductPackagingDTO> packagingItemsDTO) {
        List<ProductPackagingDTO> updatedPackaging = productService.setOrUpdatePackaging(mainProductId, packagingItemsDTO);
        return ResponseEntity.ok(updatedPackaging);
    }

    @GetMapping("/{mainProductId}/packaging")
    public ResponseEntity<List<ProductPackagingDTO>> getPackaging(@PathVariable Long mainProductId) {
        List<ProductPackagingDTO> packaging = productService.getPackagingForProduct(mainProductId);
        return ResponseEntity.ok(packaging);
    }
}