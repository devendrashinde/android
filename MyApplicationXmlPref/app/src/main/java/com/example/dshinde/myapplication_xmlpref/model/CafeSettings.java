package com.example.dshinde.myapplication_xmlpref.model;

import java.math.BigDecimal;

public class CafeSettings {
    public String rateDate;
    public BigDecimal teaRate;
    public BigDecimal coffeRate;

    public CafeSettings(String rateDate, BigDecimal teaRate, BigDecimal coffeRate){
        this.rateDate = rateDate;
        this.coffeRate = coffeRate;
        this.teaRate = teaRate;
    }
}
