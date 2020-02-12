package com.example.dshinde.myapplication_xmlpref.model;

import java.math.BigDecimal;

public class CafeSellSummary {
    public String customer;
    public int tea=0;
    public int coffe=0;
    public BigDecimal amountPaid = BigDecimal.ZERO;
    public BigDecimal amountDue = BigDecimal.ZERO;

    public CafeSellSummary(String customer){
        this.customer = customer;
    }
}
