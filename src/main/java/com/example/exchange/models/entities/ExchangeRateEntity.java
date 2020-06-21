package com.example.exchange.models.entities;

import com.example.exchange.models.ExchangeRate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchangeRates")
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currencyFrom")
    private String from;

    @Column(name = "rate")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 33, fraction = 5)
    private BigDecimal rate;

    @Column(name = "currencyTo")
    private String to;

    public ExchangeRateEntity(ExchangeRate exchangeRate) {
        from = exchangeRate.getFrom().toString();
        rate = exchangeRate.getRate();
        to = exchangeRate.getTo().toString();
    }
}
