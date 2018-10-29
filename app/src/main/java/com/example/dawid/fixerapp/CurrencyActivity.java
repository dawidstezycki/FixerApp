package com.example.dawid.fixerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CurrencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        Intent intent = getIntent();
        String name = intent.getStringExtra("currency_name");
        String value = intent.getStringExtra("currency_value");
        String date = intent.getStringExtra("currency_date");

        TextView itemName = (TextView) findViewById(R.id.item_name);
        TextView itemValue = (TextView) findViewById(R.id.item_value);
        TextView itemDate = (TextView) findViewById(R.id.item_date);

        itemName.setText(name);
        itemValue.setText(value);
        itemDate.setText(date);
    }
}
