package com.cansuiremkanli.libmanage.data.repository;

import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BorrowingRepository extends JpaRepository<Borrowing, UUID> {
    List<Borrowing> findByUserId(UUID userId);
    List<Borrowing> findByIsOverdueTrue();
}
