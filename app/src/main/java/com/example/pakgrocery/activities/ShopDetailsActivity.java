package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.DefaultTaskExecutor;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.pakgrocery.Constants;
import com.example.pakgrocery.R;
import com.example.pakgrocery.adapters.AdapterCartItem;
import com.example.pakgrocery.adapters.AdapterProductUser;
import com.example.pakgrocery.models.ModelCartItem;
import com.example.pakgrocery.models.ModelProducts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity
{

    private TextView shopnameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv,cartCountTv;
    private ImageButton  callBtn,mapBtn,cartBtn,backBtn,filterProductBtn,reviewsBtn;
    private ImageView  shopIv;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RelativeLayout top,productsRl;

    private String myLatitude,myLongitude,myPhone;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProducts> productList;
    private RatingBar ratingBar;

    private ProgressDialog progressDialog;//progress dialogue
    private AdapterProductUser adapterProductUser;
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;
    public String deliveryFee;

    private EasyDB easyDB;
    private Animation topAnim,bottomAnim;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        shopUid = getIntent().getStringExtra("shopUid");

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_2);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_2);


        reviewsBtn = findViewById(R.id.reviewsBtn);
        cartCountTv = findViewById(R.id.cartCountTv);
        shopIv = findViewById(R.id.shopIv);
        shopnameTv = findViewById(R.id.shopnameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productsRv = findViewById(R.id.productsRv);
        ratingBar = findViewById(R.id.ratingBar);
        top = findViewById(R.id.top);
        productsRl = findViewById(R.id.productsRl);

        top.setAnimation(topAnim);
        productsRl.setAnimation(bottomAnim);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);




        //get uid of shop from Intent
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();
        easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();
        //delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    adapterProductUser.getFilter().filter(s);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialogue
                showCartDialogue();

            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Chose Category")
                        .setItems(Constants.options2, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String selected = Constants.options2[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All"))
                                {
                                    loadShopProducts();
                                }
                                else
                                {
                                    adapterProductUser.getFilter().filter(selected);
                                }

                            }
                        }).show();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid",shopUid);
                startActivity(intent);
            }
        });

    }


    private float ratingSum = 0;
    private void loadReviews()
    {

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
                        ratingBar.setRating(avgRating);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void deleteCartData()
    {
        //declare it to class level and init in onCreate

        easyDB.deleteAllDataFromTable();//delete all record from cart


    }
    public void cartCount()
    {
        //keep it public so we can access it in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0)
        {//hide cart count textview

            cartCountTv.setVisibility(View.GONE);

        }
        else
        {//have items in cart show cart count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);

        }
    }

    public double allTotalPrice = 0.00;
    // need to access these views in adapter so make them public
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;

    private void showCartDialogue()
     {
        //init list
        cartItemList = new ArrayList<>();

        //inflate car layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialogue_cart,null);
        //init views
        TextView shopnameTv = view.findViewById(R.id.shopnameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
         sTotalTv = view.findViewById(R.id.sTotalTv);
         dFeeTv = view.findViewById(R.id.dFeeTv);
         allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button  ceckOutBtn= view.findViewById(R.id.ceckOutBtn);



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);


        shopnameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext())
        {
            String id = cursor.getString(1);
            String pId = cursor.getString(2);
            String name = cursor.getString(3);
            String price = cursor.getString(4);
            String cost = cursor.getString(5);
            String quantity = cursor.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem(""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this,cartItemList);
        //set to recycler view
        cartItemsRv.setAdapter(adapterCartItem);


        dFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice + Double.parseDouble(deliveryFee.replace("$",""))));

        //show dialogue
        final AlertDialog dialog = builder.create();
        dialog.show();

        //set total price on dialogue dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;

            }
        });
        //place order
        ceckOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("")||myLongitude.equals("null"))
                {

                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile(use GPS button) before place order..", Toast.LENGTH_LONG).show();

                    return; //dont proceed further
                }
                if (myPhone.equals("") || myPhone.equals("null"))
                {

                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number in your profile before place order..", Toast.LENGTH_LONG).show();

                    return; //dont proceed further
                }
                if (cartItemList.size() == 0)
                {
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart..", Toast.LENGTH_SHORT).show();
                    return; //dont proceed further

                }

                submitOrder();
                dialog.dismiss();
            }
        });

    }

    private void submitOrder()
    {
        //show progress dialogue
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();

        //for order id and order time
        final String timeStamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("$","");//remove $ if contains

        HashMap<String ,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timeStamp);
        hashMap.put("orderTime",""+timeStamp);
        hashMap.put("orderStatus","InProgress"); //InProgress/Completed/Cancel
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);



        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(shopUid).child("Orders");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        //order info added now add order item

                        for (int i=0;i<cartItemList.size();i++)
                        {
                            String pId = cartItemList.get(i).getPid();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String price = cartItemList.get(i).getPrice();
                            String name = cartItemList.get(i).getName();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String ,String > hashMap1= new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);


                            ref.child(timeStamp).child("Items").child(pId).setValue(hashMap1);


                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Place Successfully...", Toast.LENGTH_SHORT).show();

                        prepareNotificationsMessaging(timeStamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void openMap()
    {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr="+ myLatitude +","+myLongitude+"&daddr="+shopLatitude+","+shopLongitude;
        Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);

    }

    private void dialPhone()
    {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                             myPhone = ""+ds.child("phone").getValue();
                            String profileImg = ""+ds.child("profileImg").getValue();

                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void loadShopDetails()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //get shop data
                String name = ""+snapshot.child("name").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress = ""+snapshot.child("address").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();
                deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String profileImg = ""+snapshot.child("profileImg").getValue();
                String shopOpen = ""+snapshot.child("shopOpen").getValue();
                //set data
                shopnameTv.setText(shopName);
                deliveryFeeTv.setText("Delivery Fee:$"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true"))
                {
                    openCloseTv.setText("Open");
                }
                else
                {
                    openCloseTv.setText("Closed");

                }
                try
                {
                    Picasso.get().load(profileImg).into(shopIv);

                }
                catch (Exception e)
                {


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void loadShopProducts()
    {

        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {//clear list before adding item
                        productList.clear();
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            ModelProducts modelProducts = ds.getValue(ModelProducts.class);
                            productList.add(modelProducts);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productList);
                        productsRv.setAdapter(adapterProductUser);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void prepareNotificationsMessaging(String orderId)
    {
        String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC; //must be same as subscribe
        String NOTIFICATION_TITLE = "New Order"+orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order. ";
        String NOTIFICATION_TYPE = "NewOrder";


        //prepare json
        JSONObject notificationjo = new JSONObject();
        JSONObject notificationBodyjo = new JSONObject();
        try {
            //what to send
            notificationBodyjo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyjo.put("buyerUid",firebaseAuth.getUid());//since we logged in as buyer to place order so current user uid is buyer uid
            notificationBodyjo.put("sellerUid",shopUid);
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
        sendFcmNotifications(notificationjo,orderId);


    }

    private void sendFcmNotifications(JSONObject notificationjo, final String orderId)
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationjo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailUserActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
                finish();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(ShopDetailsActivity.this, "Error in notifications", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ShopDetailsActivity.this,OrderDetailUserActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
                finish();


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