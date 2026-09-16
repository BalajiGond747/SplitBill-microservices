package com.splitbill.expenseservice.repository;

import com.splitbill.expenseservice.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByGroupId(Long groupId, Pageable pageable);

    Page<Expense> findByPaidBy(Long paidBy, Pageable pageable);

    Page<Expense> findByGroupIdAndPaidBy(Long groupId, Long paidBy, Pageable pageable);

    Page<Expense> findByExpenseDateBetween(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Page<Expense> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
}