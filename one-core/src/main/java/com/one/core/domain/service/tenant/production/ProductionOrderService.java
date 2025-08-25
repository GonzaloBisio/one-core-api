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
import com.one.core.domain.service.common.UnitConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductRepository productRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final InventoryService inventoryService;
    private final ProductionOrderMapper productionOrderMapper;
    private final UnitConversionService unitConversionService;

    @Autowired
    public ProductionOrderService(ProductionOrderRepository productionOrderRepository,
                                  ProductRepository productRepository,
                                  ProductRecipeRepository productRecipeRepository,
                                  InventoryService inventoryService,
                                  ProductionOrderMapper productionOrderMapper,
                                  UnitConversionService unitConversionService) {
        this.productionOrderRepository = productionOrderRepository;
        this.productRepository = productRepository;
        this.productRecipeRepository = productRecipeRepository;
        this.inventoryService = inventoryService;
        this.productionOrderMapper = productionOrderMapper;
        this.unitConversionService = unitConversionService;
    }

    @Transactional
    public ProductionOrderDTO createProductionOrder(ProductionOrderRequestDTO requestDTO, UserPrincipal currentUser) {
        // 1. Validar que el producto a fabricar existe y es de tipo COMPOUND
        Product productToProduce = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", requestDTO.getProductId()));

        if (productToProduce.getProductType() != ProductType.COMPOUND) {
            throw new ValidationException("Production orders can only be created for products of type COMPOUND.");
        }

        // 2. Calcular la lista APLANADA de insumos básicos usando la recursividad
        Map<Product, BigDecimal> requiredRawIngredients = new HashMap<>();
        calculateRawIngredients(productToProduce, requestDTO.getQuantityProduced(), requiredRawIngredients);

        if (requiredRawIngredients.isEmpty()) {
            throw new ValidationException("Product '" + productToProduce.getName() + "' cannot be produced because its recipe is empty or leads to no raw ingredients.");
        }

        // 3. Validar el stock de los insumos básicos calculados
        for (Map.Entry<Product, BigDecimal> entry : requiredRawIngredients.entrySet()) {
            Product ingredient = entry.getKey();
            BigDecimal requiredQuantity = entry.getValue();
            BigDecimal requiredBase = unitConversionService.toBaseUnit(requiredQuantity, ingredient.getUnitOfMeasure());
            if (!inventoryService.isStockAvailable(ingredient.getId(), requiredBase)) {
                throw new ValidationException("Insufficient stock for raw ingredient: " + ingredient.getName());
            }
        }

        // 4. Si hay stock, crear la orden de producción
        ProductionOrder newOrder = new ProductionOrder();
        newOrder.setProduct(productToProduce);
        newOrder.setQuantityProduced(requestDTO.getQuantityProduced());
        newOrder.setNotes(requestDTO.getNotes());
        // Aquí podrías setear el 'createdByUser' si lo tienes en tu entidad ProductionOrder
        ProductionOrder savedOrder = productionOrderRepository.save(newOrder);

        // 5. Procesar los movimientos de stock
        // a) Descontar insumos básicos
        for (Map.Entry<Product, BigDecimal> entry : requiredRawIngredients.entrySet()) {
            Product ingredient = entry.getKey();
            BigDecimal quantityToConsume = entry.getValue();
            BigDecimal consumeBase = unitConversionService.toBaseUnit(quantityToConsume, ingredient.getUnitOfMeasure());
            inventoryService.processOutgoingStock(
                    ingredient.getId(),
                    consumeBase,
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
                unitConversionService.toBaseUnit(savedOrder.getQuantityProduced(), productToProduce.getUnitOfMeasure()),
                MovementType.PRODUCTION_IN,
                "PRODUCTION_ORDER",
                savedOrder.getId().toString(),
                currentUser.getId(),
                "Finished goods from production order ID: " + savedOrder.getId()
        );

        return productionOrderMapper.toDTO(savedOrder);
    }

    /**
     * Método recursivo que "desarma" un producto compuesto hasta llegar a sus insumos básicos (PHYSICAL_GOOD).
     * @param product El producto a procesar (puede ser COMPOUND o PHYSICAL_GOOD).
     * @param quantityNeeded La cantidad necesaria de este producto.
     * @param rawIngredientsMap El mapa que acumula el resultado final de insumos básicos.
     */
    private void calculateRawIngredients(Product product, BigDecimal quantityNeeded, Map<Product, BigDecimal> rawIngredientsMap) {
        // Caso base: Si el producto es un insumo físico, lo añadimos al mapa y terminamos.
        if (product.getProductType() == ProductType.PHYSICAL_GOOD || product.getProductType() == ProductType.PACKAGING) {
            rawIngredientsMap.merge(product, quantityNeeded, BigDecimal::add);
            return;
        }

        // Paso recursivo: Si es un producto compuesto, obtenemos su receta y procesamos cada ingrediente.
        if (product.getProductType() == ProductType.COMPOUND) {
            List<ProductRecipe> recipeItems = productRecipeRepository.findByMainProductId(product.getId());
            for (ProductRecipe recipeItem : recipeItems) {
                Product ingredient = recipeItem.getIngredientProduct();
                BigDecimal subQuantityNeeded = recipeItem.getQuantityRequired().multiply(quantityNeeded);

                // Llamada recursiva para el ingrediente, que a su vez puede ser otra sub-receta.
                calculateRawIngredients(ingredient, subQuantityNeeded, rawIngredientsMap);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductionOrderDTO> getAllProductionOrders(Pageable pageable) {
        Page<ProductionOrder> orderPage = productionOrderRepository.findAll(pageable);
        return orderPage.map(productionOrderMapper::toDTO);
    }
}