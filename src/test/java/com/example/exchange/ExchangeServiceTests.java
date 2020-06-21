package com.example.exchange;

import com.example.exchange.exceptions.ExchangeException;
import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.ExchangeRequest;
import com.example.exchange.models.entities.ExchangeRateEntity;
import com.example.exchange.models.enums.Currency;
import com.example.exchange.models.enums.OperationType;
import com.example.exchange.repositories.ExchangeRateRepository;
import com.example.exchange.services.CommissionService;
import com.example.exchange.services.ExchangeServiceImpl;
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

public class ExchangeServiceTests {

    private final Random random = new Random();
    private final ExchangeRateRepository repositoryMock = Mockito.mock(ExchangeRateRepository.class);
    private final CommissionService commissionServiceMock = Mockito.mock(CommissionService.class);
    private final ExchangeServiceImpl service = new ExchangeServiceImpl(repositoryMock, commissionServiceMock);

    @Test
    public void calculateGiveExchangeRequest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) + 1), BigDecimal.ZERO, Currency.UAH,
                Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD)).thenReturn(coefficient);

        ExchangeRequest result = service.calculateExchange(request);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualToIgnoringGivenFields(request, "amountTo");

        BigDecimal expected = request.getAmountFrom()
                .multiply(data.getRate())
                .multiply(coefficient)
                .setScale(2, BigDecimal.ROUND_DOWN);
        Assertions.assertThat(result.getAmountTo()).isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock).findByFromAndTo(Mockito.any(), Mockito.any());
        Mockito.verify(commissionServiceMock).getCommissionCoefficient(Mockito.any(), Mockito.any());
    }

    @Test
    public void calculateGetExchangeTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.valueOf(random.nextInt(100) + 1), Currency.UAH,
                Currency.USD, OperationType.GET);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD)).thenReturn(coefficient);

        ExchangeRequest result = service.calculateExchange(request);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualToIgnoringGivenFields(request, "amountFrom");

        BigDecimal divisor = data.getRate()
                .multiply(coefficient)
                .setScale(2, BigDecimal.ROUND_DOWN);
        BigDecimal expected = request.getAmountTo()
                .divide(divisor, BigDecimal.ROUND_DOWN);
        Assertions.assertThat(result.getAmountFrom()).isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock).findByFromAndTo(Mockito.any(), Mockito.any());
        Mockito.verify(commissionServiceMock).getCommissionCoefficient(Mockito.any(), Mockito.any());
    }

    @Test
    public void calculateExchangeSameCurrenciesTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) + 1), BigDecimal.ZERO, Currency.UAH,
                Currency.UAH, OperationType.GIVE);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeNotSupportedTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) + 1), BigDecimal.ZERO, Currency.UAH,
                Currency.USD, OperationType.GIVE);
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGiveZeroTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD)).thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGiveNegativeTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) - 100), BigDecimal.ZERO, Currency.UAH,
                Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD)).thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGetZeroTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GET);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD)).thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGetNegativeTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.valueOf(random.nextInt(100) - 100), Currency.UAH,
                Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD)).thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void getAllExchangeRatesTest() {
        List<ExchangeRateEntity> data = new ArrayList<>();
        data.add(new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1), Currency.USD.toString()));
        data.add(new ExchangeRateEntity(2L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1), Currency.EUR.toString()));
        data.add(new ExchangeRateEntity(3L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1), Currency.RUB.toString()));
        Mockito.when(repositoryMock.findAll()).thenReturn(data);

        List<ExchangeRate> result = service.getAllExchangeRates();

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).doesNotContain(new ExchangeRate[]{null});
        Assertions.assertThat(result).hasSize(data.size());
        Assertions.assertThat(result).containsAll(data.stream().map(ExchangeRate::new).collect(Collectors.toList()));

        Mockito.verify(repositoryMock).findAll();
    }

    @Test
    public void getAllCommissionsEmptyTest() {
        List<ExchangeRateEntity> data = new ArrayList<>();
        Mockito.when(repositoryMock.findAll()).thenReturn(data);

        List<ExchangeRate> result = service.getAllExchangeRates();

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).doesNotContain(new ExchangeRate[]{null});
        Assertions.assertThat(result).isEmpty();

        Mockito.verify(repositoryMock).findAll();
    }

    @Test
    public void setNewExchangeRateTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100) + 1), Currency.USD);
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

        service.setExchangeRate(exchangeRate);

        ArgumentCaptor<ExchangeRateEntity> entityCaptor = ArgumentCaptor.forClass(ExchangeRateEntity.class);
        Mockito.verify(repositoryMock).save(entityCaptor.capture());
        Mockito.verify(repositoryMock).saveAndFlush(entityCaptor.capture());

        Assertions.assertThat(entityCaptor.getAllValues()).hasSize(2);

        ExchangeRateEntity capturedArgument = entityCaptor.getAllValues().get(0);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId()).isNull();
        Assertions.assertThat(capturedArgument.getFrom()).isEqualTo(exchangeRate.getFrom().toString());
        Assertions.assertThat(capturedArgument.getTo()).isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getRate()).isEqualByComparingTo(exchangeRate.getRate());

        capturedArgument = entityCaptor.getAllValues().get(1);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId()).isNull();
        Assertions.assertThat(capturedArgument.getFrom()).isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getTo()).isEqualTo(exchangeRate.getFrom().toString());

        BigDecimal expected = BigDecimal.ONE.divide(exchangeRate.getRate(), 5, BigDecimal.ROUND_DOWN);
        Assertions.assertThat(capturedArgument.getRate()).isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock, Mockito.times(2)).findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void updateExchangeRateTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100) + 1), Currency.USD);
        ExchangeRateEntity data = new ExchangeRateEntity(exchangeRate);
        data.setId(1L);
        ExchangeRateEntity reverseData = new ExchangeRateEntity(2L, Currency.USD.toString(), BigDecimal.valueOf(random.nextInt(100) + 1),
                Currency.UAH.toString());
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString())).thenReturn(Optional.of(data));
        Mockito.when(repositoryMock.findByFromAndTo(Currency.USD.toString(), Currency.UAH.toString())).thenReturn(Optional.of(reverseData));

        service.setExchangeRate(exchangeRate);

        ArgumentCaptor<ExchangeRateEntity> entityCaptor = ArgumentCaptor.forClass(ExchangeRateEntity.class);
        Mockito.verify(repositoryMock).save(entityCaptor.capture());
        Mockito.verify(repositoryMock).saveAndFlush(entityCaptor.capture());

        Assertions.assertThat(entityCaptor.getAllValues()).hasSize(2);

        ExchangeRateEntity capturedArgument = entityCaptor.getAllValues().get(0);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId()).isEqualTo(1L);
        Assertions.assertThat(capturedArgument.getFrom()).isEqualTo(exchangeRate.getFrom().toString());
        Assertions.assertThat(capturedArgument.getTo()).isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getRate()).isEqualByComparingTo(exchangeRate.getRate());

        capturedArgument = entityCaptor.getAllValues().get(1);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId()).isEqualTo(2L);
        Assertions.assertThat(capturedArgument.getFrom()).isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getTo()).isEqualTo(exchangeRate.getFrom().toString());

        BigDecimal expected = BigDecimal.ONE.divide(exchangeRate.getRate(), 5, BigDecimal.ROUND_DOWN);
        Assertions.assertThat(capturedArgument.getRate()).isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock, Mockito.times(2)).findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void setExchangeRateZeroTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.ZERO, Currency.USD);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.setExchangeRate(exchangeRate));
    }

    @Test
    public void setExchangeRateNegativeTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100) - 100), Currency.USD);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.setExchangeRate(exchangeRate));
    }

    @Test
    public void setExchangeRateSameCurrenciesTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100) + 1), Currency.UAH);

        Assertions.assertThatExceptionOfType(ExchangeException.class).isThrownBy(() -> service.setExchangeRate(exchangeRate));
    }

    private void checkExchangeRate(ExchangeRateEntity capturedArgument) {
        Assertions.assertThat(capturedArgument.getRate()).isNotNull();
        Assertions.assertThat(capturedArgument.getRate()).isGreaterThan(BigDecimal.ZERO);
        Assertions.assertThat(capturedArgument.getFrom()).isNotNull();
        Assertions.assertThat(capturedArgument.getTo()).isNotNull();
        Assertions.assertThat(capturedArgument.getFrom()).isNotEqualTo(capturedArgument.getTo());
    }
}
