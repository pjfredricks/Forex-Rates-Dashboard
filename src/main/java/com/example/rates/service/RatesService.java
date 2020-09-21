package com.example.rates.service;

import com.example.rates.dao.ForexRates;

import java.util.List;

public interface RatesService {

	void updateRates();

	void addRates();
	
	List<ForexRates> fetchRates();

	void deleteRates();
}