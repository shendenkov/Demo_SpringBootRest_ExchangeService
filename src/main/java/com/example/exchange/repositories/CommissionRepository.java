package com.example.exchange.repositories;

import com.example.exchange.models.entities.CommissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<CommissionEntity, Long> {

    Optional<CommissionEntity> findByFromAndTo(String from, String to);
}
