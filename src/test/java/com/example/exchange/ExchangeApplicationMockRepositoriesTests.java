package com.example.exchange;

import com.example.exchange.models.Commission;
import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.entities.CommissionEntity;
import com.example.exchange.models.entities.ExchangeRateEntity;
import com.example.exchange.models.enums.Currency;
import com.example.exchange.repositories.CommissionRepository;
import com.example.exchange.repositories.ExchangeRateRepository;
import com.example.exchange.services.CommissionService;
import com.example.exchange.services.ExchangeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExchangeApplicationMockRepositoriesTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;
    private final Random random = new Random();

    @Autowired
    private CommissionService commissionService;
    @Autowired
    private ExchangeService exchangeService;
    @MockBean
    private CommissionRepository commissionRepositoryMock;
    @MockBean
    private ExchangeRateRepository exchangeRateRepositoryMock;

    @BeforeEach
    public void setup() {
        random.nextInt();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()) // enable security for the mock set up
                .build();
    }

    @WithMockUser
    @Test
    public void getCommissionsWithValuesTest() throws Exception {
        List<CommissionEntity> data = new ArrayList<>();
        data.add(new CommissionEntity(1L, BigDecimal.valueOf(random.nextInt(100)), Currency.UAH.toString(), Currency.USD.toString()));
        data.add(new CommissionEntity(2L, BigDecimal.valueOf(random.nextInt(100)), Currency.USD.toString(), Currency.UAH.toString()));
        data.add(new CommissionEntity(3L, BigDecimal.valueOf(random.nextInt(100)), Currency.UAH.toString(), Currency.EUR.toString()));
        data.add(new CommissionEntity(4L, BigDecimal.valueOf(random.nextInt(100)), Currency.EUR.toString(), Currency.UAH.toString()));
        Mockito.when(commissionRepositoryMock.findAll())
                .thenReturn(data);

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                .get("/api/commissions")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(data.size())))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        List<Commission> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<Commission>>(){});

        Assertions.assertThat(result).isNotNull()
                .hasSize(data.size())
                .doesNotContain(new Commission[]{null})
                .containsAll(data.stream()
                        .map(Commission::new)
                        .collect(Collectors.toList()));

        Mockito.verify(commissionRepositoryMock).findAll();
    }

    @WithMockUser
    @Test
    public void getExchangeRatesWithValuesTest() throws Exception {
        List<ExchangeRateEntity> data = new ArrayList<>();
        data.add(new ExchangeRateEntity(1L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1), Currency.USD.toString()));
        data.add(new ExchangeRateEntity(2L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1), Currency.EUR.toString()));
        data.add(new ExchangeRateEntity(3L, Currency.UAH.toString(), BigDecimal.valueOf(random.nextInt(100) + 1), Currency.RUB.toString()));
        Mockito.when(exchangeRateRepositoryMock.findAll())
                .thenReturn(data);

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                .get("/api/exchange-rates")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(data.size())))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        List<ExchangeRate> result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<ExchangeRate>>(){});

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(data.size());
        Assertions.assertThat(result).doesNotContain(new ExchangeRate[]{null});
        Assertions.assertThat(result).containsAll(data.stream()
                .map(ExchangeRate::new)
                .collect(Collectors.toList()));

        Mockito.verify(exchangeRateRepositoryMock).findAll();
    }
}
