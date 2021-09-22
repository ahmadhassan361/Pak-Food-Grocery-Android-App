package com.example.pakgrocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakgrocery.FilterProduct;
import com.example.pakgrocery.models.ModelProducts;
import com.example.pakgrocery.R;
import com.example.pakgrocery.activities.EditProductActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductsSeller> implements Filterable
{
    private Context context;
    public ArrayList<ModelProducts> productsList,filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProducts> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductsSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {//inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);

        return new HolderProductsSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductsSeller holder, int position)
    {// get data
        final ModelProducts modelProducts = productsList.get(position);
        String id = modelProducts.getProductId();
        String uid = modelProducts.getUid();
        String discountAvailable = modelProducts.getDiscountAvailable();
        String discountNote = modelProducts.getDiscountNote();
        String discountPrice = modelProducts.getDiscountPrice();
        String productCategory = modelProducts.getProductCategory();
        String productDescription = modelProducts.getProductDescription();
        String quantity = modelProducts.getProductQuantity();
        String iconIv = modelProducts.getProductIconIv();
        String timeStamp = modelProducts.getTimeStamp();
        String title = modelProducts.getProductTitle();
        String originalPrice = modelProducts.getOriginalPrice();


        //set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText("$"+discountPrice);
        holder.originalPriceTv.setText("$"+originalPrice);
        holder.quantityTv.setText(quantity);
        if (discountAvailable.equals("true"))
        {//product on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else
        {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try {
            Picasso.get().load(iconIv).placeholder(R.drawable.ic_product_add).into(holder.productIconIv);
        }
        catch (Exception e)
        {
            holder.productIconIv.setImageResource(R.drawable.ic_product_add);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {// handle clicks on item
                detailsBottomSheet(modelProducts);//here model products contain detail of clicked products


            }
        });


    }

    private void detailsBottomSheet(ModelProducts modelProducts)
    {//bottomsheet
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_detail_seller,null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);
        //init views of bottom sheet

        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data
        final String id = modelProducts.getProductId();
        String uid = modelProducts.getUid();
        String discountAvailable = modelProducts.getDiscountAvailable();
        String discountNote = modelProducts.getDiscountNote();
        String discountPrice = modelProducts.getDiscountPrice();
        String productCategory = modelProducts.getProductCategory();
        String productDescription = modelProducts.getProductDescription();
        String quantity = modelProducts.getProductQuantity();
        String iconIv = modelProducts.getProductIconIv();
        String timeStamp = modelProducts.getTimeStamp();
        final String title = modelProducts.getProductTitle();
        String originalPrice = modelProducts.getOriginalPrice();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountedNoteTv.setText(discountNote);
        discountedPriceTv.setText("$"+discountPrice);
        originalPriceTv.setText("$"+originalPrice);


        if (discountAvailable.equals("true"))
        {//product on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountedNoteTv.setVisibility(View.VISIBLE);
            discountedPriceTv.setPaintFlags(discountedPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        else
        {
            //product is not on discount
            discountedPriceTv.setVisibility(View.GONE);
            discountedNoteTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(iconIv).placeholder(R.drawable.ic_product_add).into(productIconIv);
        }
        catch (Exception e)
        {
            productIconIv.setImageResource(R.drawable.ic_product_add);

        }
        //show bottom dialogue
        bottomSheetDialog.show();

        //edit button click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {//delete confirmation dialogue
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("delete")
                        .setMessage("Are you sure you want to delete product "+title+" ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //delete the product
                                deleteProduct(id);//id is the product


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();

                            }
                        }).show();

            }
        });
        // back button click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {   //dismiss bottom sheet

                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(context, "Product deleted...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter()
    {
        if (filter == null)
        {
            filter = new FilterProduct(this,filterList);

        }
        return filter;
    }

    class HolderProductsSeller extends RecyclerView.ViewHolder
    {
        private ImageView productIconIv,nextIv;
        private TextView discountedNoteTv,titleTv,quantityTv,discountedPriceTv,originalPriceTv;

        public HolderProductsSeller(@NonNull View itemView)
        {
            super(itemView);
            productIconIv =  itemView.findViewById(R.id.productIconIv);
            nextIv =  itemView.findViewById(R.id.nextIv);
            discountedNoteTv =  itemView.findViewById(R.id.discountedNoteTv);
            titleTv =  itemView.findViewById(R.id.titleTv);
            quantityTv =  itemView.findViewById(R.id.quantityTv);
            discountedPriceTv =  itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv =  itemView.findViewById(R.id.originalPriceTv);




        }
    }
}
