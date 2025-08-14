package kr.elta.backend.repository;

import kr.elta.backend.entity.BroadcastPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BroadcastPlanRepository extends JpaRepository<BroadcastPlanEntity, Long> {
}
