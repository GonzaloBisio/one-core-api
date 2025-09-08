package com.one.core.domain.service.tenant.gym.subscription;

import com.one.core.application.dto.tenant.gym.SubscriptionPlanCreateDTO;
import com.one.core.application.dto.tenant.gym.SubscriptionPlanDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.gym.plans.SubscriptionPlanMapper;
import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.gym.GymAccessMode;
import com.one.core.domain.model.tenant.gym.SubscriptionPlan;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.repository.tenant.gym.SubscriptionPlanRepository;
import com.one.core.domain.repository.tenant.product.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepo;
    private final ProductRepository productRepo;
    private final SubscriptionPlanMapper mapper;

    public SubscriptionPlanService(SubscriptionPlanRepository planRepo,
                                   ProductRepository productRepo,
                                   SubscriptionPlanMapper mapper) {
        this.planRepo = planRepo;
        this.productRepo = productRepo;
        this.mapper = mapper;
    }

    @Transactional
    public SubscriptionPlanDTO create(SubscriptionPlanCreateDTO dto) {
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

        // Reglas básicas
        if (product.getProductType() != ProductType.SERVICE) {
            throw new ValidationException("El producto del plan debe ser de tipo SERVICE.");
        }
        validateAccessConfig(dto.getAccessMode(), dto.getVisitsAllowed(), dto.getResetDayOfWeek());

        SubscriptionPlan p = mapper.toNewEntity(dto);
        mapper.applyProduct(p, product);

        return mapper.toDTO(planRepo.save(p));
    }

    @Transactional
    public SubscriptionPlanDTO update(Long planId, SubscriptionPlanCreateDTO dto) {
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", planId));

        if (dto.getAccessMode() != null || dto.getVisitsAllowed() != null || dto.getResetDayOfWeek() != null) {
            GymAccessMode mode = (dto.getAccessMode() != null) ? dto.getAccessMode() : plan.getAccessMode();
            Integer visits = (dto.getVisitsAllowed() != null) ? dto.getVisitsAllowed() : plan.getVisitsAllowed();
            Short reset = (dto.getResetDayOfWeek() != null) ? dto.getResetDayOfWeek() : plan.getResetDayOfWeek();
            validateAccessConfig(mode, visits, reset);
        }

        mapper.applyUpdates(plan, dto);

        // Si cambia el productId
        if (dto.getProductId() != null && (plan.getProduct() == null || !plan.getProduct().getId().equals(dto.getProductId()))) {
            Product product = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));
            if (product.getProductType() != ProductType.SERVICE) {
                throw new ValidationException("El producto del plan debe ser de tipo SERVICE.");
            }
            mapper.applyProduct(plan, product);
        }

        return mapper.toDTO(planRepo.save(plan));
    }

    private void validateAccessConfig(GymAccessMode mode, Integer visitsAllowed, Short resetDayOfWeek) {
        switch (mode) {
            case UNLIMITED -> {
                if (visitsAllowed != null) throw new ValidationException("UNLIMITED no debe definir visitsAllowed.");
                if (resetDayOfWeek != null) throw new ValidationException("UNLIMITED no usa resetDayOfWeek.");
            }
            case N_PER_WEEK -> {
                if (visitsAllowed == null || visitsAllowed <= 0)
                    throw new ValidationException("N_PER_WEEK requiere visitsAllowed > 0.");
                if (resetDayOfWeek == null || resetDayOfWeek < 1 || resetDayOfWeek > 7)
                    throw new ValidationException("N_PER_WEEK requiere resetDayOfWeek entre 1 y 7.");
            }
            case N_PER_MONTH -> {
                if (visitsAllowed == null || visitsAllowed <= 0)
                    throw new ValidationException("N_PER_MONTH requiere visitsAllowed > 0.");
                if (resetDayOfWeek != null)
                    throw new ValidationException("N_PER_MONTH no usa resetDayOfWeek.");
            }
        }
    }

    @Transactional
    public SubscriptionPlanDTO get(Long id) {
        return planRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan","id", id));
    }

    @Transactional
    public Page<SubscriptionPlanDTO> list(Pageable pageable) {
        return planRepo.findAll(pageable).map(mapper::toDTO);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        SubscriptionPlan e = planRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan","id", id));
        e.setActive(active);
        planRepo.save(e);
    }

    @Transactional
    public SubscriptionPlanDTO update(Long id, SubscriptionPlanDTO dto) {
        SubscriptionPlan e = planRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan","id", id));

        // aplica cambios del DTO (nombre, descripción, accessMode, etc.)
        mapper.applyUpdates(e, dto);

        // cambio de producto (opcional)
        if (dto.getProductId() != null) {
            Product p = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product","id", dto.getProductId()));
            mapper.applyProduct(e, p);
        }
        return mapper.toDTO(planRepo.save(e));
    }
}
