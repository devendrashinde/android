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

public class ListviewKeyValueObjectAdapter extends ArrayAdapter<KeyValue> implements Filterable {

    private List<KeyValue> kvList;
    private Context context;
    private Filter kvFilter;
    private List<KeyValue> origList;
    private int layoutResource;

    public ListviewKeyValueObjectAdapter(List<KeyValue> kvList, Context ctx, int layoutResource) {
        super(ctx, layoutResource, kvList);
        this.kvList = kvList;
        this.context = ctx;
        this.origList = kvList;
        this.layoutResource = layoutResource;
    }

    public void setData(List<KeyValue> kvList){
        this.kvList = kvList;
        this.origList = kvList;
        notifyDataSetChanged();
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

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        KeyValueHolder holder = new KeyValueHolder();

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResource, parent, false);
            // Now we can fill the layout with the right values
            TextView key = (TextView) v.findViewById(R.id.listKey);
            TextView value = (TextView) v.findViewById(R.id.listValue);

            holder.keyValueView = key;
            holder.valueView = value;

            v.setTag(holder);
        } else {
            holder = (KeyValueHolder) v.getTag();
        }
        KeyValue kv = kvList.get(position);
        holder.keyValueView.setText(kv.getKey());
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
        public TextView keyValueView;
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
}
