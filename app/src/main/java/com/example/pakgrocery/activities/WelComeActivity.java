package com.example.pakgrocery.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.pakgrocery.Constants;
import com.example.pakgrocery.R;

public class WelComeActivity extends AppCompatActivity
{
    private Animation topAnim,bottomAnim;
    private RelativeLayout toolbarRl,bottom;
    private Button loginBtn,client,seller;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wel_come);


        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_2);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.top_2);

        toolbarRl = findViewById(R.id.toolbarRl);
        bottom = findViewById(R.id.bottom);
        loginBtn = findViewById(R.id.loginBtn);
        client = findViewById(R.id.clientBtn);
        seller = findViewById(R.id.sellerBtn);


        toolbarRl.setAnimation(topAnim);
        bottom.setAnimation(bottomAnim);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(WelComeActivity.this);
                builder.setTitle("Privacy Policy");
                builder.setMessage(Constants.policy)
                        .setNegativeButton("Decline",null)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                Intent intent = new Intent(WelComeActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        }).show();


            }
        });
        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WelComeActivity.this);
                builder.setTitle("Privacy Policy");
                builder.setMessage(Constants.policy)
                        .setNegativeButton("Decline",null)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                Intent intent = new Intent(WelComeActivity.this, RegisterUserActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        }).show();

            }
        });
        seller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WelComeActivity.this);
                builder.setTitle("Privacy Policy");
                builder.setMessage(Constants.policy)
                        .setNegativeButton("Decline",null)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                Intent intent = new Intent(WelComeActivity.this, RegisterSellerActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        }).show();

            }
        });

    }
}