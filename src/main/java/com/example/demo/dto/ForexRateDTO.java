package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForexRateDto {
	
	@JsonProperty("Date")
    private String date;

    @JsonProperty("USD/NTD")
    private String rate;

    private String currencyPair = "USD/NTD"; // 固定為 "USD/NTD"
    
}
