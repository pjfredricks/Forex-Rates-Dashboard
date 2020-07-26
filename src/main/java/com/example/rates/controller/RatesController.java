package com.example.rates.controller;

import com.example.rates.dao.ForexRates;
import com.example.rates.service.RatesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class RatesController {

    private final RatesService ratesService;

    public RatesController(RatesService ratesService) {
        this.ratesService = ratesService;
    }

    @PutMapping(path = "/rates")
    public ResponseEntity<String> refreshRates() {
        ratesService.updateRates();
        return new ResponseEntity<>("Rates have been updated", HttpStatus.OK);
    }

    @GetMapping(path = "/rates")
    public ResponseEntity<List<ForexRates>> fetchRates() {
        return new ResponseEntity<>(ratesService.fetchRates(), HttpStatus.OK);
    }
}