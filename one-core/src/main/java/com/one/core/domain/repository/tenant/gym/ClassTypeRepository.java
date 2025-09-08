package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {}
