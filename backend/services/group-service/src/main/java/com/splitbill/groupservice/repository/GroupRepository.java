package com.splitbill.groupservice.repository;

import com.splitbill.groupservice.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Group> findByCreatedBy(Long createdBy, Pageable pageable);

    Page<Group> findByActive(Boolean active, Pageable pageable);

   }