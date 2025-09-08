package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {}
