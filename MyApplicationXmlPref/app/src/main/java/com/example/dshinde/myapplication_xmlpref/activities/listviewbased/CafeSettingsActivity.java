package com.example.dshinde.myapplication_xmlpref.activities.listviewbased;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.CafeSettings;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CafeSettingsActivity extends BaseActivity implements ListviewActions {

    public static final String NO = "No";
    public static final String YES = "Yes";
    EditText etRateDate;
    EditText etTeaRate;
    EditText etCoffeRate;
    ListView listView;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    String collectionName = null;
    boolean editing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cafe_setting);

        etRateDate = (EditText) findViewById(R.id.etKrutisheelName);
        etTeaRate = (EditText) findViewById(R.id.etTeaRate);
        etCoffeRate = (EditText) findViewById(R.id.etCoffeRate);
        listView = (ListView) findViewById(R.id.list);
        Bundle bundle = getIntent().getExtras();
        collectionName = Constants.CAFE_SETTINGS;
        userId = bundle.getString("userId");
        setTitle(collectionName);

        dataStorageManager = Factory.getDataStorageIntsance(this, getDataStorageType(), collectionName, false, true);
        dataStorageManager.addDataStorageListener(new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {

            }

            @Override
            public void dataLoaded(List<KeyValue> data) {
                listAdapter.setData(data);
            }
        });
        dataStorageManager.loadData();

        populateRateDate();
        populateListView();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_sell);
        menu.removeItem(R.id.menu_settings);
        menu.removeItem(R.id.menu_view);
        menu.removeItem(R.id.menu_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_clear:
                clear();
                return true;
            case R.id.menu_copy:
                return true;
            case R.id.menu_edit:
                return true;
            case R.id.menu_remove:
                remove();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setEditView(String key, String value) {
        etRateDate.setText(key);
        Gson gson = new Gson();
        CafeSettings item = gson.fromJson(value, CafeSettings.class);
        editing = true;
    }

    private int getTeaRadioButtonId(int id){
        switch (id){
            case 1:
                return R.id.tea1;
            case 2:
                return R.id.tea2;
            case 3:
                return R.id.tea3;
            case 4:
                return R.id.tea4;
            case 5:
                return R.id.tea5;
            default:
                return -1;
        }
    }


    private void populateListView() {
        listAdapter = new ListviewKeyValueObjectAdapter(dataStorageManager.getValues(),this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValue kv = listAdapter.getItem(position);
                setEditView(kv.getKey(), kv.getValue());
            }
        };
        listView.setOnItemClickListener(listener);
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        CafeSettings details = getCafeSettingsDetails();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(details);
        dataStorageManager.save(details.rateDate, json);
        clear();
        editing = false;
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        String key = etRateDate.getText().toString();
        clear();
        dataStorageManager.remove(key);
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        populateRateDate();
        etCoffeRate.setText("");
        etTeaRate.setText("");
        editing=false;
    }

    private void populateRateDate() {

        SimpleDateFormat dateF = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String date = dateF.format(Calendar.getInstance().getTime());
        etRateDate.setText(date);
    }

    private CafeSettings getCafeSettingsDetails() {
        String rateDate = etRateDate.getText().toString();
        BigDecimal teaRate = new BigDecimal(etTeaRate.getText().toString());
        BigDecimal coffeRate = new BigDecimal(etCoffeRate.getText().toString());

        return new CafeSettings(rateDate, teaRate, coffeRate);
    }

}
