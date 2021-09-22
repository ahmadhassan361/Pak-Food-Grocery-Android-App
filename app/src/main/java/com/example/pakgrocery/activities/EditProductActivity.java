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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakgrocery.Constants;
import com.example.pakgrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Objects;

public class EditProductActivity extends AppCompatActivity
{
    // ui
    private String productId;

    private ImageView productIconIv;
    private EditText titleEt,descriptionEt,quantityEt,priceEt,discountPriceEt
            ,discountNoteEt;
    private TextView categoryTv;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;



    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //Gallery constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri image_uri;

    //progress dialogue
    private ProgressDialog progressDialog;
    //firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);


        productId = getIntent().getStringExtra("productId");




        //init ui views
        ImageButton backButton = findViewById(R.id.back);
        productIconIv = (ImageView) findViewById(R.id.productIconIv);
        titleEt = (EditText) findViewById(R.id.titleEt);
        descriptionEt = (EditText)findViewById(R.id.descriptionEt);
        categoryTv = (TextView)findViewById(R.id.categoryTv);
        priceEt =(EditText) findViewById(R.id.priceEt);
        quantityEt = (EditText)findViewById(R.id.quantityEt);
        discountSwitch =(SwitchCompat) findViewById(R.id.discountSwitch);
        discountPriceEt = (EditText)findViewById(R.id.discountPriceEt);
        discountNoteEt = (EditText)findViewById(R.id.discountNoteEt);
        updateProductBtn = (Button) findViewById(R.id.updateProductBtn);




        //init arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);


        //get id of product from intent


        loadProductDetails(); // to set on views



        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    discountPriceEt.setVisibility(View.VISIBLE);
                    discountNoteEt.setVisibility(View.VISIBLE);

                }
                else
                {

                    discountPriceEt.setVisibility(View.GONE);
                    discountNoteEt.setVisibility(View.GONE);
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();            }
        });


        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showImagePickerDialog();
            }
        });


        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialogue();
            }
        });
        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //flow;
                //input data
                //validate data
                //update data to db
                input();

            }
        });

    }

    private void loadProductDetails()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    { //get data
                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productIcon = ""+snapshot.child("productIconIv").getValue();
                        String originalPrice = ""+snapshot.child("originalPrice").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String discountNote = ""+snapshot.child("discountNote").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String timeStamp = ""+snapshot.child("timeStamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //set data to views
                        if (discountAvailable.equals("true"))
                        {
                            discountSwitch.setChecked(true);
                            discountPriceEt.setVisibility(View.VISIBLE);
                            discountNoteEt.setVisibility(View.VISIBLE);


                        }
                        else
                        {

                            discountSwitch.setChecked(false);
                            discountPriceEt.setVisibility(View.GONE);
                            discountNoteEt.setVisibility(View.GONE);
                        }

                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        categoryTv.setText(productCategory);
                        discountNoteEt.setText(discountNote);
                        discountPriceEt.setText(discountPrice);
                        priceEt.setText(originalPrice);
                        quantityEt.setText(productQuantity);
                        try {

                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_cart_24).into(productIconIv);
                        }
                        catch (Exception e)
                        {

                            productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
                        }




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String productTitle,productDescription,productCategory,originalPrice,productQuantity,discountPrice,discountNote;
    private boolean discountAvailable;
    private void input()
    {
        //input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();//true/false

        //validate data
        if (TextUtils.isEmpty(productTitle))
        {
            Toast.makeText(this, "title is required..", Toast.LENGTH_SHORT).show();
            return;//dont proceed further
        }
        if (TextUtils.isEmpty(productCategory))
        {
            Toast.makeText(this, "category is necassary, select first..", Toast.LENGTH_SHORT).show();
            return;//dont proceed further
        }
        if (TextUtils.isEmpty(originalPrice))
        {
            Toast.makeText(this, "price is required..", Toast.LENGTH_SHORT).show();
            return;//dont proceed further
        }
        if (TextUtils.isEmpty(productQuantity))
        {
            Toast.makeText(this, "Quantity is required..", Toast.LENGTH_SHORT).show();
            return;//dont proceed further

        }
        if (discountAvailable)
        {
            //product is with discount
            discountPrice = discountPriceEt.getText().toString().trim();
            discountNote = discountNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice))
            {
                Toast.makeText(this, "discount price is necassary...", Toast.LENGTH_SHORT).show();
                return;//dont proceed further
            }
        }
        else
        {// product without discount
            discountPrice = "0";
            discountNote = "";

        }
        updateProduct();




    }

    private void updateProduct()
    {
        //show progress
        progressDialog.setMessage("Update Product...");
        progressDialog.show();
        if (image_uri == null)
        {//update without image

            //setup data in hashMap to update
            HashMap<String ,Object> hashMap = new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);

            //upadte to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated Successfully..", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        else
        {//update with image

            //first upload image
            //image name and path on firebase
            String filePathAndName = "product_image/"+""+productId;// override previous image using same id
            //uploaded image on db
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImage = uriTask.getResult();
                            if (uriTask.isSuccessful())
                            {
                                //setup data in hashMap to update
                                HashMap<String ,Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("productIconIv",""+downloadImage);
                                hashMap.put("discountAvailable",""+discountAvailable);

                                //upadte to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Updated Successfully..", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }

    }

    private void categoryDialogue()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //get picked category

                        String category = Constants.options[which];
                        //set picked category
                        categoryTv.setText(category);
                    }
                })
                .show();
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

    private void pickFromGallery()
    {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

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
                productIconIv.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {

                productIconIv.setImageURI(image_uri);
            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }
}