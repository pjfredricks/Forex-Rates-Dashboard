package com.example.rates.repository;

import com.example.rates.dao.DailyRatesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatesRepository extends JpaRepository<DailyRatesData, Integer> {

}