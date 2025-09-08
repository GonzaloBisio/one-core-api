package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.ClassTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassTemplateRepository extends JpaRepository<ClassTemplate, Long> {}
