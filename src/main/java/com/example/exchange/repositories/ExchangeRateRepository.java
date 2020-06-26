package com.example.exchange.repositories;

import com.example.exchange.models.entities.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findByFromAndTo(String from, String to);
}
