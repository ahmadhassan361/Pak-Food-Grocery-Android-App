package com.example.pakgrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakgrocery.R;
import com.example.pakgrocery.activities.OrderDetailUserActivity;
import com.example.pakgrocery.models.ModelOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser>
{
    private Context context;
    private ArrayList<ModelOrder> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrder> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);

        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position)
    {
        //get data
        ModelOrder modelOrder= orderUserList.get(position);
        final String orderId = modelOrder.getOrderId();
        String orderBy = modelOrder.getOrderBy();
        String orderCost = modelOrder.getOrderCost();
        String orderStatus = modelOrder.getOrderStatus();
        String orderTime = modelOrder.getOrderTime();
        final String orderTo = modelOrder.getOrderTo();

        //get shop info
        loadShopInfo(modelOrder,holder);



        //set data
        holder.amountTv.setText("Amount: $"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderID:"+orderId);
        //change order status color
        switch (orderStatus) {
            case "InProgress":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));

                break;
            case "Completed":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));

                break;
            case "Cancelled":
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));

                break;

            default:
                holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGray02));
                break;
        }

        //convert timestamp for proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();

        holder.dateTv.setText(formatedDate);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details activity, we need to keys there ,orderId,order To
                Intent intent = new Intent(context, OrderDetailUserActivity.class);
                intent.putExtra("orderTo",orderTo);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent);//now get these values form intent to OrderDetailsUserActivity
            }
        });



    }

    private void loadShopInfo(ModelOrder modelOrder, final HolderOrderUser holder)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrder.getOrderTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String shopName = ""+snapshot.child("shopName").getValue();
                holder.shopNameTv.setText(shopName);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    class HolderOrderUser extends RecyclerView.ViewHolder
    {
        //views of layout
        private TextView orderIdTv,dateTv,shopNameTv,amountTv,statusTv;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            //init views of layout
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }

}
