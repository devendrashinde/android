package com.example.dshinde.myapplication_xmlpref.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListviewKeyValueMapAdapter extends ArrayAdapter<Map<String, String>> implements Filterable {

    private List<Map<String, String>> kvList;
    private List<Map<String, String>> origList;
    private Context context;
    private Filter kvFilter;
    private int layoutResource;

    public ListviewKeyValueMapAdapter(List<Map<String, String>> kvList, Context ctx, int layoutResource) {
        super(ctx, layoutResource, kvList);
        this.kvList = kvList;
        this.context = ctx;
        this.origList = kvList;
        this.layoutResource = layoutResource;
    }

    public void setData(List<Map<String, String>> kvList){
        this.kvList = kvList;
        this.origList = kvList;
        notifyDataSetChanged();
    }

    public int getCount() {
        return kvList.size();
    }

    public Map<String, String> getItem(int position) {
        return kvList.get(position);
    }

    public long getItemId(int position) {
        return kvList.get(position).hashCode();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        KeyValueHolder holder = new KeyValueHolder();

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResource, parent, false);
            // Now we can fill the layout with the right values
            TextView tv = (TextView) v.findViewById(R.id.listKey);
            TextView distView = (TextView) v.findViewById(R.id.listValue);

            holder.keyView = tv;
            holder.valueView = distView;

            v.setTag(holder);
        } else
            holder = (KeyValueHolder) v.getTag();

        KeyValue kv = KeyValue.getInstance(kvList.get(position));
        holder.keyView.setText(kv.getKey());
        holder.valueView.setText(kv.getValue());

        return v;
    }

    public void resetData() {
        kvList = origList;
    }


    /* *********************************
     * We use the holder pattern
     * It makes the view faster and avoid finding the component
     * **********************************/

    private static class KeyValueHolder {
        public TextView keyView;
        public TextView valueView;
    }

    /*
     * We create our filter
     */

    @Override
    public Filter getFilter() {
        if (kvFilter == null)
            kvFilter = new KeyValueFilter();

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
                List<Map<String, String>> filteredList = new ArrayList<>();

                for (Map<String, String> item : kvList) {
                    KeyValue kv = KeyValue.getInstance(item);
                    if (kv.getKey().toUpperCase().contains(constraint.toString().toUpperCase()) ||
                            kv.getValue().toUpperCase().contains(constraint.toString().toUpperCase())) {
                        filteredList.add(item);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();

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
                kvList = (List<Map<String, String>>) results.values;
                notifyDataSetChanged();
            }

        }

    }

}
