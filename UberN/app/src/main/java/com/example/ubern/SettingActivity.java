package com.example.ubern;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
//import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    private String getType;
    private ImageView profile,save,close;
    private EditText edtName,edtPhone,edtCar;
    private TextView changeProfileBtn;
    private  String Checker="";
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        getType= getIntent().getStringExtra("type");
        Toast.makeText(this, getType, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users/"+getType);
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference("Profile Pictures");


        profile = findViewById(R.id.profile_img);
        save = findViewById(R.id.save_btn);
        close = findViewById(R.id.close_btn);

        edtName=findViewById(R.id.edtName);
        edtPhone=findViewById(R.id.edtPhone);
        edtCar=findViewById(R.id.edtCarName);
        if(getType.equals("Drivers")){
            edtCar.setVisibility(View.VISIBLE);
        }
        changeProfileBtn=findViewById(R.id.changeImg);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getType.equals("Drivers")){
                    startActivity(new Intent(SettingActivity.this,DriversMapActivity.class));
                }
                else
                {
                    startActivity(new Intent(SettingActivity.this,CustomersMapActivity.class));
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Checker.equals("clicked")){
                    validateControllers();
                }
                else{
                    validateAndSaveOnlyInformation();
                }
            }
        });
        changeProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Checker="clicked";
//                CropImage.acctivity().setAspectRatio(1,1).start(SettingActivity.this);
            }
        });


    }

    private void validateAndSaveOnlyInformation() {
        if(TextUtils.isEmpty(edtName.getText().toString())){
            Toast.makeText(this, "Please provide your name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(edtPhone.getText().toString())){
            Toast.makeText(this, "Please provide your phone", Toast.LENGTH_SHORT).show();
        }
        else if(getType.equals("Drivers") && TextUtils.isEmpty(edtCar.getText().toString())){
            Toast.makeText(this, "Please provide your car name", Toast.LENGTH_SHORT).show();
        }else {
            HashMap<String,Object> userMap = new HashMap<>();
            userMap.put("uid",mAuth.getCurrentUser().getUid());
            userMap.put("name",edtName.getText().toString());
            userMap.put("phone",edtPhone.getText().toString());

            if(getType.equals("Drivers")){
                userMap.put("car",edtCar.getText().toString());
            }
            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            if(getType.equals("Drivers")){
                startActivity(new Intent(SettingActivity.this,DriversMapActivity.class));
            }
            else {
                startActivity(new Intent(SettingActivity.this,CustomersMapActivity.class));
            }
        }

    }
    private void  getUserInformation(){
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&& snapshot.getChildrenCount()>0){
                    String name = snapshot.child("name").getValue().toString();
                    String phone= snapshot.child("phone").getValue().toString();

                    edtName.setText(name);
                    edtPhone.setText(phone);
                    if(getType.equals("Drivers")){
                        String car = snapshot.child("car").getValue().toString();
                        edtCar.setText(car);
                    }

                    if(snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
//                        Picasso.get().load(image).into(profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data!=null){
//            CropImage.ActivityResult result = CropImage.getActivitiResult(data);
//
//            imageUri = result.getUri();
//            profile.setImageURI(imageUri);
//        }
//        else{
            if(getType.equals("Drivers")){
                startActivity(new Intent(SettingActivity.this,DriversMapActivity.class));
            }
            else
            {
                startActivity(new Intent(SettingActivity.this,CustomersMapActivity.class));
            }
            Toast.makeText(this, "Error, Try again", Toast.LENGTH_SHORT).show();
        }

//    }
    private  void validateControllers()
    {
        if(TextUtils.isEmpty(edtName.getText().toString())){
            Toast.makeText(this, "Please provide your name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(edtPhone.getText().toString())){
            Toast.makeText(this, "Please provide your phone", Toast.LENGTH_SHORT).show();
        }
        else if(getType.equals("Drivers") && TextUtils.isEmpty(edtCar.getText().toString())){
            Toast.makeText(this, "Please provide your car name", Toast.LENGTH_SHORT).show();
        }
        else if (Checker.equals("clicked"))
        {
            UploadProfilePicture();
        }
    }

    private void UploadProfilePicture() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Account Information");
        progressDialog.setMessage("Please wait, while we are settings your account information");
        progressDialog.show();
        if(imageUri !=null){
            final StorageReference fileRef = storageProfilePicsRef
                    .child(mAuth.getCurrentUser().getUid()+".jpg" );
            uploadTask=fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl=downloadUrl.toString();

                        HashMap<String,Object> userMap = new HashMap<>();
                        userMap.put("uid",mAuth.getCurrentUser().getUid());
                        userMap.put("name",edtName.getText().toString());
                        userMap.put("phone",edtPhone.getText().toString());
                        userMap.put("image",myUrl);
                        if(getType.equals("Drivers")){
                            userMap.put("car",edtCar.getText().toString());
                        }
                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        progressDialog.dismiss();
                        if(getType.equals("Drivers")){
                            startActivity(new Intent(SettingActivity.this,DriversMapActivity.class));
                        }
                        else {
                            startActivity(new Intent(SettingActivity.this,CustomersMapActivity.class));
                        }

                    }
                }
            });
        }else{
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }


    }
}