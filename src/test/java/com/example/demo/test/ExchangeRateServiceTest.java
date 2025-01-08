package com.example.demo.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entity.ExchangeRateEntity;
import com.example.demo.repository.ExchangeRateRepository;
import com.example.demo.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;

class ExchangeRateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchAndStoreLatestForexRate_ShouldSaveLatestRate_WhenApiReturnsData() throws Exception {
        // Arrange
        String jsonResponse = """
                [
        			  {"Date":"20241210","USD/NTD":"32.44","RMB/NTD":"4.47114","EUR/USD":"1.0541","USD/JPY":"151.785","GBP/USD":"1.2741","AUD/USD":"0.6383","USD/HKD":"7.7755","USD/RMB":"7.2554","USD/ZAR":"17.8758","NZD/USD":"0.58155"},
        		      {"Date":"20241211","USD/NTD":"32.51","RMB/NTD":"4.467524","EUR/USD":"1.04985","USD/JPY":"151.675","GBP/USD":"1.27425","AUD/USD":"0.63555","USD/HKD":"7.7762","USD/RMB":"7.27695","USD/ZAR":"17.85145","NZD/USD":"0.57755"},
                      {"Date":"20241212","USD/NTD":"32.488","RMB/NTD":"4.472753","EUR/USD":"1.0513","USD/JPY":"152.525","GBP/USD":"1.27625","AUD/USD":"0.64135","USD/HKD":"7.77495","USD/RMB":"7.26355","USD/ZAR":"17.67845","NZD/USD":"0.58055"},
                      {"Date":"20241213","USD/NTD":"32.508","RMB/NTD":"4.461658","EUR/USD":"1.04705","USD/JPY":"152.87","GBP/USD":"1.26345","AUD/USD":"0.637","USD/HKD":"7.77375","USD/RMB":"7.2861","USD/ZAR":"17.82075","NZD/USD":"0.5768"}, 
                      {"Date":"20241216","USD/NTD":"32.485","RMB/NTD":"4.457039","EUR/USD":"1.0504","USD/JPY":"153.485","GBP/USD":"1.263","AUD/USD":"0.6372","USD/HKD":"7.7736","USD/RMB":"7.28845","USD/ZAR":"17.8564","NZD/USD":"0.57805"},
                      {"Date":"20241217","USD/NTD":"32.515","RMB/NTD":"4.460213","EUR/USD":"1.04965","USD/JPY":"154.095","GBP/USD":"1.26935","AUD/USD":"0.63495","USD/HKD":"7.77015","USD/RMB":"7.29","USD/ZAR":"17.903","NZD/USD":"0.57615"}, 
                      {"Date":"20241218","USD/NTD":"32.481","RMB/NTD":"4.453632","EUR/USD":"1.04915","USD/JPY":"153.545","GBP/USD":"1.26835","AUD/USD":"0.63125","USD/HKD":"7.7704","USD/RMB":"7.29315","USD/ZAR":"18.08515","NZD/USD":"0.57325"}, 
                      {"Date":"20241219","USD/NTD":"32.668","RMB/NTD":"4.468884","EUR/USD":"1.04005","USD/JPY":"156.525","GBP/USD":"1.26205","AUD/USD":"0.62365","USD/HKD":"7.76885","USD/RMB":"7.3101","USD/ZAR":"18.26845","NZD/USD":"0.5643"},
	                  {"Date":"20241220","USD/NTD":"32.691","RMB/NTD":"4.474744","EUR/USD":"1.03915","USD/JPY":"156.765","GBP/USD":"1.25015","AUD/USD":"0.62475","USD/HKD":"7.77045","USD/RMB":"7.30565","USD/ZAR":"18.30765","NZD/USD":"0.5644"}, 
	                  {"Date":"20241223","USD/NTD":"32.688","RMB/NTD":"4.471816","EUR/USD":"1.0416","USD/JPY":"156.68","GBP/USD":"1.25625","AUD/USD":"0.6255","USD/HKD":"7.76985","USD/RMB":"7.3098","USD/ZAR":"18.32615","NZD/USD":"0.56525"}, 
	                  {"Date":"20241224","USD/NTD":"32.67","RMB/NTD":"4.470497","EUR/USD":"1.0395","USD/JPY":"157.085","GBP/USD":"1.2535","AUD/USD":"0.6241","USD/HKD":"7.7664","USD/RMB":"7.3079","USD/ZAR":"18.56725","NZD/USD":"0.56465"}, 
	                  {"Date":"20241225","USD/NTD":"32.668","RMB/NTD":"4.475973","EUR/USD":"1.04065","USD/JPY":"157.335","GBP/USD":"1.255","AUD/USD":"0.62405","USD/HKD":"7.767","USD/RMB":"7.29855","USD/ZAR":"18.66525","NZD/USD":"0.56585"}, 
	                  {"Date":"20241226","USD/NTD":"32.696","RMB/NTD":"4.473434","EUR/USD":"1.0397","USD/JPY":"157.37","GBP/USD":"1.25325","AUD/USD":"0.62355","USD/HKD":"7.76765","USD/RMB":"7.3089","USD/ZAR":"18.6219","NZD/USD":"0.5644"}, 
	                  {"Date":"20241227","USD/NTD":"32.72","RMB/NTD":"4.478681","EUR/USD":"1.0425","USD/JPY":"157.83","GBP/USD":"1.25315","AUD/USD":"0.62255","USD/HKD":"7.76165","USD/RMB":"7.3057","USD/ZAR":"18.76915","NZD/USD":"0.5637"}, 
	                  {"Date":"20241230","USD/NTD":"32.717","RMB/NTD":"4.476144","EUR/USD":"1.04235","USD/JPY":"157.92","GBP/USD":"1.25705","AUD/USD":"0.62325","USD/HKD":"7.76235","USD/RMB":"7.3092","USD/ZAR":"18.72035","NZD/USD":"0.56575"}, 
	                  {"Date":"20241231","USD/NTD":"32.781","RMB/NTD":"4.476213","EUR/USD":"1.04095","USD/JPY":"156.135","GBP/USD":"1.2548","AUD/USD":"0.6216","USD/HKD":"7.76465","USD/RMB":"7.3234","USD/ZAR":"18.7176","NZD/USD":"0.56295"}, 
	                  {"Date":"20250102","USD/NTD":"32.868","RMB/NTD":"4.486909","EUR/USD":"1.03575","USD/JPY":"156.695","GBP/USD":"1.25105","AUD/USD":"0.62065","USD/HKD":"7.77565","USD/RMB":"7.3253","USD/ZAR":"18.7963","NZD/USD":"0.5608"}, 
	                  {"Date":"20250103","USD/NTD":"32.917","RMB/NTD":"4.478983","EUR/USD":"1.02845","USD/JPY":"157.275","GBP/USD":"1.23965","AUD/USD":"0.62125","USD/HKD":"7.77795","USD/RMB":"7.3492","USD/ZAR":"18.7189","NZD/USD":"0.5602"}, 
	                  {"Date":"20250106","USD/NTD":"32.862","RMB/NTD":"4.468903","EUR/USD":"1.03375","USD/JPY":"157.69","GBP/USD":"1.24655","AUD/USD":"0.62345","USD/HKD":"7.77465","USD/RMB":"7.3535","USD/ZAR":"18.692","NZD/USD":"0.56285"}
                ]
                """;
        byte[] jsonResponseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(jsonResponseBytes, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://openapi.taifex.com.tw/v1/DailyForeignExchangeRates"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                eq(byte[].class)
        )).thenReturn(responseEntity);

        ArgumentCaptor<ExchangeRateEntity> entityCaptor = ArgumentCaptor.forClass(ExchangeRateEntity.class);

        // Act
        exchangeRateService.fetchAndStoreLatestForexRate();

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(byte[].class));
    }

}
