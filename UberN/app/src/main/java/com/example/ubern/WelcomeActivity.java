package com.example.ubern;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button btnDriver,btnCustomer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        btnDriver=findViewById(R.id.btnDriver);
        btnCustomer=findViewById(R.id.btnCustomer);
        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginDriverIntent=new Intent(WelcomeActivity.this, DriverLoginActivity.class);
                startActivity(loginDriverIntent);
            }
        });
        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginCustomerIntent=new Intent(WelcomeActivity.this, CustomerLoginActivity.class);
                startActivity(loginCustomerIntent);
            }
        });
    }
}