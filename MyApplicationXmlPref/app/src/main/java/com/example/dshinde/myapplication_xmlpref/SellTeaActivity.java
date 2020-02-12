package com.example.dshinde.myapplication_xmlpref;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.dshinde.myapplication_xmlpref.adapters.ListviewKeyValueObjectAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.listners.ListviewActions;
import com.example.dshinde.myapplication_xmlpref.model.CafeItem;
import com.example.dshinde.myapplication_xmlpref.model.CafeSellSummary;
import com.example.dshinde.myapplication_xmlpref.model.CafeSettings;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.DataStorage;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellTeaActivity extends BaseActivity implements ListviewActions {
    public static final String NO = "No";
    public static final String YES = "Yes";
    public static final String DD_MMM_YYYY = "dd MMM yyyy";
    TextView tvSellDate;
    TextView tvTeaRate;
    TextView tvCoffeRate;
    TextView tvTotal;
    TextView tvNotPaidTotal;
    RadioGroup radioTeaGroup;
    RadioGroup radioCoffeGroup;
    RadioGroup radioPaymentGroup;
    ListView listView;
    ListviewKeyValueObjectAdapter listAdapter;
    DataStorage dataStorageManager;
    ReadOnceDataStorage settingsDsRef;
    String customer = null;

    List<KeyValue> settings = null;
    CafeSettings cafeSettings = null;
    CafeSellSummary cafeSellSummary = null;
    boolean editing = false;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cafe_sell);

        tvSellDate = (TextView) findViewById(R.id.etSellDate);
        tvTeaRate = (TextView) findViewById(R.id.etTeaRate);
        tvCoffeRate = (TextView) findViewById(R.id.etCoffeRate);
        tvTotal = (TextView) findViewById(R.id.etTotal);
        tvNotPaidTotal = (TextView) findViewById(R.id.etTotalNotPaid);
        radioTeaGroup = (RadioGroup) findViewById(R.id.radioGroupTea);
        radioCoffeGroup = (RadioGroup) findViewById(R.id.radioGroupCoffe);
        radioPaymentGroup = (RadioGroup) findViewById(R.id.radioGroupPayment);
        listView = (ListView) findViewById(R.id.list);

        Bundle bundle = getIntent().getExtras();
        customer = bundle.getString("filename");
        userId = bundle.getString("userId");
        setTitle(customer);
        initialiseDataStorageManager();
        cafeSellSummary = new CafeSellSummary(customer);
        populateDefaults();
        initialiseSettingStorage();
        populateListView();
        setRadioGroupChangeListner();
    }

    private void initialiseDataStorageManager() {
        dataStorageManager = Factory.getDataStorageIntsance(this, getDataStorageType(), customer, false, false);
        dataStorageManager.addDataStorageListener(new DataStorageListener() {
            @Override
            public void dataChanged(String key, String value) {
                List<KeyValue> data = dataStorageManager.getValues();
                listAdapter.setData(data);
                calculateNotPaidTotal(data);
            }

            @Override
            public void dataLoaded(List<KeyValue> data) {
                listAdapter.setData(data);
                calculateNotPaidTotal(data);
            }
        });
    }

    private void initialiseSettingStorage() {
        settingsDsRef = Factory.getReadOnceDataStorageIntsance(this,
                getDataStorageType(),
                Constants.CAFE_SETTINGS,
                new DataStorageListener() {
                    @Override
                    public void dataChanged(String key, String value) {
                    }

                    @Override
                    public void dataLoaded(List<KeyValue> data) {
                        settings = data;
                        getCafeSettings();
                        populateRates();

                    }
        });
    }


    private void setRadioGroupChangeListner(){
        radioTeaGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        calculateTotal();
                    }
                });
        radioCoffeGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        calculateTotal();
                    }
                });
        radioPaymentGroup.check(R.id.radioButtonUnpaid);
        radioPaymentGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        changeTotalColor();
                    }
                });

    }

    private void changeTotalColor(){
        String status = getPaymentStatus();
        if(status.equals(YES)){
            tvTotal.setTextColor(getColor( R.color.colorWhite));
        } else {
            tvTotal.setTextColor(getColor( R.color.colorRed));
        }
    }

    private void calculateTotal(){
        BigDecimal teaTotal = new BigDecimal(tvTeaRate.getText().toString()).multiply(new BigDecimal(getTea()));
        BigDecimal coffeTotal = new BigDecimal(tvCoffeRate.getText().toString()).multiply(new BigDecimal(getCoffe()));
        BigDecimal total = teaTotal.add(coffeTotal);
        tvTotal.setText(total.toString());
    }

    public void showSelected(String text){
        Toast.makeText(this, text,
                Toast.LENGTH_SHORT).show();

    }

    public void noTea(View view){
        radioTeaGroup.clearCheck();
    }

    public void noCoffe(View view){
        radioCoffeGroup.clearCheck();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_share:
                share();
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
            case R.id.menu_settings:
                doSettings();
                return true;
            case R.id.menu_pay:
                pay();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void share() {

        if (cafeSellSummary != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);

            String json = gson.toJson(cafeSellSummary);
            sendIntent.putExtra(Intent.EXTRA_TEXT, json);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    public void doSettings() {
        Intent intent = new Intent(SellTeaActivity.this, CafeSettingsActivity.class);
        intent.putExtra("filename", "CafeSettings");
        startActivity(intent);
    }

    private void setEditView(String key, String value) {
        tvSellDate.setText(key);
        CafeItem item = gson.fromJson(value, CafeItem.class);
        radioTeaGroup.check(getTeaRadioButtonId(item.tea));
        radioCoffeGroup.check(getCoffeRadioButtonId(item.coffe));
        radioPaymentGroup.check(getPaymentRadioButtonId(item.paid));
        getCafeSettings(key.substring(0,11));
        populateRates();
        calculateTotal();
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

    private int getCoffeRadioButtonId(int id){
        switch (id){
            case 1:
                return R.id.coffe1;
            case 2:
                return R.id.coffe2;
            case 3:
                return R.id.coffe3;
            case 4:
                return R.id.coffe4;
            case 5:
                return R.id.coffe5;
            default:
                return -1;
        }
    }

    private int getPaymentRadioButtonId(String paid){
        switch (paid){
            case YES:
                return R.id.radioButtonPaid;
            default:
                return R.id.radioButtonUnpaid;
        }
    }

    private void populateListView() {
        List<KeyValue> data = dataStorageManager.getValues();
        listAdapter = new ListviewKeyValueObjectAdapter(data,this, R.layout.list_view_items_flexbox);
        listView.setAdapter(listAdapter);
        setOnItemClickListenerToListView();
        calculateNotPaidTotal(data);
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

    @Override
    public void save(View view) {
        save();
    }

    public void save() {
        save(getCafeItemDetails());
        clear();
        editing = false;
    }

    private void save(CafeItem details){
        String json = gson.toJson(details);
        dataStorageManager.save(details.sellDateTime, json);
    }

    @Override
    public void remove(View view) {
        remove();
    }

    public void remove() {
        String key = tvSellDate.getText().toString();
        dataStorageManager.remove(key);
        clear();
    }

    public void clear(View view) {
        clear();
    }

    public void clear() {
        populateDefaults();
        radioTeaGroup.clearCheck();
        radioCoffeGroup.clearCheck();
        radioPaymentGroup.check(R.id.radioButtonUnpaid);
        editing=false;
    }

    private void populateDefaults() {

        SimpleDateFormat dateF = new SimpleDateFormat(DD_MMM_YYYY, Locale.getDefault());
        String date = dateF.format(Calendar.getInstance().getTime());
        tvSellDate.setText(date);

    }

    private void populateRates(){
        if(cafeSettings != null) {
            tvTeaRate.setText(cafeSettings.teaRate.toString());
            tvCoffeRate.setText(cafeSettings.coffeRate.toString());
        }
    }

    private void getCafeSettings() {
        getCafeSettings(tvSellDate.getText().toString());
    }

    private void getCafeSettings(String date) {
        if(cafeSettings != null) {
            if(cafeSettings.rateDate.equals(date)){
                return;
            }
        }

        cafeSettings = null;
        try {
            Date date1 = new SimpleDateFormat(DD_MMM_YYYY).parse(date);
            if (settings != null && !settings.isEmpty()) {
                for (KeyValue entry : settings) {
                    Date date2 = new SimpleDateFormat(DD_MMM_YYYY).parse(entry.getKey());
                    if (date2.compareTo(date1) <= 0) {
                        cafeSettings = gson.fromJson(entry.getValue(), CafeSettings.class);
                    }
                }
            }
        } catch(ParseException ex){

        }
    }

    private void calculateNotPaidTotal(List<KeyValue> data){

        BigDecimal total = BigDecimal.ZERO;
        if (!data.isEmpty()) {

            cafeSellSummary = new CafeSellSummary(customer);
            for (KeyValue entry: data) {
                CafeItem cafeItem = gson.fromJson(entry.getValue(), CafeItem.class);
                if(cafeItem.paid.equals(NO)){
                    total = total.add(cafeItem.amountDue);
                }
                buildSummary(cafeItem);
            }
        }
        if(total.compareTo(BigDecimal.ZERO) > 0) {
            tvNotPaidTotal.setText(total.toString());
            tvTotal.setTextColor(getColor( R.color.colorRed));
        } else{
            tvNotPaidTotal.setText("");
            tvTotal.setTextColor(getColor( R.color.colorWhite));
        }
    }

    private void buildSummary(CafeItem cafeItem){
        cafeSellSummary.tea += cafeItem.tea;
        cafeSellSummary.coffe += cafeItem.coffe;
        cafeSellSummary.amountDue = cafeSellSummary.amountDue.add(cafeItem.amountDue);
        cafeSellSummary.amountPaid = cafeSellSummary.amountPaid.add(cafeItem.amountPaid);
    }

    private CafeItem getCafeItemDetails() {
        String sellDateTime = tvSellDate.getText().toString();
        if(!editing) {
            SimpleDateFormat timeF = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = timeF.format(Calendar.getInstance().getTime());
            sellDateTime += " " + time;
        }
        int tea = getTea();
        int coffe = getCoffe();

        String paid = getPaymentStatus();
        return new CafeItem(sellDateTime, tea, coffe, paid, cafeSettings.teaRate, cafeSettings.coffeRate);
    }

    @NonNull
    private String getPaymentStatus() {
        int selectedId;
        RadioButton selectedButton;
        selectedId = radioPaymentGroup.getCheckedRadioButtonId();
        String paid = NO;
        if(selectedId != -1) {
            selectedButton = (RadioButton) findViewById(selectedId);
            if(R.id.radioButtonPaid == selectedId) {
                paid = YES;
            }
        }
        return paid;
    }

    private int getCoffe() {
        int selectedId;
        RadioButton selectedButton;

        selectedId = radioCoffeGroup.getCheckedRadioButtonId();
        int coffe = 0;
        if(selectedId != -1) {
            selectedButton = (RadioButton) findViewById(selectedId);
            coffe = Integer.valueOf(selectedButton.getText().toString());
        }
        return coffe;
    }

    private int getTea() {
        int selectedId = radioTeaGroup.getCheckedRadioButtonId();
        RadioButton selectedButton;
        int tea = 0;
        if(selectedId != -1) {
            selectedButton = (RadioButton) findViewById(selectedId);
            tea = Integer.valueOf(selectedButton.getText().toString());
        }
        return tea;
    }

    private void pay() {
        String initialValue = tvNotPaidTotal.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Amount paid");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(initialValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Pay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String amountPaid = input.getText().toString();
                if (amountPaid == null || amountPaid.isEmpty()) {
                    return;
                }
                payDues(new BigDecimal(amountPaid));

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void payDues(BigDecimal amountPaid) {
        List<KeyValue> data = dataStorageManager.getValues();

        if (!data.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (KeyValue entry : data) {
                CafeItem cafeItem = gson.fromJson(entry.getValue(), CafeItem.class);
                if(cafeItem.paid.equals(NO)){
                    if(amountPaid.compareTo(cafeItem.amountDue) >= 0){
                        cafeItem.paid = YES;
                        amountPaid = amountPaid.subtract(cafeItem.amountDue);
                        cafeItem.amountPaid = cafeItem.amountDue;
                        cafeItem.amountDue = BigDecimal.ZERO;
                    } else {
                        cafeItem.amountDue = cafeItem.amountDue.subtract(amountPaid);
                        cafeItem.amountPaid = cafeItem.amountPaid.add(amountPaid);
                        amountPaid = amountPaid.subtract(cafeItem.amountDue);
                    }
                    save(cafeItem);
                }
            }
        }
    }
}
