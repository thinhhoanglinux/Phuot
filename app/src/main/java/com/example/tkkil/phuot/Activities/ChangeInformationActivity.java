package com.example.tkkil.phuot.Activities;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.tkkil.phuot.Models.User;
import com.example.tkkil.phuot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangeInformationActivity extends AppCompatActivity {
    private static final String TAG = "ChangeInformationActivi";
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private EditText edtFullname, edtBirthday, edtPhone, edtAddress, edtUser;
    Button btnChangeInfo;
    private RelativeLayout main;
    User userRoot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_information);
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        init();
        initToolbar();
        myRef.child("Users/"+mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userRoot = dataSnapshot.getValue(User.class);
                if(userRoot!=null){
                    edtUser.setText(userRoot.getUsername());
                    edtFullname.setText(userRoot.getFullname());
                    edtBirthday.setText(userRoot.getBirthday());
                    edtPhone.setText(userRoot.getPhone());
                    edtAddress.setText(userRoot.getAddress());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        main = findViewById(R.id.main);
        edtUser = findViewById(R.id.edtUser);
        edtFullname = findViewById(R.id.edtFullname);
        edtBirthday = findViewById(R.id.edtBirthday);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnChangeInfo = findViewById(R.id.btnChangeInfo);

        btnChangeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Validate()){
                    return;
                }
                onChangeInfo();
            }
        });
    }

    private void onChangeInfo() {
        User user = new User();
        user.setFullname(edtFullname.getText().toString().trim());
        user.setBirthday(edtBirthday.getText().toString().trim());
        user.setPhone(edtPhone.getText().toString().trim());
        user.setAddress(edtAddress.getText().toString().trim());
        user.setUsername(edtUser.getText().toString().trim());
        user.setEmail(mAuth.getCurrentUser().getEmail());
        user.setUid(mAuth.getCurrentUser().getUid());
        user.setAvatar(userRoot.getAvatar());
        myRef.child("Users/"+mAuth.getCurrentUser().getUid()).setValue(user)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Snackbar.make(main,"Changed",Snackbar.LENGTH_SHORT).show();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(main,"Error: " + e.getMessage(),Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private boolean Validate(){
        boolean isValidate=true;
        if(TextUtils.isEmpty(edtFullname.getText().toString().trim())){
            edtFullname.setError("Please enter Full name");
            isValidate=false;
        }
        if(TextUtils.isEmpty(edtBirthday.getText().toString().trim())){
            edtBirthday.setError("Please enter Birthday");
            isValidate=false;
        }
        if(TextUtils.isEmpty(edtPhone.getText().toString().trim())){
            edtPhone.setError("Please enter Phone");
            isValidate=false;
        }
        if(TextUtils.isEmpty(edtAddress.getText().toString().trim())){
            edtAddress.setError("Please enter Address");
            isValidate=false;
        }
        return isValidate;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
