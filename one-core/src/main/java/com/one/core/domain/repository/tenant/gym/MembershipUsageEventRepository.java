package com.one.core.domain.repository.tenant.gym;

import aj.org.objectweb.asm.commons.Remapper;
import com.one.core.domain.model.tenant.gym.MembershipUsageEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipUsageEventRepository extends JpaRepository<MembershipUsageEvent, Long> {
    boolean existsByMembershipIdAndSessionId(Long membershipId, Long sessionId);
    Page<MembershipUsageEvent> findByMembershipIdOrderByEventDateDesc(Long membershipId, Pageable pageable);
}