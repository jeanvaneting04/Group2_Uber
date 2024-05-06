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

public class DriverLoginActivity extends AppCompatActivity {

    TextView txtDriverStatus,txtDriverRegisterLink,txtDriverBackToLogin;
    Button btnDriverLogin,btnDriverRegister;
    EditText edtDriverEmail,edtDriverPass;
    FirebaseAuth mAuth;
    private DatabaseReference DriverDatabaseRef;
    private  String OnlineDriverID;
    ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        mAuth=FirebaseAuth.getInstance();

        loadingBar=new ProgressDialog(this);
        txtDriverStatus=findViewById(R.id.txtDriverStatus);
        txtDriverRegisterLink=findViewById(R.id.txtDriverRegisterLink);
        btnDriverLogin=findViewById(R.id.btnDriverLogin);
        btnDriverRegister=findViewById(R.id.btnDriverRegister);
        txtDriverBackToLogin=findViewById(R.id.txtDriverBackToLogin);
        edtDriverEmail=findViewById(R.id.edtDriverEmail);
        edtDriverPass=findViewById(R.id.edtDriverPass);

        txtDriverBackToLogin.setVisibility(View.INVISIBLE);
        btnDriverRegister.setVisibility(View.INVISIBLE);
        btnDriverRegister.setEnabled(false);

        txtDriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDriverStatus.setText("Driver Register");
                btnDriverLogin.setVisibility(View.INVISIBLE);
                txtDriverRegisterLink.setVisibility(View.INVISIBLE);
                txtDriverBackToLogin.setVisibility(View.VISIBLE);

                txtDriverBackToLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        txtDriverBackToLogin.setVisibility(View.INVISIBLE);
                        btnDriverRegister.setVisibility(View.INVISIBLE);
                        btnDriverRegister.setEnabled(false);
                        txtDriverStatus.setText("Driver Login");
                        txtDriverRegisterLink.setVisibility(View.VISIBLE);
                        txtDriverBackToLogin.setVisibility(View.INVISIBLE);
                        btnDriverLogin.setVisibility(View.VISIBLE);
                    }
                });

                btnDriverRegister.setVisibility(View.VISIBLE);
                btnDriverRegister.setEnabled(true);
            }
        });
        btnDriverRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=edtDriverEmail.getText().toString();
                String password=edtDriverPass.getText().toString();

                RegisterDriver(email,password);
            }
        });
        btnDriverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=edtDriverEmail.getText().toString();
                String password=edtDriverPass.getText().toString();

                LoginDriver(email,password);
            }
        });
    }

    private void LoginDriver(String email, String password) {
        if(email.isEmpty()){
            Toast.makeText(DriverLoginActivity.this,"Vui lòng nhập email",Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty()){
            Toast.makeText(DriverLoginActivity.this,"Vui lòng nhập password",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Đăng nhập tài xế !");
            loadingBar.setMessage("Vui lòng đợi .......");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent driverIntent=new Intent(DriverLoginActivity.this, DriversMapActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(DriverLoginActivity.this,"Đăng nhập thành công !",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                Toast.makeText(DriverLoginActivity.this,"Đăng nhập thất bại, vui lòng thử lại ",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void RegisterDriver(String email, String password) {
        if(email.isEmpty()){
            Toast.makeText(DriverLoginActivity.this,"Vui lòng nhập email",Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty()){
            Toast.makeText(DriverLoginActivity.this,"Vui lòng nhập password",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Đăng ký tài xế !");
            loadingBar.setMessage("Vui lòng đợi ........");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                OnlineDriverID = mAuth.getCurrentUser().getUid();
                                DriverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Drivers")
                                        .child(OnlineDriverID);
                                DriverDatabaseRef.setValue(true);

                                Intent driverIntent = new Intent(DriverLoginActivity.this,DriversMapActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(DriverLoginActivity.this,"Đăng ký tài xế thành công! ",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                Toast.makeText(DriverLoginActivity.this,"Đăng ký tài xế thất bại, xin vui lòng thử lại!",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}