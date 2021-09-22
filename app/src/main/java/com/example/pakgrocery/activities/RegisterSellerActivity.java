package com.example.pakgrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {
    private ImageButton backbtn, gpsbtn,policy;
    private ImageView profileIv;
    private EditText nameEt, shopnameEt, phoneEt, deliveryFee, countryEt, stateEt, cityEt, addressEt, emailEt, passwordEt, cPpasswordEt;
    private Button registerBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private LocationManager locationManager;
    private double latitude= 0.0, longitude=0.0;
    //perissions constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //Gallery constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;


    //permissions arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picker uri
    private Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        backbtn = (ImageButton) findViewById(R.id.bacKbtn);
        gpsbtn = (ImageButton) findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        nameEt = (EditText) findViewById(R.id.nameEt);
        shopnameEt = (EditText) findViewById(R.id.shopnameEt);
        phoneEt = (EditText) findViewById(R.id.phoneEt);
        deliveryFee = (EditText) findViewById(R.id.deliveryFee);
        countryEt = (EditText) findViewById(R.id.countryEt);
        stateEt = (EditText) findViewById(R.id.stateEt);
        cityEt = (EditText) findViewById(R.id.cityEt);
        addressEt = (EditText) findViewById(R.id.addressEt);
        emailEt = (EditText) findViewById(R.id.emailEt);
        passwordEt = (EditText) findViewById(R.id.passwordEt);
        cPpasswordEt = (EditText) findViewById(R.id.cPpasswordEt);
        registerBtn = findViewById(R.id.registerBtn);
        policy = findViewById(R.id.policy);

        //init permission array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        gpsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pick Image
                showImagePickerDialog();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register user
                inputData();
            }
        });
        policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterSellerActivity.this,PolicyActivity.class);
                startActivity(intent);

            }
        });

    }

    private String fullName,shopName,phoneNumber,deliveryfee,country,state,city,email,password,confirmPassword,address;

    private void inputData()
    {
        //input data
        fullName = nameEt.getText().toString().trim();
        shopName = shopnameEt.getText().toString().trim();
        phoneNumber = phoneEt.getText().toString().trim();
        deliveryfee = deliveryFee.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        confirmPassword = cPpasswordEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();


        //validate.data
        if (TextUtils.isEmpty(fullName))
        {
            Toast.makeText(this, "Enter fullname ....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(shopName))
        {
            Toast.makeText(this, "Enter shopname ...", Toast.LENGTH_SHORT).show();
            return;
        }if (TextUtils.isEmpty(phoneNumber))
    {
        Toast.makeText(this, "ENter phoneNumber ..", Toast.LENGTH_SHORT).show();
        return;
    }if (longitude==0.0||latitude==0.0)
    {
        Toast.makeText(this, "click GPS button(right top corner) to detect your location...", Toast.LENGTH_LONG).show();
        return;
    }
        if (TextUtils.isEmpty(deliveryfee))
    {
        Toast.makeText(this, "Enter delivery fee ...", Toast.LENGTH_SHORT).show();
        return;
    }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Enter valid email address...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length()<6)
        {
            Toast.makeText(this, "password must be 6 characters,letters,numbers ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword))
        {
            Toast.makeText(this, " Password does not match...", Toast.LENGTH_SHORT).show();
            return;
        }


        ceateAccount();

    }

    private void ceateAccount()
    {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        //create account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                    //account creating

                        saverFireBaseData();
                        
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                   //failed creating account
                    progressDialog.dismiss();
                        Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    }
                });
    }

    private void saverFireBaseData()
    {
        progressDialog.setMessage("saving account info");
        final String timestamp = ""+System.currentTimeMillis();
        if (image_uri == null)
        {//saving account without image
            //setup data to save

            HashMap<String ,Object> hashMap = new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+fullName);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("deliveryFee",""+deliveryfee);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","Seller");
            hashMap.put("online","true");
            hashMap.put("shopOpen","true");
            hashMap.put("profileImg","");


            //save info to database

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            //db updating
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                            finish();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            //failed update
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                            finish();


                        }
                    });

        }
        else
        {//saving account with image
            //name and path of image
            String filePathAndName ="profile_images/"+ ""+firebaseAuth.getUid();
            //upload images
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            //get url of upload image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful())
                            {
                                HashMap<String ,Object> hashMap = new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+fullName);
                                hashMap.put("shopName",""+shopName);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("deliveryFee",""+deliveryfee);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longitude);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("accountType","Seller");
                                hashMap.put("online","true");
                                hashMap.put("shopOpen","true");
                                hashMap.put("profileImg",""+downloadImageUri);//url of uploaded image


                                //save info to database

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                //db updating
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                                finish();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                //failed update
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                                finish();


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
                            Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            

                        }
                    });

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted)
                    {
                        //permission allow
                        detectLocation();

                    }
                    else
                    {
                        //permission denied
                        Toast.makeText(this,"Location Permission is necessary..",Toast.LENGTH_LONG).show();

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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