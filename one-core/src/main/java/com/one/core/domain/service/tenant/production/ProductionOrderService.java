package com.one.core.domain.service.tenant.production;

import com.one.core.application.dto.tenant.production.ProductionOrderDTO;
import com.one.core.application.dto.tenant.production.ProductionOrderRequestDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.production.ProductionOrderMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.movements.MovementType;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductRecipe;
import com.one.core.domain.model.tenant.production.ProductionOrder;
import com.one.core.domain.repository.tenant.product.ProductRecipeRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import com.one.core.domain.repository.tenant.production.ProductionOrderRepository;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductRepository productRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final InventoryService inventoryService;
    private final ProductionOrderMapper productionOrderMapper;

    @Autowired
    public ProductionOrderService(ProductionOrderRepository productionOrderRepository,
                                  ProductRepository productRepository,
                                  ProductRecipeRepository productRecipeRepository,
                                  InventoryService inventoryService,
                                  ProductionOrderMapper productionOrderMapper) {
        this.productionOrderRepository = productionOrderRepository;
        this.productRepository = productRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.inventoryService = inventoryService;
        this.productionOrderMapper = productionOrderMapper;
    }

    @Transactional
    public ProductionOrderDTO createProductionOrder(ProductionOrderRequestDTO requestDTO, UserPrincipal currentUser) {
        // 1. Validar que el producto a fabricar existe y es de tipo COMPOUND
        Product productToProduce = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", requestDTO.getProductId()));

        if (productToProduce.getProductType() != ProductType.COMPOUND) {
            throw new ValidationException("Production orders can only be created for products of type COMPOUND.");
        }

        // 2. Obtener la receta y validar que hay stock de insumos
        List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(productToProduce.getId());
        if (recipeItems.isEmpty()) {
            throw new ValidationException("Product " + productToProduce.getName() + " cannot be produced because it has no recipe defined.");
        }

        for (ProductRecipe recipeItem : recipeItems) {
            BigDecimal requiredQuantity = recipeItem.getQuantityRequired().multiply(requestDTO.getQuantityProduced());
            if (!inventoryService.isStockAvailable(recipeItem.getIngredientProduct().getId(), requiredQuantity)) {
                throw new ValidationException("Insufficient stock for ingredient: " + recipeItem.getIngredientProduct().getName());
            }
        }

        // 3. Si hay stock, crear la orden de producción
        ProductionOrder newOrder = new ProductionOrder();
        newOrder.setProduct(productToProduce);
        newOrder.setQuantityProduced(requestDTO.getQuantityProduced());
        newOrder.setNotes(requestDTO.getNotes());

        ProductionOrder savedOrder = productionOrderRepository.save(newOrder);

        // 4. Procesar los movimientos de stock
        // a) Descontar insumos
        for (ProductRecipe recipeItem : recipeItems) {
            BigDecimal quantityToConsume = recipeItem.getQuantityRequired().multiply(savedOrder.getQuantityProduced());
            inventoryService.processOutgoingStock(
                    recipeItem.getIngredientProduct().getId(),
                    quantityToConsume,
                    MovementType.COMPONENT_CONSUMPTION,
                    "PRODUCTION_ORDER",
                    savedOrder.getId().toString(),
                    currentUser.getId(),
                    "Consumed for production of " + productToProduce.getName()
            );
        }

        // b) Incrementar stock del producto terminado
        inventoryService.processIncomingStock(
                productToProduce.getId(),
                savedOrder.getQuantityProduced(),
                MovementType.PRODUCTION_IN,
                "PRODUCTION_ORDER",
                savedOrder.getId().toString(),
                currentUser.getId(),
                "Finished goods from production order ID: " + savedOrder.getId()
        );

        return productionOrderMapper.toDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<ProductionOrderDTO> getAllProductionOrders(Pageable pageable) {
        Page<ProductionOrder> orderPage = productionOrderRepository.findAll(pageable);
        return orderPage.map(productionOrderMapper::toDTO);
    }
}