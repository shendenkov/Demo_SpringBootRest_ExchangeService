package com.example.exchange.services;

import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.ExchangeRequest;

import java.util.List;

public interface ExchangeService {

    ExchangeRequest calculateExchange(ExchangeRequest exchangeRequest);
    List<ExchangeRate> getAllExchangeRates();
    void setExchangeRate(ExchangeRate exchangeRate);
}
