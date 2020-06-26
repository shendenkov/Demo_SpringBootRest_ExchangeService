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
        calculateExchangeTest(OperationType.GIVE);
    }

    @Test
    public void calculateGetExchangeTest() {
        calculateExchangeTest(OperationType.GET);
    }

    public void calculateExchangeTest(OperationType operationType) {
        ExchangeRequest request;
        switch (operationType) {
            case GIVE: {
                request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN),
                        BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GIVE);
                break;
            }
            case GET: {
                request = new ExchangeRequest(BigDecimal.ZERO,
                        BigDecimal.valueOf(random.nextInt(100) + 1) .setScale(5, BigDecimal.ROUND_DOWN), Currency.UAH, Currency.USD,
                        OperationType.GET);
                break;
            }
            default: {
                throw new RuntimeException("Unsupported operation type: " + operationType);
            }
        }
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(),
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d).setScale(2, BigDecimal.ROUND_DOWN);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD))
                .thenReturn(coefficient);

        ExchangeRequest result = service.calculateExchange(request);

        BigDecimal expected;
        switch (operationType) {
            case GIVE: {
                expected = request.getAmountFrom()
                        .multiply(data.getRate())
                        .multiply(coefficient)
                        .setScale(2, BigDecimal.ROUND_DOWN);
                break;
            }
            case GET: {
                BigDecimal divisor = data.getRate()
                        .multiply(coefficient)
                        .setScale(2, BigDecimal.ROUND_DOWN);
                expected = request.getAmountTo()
                        .divide(divisor, BigDecimal.ROUND_DOWN);
                break;
            }
            default: {
                throw new RuntimeException("Unsupported operation type: " + operationType);
            }
        }
        Assertions.assertThat(result)
                .isNotNull();
        switch (operationType) {
            case GIVE: {
                Assertions.assertThat(result)
                        .isEqualToIgnoringGivenFields(request, "amountTo");
                Assertions.assertThat(result.getAmountTo())
                        .isEqualByComparingTo(expected);
                break;
            }
            case GET: {
                Assertions.assertThat(result)
                        .isEqualToIgnoringGivenFields(request, "amountFrom");
                Assertions.assertThat(result.getAmountFrom())
                        .isEqualByComparingTo(expected);
                break;
            }
            default: {
                throw new RuntimeException("Unsupported operation type: " + operationType);
            }
        }

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
        Mockito.verify(commissionServiceMock)
                .getCommissionCoefficient(Mockito.any(), Mockito.any());
    }

    @Test
    public void calculateExchangeSameCurrenciesTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN),
                BigDecimal.ZERO, Currency.UAH, Currency.UAH, OperationType.GIVE);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeNotSupportedTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN),
                BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GIVE);
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGiveZeroTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(),
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d).setScale(2, BigDecimal.ROUND_DOWN);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD))
                .thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGiveNegativeTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.valueOf(random.nextInt(100) - 100).setScale(5, BigDecimal.ROUND_DOWN),
                BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(),
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d).setScale(2, BigDecimal.ROUND_DOWN);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD))
                .thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGetZeroTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.ZERO, Currency.UAH, Currency.USD, OperationType.GET);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(),
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d).setScale(2, BigDecimal.ROUND_DOWN);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD))
                .thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void calculateExchangeGetNegativeTest() {
        ExchangeRequest request = new ExchangeRequest(BigDecimal.ZERO, BigDecimal.valueOf(random.nextInt(100) - 100).setScale(5,
                BigDecimal.ROUND_DOWN), Currency.UAH, Currency.USD, OperationType.GIVE);
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(),
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD.toString());
        BigDecimal coefficient = BigDecimal.valueOf(0.1d).setScale(2, BigDecimal.ROUND_DOWN);
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));
        Mockito.when(commissionServiceMock.getCommissionCoefficient(Currency.UAH, Currency.USD))
                .thenReturn(coefficient);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.calculateExchange(request));
    }

    @Test
    public void getAllExchangeRatesTest() {
        List<ExchangeRateEntity> data = new ArrayList<>();
        data.add(new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5,
                BigDecimal.ROUND_DOWN), Currency.USD.toString()));
        data.add(new ExchangeRateEntity(2L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5,
                BigDecimal.ROUND_DOWN), Currency.EUR.toString()));
        data.add(new ExchangeRateEntity(3L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5,
                BigDecimal.ROUND_DOWN), Currency.RUB.toString()));
        Mockito.when(repositoryMock.findAll())
                .thenReturn(data);

        List<ExchangeRate> result = service.getAllExchangeRates();

        Assertions.assertThat(result)
                .isNotNull()
                .hasSize(data.size())
                .doesNotContain(new ExchangeRate[]{null})
                .containsAll(data.stream()
                        .map(ExchangeRate::new)
                        .collect(Collectors.toList()));

        Mockito.verify(repositoryMock)
                .findAll();
    }

    @Test
    public void getAllExchangeRatesEmptyTest() {
        List<ExchangeRateEntity> data = new ArrayList<>();
        Mockito.when(repositoryMock.findAll())
                .thenReturn(data);

        List<ExchangeRate> result = service.getAllExchangeRates();

        Assertions.assertThat(result)
                .isNotNull()
                .isEmpty();

        Mockito.verify(repositoryMock)
                .findAll();
    }

    @Test
    public void getExchangeRateTest() {
        ExchangeRateEntity data = new ExchangeRateEntity(1L, Currency.UAH.toString(),
                BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN), Currency.USD.toString());
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));

        Optional<ExchangeRate> result = service.getExchangeRate(Currency.UAH, Currency.USD);

        Assertions.assertThat(result)
                .isNotNull()
                .isPresent()
                .get().isEqualTo(new ExchangeRate(data));

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void getExchangeRateNotFoundTest() {
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        Optional<ExchangeRate> result = service.getExchangeRate(Currency.UAH, Currency.USD);

        Assertions.assertThat(result)
                .isNotNull()
                .isNotPresent();

        Mockito.verify(repositoryMock)
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void setNewExchangeRateTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH,
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD);
        Mockito.when(repositoryMock.findByFromAndTo(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        service.setExchangeRate(exchangeRate);

        ArgumentCaptor<ExchangeRateEntity> entityCaptor = ArgumentCaptor.forClass(ExchangeRateEntity.class);
        Mockito.verify(repositoryMock)
                .save(entityCaptor.capture());
        Mockito.verify(repositoryMock)
                .saveAndFlush(entityCaptor.capture());

        Assertions.assertThat(entityCaptor.getAllValues())
                .hasSize(2);

        ExchangeRateEntity capturedArgument = entityCaptor.getAllValues().get(0);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId())
                .isNull();
        Assertions.assertThat(capturedArgument.getFrom())
                .isEqualTo(exchangeRate.getFrom().toString());
        Assertions.assertThat(capturedArgument.getTo())
                .isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getRate())
                .isEqualByComparingTo(exchangeRate.getRate());

        capturedArgument = entityCaptor.getAllValues().get(1);
        BigDecimal expected = BigDecimal.ONE
                .divide(exchangeRate.getRate(), 5, BigDecimal.ROUND_DOWN);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId())
                .isNull();
        Assertions.assertThat(capturedArgument.getFrom())
                .isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getTo())
                .isEqualTo(exchangeRate.getFrom().toString());
        Assertions.assertThat(capturedArgument.getRate())
                .isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock, Mockito.times(2))
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void updateExchangeRateTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH,
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.USD);
        ExchangeRateEntity data = new ExchangeRateEntity(exchangeRate);
        long firstId = 1L;
        data.setId(firstId);
        long secondId = 2L;
        ExchangeRateEntity reverseData = new ExchangeRateEntity(secondId, Currency.USD.toString(),
                BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), Currency.UAH.toString());
        Mockito.when(repositoryMock.findByFromAndTo(Currency.UAH.toString(), Currency.USD.toString()))
                .thenReturn(Optional.of(data));
        Mockito.when(repositoryMock.findByFromAndTo(Currency.USD.toString(), Currency.UAH.toString()))
                .thenReturn(Optional.of(reverseData));

        service.setExchangeRate(exchangeRate);

        ArgumentCaptor<ExchangeRateEntity> entityCaptor = ArgumentCaptor.forClass(ExchangeRateEntity.class);
        Mockito.verify(repositoryMock)
                .save(entityCaptor.capture());
        Mockito.verify(repositoryMock)
                .saveAndFlush(entityCaptor.capture());

        Assertions.assertThat(entityCaptor.getAllValues())
                .hasSize(2);

        ExchangeRateEntity capturedArgument = entityCaptor.getAllValues().get(0);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId())
                .isEqualTo(firstId);
        Assertions.assertThat(capturedArgument.getFrom())
                .isEqualTo(exchangeRate.getFrom().toString());
        Assertions.assertThat(capturedArgument.getTo())
                .isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getRate())
                .isEqualByComparingTo(exchangeRate.getRate());

        capturedArgument = entityCaptor.getAllValues().get(1);
        BigDecimal expected = BigDecimal.ONE
                .divide(exchangeRate.getRate(), 5, BigDecimal.ROUND_DOWN);
        checkExchangeRate(capturedArgument);
        Assertions.assertThat(capturedArgument.getId())
                .isEqualTo(secondId);
        Assertions.assertThat(capturedArgument.getFrom())
                .isEqualTo(exchangeRate.getTo().toString());
        Assertions.assertThat(capturedArgument.getTo())
                .isEqualTo(exchangeRate.getFrom().toString());
        Assertions.assertThat(capturedArgument.getRate())
                .isEqualByComparingTo(expected);

        Mockito.verify(repositoryMock, Mockito.times(2))
                .findByFromAndTo(Mockito.any(), Mockito.any());
    }

    @Test
    public void setExchangeRateZeroTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.ZERO, Currency.USD);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.setExchangeRate(exchangeRate));
    }

    @Test
    public void setExchangeRateNegativeTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100) - 100).setScale(5,
                BigDecimal.ROUND_DOWN), Currency.USD);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.setExchangeRate(exchangeRate));
    }

    @Test
    public void setExchangeRateSameCurrenciesTest() {
        ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5,
                BigDecimal.ROUND_DOWN), Currency.UAH);

        Assertions.assertThatExceptionOfType(ExchangeException.class)
                .isThrownBy(() -> service.setExchangeRate(exchangeRate));
    }

    private void checkExchangeRate(ExchangeRateEntity capturedArgument) {
        Assertions.assertThat(capturedArgument.getRate())
                .isNotNull()
                .isGreaterThan(BigDecimal.ZERO);
        Assertions.assertThat(capturedArgument.getFrom())
                .isNotNull()
                .isNotEqualTo(capturedArgument.getTo());
        Assertions.assertThat(capturedArgument.getTo())
                .isNotNull();
    }
}
