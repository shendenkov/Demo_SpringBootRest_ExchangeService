package com.example.exchange.models;

import com.example.exchange.models.entities.ExchangeRateEntity;
import com.example.exchange.models.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    private Currency from;
    private BigDecimal rate;
    private Currency to;

    public ExchangeRate(ExchangeRateEntity entity) {
        from = Currency.valueOf(entity.getFrom());
        rate = entity.getRate();
        to = Currency.valueOf(entity.getTo());
    }
}
