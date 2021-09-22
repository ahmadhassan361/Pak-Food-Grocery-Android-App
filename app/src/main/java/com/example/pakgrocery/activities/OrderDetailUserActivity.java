package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pakgrocery.R;
import com.example.pakgrocery.adapters.AdapterOrdereItem;
import com.example.pakgrocery.models.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailUserActivity extends AppCompatActivity
{

    private String orderTo,orderId;
    //ui views
    private ImageButton back,writeReviewBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,shopNameTv,totalItemsTv,amountTv,addresTv;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrdereItem adapterOrdereItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_user);

       back = findViewById(R.id.back);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addresTv = findViewById(R.id.addresTv);
        itemsRv = findViewById(R.id.itemsRv);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        firebaseAuth = FirebaseAuth.getInstance();

        final Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");//contain uid of shop where we place order
        orderId = intent.getStringExtra("orderId");

        loadShopInfo();
        loadOrderedDetails();
        loadOrderedItems();


        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OrderDetailUserActivity.this,WriteReviewActivity.class);
                intent1.putExtra("shopUid",orderTo);
                startActivity(intent1);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
finish();            }
        });
    }

    private void loadOrderedItems()
    {
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        orderedItemArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);

                            orderedItemArrayList.add(modelOrderedItem);

                        }
                        //all items added to list
                        //setup adapter
                        adapterOrdereItem = new AdapterOrdereItem(OrderDetailUserActivity.this,orderedItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrdereItem);

                        //set items count
                        totalItemsTv.setText(""+snapshot.getChildrenCount());


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadOrderedDetails()
    {
        //load orders detail
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //get data
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

                        //convert timeStamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();// set date & time format


                        switch (orderStatus) {
                            case "InProgress":
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));

                                break;
                            case "Completed":
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));

                                break;
                            case "Cancelled":
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));

                                break;

                            default:
                                orderStatusTv.setTextColor(getResources().getColor(R.color.colorGray02));
                                break;
                        }
                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$"+orderCost+" Delivery fee included");
                        dateTv.setText(formatDate);

                        findAddress(latitude,longitude);






                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo()
    {
        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void findAddress(String latitude, String longitude)
    {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find address,city,state,country
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());


        try
        {
            addresses = geocoder.getFromLocation(lat,lon,1);

            String address = addresses.get(0).getAddressLine(0);//complete address
            addresTv.setText(address);

        }
        catch (Exception e)
        {

        }

    }

}