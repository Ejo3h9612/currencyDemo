package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entity.ExchangeRateEntity;
import com.example.demo.repository.ExchangeRateRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExchangeRateServiceTest {

    @MockBean
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testFetchAndStoreExchangeRates() {
        // 模擬 API 回應
        List<Map<String, Object>> mockResponse = List.of(
            Map.of("CurrencyPair", "USD/NTD", "Rate", "30.5", "Date", "2024-01-01 18:00:00")
        );
        ResponseEntity<List<Map<String, Object>>> response = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
            Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.isNull(),
            Mockito.<ParameterizedTypeReference<List<Map<String, Object>>>>any()
        )).thenReturn(response);

        // 執行服務方法
        exchangeRateService.fetchAndStoreForexRates();

        // 驗證儲存行為
        ArgumentCaptor<ExchangeRateEntity> captor = ArgumentCaptor.forClass(ExchangeRateEntity.class);
        Mockito.verify(exchangeRateRepository, Mockito.times(1)).save(captor.capture());

        ExchangeRateEntity savedRate = captor.getValue();
        Assertions.assertEquals("USD/NTD", savedRate.getCurrencyPair());
        Assertions.assertEquals(new BigDecimal("30.5"), savedRate.getRate());
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 1, 18, 0, 0), savedRate.getTimestamp());
    }
}

