package com.example.exchange;

import com.example.exchange.models.Commission;
import com.example.exchange.models.DataBaseUserPrincipal;
import com.example.exchange.models.ExchangeRate;
import com.example.exchange.models.ExchangeRequest;
import com.example.exchange.models.entities.UserEntity;
import com.example.exchange.models.enums.Currency;
import com.example.exchange.models.enums.OperationType;
import com.example.exchange.services.CommissionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExchangeApplicationTests {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;
	private final Random random = new Random();

	@Autowired
	private CommissionService commissionService;

	@BeforeEach
	public void setup() {
		random.nextInt();
		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(SecurityMockMvcConfigurers.springSecurity()) // enable security for the mock set up
				.build();
	}

	@Test
	@WithMockUser
	public void getCommissionsByUserTest() throws Exception {
		getCommissionsTest();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void getCommissionsByAdminTest() throws Exception {
		getCommissionsTest();
	}

	private void getCommissionsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.get("/api/commissions")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser
	public void setCommissionByUserTest() throws Exception {
		Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN), Currency.UAH,
				Currency.USD);

		mvc.perform(MockMvcRequestBuilders
				.post("/api/commissions")
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(commission))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void setCommissionTest() throws Exception {
		Commission commission = new Commission(BigDecimal.valueOf(random.nextInt(100)).setScale(2, BigDecimal.ROUND_DOWN), Currency.UAH,
				Currency.USD);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
				.post("/api/commissions")
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(commission))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();
		Commission result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Commission>(){});

		Assertions.assertThat(result)
				.isNotNull()
				.isEqualTo(commission);

		mvcResult = mvc.perform(MockMvcRequestBuilders
				.get("/api/commissions")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
				.andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();
		List<Commission> resultList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				new TypeReference<List<Commission>>(){});

		Assertions.assertThat(resultList)
				.isNotNull()
				.isNotEmpty()
				.doesNotContain(new Commission[]{null})
				.contains(commission);
	}

	@Test
	@WithMockUser
	public void exchangeRequestGiveTest() throws Exception {
		exchangeRequestTest(OperationType.GIVE);
	}

	@Test
	@WithMockUser
	public void exchangeRequestGetTest() throws Exception {
		exchangeRequestTest(OperationType.GET);
	}

	private void exchangeRequestTest(OperationType operationType) throws Exception {
		ExchangeRequest exchangeRequest;
		switch (operationType) {
			case GIVE: {
				exchangeRequest = new ExchangeRequest(
						BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN),
						BigDecimal.ZERO,
						Currency.UAH, Currency.USD, OperationType.GIVE
				);
				break;
			}
			case GET: {
				exchangeRequest = new ExchangeRequest(
						BigDecimal.ZERO,
						BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN),
						Currency.UAH, Currency.USD, OperationType.GET
				);
				break;
			}
			default: {
				throw new RuntimeException("Unsupported operation type: " + operationType);
			}
		}
		ExchangeRate exchangeRate = new ExchangeRate(exchangeRequest.getCurrencyFrom(),
				BigDecimal.valueOf(random.nextInt(100) + 1).setScale(5, BigDecimal.ROUND_DOWN), exchangeRequest.getCurrencyTo());
		UserDetails admin = new DataBaseUserPrincipal(new UserEntity(1L, "admin", "pass", "ROLE_ADMIN"));
		mvc.perform(MockMvcRequestBuilders
				.post("/api/exchange-rates")
				.with(SecurityMockMvcRequestPostProcessors.user(admin))
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(exchangeRate))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print());

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
				.post("/api/exchange")
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(exchangeRequest))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();
		ExchangeRequest result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				new TypeReference<ExchangeRequest>(){});

		BigDecimal coefficient = commissionService.getCommissionCoefficient(exchangeRequest.getCurrencyFrom(),
				exchangeRequest.getCurrencyTo());
		BigDecimal expected;
		switch (operationType) {
			case GIVE: {
				expected = exchangeRequest.getAmountFrom()
						.multiply(exchangeRate.getRate())
						.multiply(coefficient)
						.setScale(2, BigDecimal.ROUND_DOWN);
				break;
			}
			case GET: {
				BigDecimal divisor = exchangeRate.getRate()
						.multiply(coefficient)
						.setScale(2, BigDecimal.ROUND_DOWN);
				expected = exchangeRequest.getAmountTo()
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
						.isEqualToIgnoringGivenFields(exchangeRequest, "amountTo");
				Assertions.assertThat(result.getAmountTo())
						.isEqualByComparingTo(expected);
				break;
			}
			case GET: {
				Assertions.assertThat(result)
						.isEqualToIgnoringGivenFields(exchangeRequest, "amountFrom");
				Assertions.assertThat(result.getAmountFrom())
						.isEqualByComparingTo(expected);
				break;
			}
			default: {
				throw new RuntimeException("Unsupported operation type: " + operationType);
			}
		}
	}

	@Test
	@WithMockUser
	public void getExchangeRatesByUserTest() throws Exception {
		getExchangeRatesTest();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void getExchangeRatesByAdminTest() throws Exception {
		getExchangeRatesTest();
	}

	private void getExchangeRatesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders
				.get("/api/exchange-rates")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser
	public void setExchangeRateByUserTest() throws Exception {
		ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100)).setScale(5,
				BigDecimal.ROUND_DOWN), Currency.USD);

		mvc.perform(MockMvcRequestBuilders
				.post("/api/exchange-rates")
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(exchangeRate))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isUnauthorized())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void setExchangeRateTest() throws Exception {
		ExchangeRate exchangeRate = new ExchangeRate(Currency.UAH, BigDecimal.valueOf(random.nextInt(100)).setScale(5,
				BigDecimal.ROUND_DOWN), Currency.USD);

		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
				.post("/api/exchange-rates")
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(exchangeRate))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();
		ExchangeRate result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<ExchangeRate>(){});

		Assertions.assertThat(result)
				.isNotNull()
				.isEqualTo(exchangeRate);

		mvcResult = mvc.perform(MockMvcRequestBuilders
				.get("/api/exchange-rates")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
				.andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
				.andDo(MockMvcResultHandlers.print())
				.andReturn();
		List<ExchangeRate> resultList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
				new TypeReference<List<ExchangeRate>>(){});

		Assertions.assertThat(resultList)
				.isNotNull()
				.isNotEmpty()
				.doesNotContain(new ExchangeRate[]{null})
				.contains(exchangeRate);
	}
}
