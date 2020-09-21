package com.example.rates.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Table(name = "dailyRatesData")
@Entity
@Data
public class DailyRatesData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private int id;

    @Column(name = "forexRate", nullable = false)
    private double forexRate;

    @Column(name = "countryCode", nullable = false)
    private String countryCode;

    @Column(name = "countryName", nullable = false)
    private String countryName;

    @Column(name = "currencyName", nullable = false)
    private String currencyName;

    @Column(name = "createDate", nullable = false)
    private String createDate;
}
