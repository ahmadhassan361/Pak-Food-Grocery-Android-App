package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity
{
    private ImageButton back;
    private String shopUid;
    private ImageView profileIv;
    private TextView shopnameTv;
    private EditText reviewEt;
    private RatingBar ratingBar;
    private FloatingActionButton submitBtn;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        back = findViewById(R.id.back);
        profileIv = findViewById(R.id.profileIv);
        shopnameTv = findViewById(R.id.shopnameTv);
        reviewEt = findViewById(R.id.reviewEt);
        ratingBar = findViewById(R.id.ratingBar);
        submitBtn = findViewById(R.id.submitBtn);


        shopUid = getIntent().getStringExtra("shopUid");



        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();

        loadMyReview();



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private void loadShopInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String shopName = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("profileImg").getValue();
                //set data
                shopnameTv.setText(shopName);

                try
                {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(profileIv);

                }
                catch (Exception e)
                {
                    profileIv.setImageResource(R.drawable.ic_store_gray);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            //set data
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEt.setText(review);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData()
    {
        String ratings = ""+ratingBar.getRating();
        String review = reviewEt.getText().toString().trim();
        String timeStamp = ""+System.currentTimeMillis();


        //setup data in hashmap
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timeStamp);


        //upload to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(WriteReviewActivity.this, "Review Published Successfully...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

    }
}