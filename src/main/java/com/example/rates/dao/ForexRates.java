package com.example.rates.dao;

import lombok.Data;

@Data
public class ForexRates {
    private String countryCode;
    private String countryName;
    private double forexRate;
    private double latitude;
    private double longitude;
    private String currencyName;
}
