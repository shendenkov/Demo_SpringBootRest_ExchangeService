package com.example.exchange.services;

import com.example.exchange.models.enums.Currency;
import com.example.exchange.exceptions.CommissionException;
import com.example.exchange.models.Commission;
import com.example.exchange.models.entities.CommissionEntity;
import com.example.exchange.repositories.CommissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommissionServiceImpl implements CommissionService {

    private CommissionRepository commissionRepository;

    @Autowired
    public CommissionServiceImpl(CommissionRepository commissionRepository) {
        this.commissionRepository = commissionRepository;
    }

    @Override
    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll().stream()
                .map(Commission::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Commission> getCommission(Currency from, Currency to) {
        Optional<CommissionEntity> optional = commissionRepository.findByFromAndTo(from.toString(), to.toString());
        return optional.map(Commission::new);
    }

    @Override
    public BigDecimal getCommissionCoefficient(Currency from, Currency to) {
        Optional<Commission> optionalCommission = getCommission(from, to);
        BigDecimal commissionCoefficient;
        if (optionalCommission.isPresent()) {
            commissionCoefficient = optionalCommission.get().getCommissionPt();
        } else {
            commissionCoefficient = BigDecimal.ZERO;
        }
        return BigDecimal.ONE.subtract(commissionCoefficient.divide(BigDecimal.valueOf(100), 5, BigDecimal.ROUND_DOWN));
    }

    @Override
    public void setCommission(Commission commission) throws Exception {
        if (commission.getCommissionPt().compareTo(BigDecimal.ZERO) < 0) {
            throw new CommissionException("Commission percent shouldn't be negative");
        }
        if (commission.getFrom().equals(commission.getTo())) {
            throw new CommissionException("Currencies From and To should be different");
        }
        Optional<CommissionEntity> optional = commissionRepository.findByFromAndTo(commission.getFrom().toString(),
                commission.getTo().toString());
        if (optional.isPresent()) {
            CommissionEntity dbEntity = optional.get();
            dbEntity.setCommissionPt(commission.getCommissionPt());
            commissionRepository.save(dbEntity);
        } else {
            commissionRepository.save(new CommissionEntity(commission));
        }
    }
}
