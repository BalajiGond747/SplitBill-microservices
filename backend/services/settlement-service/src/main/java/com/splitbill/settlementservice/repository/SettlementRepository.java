package com.splitbill.settlementservice.repository;

import com.splitbill.settlementservice.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByGroupIdOrderBySettlementDateDesc(Long groupId);

    List<Settlement> findByFromUserIdOrToUserIdOrderBySettlementDateDesc(
            Long fromUserId,
            Long toUserId
    );
}