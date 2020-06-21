package com.example.exchange.models;

import com.example.exchange.models.entities.CommissionEntity;
import com.example.exchange.models.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commission {

    private BigDecimal commissionPt;
    private Currency from;
    private Currency to;

    public Commission(CommissionEntity entity) {
        commissionPt = entity.getCommissionPt();
        from = Currency.valueOf(entity.getFrom());
        to = Currency.valueOf(entity.getTo());
    }
}
