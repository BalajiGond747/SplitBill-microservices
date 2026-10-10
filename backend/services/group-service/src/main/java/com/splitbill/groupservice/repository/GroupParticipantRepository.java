package com.splitbill.groupservice.repository;

import com.splitbill.groupservice.entity.GroupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupParticipantRepository
        extends JpaRepository<GroupParticipant, Long> {

    List<GroupParticipant> findByGroupIdAndActiveTrue(Long groupId);

    Optional<GroupParticipant> findByGroupIdAndUserId(
            Long groupId,
            Long userId
    );

    boolean existsByGroupIdAndUserId(
            Long groupId,
            Long userId
    );
}