package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    //UI views
    private EditText emailEt,passwordEt;
    private Button forgotTv,noAccountTv;
    private Button loginBtn;
    private ImageView iconIv;
    private TextView signInFirst;
    private Animation topAnim,bottomAnim;
    private RelativeLayout half;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_2);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_2);
        //init ui views
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        forgotTv =  findViewById(R.id.forgotTv);
        loginBtn = findViewById(R.id.loginBtn);
        noAccountTv =  findViewById(R.id.noAccountTv);
        iconIv =  findViewById(R.id.iconIv);
        signInFirst =  findViewById(R.id.signInFirst);
        half =  findViewById(R.id.half);

        half.setAnimation(topAnim);
        emailEt.setAnimation(bottomAnim);
        passwordEt.setAnimation(bottomAnim);
        forgotTv.setAnimation(bottomAnim);
        loginBtn.setAnimation(bottomAnim);
        noAccountTv.setAnimation(bottomAnim);





        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);



        noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterUserActivity.class);
                Pair[] pairs = new Pair[5];
                //pairs[0] = new Pair<View,String>(iconIv,"logo");
                pairs[0] = new Pair<View,String>(signInFirst,"logo_text");
                pairs[1] = new Pair<View,String>(emailEt,"email");
                pairs[2] = new Pair<View,String>(passwordEt,"password");
                pairs[3] = new Pair<View,String>(loginBtn,"login_btn");
                pairs[4] = new Pair<View,String>(noAccountTv,"another_btn");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this,pairs);
                    startActivity(intent,options.toBundle());
                }
                else
                {
                    startActivity(intent);

                }

            }
        });
        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));


            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });




    }

    private String email,password;


    private void loginUser()
    {

        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {

            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
        return;
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Enter password....", Toast.LENGTH_SHORT).show();
        return;
        }


        progressDialog.setMessage("Logging In..");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult)
                    {
                        //logged in successfully
                        makeMeOnlie();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed logging in
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void makeMeOnlie()
    {
        progressDialog.setMessage("Checking User...");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","true");


        // update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        //update succesfuly
                        checkUserType();



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //update failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUserType()
    {
        //if user is seller ,start seller main screen
        //if user buyer ,start user main activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren())
                        {
                            String accountType = ""+ds.child("accountType").getValue();
                        if (accountType.equals("Seller"))
                        {
                            //user is seller
                            progressDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                            finish();

                        }
                        else
                        {
                            //user is buyer

                            progressDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                            finish();
                        }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}