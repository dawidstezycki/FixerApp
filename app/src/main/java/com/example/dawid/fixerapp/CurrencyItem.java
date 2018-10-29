package com.example.dawid.fixerapp;

public class CurrencyItem {
    private String mCurrency;
    private String mValue;
    private String mDate;

    public CurrencyItem(String currency, String value, String date){
        mCurrency = currency;
        mValue = value;
        mDate = date;
    }

    public String getCurrency(){
        return mCurrency;
    }

    public String getValue(){
        return mValue;
    }

    public String getDate(){
        return mDate;
    }
}
