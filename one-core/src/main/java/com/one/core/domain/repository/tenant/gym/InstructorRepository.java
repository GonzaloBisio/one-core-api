package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {}
