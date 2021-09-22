package com.example.pakgrocery;

import android.widget.Filter;

import com.example.pakgrocery.adapters.AdapterProductSeller;
import com.example.pakgrocery.adapters.AdapterProductUser;
import com.example.pakgrocery.models.ModelProducts;

import java.util.ArrayList;

public class FilterProductUser extends Filter
{
    private AdapterProductUser adapter;
    private ArrayList<ModelProducts> filterList;


    public FilterProductUser(AdapterProductUser adapter, ArrayList<ModelProducts> filterList) {
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
            ArrayList<ModelProducts> filteredModel = new ArrayList<>();
            for (int i=0;i<filterList.size(); i++)
            {
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                filterList.get(i).getProductCategory().toUpperCase().contains(constraint))
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
        adapter.productList = (ArrayList<ModelProducts>) results.values;

        adapter.notifyDataSetChanged();

    }
}
