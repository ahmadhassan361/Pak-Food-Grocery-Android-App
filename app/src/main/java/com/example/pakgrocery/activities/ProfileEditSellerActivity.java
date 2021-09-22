package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditSellerActivity extends AppCompatActivity implements LocationListener {

    private ImageButton bacKbtn,gpsBtn;
    private ImageView profileIv;
    private EditText nameEt,shopnameEt,deliveryfee,phoneEt,countryEt,stateEt,cityEt
            ,addressEt;
    private SwitchCompat shopOpenSwitch;
    private Button updateBtn;
    private RelativeLayout toolbarRl;
    private ScrollView bottom1;


    //perissions constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //Gallery constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permissoin arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //progress dialogue
    private ProgressDialog progressDialog;
    //firebase
    private FirebaseAuth firebaseAuth;

    private LocationManager locationManager;
    private double latitude= 0.0, longitude=0.0;


    //image picker uri
    private Uri image_uri;
    private Animation topAnim,bottomAnim;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_seller);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_2);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_2);

        bacKbtn = findViewById(R.id.bacKbtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        shopnameEt = findViewById(R.id.shopnameEt);
        deliveryfee = findViewById(R.id.deliveryFee);
        phoneEt = findViewById(R.id.phoneEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.stateEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        shopOpenSwitch = findViewById(R.id.shopOpenSwitch);
        updateBtn = findViewById(R.id.updateBtn);


        toolbarRl = findViewById(R.id.toolbarRl);
        bottom1 = findViewById(R.id.bottom1);

        toolbarRl.setAnimation(topAnim);
        bottom1.setAnimation(bottomAnim);

        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);




        checkUser();
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pick Image
                showImagePickerDialog();
            }
        });
        bacKbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect location
                //direct current location
                if (checkLocationOermission()) {
                    //already allow
                    detectLocation();
                } else {
                    //not allowed
                    requestLocationPermission();
                }
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //begin updated profile

                inputData();

            }
        });
    }

    private String name,shopName,deliveryFee,phone,country,state,city,address;
    private boolean shopOpen;
    private void inputData()
    {

        name = nameEt.getText().toString().trim();
        shopName = shopnameEt.getText().toString().trim();
        deliveryFee = deliveryfee.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        country = cityEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        shopOpen = shopOpenSwitch.isChecked();

        updateProfile();

    }

    private void updateProfile()
    {
        progressDialog.setMessage("updating profile...");
        progressDialog.show();

        if (image_uri == null)
        {//update without image
            //setup data to update

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("name",""+name);
            hashMap.put("shopName",""+shopName);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("phone",""+phone);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen",""+shopOpen);


            //update to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated ....", Toast.LENGTH_SHORT).show();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {

                        @Override
                        public void onFailure(@NonNull Exception e)
                        {//failed to update


                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();



                        }
                    });



        }
        else
        {//update with image
            /*-----upload image first-------*/
            String filePathAndName = "profile_images/"+""+firebaseAuth.getUid();
            //get storage refrence
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful())
                            {
                                //image url received and updated db




                                //update without image
                                //setup data to update

                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("name",""+name);
                                hashMap.put("shopName",""+shopName);
                                hashMap.put("deliveryFee",""+deliveryFee);
                                hashMap.put("phone",""+phone);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("shopOpen",""+shopOpen);
                                hashMap.put("profileImg",""+downloadImageUri);


                                //update to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, "Profile Updated ....", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {

                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {//failed to update


                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();



                                            }
                                        });




                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }

    private void checkUser()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        else
        {
            loadMyinfo();
        }
    }

    private void loadMyinfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        for (DataSnapshot ds:snapshot.getChildren())
                        {
                            String accountType = ""+ds.child("accountType").getValue();
                            String address = ""+ds.child("address").getValue();
                            String city = ""+ds.child("city").getValue();
                            String state = ""+ds.child("state").getValue();
                            String country = ""+ds.child("country").getValue();
                            String deliveryFee = ""+ds.child("deliveryFee").getValue();
                            String email = ""+ds.child("email").getValue();
                             latitude = Double.parseDouble(""+ds.child("latitude").getValue());
                             longitude = Double.parseDouble(""+ds.child("longitude").getValue());
                            String name = ""+ds.child("name").getValue();
                            String online = ""+ds.child("online").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImg = ""+ds.child("profileImg").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String shopOpen = ""+ds.child("shopOpen").getValue();
                            String uid = ""+ds.child("uid").getValue();


                            nameEt.setText(name);
                            phoneEt.setText(phone);
                            countryEt.setText(country);
                            stateEt.setText(state);
                            cityEt.setText(city);
                            addressEt.setText(address);
                            shopnameEt.setText(shopName);
                            deliveryfee.setText(deliveryFee);

                            if (shopOpen.equals("true"))
                            {
                                shopOpenSwitch.setChecked(true);
                            }
                            else
                            {
                                shopOpenSwitch.setChecked(false);
                            }
                            try {
                                Picasso.get().load(profileImg).placeholder(R.drawable.ic_baseline_person).into(profileIv);
                            }
                            catch (Exception e)
                            {
                                profileIv.setImageResource(R.drawable.ic_baseline_person);
                            }





                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress() {
        //find address country,state,city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set adresses
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);


        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void showImagePickerDialog() {
        //options
        String[] options = {"Camera", "Gallery"};
        //dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image..")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {//camera permission allowed

                                picFromCamera();

                            } else {//not allowed request


                                requestCameraPermission();


                            }

                        } else {
                            //Gallery clicked

                            if (checkStoragePermission()) {//storage permission allowed
                                pickFromGallery();

                            } else {
                                //not allowed request

                                requestStoragePermission();

                            }

                        }
                    }
                })
                .show();
    }
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void picFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }
    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);

    }


    private void detectLocation() {
        Toast.makeText(this, "Please wait..", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    private boolean checkLocationOermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission()
    {
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);

    }

    @Override
    public void onLocationChanged(Location location)
    {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();


    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider)
    {
        //gps/location disabled
        Toast.makeText(this,"please turn on location..",Toast.LENGTH_LONG).show();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permission allow
                        detectLocation();

                    } else {
                        //permission denied
                        Toast.makeText(this, "Location Permission is necessary..", Toast.LENGTH_LONG).show();

                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                    {
                        //permission allow
                        picFromCamera();

                    }
                    else
                    {
                        //permission denied
                        Toast.makeText(this,"Camera Permissions are necessary..",Toast.LENGTH_LONG).show();

                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0)
                {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted)
                    {
                        //permission allow
                        pickFromGallery();

                    }
                    else
                    {
                        //permission denied
                        Toast.makeText(this,"storage Permission is necessary..",Toast.LENGTH_LONG).show();

                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK)

        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri = data.getData();
                profileIv.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {

                profileIv.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}