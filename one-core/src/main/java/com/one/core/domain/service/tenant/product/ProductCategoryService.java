package com.one.core.domain.service.tenant.product;

import com.one.core.application.dto.tenant.product.ProductCategoryDTO;
import com.one.core.application.dto.tenant.product.ProductCategoryFilterDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.product.ProductCategoryMapper;
import com.one.core.domain.model.tenant.product.ProductCategory;
import com.one.core.domain.repository.tenant.product.ProductCategoryRepository;
import com.one.core.domain.service.tenant.product.criteria.ProductCategorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper categoryMapper;

    @Autowired
    public ProductCategoryService(ProductCategoryRepository categoryRepository, ProductCategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductCategoryDTO> getAllCategories(ProductCategoryFilterDTO filterDTO, Pageable pageable) {
        Specification<ProductCategory> spec = ProductCategorySpecification.filterBy(filterDTO);
        Page<ProductCategory> categoryPage = categoryRepository.findAll(spec, pageable);
        List<ProductCategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(categoryDTOs, pageable, categoryPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductCategoryDTO getCategoryById(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", id));
        return categoryMapper.toDTO(category);
    }

    @Transactional
    public ProductCategoryDTO createCategory(ProductCategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName().trim())) {
            throw new DuplicateFieldException("Category name", categoryDTO.getName().trim());
        }
        ProductCategory category = categoryMapper.toEntityForCreation(categoryDTO);

        if (categoryDTO.getParentId() != null) {
            ProductCategory parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent ProductCategory", "id", categoryDTO.getParentId()));
            category.setParentCategory(parent);
        }

        ProductCategory savedCategory = categoryRepository.save(category);
        return categoryMapper.toDTO(savedCategory);
    }

    @Transactional
    public ProductCategoryDTO updateCategory(Long id, ProductCategoryDTO categoryDTO) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", id));

        if (!category.getName().equalsIgnoreCase(categoryDTO.getName().trim())) {
            if (categoryRepository.existsByName(categoryDTO.getName().trim())) {
                throw new DuplicateFieldException("Category name", categoryDTO.getName().trim());
            }
        }
        categoryMapper.updateEntityFromDTO(categoryDTO, category);

        if (categoryDTO.getParentId() != null) {
            if (categoryDTO.getParentId().equals(category.getId())) {
                throw new ValidationException("A category cannot be its own parent.");
            }
            ProductCategory parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent ProductCategory", "id", categoryDTO.getParentId()));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        ProductCategory updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw new ResourceNotFoundException("ProductCategory", "id", id);
        categoryRepository.deleteById(id);
    }
}