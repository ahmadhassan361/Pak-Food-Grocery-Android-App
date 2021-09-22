package com.example.pakgrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakgrocery.Constants;
import com.example.pakgrocery.R;
import com.example.pakgrocery.activities.ShopDetailsActivity;
import com.example.pakgrocery.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>
{
    private Context context;
    public ArrayList<ModelShop> shopsList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);


        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position)
    {
        //get data
        ModelShop modelShop = shopsList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String state = modelShop.getState();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String name = modelShop.getName();
        String shopName = modelShop.getShopName();
        String online = modelShop.getOnline();
        String profileImg = modelShop.getProfileImg();
        String phone = modelShop.getPhone();
        String shopOpen = modelShop.getShopOpen();
        String timestamp = modelShop.getTimestamp();
        final String uid = modelShop.getUid();

        loadReviews(modelShop,holder);

        holder.shopnameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        if (online.equals("true"))
        {//shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else
        {//shop owner offline
            holder.onlineIv.setVisibility(View.GONE);
        }

        if (shopOpen.equals("true"))
        {
            holder.shopClosedTv.setVisibility(View.GONE);
        }
        else
        {
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }
        try
        {
            Picasso.get().load(profileImg).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);

        }
        catch (Exception e)
        {
            holder.shopIv.setImageResource(R.drawable.ic_store_gray);

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);

            }
        });





    }
    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop, final HolderShop holder)
    {
        String shopUid = modelShop.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;


                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;
                        holder.ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return shopsList.size();  //return number of records
    }

    class HolderShop extends RecyclerView.ViewHolder
    {
        private ImageView shopIv,onlineIv;
        private TextView shopClosedTv,shopnameTv,phoneTv,addressTv;
        private RatingBar ratingBar;

        //ui views of row_shop.xml
        public HolderShop(@NonNull View itemView)
        {
            super(itemView);

            //init ui views
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopnameTv = itemView.findViewById(R.id.shopnameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);




        }
    }
}
