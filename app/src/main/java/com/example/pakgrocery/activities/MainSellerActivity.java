package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakgrocery.adapters.AdapterOrderShop;
import com.example.pakgrocery.adapters.AdapterProductSeller;
import com.example.pakgrocery.Constants;
import com.example.pakgrocery.models.ModelOrderShop;
import com.example.pakgrocery.models.ModelProducts;
import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainSellerActivity extends AppCompatActivity
{
    private TextView filteredOrdersTv,nameTv,shopnameTv,emailTv,tabProductsTv,tabOrdersTv,filteredProductTv;
    private FirebaseAuth firebaseAuth;
private ProgressDialog progressDialog;
private ImageView profileIv;
private RelativeLayout productsRl,ordersRl;
private RecyclerView productsRv,ordersRv;
private ArrayList<ModelProducts> productList;
private AdapterProductSeller adapterProductSeller;
private ArrayList<ModelOrderShop> orderShopArrayList;
private AdapterOrderShop adapterOrderShop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_1);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_1);

        ImageButton settingsBtn = findViewById(R.id.settingsBtn);
        nameTv = findViewById(R.id.nameTv);
        filteredProductTv = findViewById(R.id.filteredProductTv);
        shopnameTv = findViewById(R.id.shopnameTv);
        emailTv = findViewById(R.id.emailTv);
        ImageButton logout = findViewById(R.id.logoutBtn);
        ImageButton editProfileBtn = findViewById(R.id.editProfileBtn);
        ImageButton addProductBtn = findViewById(R.id.addProductBtn);
        profileIv = findViewById(R.id.profileIv);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        EditText searchProductsEt = findViewById(R.id.searchProductsEt);
        ImageButton filterProductButton = findViewById(R.id.filterProductButton);
        productsRv = findViewById(R.id.productsRv);
        ordersRv = findViewById(R.id.ordersRv);
        ImageButton reviewsBtn = findViewById(R.id.reviewsBtn);
        ImageButton filteredOrderBtn = findViewById(R.id.filteredOrderBtn);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        RelativeLayout toolbarRl = findViewById(R.id.toolbarRl);



        toolbarRl.setAnimation(topAnim);
        productsRl.setAnimation(bottomAnim);


        progressDialog  = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();
        showProductsUi();
        loadAllProducts();
        loadAllOrders();



        searchProductsEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    adapterProductSeller.getFilter().filter(s);

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


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //make offline
                //sign out
                //go to login activity
                makeMeOffline();

            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));

            }
        });
        addProductBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));


            }
        });
        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {//load products

                showProductsUi();

            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {//load orders
                shoeOrdersUi();

            }
        });
        filterProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Chose Category")
                        .setItems(Constants.options2, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            String selected = Constants.options2[which];
                            filteredProductTv.setText(selected);
                            if (selected.equals("All"))
                            {
                                loadAllProducts();
                            }
                            else
                            {
                                loadFilteredProducts(selected);
                            }

                            }
                        }).show();
            }
        });
        filteredOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] options = {"All","InProgress","Completed","Cancelled"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders: ")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which==0)
                                {
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");
                                }
                                else {
                                    String optionsClicked = options[which];
                                    filteredOrdersTv.setText("Showing "+optionsClicked+" Orders");
                                    adapterOrderShop.getFilter().filter(optionsClicked);
                                }


                            }
                        }).show();
            }
        });
        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainSellerActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid",""+firebaseAuth.getUid());
                startActivity(intent);
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this,SettingsActivity.class));

            }
        });

    }

    private void loadAllOrders()
    {
        orderShopArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);

                            orderShopArrayList.add(modelOrderShop);
                        }

                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,orderShopArrayList);
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(final String selected)
    {
        productList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        for (DataSnapshot ds:snapshot.getChildren())
                        {
                            String productCategory = ""+ds.child("productCategory").getValue();

                            if (selected.equals(productCategory))
                            {
                                ModelProducts modelProducts = ds.getValue(ModelProducts.class);
                                productList.add(modelProducts);
                            }



                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);

                        productsRv.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadAllProducts()
    {
        productList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        productList.clear();
                        for (DataSnapshot ds:snapshot.getChildren())
                        {
                            ModelProducts modelProducts = ds.getValue(ModelProducts.class);
                            productList.add(modelProducts);
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);

                        productsRv.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUi()
    {// show products ui & hide orders ui

        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackground(getResources().getDrawable(R.drawable.shaper_rect04));
        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }
    private void shoeOrdersUi()
    {// show orders ui & hide products ui


        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackground(getResources().getDrawable(R.drawable.shaper_rect04));
        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));    }



    private void makeMeOffline()
    {
        progressDialog.setMessage("Logging out...");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");


        // update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        //update succesfuly
                        firebaseAuth.signOut();
                        checkUser();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //update failed
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUser()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(MainSellerActivity.this, WelComeActivity.class));
            finish();
        }
        else
        {
            loadMyInfo();
        }
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
                            //get data from db
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImg = ""+ds.child("profileImg").getValue();


                            //set data to ui

                            nameTv.setText(name);
                            shopnameTv.setText(shopName);
                            emailTv.setText(email);

                            try {
                                Picasso.get().load(profileImg).placeholder(R.drawable.ic_store_gray).into(profileIv);

                            }
                            catch (Exception e)
                            {
                                profileIv.setImageResource(R.drawable.ic_store_gray);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}