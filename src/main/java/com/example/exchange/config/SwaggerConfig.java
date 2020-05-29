package com.example.exchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String TAG_COMMISSIONS = "commissions";
    public static final String TAG_EXCHANGE = "exchange";
    public static final String TAG_EXCHANGE_RATES = "exchange-rates";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.exchange"))
                .paths(PathSelectors.regex("/api.*"))
                .build()
                .apiInfo(apiEndPointsInfo())
                .tags(new Tag(TAG_COMMISSIONS, "Значения комиссий взымаемых при обмене валют. Значение процента комиссии можно " +
                        "задать для каждой валютной пары. Валидные значения в диапазоне от 0.00 до 100.00."))
                .tags(new Tag(TAG_EXCHANGE, "Обмен валют. Позволяет получать информацию по суммам при прямом и обратном обмене " +
                        "валют с учетом комисии. Пример прямого обмена: обменять 100 USD на EUR, в этом случае запрос должен содержать " +
                        "объект вида: {\"amountFrom\": 100.00,\"currencyFrom\": \"USD\",\"currencyTo\": \"EUR\"," +
                        "\"operationType\":\"GIVE\"}. В ответ должен вернуться полностью заполненый объект. Пример обратного обмена: " +
                        "узнать сколько нужно USD для того чтобы получить в результате обмена 100 EUR, в этом случае запрос должен " +
                        "содержать объект вида: {\"amountTo\": 100.00,\"currencyFrom\": \"USD\",\"currencyTo\": \"EUR\"," +
                        "\"operationType\":\"GET\"}"))
                .tags(new Tag(TAG_EXCHANGE_RATES, "Значение курсов обмена валют. Позволяют устанавливать и получать список " +
                        "курсов обмена валют для всех валютных пар. При установке курса обмена по любой из пар обратный курс должен быть " +
                        "установлен автоматически."))
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(basicAuthScheme()))
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder()
                .title("Exchange service")
                .description("Сервис обмена валют. Позволяет задавать курсы валют и комиссию за обмен по каждой валютной паре. " +
                        "На основании этих данных сервис позволяет получать суммы для прямого и обратного обмена валют.")
                .contact(new Contact(null, null, "s.vasnev@advcash.com"))
                .version("1.0")
                .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(basicAuthReference()))
                .forPaths(PathSelectors.any())
                .build();
    }

    private SecurityReference basicAuthReference() {
        return new SecurityReference("basicAuth", new AuthorizationScope[0]);
    }

    private SecurityScheme basicAuthScheme() {
        return new BasicAuth("basicAuth");
    }
}
