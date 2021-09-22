package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakgrocery.R;
import com.example.pakgrocery.adapters.AdapterOrderUser;
import com.example.pakgrocery.adapters.AdapterShop;
import com.example.pakgrocery.models.ModelOrder;
import com.example.pakgrocery.models.ModelShop;
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
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv,emailTv,phoneTv,tabShopsTv,tabOrdersTv;
    private ImageButton logout,editProfileBtn,settingsBtn;
    private RelativeLayout shopsRl,ordersRl,toolbarRl;
    private ImageView profileIv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private RecyclerView shopsRv,ordersRv;
    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;
    private ArrayList<ModelOrder> orderList;
    private AdapterOrderUser adapterOrderUser;
    private Animation topAnim,bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_1);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_1);


        settingsBtn = findViewById(R.id.settingsBtn);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        logout = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        profileIv = findViewById(R.id.profileIv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        shopsRv = findViewById(R.id.shopsRv);
        ordersRv = findViewById(R.id.ordersRv);
        toolbarRl = findViewById(R.id.toolbarRl);

        toolbarRl.setAnimation(topAnim);
        shopsRl.setAnimation(bottomAnim);






        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // at start show shops ui
        showShopsUi();


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeMeOffline();

            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            }
        });
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopsUi();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUi();
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });
    }

    private void showShopsUi()
    {   //show shops ui, hide users ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shaper_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundResource(android.R.color.transparent);


    }
    private void showOrdersUi()
    {   //show shops ui, hide users ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundResource(android.R.color.transparent);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shaper_rect04);


    }

    private void checkUser()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(MainUserActivity.this, WelComeActivity.class));
            finish();
        }
        else
        {
            loadMyInfo();
        }
    }
    private void makeMeOffline()
    {
        progressDialog.setMessage("Logging out...");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");


        // update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //update succesfuly
                    firebaseAuth.signOut();
                    checkUser();


                })
                .addOnFailureListener(e -> {
                    //update failed
                    progressDialog.dismiss();
                    Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                });
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
                            String phone = ""+ds.child("phone").getValue();
                            String profileImg = ""+ds.child("profileImg").getValue();

                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();

                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);

                            try
                            {
                                Picasso.get().load(profileImg).placeholder(R.drawable.ic_baseline_person).into(profileIv);

                            }
                            catch (Exception e)
                            {
                            profileIv.setImageResource(R.drawable.ic_baseline_person);
                            }

                            //load only those cities that are in user city
                            loadShops(city);
                            loadOrders();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders()
    {
        //init order list
        orderList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                orderList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String uid = ""+ds.getRef().getKey();


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                {
                                    if (snapshot.exists())
                                    {
                                        for (DataSnapshot ds: snapshot.getChildren())
                                        {
                                            ModelOrder modelOrder = ds.getValue(ModelOrder.class);

                                            orderList.add(modelOrder);
                                        }
                                        //setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,orderList);
                                        //set to recycler view
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String mycity)

    {
        shopsList = new ArrayList<>();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        shopsList.clear();
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            ModelShop modelShop = ds.getValue(ModelShop.class);
                            String shopCity = ""+ds.child("city").getValue();

                            if (shopCity.equals(mycity))
                            {
                                shopsList.add(modelShop);
                            }
                            // if u want to show all shops skip this if condition
                            //shopsList.add(modelShop);

                        }
                        adapterShop = new AdapterShop(MainUserActivity.this,shopsList);
                        //set adapter to recycler view
                        shopsRv.setAdapter(adapterShop);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {


                    }
                });
    }

}