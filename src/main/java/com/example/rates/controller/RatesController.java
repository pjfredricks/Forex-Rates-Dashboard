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
    public ResponseEntity<String> updateRates() {
        ratesService.updateRates();
        return new ResponseEntity<>("Rates have been updated", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/rates")
    public ResponseEntity<String> addRates() {
        ratesService.addRates();
        return new ResponseEntity<>("Rates have been updated", HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/rates")
    public ResponseEntity<List<ForexRates>> fetchRates() {
        return new ResponseEntity<>(ratesService.fetchRates(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/rates")
    public ResponseEntity<String> deleteRates() {
        ratesService.deleteRates();
        return new ResponseEntity<>("Rates have been deleted", HttpStatus.ACCEPTED);
    }
}