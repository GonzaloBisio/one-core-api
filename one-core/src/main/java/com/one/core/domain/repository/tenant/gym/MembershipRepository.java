package com.one.core.domain.repository.tenant.gym;


import com.one.core.domain.model.tenant.gym.Membership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByCustomerIdAndStatus(Long customerId, com.one.core.domain.model.enums.gym.MembershipStatus status);
    Page<Membership> findByCustomerId(Long customerId, Pageable pageable);

}