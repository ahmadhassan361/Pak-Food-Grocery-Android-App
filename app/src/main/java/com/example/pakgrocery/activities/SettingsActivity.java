package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakgrocery.Constants;
import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity
{
    private SwitchCompat fcmSwitch;
    private TextView notificationsStatusTv;
    private ImageButton backBtn,policy;

    private static final String enabledMessage = "Notifications are Enabled";
    private static final String disabledMessage = "Notifications are Disabled";

    private boolean isChecked = false;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationsStatusTv = findViewById(R.id.notificationsStatusTv);
        fcmSwitch = findViewById(R.id.fcmSwitch);
        backBtn = findViewById(R.id.backBtn);
        policy = findViewById(R.id.policy);


        firebaseAuth = FirebaseAuth.getInstance();

        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        isChecked = sp.getBoolean("FCM_ENABLED",true);
        fcmSwitch.setChecked(isChecked);
        if (isChecked)
        {
            notificationsStatusTv.setText(enabledMessage);
        }
        else
        {
            notificationsStatusTv.setText(disabledMessage);
        }

                backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

      //  subScribeToTopic();

        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    subScribeToTopic();

                }
                else
                {
                    unSubscribeToTopic();

                }
            }
        });



        policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this,PolicyActivity.class);
                startActivity(intent);
            }
        });

    }
    private void subScribeToTopic()
    {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationsStatusTv.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
    private void unSubscribeToTopic()
    {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationsStatusTv.setText(disabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}