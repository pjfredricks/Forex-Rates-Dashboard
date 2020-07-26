package com.example.rates.service.impl;

import com.example.rates.dao.DailyRatesData;
import com.example.rates.dao.ForexRates;
import com.example.rates.repository.RatesRepository;
import com.example.rates.service.RatesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RatesServiceImpl implements RatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatesServiceImpl.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Set<String> countryList = Stream.of("INR", "USD", "AUD", "GBP", "EUR", "CAD",
            "CNY", "SAR", "SGD", "MYR", "THB",
            "IDR", "ILS", "JPY", "KRW", "CHF",
            "PHP", "FJD", "HKD", "ZAR", "SEK",
            "NOK", "DKK", "NZD", "BHD", "OMR",
            "KWD").collect(Collectors.toSet());
    private static final Set<String> noCarouselCountryList = Stream.of("INR", "ILS", "JPY", "KRW", "CHF",
            "PHP", "FJD", "HKD", "ZAR",
            "SEK", "NOK", "DKK", "NZD",
            "BHD", "OMR", "KWD").collect(Collectors.toSet());

    private static final ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Number> currencyValues = new HashMap<>();
    private List<ForexRates> exchangeRates = new ArrayList<>();
    private List<DailyRatesData> dailyRatesDataList = new ArrayList<>();

    private final RatesRepository ratesRepository;

    public RatesServiceImpl(RatesRepository ratesRepository) {
        this.ratesRepository = ratesRepository;
    }

    @PostConstruct
    public void onStartup() {
        try {
            exchangeRates = mapper.readValue(
                    Thread
                            .currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("countryData/Country_Details.json"),
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, ForexRates.class));
        } catch (IOException e) {
            exchangeRates = new ArrayList<>();
        }
        updateRates();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledTask() {
        LOGGER.info("Updating Database with latest Rates @ Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        updateRates();
    }

    @Override
    public void updateRates() {
        // Removes old values from both collections
        currencyValues.clear();
        dailyRatesDataList.clear();

        // Updates latest forex currency values
        getExchangeRates();
        double inrValue = currencyValues.get("INR").doubleValue();

        // Remove unwanted countries and convert rates
        currencyValues.keySet().retainAll(countryList);
        currencyValues.entrySet().forEach(currencyValue -> currencyValue.setValue(currencyValue.getValue().doubleValue() / inrValue));
        currencyValues.replaceAll((countryCode, currencyValue) -> convertRate(1 / currencyValue.doubleValue(), 6));

        // Set buy and sell Rates, and update carousel values
        exchangeRates = exchangeRates.stream()
                .map(forexRate -> setCarouselAndRates(forexRate, currencyValues))
                .sorted(Comparator.comparing(ForexRates::getCountryName))
                .collect(Collectors.toList());

        updateDailyRates();
        saveDailyRates();
    }

    @Override
    public List<ForexRates> fetchRates() {
        if (exchangeRates.isEmpty() || dailyRatesDataList.isEmpty()) {
            updateRates();
        }
        return exchangeRates;
    }

    private void updateDailyRates() {
        exchangeRates.forEach(exchangeRate -> {
            DailyRatesData dailyRatesData = new DailyRatesData();
            BeanUtils.copyProperties(exchangeRate, dailyRatesData);
            dailyRatesDataList.add(dailyRatesData);
        });
    }

    private void saveDailyRates() {
        if (!dailyRatesDataList.isEmpty()) {
            dailyRatesDataList.forEach(ratesData -> {
                ratesData.setCreateDate(LocalDateTime.now().toString());
                ratesRepository.save(ratesData);
            });
        }
    }

    private ForexRates setCarouselAndRates(ForexRates forexRate, Map<String, Number> currencyValues) {
        forexRate.setCarousel(true);

        if (noCarouselCountryList.contains(forexRate.getCountryCode())) {
            forexRate.setCarousel(false);
        }
        if (ObjectUtils.isNotEmpty(currencyValues.get(forexRate.getCountryCode()))) {
            forexRate.setBuyRate(
                    calculateBuyRate(currencyValues.get(forexRate.getCountryCode()).doubleValue()));
            forexRate.setSellRate(
                    calculateSellRate(currencyValues.get(forexRate.getCountryCode()).doubleValue()));
        }
        return forexRate;
    }

    private double calculateBuyRate(double currencyValue) {
        int percent = 2;

        if (currencyValue < 0.01) {
            return convertRate(currencyValue, 4);
        }
        if (currencyValue < 5.000) percent = 6;
        else if (currencyValue < 30.000) percent = 5;
        else if (currencyValue < 80.000) percent = 3;

        currencyValue = currencyValue - (currencyValue / 100) * percent;
        return convertRate(currencyValue, 2);
    }

    private double calculateSellRate(double currencyValue) {
        int percent = 1;

        if (currencyValue < 0.01) {
            return convertRate(currencyValue, 4);
        }

        if (currencyValue < 5.000) percent = 4;
        else if (currencyValue < 30.000) percent = 3;
        else if (currencyValue < 80.000) percent = 2;

        currencyValue = currencyValue - (currencyValue / 100) * percent;
        return convertRate(currencyValue, 2);
    }

    private double convertRate(double currencyValue, int scalingValue) {
        return BigDecimal.valueOf(currencyValue).setScale(scalingValue, RoundingMode.HALF_EVEN).doubleValue();
    }

    private void getExchangeRates() {
        try {
            currencyValues = (Map<String, Number>) new RestTemplate()
                    .getForEntity("https://api.exchangerate-api.com/v4/latest/INR", LinkedHashMap.class)
                    .getBody()
                    .get("rates");
        } catch (Exception e) {
            LOGGER.error("Error fetching data from exchange rate api @ Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        }
    }
}