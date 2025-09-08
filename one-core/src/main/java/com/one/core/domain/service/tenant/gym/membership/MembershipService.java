package com.one.core.domain.service.tenant.gym.membership;

import com.one.core.application.dto.tenant.gym.MembershipCreateDTO;
import com.one.core.application.dto.tenant.gym.MembershipDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.gym.membership.MembershipMapper;
import com.one.core.domain.model.enums.gym.MembershipStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.gym.Membership;
import com.one.core.domain.model.tenant.gym.SubscriptionPlan;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.repository.tenant.gym.MembershipRepository;
import com.one.core.domain.repository.tenant.gym.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepo;
    private final SubscriptionPlanRepository planRepo;
    private final CustomerRepository customerRepo;
    private final MembershipMapper mapper;

    public MembershipService(MembershipRepository membershipRepo,
                             SubscriptionPlanRepository planRepo,
                             CustomerRepository customerRepo,
                             MembershipMapper mapper) {
        this.membershipRepo = membershipRepo;
        this.planRepo = planRepo;
        this.customerRepo = customerRepo;
        this.mapper = mapper;
    }

    @Transactional
    public MembershipDTO create(MembershipCreateDTO dto) {
        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));

        SubscriptionPlan plan = planRepo.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", dto.getPlanId()));

        if (!plan.isActive()) {
            throw new ValidationException("El plan está inactivo.");
        }

        if (dto.getStartDate() == null) {
            throw new ValidationException("startDate es requerido para la membresía.");
        }

        Membership m = mapper.toNewEntity(dto);
        mapper.applyAssociations(m, customer, plan);

        // Completar fechas si falta endDate / nextBillingDate
        if (m.getEndDate() == null) {
            m.setEndDate(m.getStartDate().plusMonths(plan.getBillingPeriodMonths()).minusDays(1));
        }
        m.setNextBillingDate(m.getStartDate().plusMonths(plan.getBillingPeriodMonths()));

        Membership saved = membershipRepo.save(m);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void pause(Long membershipId) {
        Membership m = requireMembership(membershipId);
        if (m.getStatus() != MembershipStatus.ACTIVE) {
            throw new ValidationException("Solo se puede pausar una membresía ACTIVA.");
        }
        m.setStatus(MembershipStatus.PAUSED);
        membershipRepo.save(m);
    }

    @Transactional
    public void resume(Long membershipId) {
        Membership m = requireMembership(membershipId);
        if (m.getStatus() != MembershipStatus.PAUSED) {
            throw new ValidationException("Solo se puede reanudar una membresía PAUSED.");
        }
        m.setStatus(MembershipStatus.ACTIVE);
        membershipRepo.save(m);
    }

    @Transactional
    public void cancel(Long membershipId) {
        Membership m = requireMembership(membershipId);
        if (m.getStatus() == MembershipStatus.CANCELLED) return;
        m.setStatus(MembershipStatus.CANCELLED);
        m.setEndDate(LocalDate.now());
        membershipRepo.save(m);
    }

    @Transactional
    public MembershipDTO renew(Long membershipId) {
        Membership m = requireMembership(membershipId);
        SubscriptionPlan plan = m.getPlan();
        if (plan == null) throw new ValidationException("La membresía no tiene plan asociado.");
        // Extiende ciclo
        LocalDate newStart = (m.getEndDate() != null) ? m.getEndDate().plusDays(1) : LocalDate.now();
        m.setStartDate(newStart);
        m.setEndDate(newStart.plusMonths(plan.getBillingPeriodMonths()).minusDays(1));
        m.setNextBillingDate(newStart.plusMonths(plan.getBillingPeriodMonths()));
        m.setStatus(MembershipStatus.ACTIVE);
        return mapper.toDTO(membershipRepo.save(m));
    }

    private Membership requireMembership(Long id) {
        return membershipRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));
    }

    @Transactional
    public MembershipDTO changePlan(Long id, Long newPlanId) {
        Membership e = membershipRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));
        SubscriptionPlan plan = planRepo.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", newPlanId));
        e.setPlan(plan);
        if (plan.getBillingPeriodMonths() != null) {
            e.setNextBillingDate(LocalDate.now().plusMonths(plan.getBillingPeriodMonths()));
        }
        return mapper.toDTO(membershipRepo.save(e));
    }

    @Transactional
    public MembershipDTO get(Long id) {
        return membershipRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Membership","id", id));
    }

    @Transactional
    public Page<MembershipDTO> list(Long customerId, Pageable pageable) {
        Page<Membership> page = (customerId != null)
                ? membershipRepo.findByCustomerId(customerId, pageable)
                : membershipRepo.findAll(pageable);
        return page.map(mapper::toDTO);
    }

}
