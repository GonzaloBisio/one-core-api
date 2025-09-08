package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findByStartAtBetween(LocalDateTime from, LocalDateTime to);
}