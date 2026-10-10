package com.splitbill.settlementservice.repository;

import com.splitbill.settlementservice.entity.Settlement;
import com.splitbill.settlementservice.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findFirstByGroupIdAndFromUserIdAndToUserIdAndStatusOrderByIdDesc(Long groupId, Long fromUserId, Long toUserId, SettlementStatus status);

    List<Settlement> findByFromUserIdOrToUserIdOrderByCreatedAtDesc(Long fromUserId, Long toUserId);

    List<Settlement> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}