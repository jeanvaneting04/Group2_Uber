package com.example.ubern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity {

    TextView txtCustomerStatus, txtCustomerRegisterLink, txtCustomerBackToLogin;
    Button btnCustomerLogin, btnCustomerRegister;
    EditText edtCustomerEmail, edtCustomerPass;
    ProgressDialog loadingBar;
    FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;
    private String OnlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        mAuth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);
        txtCustomerStatus = findViewById(R.id.txtCustomerStatus);
        txtCustomerRegisterLink = findViewById(R.id.txtCustomerRegisterLink);
        btnCustomerLogin = findViewById(R.id.btnCustomerLogin);
        btnCustomerRegister = findViewById(R.id.btnCustomerRegister);
        txtCustomerBackToLogin = findViewById(R.id.txtCustomerBackToLogin);
        edtCustomerEmail = findViewById(R.id.edtCustomerEmail);
        edtCustomerPass = findViewById(R.id.edtCustomerPass);

        txtCustomerBackToLogin.setVisibility(View.INVISIBLE);
        btnCustomerRegister.setVisibility(View.INVISIBLE);
        btnCustomerRegister.setEnabled(false);

        txtCustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtCustomerStatus.setText("Customer Register");
                btnCustomerLogin.setVisibility(View.INVISIBLE);
                txtCustomerRegisterLink.setVisibility(View.INVISIBLE);
                txtCustomerBackToLogin.setVisibility(View.VISIBLE);

                txtCustomerBackToLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        txtCustomerBackToLogin.setVisibility(View.INVISIBLE);
                        btnCustomerRegister.setVisibility(View.INVISIBLE);
                        btnCustomerRegister.setEnabled(false);
                        txtCustomerStatus.setText("Customer Login");
                        txtCustomerRegisterLink.setVisibility(View.VISIBLE);
                        txtCustomerBackToLogin.setVisibility(View.INVISIBLE);
                        btnCustomerLogin.setVisibility(View.VISIBLE);
                    }
                });

                btnCustomerRegister.setVisibility(View.VISIBLE);
                btnCustomerRegister.setEnabled(true);
            }
        });
        btnCustomerRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtCustomerEmail.getText().toString();
                String password = edtCustomerPass.getText().toString();

                RegisterCustomer(email, password);
            }
        });
        btnCustomerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtCustomerEmail.getText().toString();
                String password = edtCustomerPass.getText().toString();

                LoginCustomer(email, password);
            }
        });
    }

    private void LoginCustomer(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(CustomerLoginActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(CustomerLoginActivity.this, "Vui lòng nhập password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Đăng nhập khách ");
            loadingBar.setMessage("Vui lòng đợi .......");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent customerIntent = new Intent(CustomerLoginActivity.this, CustomersMapActivity.class);
                                startActivity(customerIntent);

                                Toast.makeText(CustomerLoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(CustomerLoginActivity.this, "Đăng nhập thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void RegisterCustomer(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(CustomerLoginActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(CustomerLoginActivity.this, "Vui lòng nhập password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Đăng ký");
            loadingBar.setMessage("Vui lòng đợi .......");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                OnlineCustomerID = mAuth.getCurrentUser().getUid();
                                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Customers")
                                        .child(OnlineCustomerID);
                                CustomerDatabaseRef.setValue(true);

                                Intent driverIntent = new Intent(CustomerLoginActivity.this, CustomersMapActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(CustomerLoginActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(CustomerLoginActivity.this, "Đăng ký thất bại, xin vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}