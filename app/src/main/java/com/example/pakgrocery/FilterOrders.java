package com.example.pakgrocery;

import android.widget.Filter;

import com.example.pakgrocery.adapters.AdapterOrderShop;
import com.example.pakgrocery.adapters.AdapterProductSeller;
import com.example.pakgrocery.models.ModelOrderShop;
import com.example.pakgrocery.models.ModelProducts;

import java.util.ArrayList;

public class FilterOrders extends Filter
{
    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;


    public FilterOrders(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search queries
        if (constraint != null && constraint.length() >0)
        {//change to upper case to make insensitive case
            // searching item
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelOrderShop> filteredModel = new ArrayList<>();
            for (int i=0;i<filterList.size(); i++)
            {
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint))
                {

                    filteredModel.add(filterList.get(i));

                }
            }
            results.count = filteredModel.size();
            results.values = filteredModel;

        }
        else
        {//not searching item


            results.count = filterList.size();
            results.values = filterList;
        }


        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results)
    {
        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>) results.values;

        adapter.notifyDataSetChanged();

    }
}
