package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.pakgrocery.R;
import com.example.pakgrocery.adapters.AdapterReview;
import com.example.pakgrocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity
{
    private String shopUid;
    private ImageButton backBtn;
    private TextView shopnameTv,ratingTv;
    private ImageView profileIv;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        shopUid = getIntent().getStringExtra("shopUid");

        backBtn = findViewById(R.id.backBtn);
        ratingTv = findViewById(R.id.ratingTv);
        shopnameTv = findViewById(R.id.shopnameTv);
        profileIv = findViewById(R.id.profileIv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewsRv = findViewById(R.id.reviewsRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails();
        loadReview();




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



    }

    private float ratingSum = 0;
    private void loadReview()
    {
        reviewArrayList  = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        reviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;
                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);

                        }
                        adapterReview = new AdapterReview(ShopReviewsActivity.this,reviewArrayList);
                        reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;
                        ratingTv.setText(String.format("%.2f",avgRating)+"["+numberOfReviews+"]");
                        ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String profileImg = ""+snapshot.child("profileImg").getValue();

                        //set data
                        shopnameTv.setText(shopName);

                        try
                        {
                            Picasso.get().load(profileImg).placeholder(R.drawable.ic_store_gray).into(profileIv);

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
}