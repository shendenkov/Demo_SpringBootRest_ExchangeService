package com.example.exchange.services;

import com.example.exchange.models.enums.Currency;
import com.example.exchange.models.Commission;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CommissionService {

    List<Commission> getAllCommissions();
    Optional<Commission> getCommission(Currency from, Currency to);
    BigDecimal getCommissionCoefficient(Currency from, Currency to);
    void setCommission(Commission commission) throws Exception;
}
