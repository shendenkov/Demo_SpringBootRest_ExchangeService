package com.example.exchange.services;

import com.example.exchange.models.entities.ExchangeRateEntity;
import com.example.exchange.models.enums.OperationType;
import com.example.exchange.exceptions.ExchangeException;
import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.ExchangeRequest;
import com.example.exchange.repositories.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CommissionService commissionService;

    @Autowired
    public ExchangeServiceImpl(ExchangeRateRepository exchangeRateRepository, CommissionService commissionService) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.commissionService = commissionService;
    }

    @Override
    public ExchangeRequest calculateExchange(ExchangeRequest exchangeRequest) {
        if (exchangeRequest.getCurrencyFrom().equals(exchangeRequest.getCurrencyTo())) {
            throw new ExchangeException("Currencies From and To should be different");
        }

        Optional<ExchangeRateEntity> optionalRate = exchangeRateRepository.findByFromAndTo(exchangeRequest.getCurrencyFrom().toString(),
                exchangeRequest.getCurrencyTo().toString());
        if (!optionalRate.isPresent()) {
            throw new ExchangeException("This service doesn't support exchange between " + exchangeRequest.getCurrencyFrom() + " and " + exchangeRequest.getCurrencyTo());
        }

        BigDecimal commissionCoefficient = commissionService.getCommissionCoefficient(exchangeRequest.getCurrencyFrom(),
                exchangeRequest.getCurrencyTo());

        if (exchangeRequest.getOperationType() == OperationType.GIVE) {
            if (exchangeRequest.getAmountFrom().compareTo(BigDecimal.ZERO) < 0
                    || exchangeRequest.getAmountFrom().compareTo(BigDecimal.ZERO) == 0) {
                throw new ExchangeException("For operation " + exchangeRequest.getOperationType()
                        + " amountFrom should be greater than zero");
            }
            exchangeRequest.setAmountTo(
                    exchangeRequest.getAmountFrom()
                    .multiply(optionalRate.get().getRate())
                    .multiply(commissionCoefficient)
                    .setScale(2, BigDecimal.ROUND_DOWN)
            );

        } else if (exchangeRequest.getOperationType() == OperationType.GET) {
            if (exchangeRequest.getAmountTo().compareTo(BigDecimal.ZERO) < 0
                    || exchangeRequest.getAmountTo().compareTo(BigDecimal.ZERO) == 0) {
                throw new ExchangeException("For operation " + exchangeRequest.getOperationType()
                        + " amountTo should be greater than zero");
            }
            BigDecimal divisor = optionalRate.get().getRate()
                    .multiply(commissionCoefficient)
                    .setScale(2, BigDecimal.ROUND_DOWN);
            exchangeRequest.setAmountFrom(
                    exchangeRequest.getAmountTo()
                    .divide(divisor, BigDecimal.ROUND_DOWN)
            );

        } else {
            throw new ExchangeException("Unsupported operation type: " + exchangeRequest.getOperationType());
        }
        return exchangeRequest;
    }

    @Override
    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAll().stream()
                .map(ExchangeRate::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setExchangeRate(ExchangeRate exchangeRate) {
        if (exchangeRate.getRate().compareTo(BigDecimal.ZERO) < 0
                || exchangeRate.getRate().compareTo(BigDecimal.ZERO) == 0) {
            throw new ExchangeException("Rate shouldn't be more than zero");
        }
        if (exchangeRate.getFrom().equals(exchangeRate.getTo())) {
            throw new ExchangeException("Currencies From and To should be different");
        }
        Optional<ExchangeRateEntity> optionalGive = exchangeRateRepository.findByFromAndTo(exchangeRate.getFrom().toString(),
                exchangeRate.getTo().toString());
        if (optionalGive.isPresent()) {
            ExchangeRateEntity dbEntity = optionalGive.get();
            dbEntity.setRate(exchangeRate.getRate());
            exchangeRateRepository.save(dbEntity);
        } else {
            exchangeRateRepository.save(new ExchangeRateEntity(exchangeRate));
        }
        Optional<ExchangeRateEntity> optionalGet = exchangeRateRepository.findByFromAndTo(exchangeRate.getTo().toString(),
                exchangeRate.getFrom().toString());
        BigDecimal reverseRate = BigDecimal.ONE.divide(exchangeRate.getRate(), 5, BigDecimal.ROUND_DOWN);
        if (optionalGet.isPresent()) {
            ExchangeRateEntity dbEntity = optionalGet.get();
            dbEntity.setRate(reverseRate);
            exchangeRateRepository.saveAndFlush(dbEntity);
        } else {
            ExchangeRate reverseExchangeRate = new ExchangeRate();
            reverseExchangeRate.setFrom(exchangeRate.getTo());
            reverseExchangeRate.setRate(reverseRate);
            reverseExchangeRate.setTo(exchangeRate.getFrom());
            exchangeRateRepository.saveAndFlush(new ExchangeRateEntity(reverseExchangeRate));
        }
    }
}
