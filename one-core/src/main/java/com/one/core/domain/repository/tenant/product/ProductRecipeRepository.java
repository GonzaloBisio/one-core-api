package com.one.core.domain.repository.tenant.product;

import com.one.core.domain.model.tenant.product.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRecipeRepository extends JpaRepository<ProductRecipe, Long> {

    List<ProductRecipe> findByMainProductId(Long mainProductId);
}