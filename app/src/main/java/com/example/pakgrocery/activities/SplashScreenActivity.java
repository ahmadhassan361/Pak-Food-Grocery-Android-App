package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pakgrocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView imageView;
    TextView name,slogen;

    Animation topAnim,bottomAnim;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        imageView = findViewById(R.id.icon);
        name = findViewById(R.id.hh);
        slogen = findViewById(R.id.slogan);


        imageView.setAnimation(topAnim);
        name.setAnimation(bottomAnim);
        slogen.setAnimation(bottomAnim);

        firebaseAuth = FirebaseAuth.getInstance();
        //start login activity after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null)
                {
                    //user not logging in start login activity
                    Intent intent = new Intent(SplashScreenActivity.this,WelComeActivity.class);
                    startActivity(intent);
                    finish();

                }
                else
                {
                    //user logging and check account type
                    checkUserType();

                }



            }
        },1000 );
    }

    private void checkUserType()
    {
        //if user is seller ,start seller main screen
        //if user buyer ,start user main activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();
                        if (accountType.equals("Seller"))
                        {
                            //user is seller
                            startActivity(new Intent(SplashScreenActivity.this, MainSellerActivity.class));
                            finish();

                        }
                        else
                        {
                            //user is buyer

                            startActivity(new Intent(SplashScreenActivity.this, MainUserActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}