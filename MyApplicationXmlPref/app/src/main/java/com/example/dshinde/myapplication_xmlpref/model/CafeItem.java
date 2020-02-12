package com.example.dshinde.myapplication_xmlpref.model;

import java.math.BigDecimal;

public class CafeItem {
    public String sellDateTime;
    public int tea;
    public int coffe;
    public String paid;
    public BigDecimal teaRate;
    public BigDecimal coffeRate;
    public BigDecimal amountDue;
    public BigDecimal amountPaid;

    public CafeItem(String sellDateTime, int tea, int coffe, String paid, BigDecimal teaRate, BigDecimal coffeRate){
        this.coffe = coffe;
        this.paid = paid;
        this.sellDateTime = sellDateTime;
        this.tea = tea;
        this.teaRate = teaRate;
        this.coffeRate = coffeRate;
        BigDecimal teaTotal = teaRate.multiply(new BigDecimal(tea));
        BigDecimal coffeTotal = coffeRate.multiply(new BigDecimal(coffe));
        this.amountDue = teaTotal.add(coffeTotal);
        if(paid.equals("Yes")){
            this.amountPaid = amountDue;
            this.amountDue = BigDecimal.ZERO;
        } else {
            this.amountPaid = BigDecimal.ZERO;
        }

    }
}
