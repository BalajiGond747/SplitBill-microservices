package com.splitbill.expenseservice.repository;

import com.splitbill.expenseservice.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    List<ExpenseSplit> findByExpenseId(Long expenseId);

    void deleteByExpenseId(Long expenseId);
}