package com.example.pakgrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakgrocery.FilterOrders;
import com.example.pakgrocery.R;
import com.example.pakgrocery.activities.OrdersDetailsSellerActivity;
import com.example.pakgrocery.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable
{
    private Context context;
    public ArrayList<ModelOrderShop> orderShopArrayList,filterList;
    private FilterOrders filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList = orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);

        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position)
    {
        ModelOrderShop modelOrderShop = orderShopArrayList.get(position);
        final String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        final String orderId = modelOrderShop.getOrderId();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTo = modelOrderShop.getOrderTo();
        String orderTime = modelOrderShop.getOrderTime();
        String nameTv = modelOrderShop.getNameTv();

        loadUserInfo(modelOrderShop,holder);

        //set Data
        holder.amountTv.setText("Amount: $"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order ID: "+orderId);

        if (orderStatus.equals("InProgress"))
        {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));

        }
        else if (orderStatus.equals("Completed"))
        {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));

        }
        else if (orderStatus.equals("Cancelled"))
        {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));

        }
        else
        {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGray02));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();

        holder.orderDateTv.setText(formatDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, OrdersDetailsSellerActivity.class);
                intent.putExtra("orderId",orderId);
                intent.putExtra("orderBy",orderBy);
                context.startActivity(intent);



            }
        });



    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, final HolderOrderShop holder)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        String email = "Phone :"+snapshot.child("phone").getValue();
                        holder.emailTv.setText(email);
                        String name = "Name :"+snapshot.child("name").getValue();
                        holder.nameTv.setText(name);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter()
    {
        if (filter == null)
        {
            filter = new FilterOrders(this,filterList);


        }
        return filter;
    }

    class HolderOrderShop extends RecyclerView.ViewHolder
    {
        private TextView orderIdTv,orderDateTv,emailTv,amountTv,statusTv,nameTv;

        public HolderOrderShop(@NonNull View itemView)
        {
            super(itemView);

            statusTv = itemView.findViewById(R.id.statusTv);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            nameTv = itemView.findViewById(R.id.nameTv);

        }
    }
}
