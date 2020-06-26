package com.example.exchange.controllers;

import com.example.exchange.config.SwaggerConfig;
import com.example.exchange.models.ApiError;
import com.example.exchange.models.Commission;
import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.ExchangeRequest;
import com.example.exchange.services.CommissionService;
import com.example.exchange.services.ExchangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = {SwaggerConfig.TAG_COMMISSIONS, SwaggerConfig.TAG_EXCHANGE, SwaggerConfig.TAG_EXCHANGE_RATES})
@Log4j2
@RestController
@RequestMapping("api")
public class ExchangeController {

    @Autowired
    private CommissionService commissionService;
    @Autowired
    private ExchangeService exchangeService;

    @ApiOperation(value = "Получить список установленных комиссий", tags = {SwaggerConfig.TAG_COMMISSIONS})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Commission.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @GetMapping(value = "commissions", produces = "application/json")
    public ResponseEntity<List<Commission>> getCommissions() {
        log.info("Received GET commissions");
        List<Commission> results = commissionService.getAllCommissions();
        log.info(results.size() + " commissions in database");
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @ApiOperation(value = "Установить значение комиссии для валютной пары", tags = {SwaggerConfig.TAG_COMMISSIONS})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = Commission.class),
            @ApiResponse(code = 400, message = "Error", response = ApiError.class),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping(value = "commissions", consumes = "application/json", produces = "application/json")
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<Commission> setCommission(@RequestBody Commission commission) throws Exception {
        log.info("Received POST commissions");
        commissionService.setCommission(commission);
        log.info("New commission " + commission.getCommissionPt() + "% was set for exchange from " + commission.getFrom() + " to " + commission.getTo());
        return new ResponseEntity<>(commission, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Запрос обмена валют", tags = {SwaggerConfig.TAG_EXCHANGE}, produces = "*/*")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ExchangeRequest.class),
            @ApiResponse(code = 400, message = "Error", response = ApiError.class),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping(value = "exchange", consumes = "application/json")
    public ExchangeRequest exchangeRequest(@RequestBody ExchangeRequest exchangeRequest) {
        log.info("Received POST exchange");
        ExchangeRequest exchangeResponse = exchangeService.calculateExchange(exchangeRequest);
        log.info("Exchange response: " + exchangeResponse);
        return exchangeResponse;
    }

    @ApiOperation(value = "Получить все курсы обмена валют", tags = {SwaggerConfig.TAG_EXCHANGE_RATES})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ExchangeRate.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @GetMapping(value = "exchange-rates", produces = "application/json")
    public ResponseEntity<List<ExchangeRate>> getExchangeRates() {
        log.info("Received GET exchange-rates");
        List<ExchangeRate> results = exchangeService.getAllExchangeRates();
        log.info(results.size() + " rates in database");
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @ApiOperation(value = "Установить курс обмена валют по валютной паре. Курс обратной пары должен быть установлен автоматически.",
            tags = {SwaggerConfig.TAG_EXCHANGE_RATES})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ExchangeRate.class),
            @ApiResponse(code = 400, message = "Error", response = ApiError.class),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @PostMapping(value = "exchange-rates", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ExchangeRate> setExchangeRate(@RequestBody ExchangeRate exchangeRate) {
        log.info("Received POST exchange-rates");
        exchangeService.setExchangeRate(exchangeRate);
        log.info("New rate " + exchangeRate.getRate() + " was set for exchange from " + exchangeRate.getFrom() + " to " + exchangeRate.getTo());
        return new ResponseEntity<>(exchangeRate, HttpStatus.OK);
    }
}
