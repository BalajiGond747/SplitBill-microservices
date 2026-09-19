package com.splitbill.balanceservice.repository;

import com.splitbill.balanceservice.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

    List<Balance> findByGroupId(Long groupId);

    List<Balance> findByFromUserId(Long fromUserId);

    List<Balance> findByToUserId(Long toUserId);

    Optional<Balance> findByGroupIdAndFromUserIdAndToUserId(Long groupId, Long fromUserId, Long toUserId);

    void deleteByGroupId(Long groupId);
}