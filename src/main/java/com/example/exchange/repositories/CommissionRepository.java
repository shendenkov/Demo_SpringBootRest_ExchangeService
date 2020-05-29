package com.example.exchange.repositories;

import com.example.exchange.models.entities.CommissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionRepository extends JpaRepository<CommissionEntity, Long> {

    Optional<CommissionEntity> findByFromAndTo(String from, String to);
}
