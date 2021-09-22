package com.example.pakgrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakgrocery.R;
import com.example.pakgrocery.models.ModelCartItem;
import com.example.pakgrocery.models.ModelOrderedItem;

import java.util.ArrayList;

public class AdapterOrdereItem extends RecyclerView.Adapter<AdapterOrdereItem.HolderOrdereItem>
{
    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;

    public AdapterOrdereItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList) {
        this.context = context;
        this.orderedItemArrayList = orderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrdereItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);

        return new HolderOrdereItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrdereItem holder, int position)
    {
        //get data at position
        ModelOrderedItem modelOrderedItem = orderedItemArrayList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("$"+price);
        holder.itemPriceTv.setText("$"+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");





    }

    @Override
    public int getItemCount() {
        return orderedItemArrayList.size();
    }

    class HolderOrdereItem extends RecyclerView.ViewHolder
    {
        //ui views

        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;

        public HolderOrdereItem(@NonNull View itemView)
        {
            super(itemView);
            //init ui views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);

        }
    }
}
