package com.example.dshinde.myapplication_xmlpref;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CafeSettingsActivity extends AppCompatActivity implements ListviewActions {

    public static final String NO = "No";
    public static final String YES = "Yes";
    EditText etRateDate;
    EditText etTeaRate;
    EditText etCoffeRate;
    ListView listView;
    ListviewKeyValueObjectAdapter listAdapter;
    SharedPrefManager sharedPrefManager;
    String sharedPreferenceName = null;
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
        sharedPreferenceName = bundle.getString("filename");
        setTitle(sharedPreferenceName);
        sharedPrefManager = new SharedPrefManager(this, sharedPreferenceName, false, true);
        sharedPrefManager.add(new SharedPrefListener() {
            @Override
            public void sharedPrefChanged(String key, String value) {
                listAdapter.setData(sharedPrefManager.getValues());
            }
        });
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
        listAdapter = new ListviewKeyValueObjectAdapter(sharedPrefManager.getValues(),this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
        setSharedPrefManagerListener();
    }

    private void setOnItemClickListenerToListView() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = ((TextView) view.findViewById(R.id.listKey)).getText().toString();
                String value = ((TextView) view.findViewById(R.id.listValue)).getText().toString();
                setEditView(key, value);
            }
        };
        listView.setOnItemClickListener(listener);
    }

    private void setSharedPrefManagerListener() {
        SharedPrefListener listener = new SharedPrefListener() {
            public void sharedPrefChanged(String changedKey, String changedValue) {
                listAdapter.notifyDataSetChanged();
                setEditView(changedKey, changedValue);
            }
        };
    }

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        CafeSettings details = getCafeSettingsDetails();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(details);
        sharedPrefManager.save(details.rateDate, json);
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
        sharedPrefManager.remove(key);
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
