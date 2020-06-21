package com.example.exchange.models;

import com.example.exchange.models.enums.Currency;
import com.example.exchange.models.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequest {

    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private Currency currencyFrom;
    private Currency currencyTo;
    private OperationType operationType;
}
