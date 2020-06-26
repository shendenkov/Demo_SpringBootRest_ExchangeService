package com.example.exchange;

import com.example.exchange.exceptions.CommissionException;
import com.example.exchange.models.Commission;
import com.example.exchange.models.entities.CommissionEntity;
import com.example.exchange.models.enums.Currency;
import com.example.exchange.repositories.CommissionRepository;
import com.example.exchange.services.CommissionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class CommissionServiceTests {

    private final Random random = new Random();
    private final CommissionRepository repositoryMock = Mockito.mock(CommissionRepository.class);
    private final CommissionServiceImpl service = new CommissionServiceImpl(repositoryMock);

    @Test
    public void getAllCommissionsTest() {
        List<CommissionEntity> data = new ArrayList<>(4);
        data.add(new CommissionEntity(1L, BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.UAH.toString(), Currency.USD.toString()));
        data.add(new CommissionEntity(2L, BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.USD.toString(), Currency.UAH.toString()));
        data.add(new CommissionEntity(3L, BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.UAH.toString(), Currency.EUR.toString()));
        data.add(new CommissionEntity(4L, BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.EUR.toString(), Currency.UAH.toString()));
        Mockito.when(repositoryMock.findAll())
                .thenReturn(data);

        List<Commission> result = service.getAllCommissions();

        Assertions.assertThat(result)
                .isNotNull()
                .hasSize(data.size())
                .doesNotContain(new Commission[]{null})
                .containsAll(data.stream()
                        .map(Commission::new)
                        .collect(Collectors.toList()));

        Mockito.verify(repositoryMock)
                .findAll();
    }

    @Test
    public void getAllCommissionsEmptyTest() {
        List<CommissionEntity> data = new ArrayList<>(0);
        Mockito.when(repositoryMock.findAll())
                .thenReturn(data);

        List<Commission> result = service.getAllCommissions();

        Assertions.assertThat(result)
                .isNotNull()
                .isEmpty();

        Mockito.verify(repositoryMock)
                .findAll();
    }

    @Test
    public void getCommissionTest() {
        CommissionEntity data = new CommissionEntity(1L, BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.UAH.toString(), Currency.USD.toString());
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));

        Optional<Commission> result = service.getCommission(Currency.UAH, Currency.USD);

        Assertions.assertThat(result)
                .isNotNull()
                .isPresent()
                .get().isEqualTo(new Commission(data));

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void getCommissionNotFoundTest() {
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        Optional<Commission> result = service.getCommission(Currency.UAH, Currency.USD);

        Assertions.assertThat(result)
                .isNotNull()
                .isNotPresent();

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void getCommissionCoefficientTest() {
        CommissionEntity data = new CommissionEntity(1L, BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.UAH.toString(), Currency.USD.toString());
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));

        BigDecimal result = service.getCommissionCoefficient(Currency.UAH, Currency.USD);

        BigDecimal expected = BigDecimal.ONE
                .subtract(data.getCommissionPt()
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_DOWN));
        Assertions.assertThat(result)
                .isNotNull()
                .isGreaterThan(BigDecimal.ZERO)
                .isLessThanOrEqualTo(BigDecimal.ONE)
                .isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void setNewCommissionTest() {
        Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN), Currency.UAH,
                Currency.USD);
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        service.setCommission(commission);

        ArgumentCaptor<CommissionEntity> entityCaptor = ArgumentCaptor.forClass(CommissionEntity.class);
        Mockito.verify(repositoryMock)
                .saveAndFlush(entityCaptor.capture());

        Assertions.assertThat(entityCaptor.getAllValues())
                .hasSize(1);

        CommissionEntity capturedArgument = entityCaptor.getValue();
        checkCommission(capturedArgument);
        Assertions.assertThat(capturedArgument.getId())
                .isNull();

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void updateCommissionTest() {
        Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN), Currency.UAH,
                Currency.USD);
        CommissionEntity data = new CommissionEntity(commission);
        long id = 1L;
        data.setId(id);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));

        service.setCommission(commission);

        ArgumentCaptor<CommissionEntity> entityCaptor = ArgumentCaptor.forClass(CommissionEntity.class);
        Mockito.verify(repositoryMock)
                .saveAndFlush(entityCaptor.capture());

        Assertions.assertThat(entityCaptor.getAllValues())
                .hasSize(1);

        CommissionEntity capturedArgument = entityCaptor.getValue();
        checkCommission(capturedArgument);
        Assertions.assertThat(capturedArgument.getId())
                .isEqualTo(id);

        Mockito.verify(repositoryMock).findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void setCommissionWithNegativePercentTest() {
        Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100) - 100).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.UAH, Currency.USD);

        Assertions.assertThatExceptionOfType(CommissionException.class)
                .isThrownBy(() -> service.setCommission(commission));
    }

    @Test
    public void setCommissionWithBigPercentTest() {
        Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100) + 100).setScale(2, BigDecimal.ROUND_DOWN),
                Currency.UAH, Currency.USD);

        Assertions.assertThatExceptionOfType(CommissionException.class)
                .isThrownBy(() -> service.setCommission(commission));
    }

    @Test
    public void setCommissionWithSameCurrenciesTest() {
        Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN), Currency.UAH,
                Currency.UAH);

        Assertions.assertThatExceptionOfType(CommissionException.class)
                .isThrownBy(() -> service.setCommission(commission));
    }

    private void checkCommission(CommissionEntity capturedArgument) {
        Assertions.assertThat(capturedArgument.getCommissionPt())
                .isNotNull()
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                .isLessThan(BigDecimal.valueOf(100).setScale(2, BigDecimal.ROUND_DOWN));
        Assertions.assertThat(capturedArgument.getFrom())
                .isNotNull()
                .isNotEqualTo(capturedArgument.getTo());
        Assertions.assertThat(capturedArgument.getTo())
                .isNotNull();
    }
}
