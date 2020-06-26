package com.example.exchange.services;

import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.ExchangeRequest;
import com.example.exchange.models.enums.Currency;

import java.util.List;
import java.util.Optional;

public interface ExchangeService {

    ExchangeRequest calculateExchange(ExchangeRequest exchangeRequest);
    List<ExchangeRate> getAllExchangeRates();
    Optional<ExchangeRate> getExchangeRate(Currency from, Currency to);
    void setExchangeRate(ExchangeRate exchangeRate);
}
