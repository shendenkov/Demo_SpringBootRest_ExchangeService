package com.example.exchange.models.entities;

import com.example.exchange.models.Commission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "commissions")
public class CommissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commissionPt")
    @DecimalMax(value = "100.00")
    @DecimalMin(value = "0.0")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal commissionPt;

    @Column(name = "currencyFrom")
    private String from;

    @Column(name = "currencyTo")
    private String to;

    public CommissionEntity(Commission commission) {
        commissionPt = commission.getCommissionPt();
        from = commission.getFrom().toString();
        to = commission.getTo().toString();
    }
}
