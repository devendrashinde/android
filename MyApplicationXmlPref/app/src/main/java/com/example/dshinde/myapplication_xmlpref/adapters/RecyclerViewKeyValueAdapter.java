package com.example.dshinde.myapplication_xmlpref.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.DataChangeType;
import com.example.dshinde.myapplication_xmlpref.helper.JsonHelper;
import com.example.dshinde.myapplication_xmlpref.listners.RecyclerViewKeyValueItemListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewKeyValueAdapter extends RecyclerView.Adapter<RecyclerViewKeyValueAdapter.RecyclerViewKeyValueViewHolder> implements Filterable {
    private List<KeyValue> kvList = new ArrayList<>();
    private List<KeyValue> origList = new ArrayList<>();
    RecyclerViewKeyValueItemListener recyclerViewKeyValueItemListener;
    private Context context;
    private int layoutResource;
    private Filter kvFilter;
    private int lastItemClicked;

    public RecyclerViewKeyValueAdapter(List<KeyValue> kvList, Context ctx, int layoutResource) {
        this(kvList, ctx, layoutResource, null);
    }

    public RecyclerViewKeyValueAdapter(List<KeyValue> kvList, Context ctx, int layoutResource, RecyclerViewKeyValueItemListener recyclerViewKeyValueItemListener) {
        super();
        this.kvList = kvList;
        this.context = ctx;
        this.layoutResource = layoutResource;
        this.origList = kvList;
        this.recyclerViewKeyValueItemListener = recyclerViewKeyValueItemListener;
    }

    @Override
    public Filter getFilter() {
        if (kvFilter == null)
            kvFilter = new RecyclerViewKeyValueAdapter.KeyValueFilter();

        return kvFilter;
    }

    private class KeyValueFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = origList;
                results.count = origList.size();
            } else {
                // We perform filtering operation
                List<KeyValue> nKeyValueList = new ArrayList<KeyValue>();

                for (KeyValue value : kvList) {
                    if ((value.getKey() != null && value.getKey().toUpperCase().contains(constraint.toString().toUpperCase())) ||
                            (value.getValue() != null && value.getValue().toUpperCase().contains(constraint.toString().toUpperCase())))
                        nKeyValueList.add(value);
                }

                results.values = nKeyValueList;
                results.count = nKeyValueList.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                kvList = (List<KeyValue>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    private void notifyDataSetInvalidated() {
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public  class RecyclerViewKeyValueViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView keyView;
        public TextView valueView;

        public RecyclerViewKeyValueViewHolder(View itemView) {
            super(itemView);
            keyView = itemView.findViewById(R.id.listKey);
            valueView = itemView.findViewById(R.id.listValue);

            if(recyclerViewKeyValueItemListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // send selected contact in callback
                        lastItemClicked = getBindingAdapterPosition();
                        recyclerViewKeyValueItemListener.onItemClick(kvList.get(lastItemClicked));
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        lastItemClicked = getBindingAdapterPosition();
                        return recyclerViewKeyValueItemListener.onItemLongClick(kvList.get(lastItemClicked));
                    }
                });
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewKeyValueViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View kvView = inflater.inflate(layoutResource, parent, false);

        // Return a new holder instance
        RecyclerViewKeyValueViewHolder viewHolder = new RecyclerViewKeyValueViewHolder(kvView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerViewKeyValueViewHolder viewHolder, int position) {
        // Get the data model based on position
        KeyValue keyValue = kvList.get(position);

        // Set item views based on your views and data model
        viewHolder.keyView.setText(keyValue.getKey());

        if(JsonHelper.isJSONValid(keyValue.getValue())) {
            viewHolder.valueView.setText(HtmlCompat.fromHtml(JsonHelper.formatAsString(keyValue.getValue(), true),HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            viewHolder.valueView.setText(getValue(keyValue));
        }

    }

    private String getValue(KeyValue keyValue) {
        if(keyValue.getValue() == null || keyValue.getValue().trim().isEmpty()){
            return keyValue.getKey();
        }
        return keyValue.getValue();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return getCount();
    }

    public void setData(List<KeyValue> kvList) {
        this.kvList = kvList;
        this.origList = kvList;
        notifyDataSetChanged();
    }

    public List<KeyValue> getData(){
        return this.kvList;
    }

    public int getCount() {
        return kvList.size();
    }

    public KeyValue getItem(int position) {
        return kvList.get(position);
    }

    public long getItemId(int position) {
        return kvList.get(position).hashCode();
    }

    public void resetData() {
        kvList = origList;
    }
}