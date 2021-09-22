package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.pakgrocery.Constants;
import com.example.pakgrocery.R;
import com.example.pakgrocery.adapters.AdapterOrdereItem;
import com.example.pakgrocery.models.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdersDetailsSellerActivity extends AppCompatActivity
{
    String orderId,orderBy;
    private ImageButton backBtn,editBtn,mapBtn;
    private TextView orderIdTv,dateTv,statusTv,phoneTv,nameTv,totalItemsTv,amountTv,addressTv;
    private RecyclerView itemsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrdereItem adapterOrdereItem;

    String  sourceLatitude,sourceLongitude,destinationLatitude,destinationLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_details_seller);

        orderId = getIntent().getStringExtra("orderId");
        orderBy =  getIntent().getStringExtra("orderBy");

        mapBtn = findViewById(R.id.mapBtn);
        editBtn = findViewById(R.id.editBtn);
        backBtn = findViewById(R.id.backBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        statusTv = findViewById(R.id.statusTv);
        phoneTv = findViewById(R.id.phoneTv);
        nameTv = findViewById(R.id.nameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openMap();

            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOrderedStatusDialogue();
            }
        });
    }

    private void editOrderedStatusDialogue()
    {
        final String[] optionns ={"Received","Delivered in 30 Minutes","Delivered in 45 Minutes","Delivered in 1 hour","Delivered in 1:30 minutes","Delivered in 2 hours","InProgress","Completed","Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(optionns, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String selectedOptions = optionns[which];

                        editOrderStatus(selectedOptions);

                    }
                })
                .show();
    }

    private void editOrderStatus(final String selectedOptions)
    {
        HashMap<String ,Object> hashMap= new HashMap<>();
        hashMap.put("orderStatus",""+selectedOptions);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        String message = "Order is Now "+selectedOptions;
                        Toast.makeText(OrdersDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();

                        prepareNotificationsMessaging(orderId,message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                        Toast.makeText(OrdersDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }



    private void openMap()
    {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr="+ sourceLatitude +","+sourceLongitude+"&daddr="+destinationLatitude+","+destinationLongitude;
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);

    }

    private void loadMyInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        sourceLatitude = ""+snapshot.child("latitude").getValue();
                        sourceLongitude = ""+snapshot.child("longitude").getValue();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadBuyerInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        destinationLatitude = ""+snapshot.child("latitude").getValue();
                        destinationLongitude = ""+snapshot.child("longitude").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String phone = ""+snapshot.child("phone").getValue();


                        nameTv.setText(name);
                        phoneTv.setText(phone);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadOrderDetails()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();


                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();
                        switch (orderStatus) {
                            case "InProgress":
                                statusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                                break;
                            case "Completed":
                                statusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                                break;
                            case "Cancelled":
                                statusTv.setTextColor(getResources().getColor(R.color.colorRed));
                                break;
                            default:
                                statusTv.setTextColor(getResources().getColor(R.color.colorGray02));
                                break;
                        }

                        orderIdTv.setText(orderId);
                        statusTv.setText(orderStatus);
                        amountTv.setText("$"+orderCost+"Delivery Fee Inluded");
                        dateTv.setText(formatDate);
                        findAddress(latitude,longitude);




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


        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat,lon,1);
            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);

        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }
    private void loadOrderedItems()
    {
        orderedItemArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
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
                        adapterOrdereItem = new AdapterOrdereItem(OrdersDetailsSellerActivity.this,orderedItemArrayList);
                        itemsRv.setAdapter(adapterOrdereItem);
                        totalItemsTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void prepareNotificationsMessaging(String orderId,String message)
    {
        String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC; //must be same as subscribe
        String NOTIFICATION_TITLE = "Your Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";


        //prepare json
        JSONObject notificationjo = new JSONObject();
        JSONObject notificationBodyjo = new JSONObject();
        try {
            //what to send
            notificationBodyjo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyjo.put("buyerUid",orderBy);//since we logged in as buyer to place order so current user uid is buyer uid
            notificationBodyjo.put("sellerUid",firebaseAuth.getUid());
            notificationBodyjo.put("orderId",orderId);
            notificationBodyjo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyjo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //where to send
            notificationjo.put("to",NOTIFICATION_TOPIC);
            notificationjo.put("data",notificationBodyjo);


        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        sendFcmNotifications(notificationjo);


    }

    private void sendFcmNotifications(JSONObject notificationjo)
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationjo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);



                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }


}