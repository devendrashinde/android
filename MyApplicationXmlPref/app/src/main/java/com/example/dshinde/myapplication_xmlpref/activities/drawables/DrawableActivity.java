package com.example.dshinde.myapplication_xmlpref.activities.drawables;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.ReadOnceDataStorage;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DrawableActivity extends BaseActivity {

    RelationshipView customDrawableView;
    public ScrollView scroll_view;
    public HorizontalScrollView h_scroll_view;
    public LinearLayout lin_layout;
    PhotoView photoView;
    String collectionName;
    private static final String CLASS_TAG = "DrawableActivity";
    ReadOnceDataStorage readOnceDataStorage;
    Set<String> parent = new HashSet<>();
    Map<String, Set<String>> relationShips;
    String parentNode;
    DocumentFile selectedDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString("filename");
        userId = bundle.getString("userId");

        setContentView(R.layout.activity_dynamic_linear_layout);
        lin_layout = findViewById(R.id.linear_layout);

        scroll_view = new ScrollView(this);
        h_scroll_view = new HorizontalScrollView(this);

        scroll_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        h_scroll_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        /*
        lin_layout = new LinearLayout(this);
        lin_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        lin_layout.setOrientation(LinearLayout.VERTICAL);
        */
        initDataStorageAndLoadData();
    }

    private void setView() {
        addRelationshipView(parentNode);
        h_scroll_view.addView(scroll_view);
        setParents();
        lin_layout.addView(h_scroll_view);
    }

    private void addRelationshipView(String parentNode) {
        if(customDrawableView != null){
            scroll_view.removeView(customDrawableView);
        }
        customDrawableView = new RelationshipView(this, parentNode, relationShips);
        customDrawableView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scroll_view.addView(customDrawableView);
    }

    private void setParents() {
        if(parent != null) {
            String[] arr = parent.stream().toArray(String[] ::new);
            RadioGroup rg = DynamicControls.getRadioGroupControl(this, arr, Collections.singletonList(parentNode));
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton selectedButton = (RadioButton) lin_layout.findViewById(checkedId);
                    if (selectedButton != null) {
                        parentNode = selectedButton.getText().toString();
                        addRelationshipView(parentNode);
                    }
                }
            });
            lin_layout.addView(rg);
        }
    }

    private void initDataStorageAndLoadData() {

        readOnceDataStorage = Factory.getReadOnceDataStorageIntsance(this,
            getDataStorageType(),
            collectionName,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    String jsonString = Converter.getValuesJsonString(data);
                    readOnceDataStorage.removeDataStorageListeners();
                    parse(data);
                    setView();
                }
            });
    }

    private void parse(List<KeyValue> keyValues){
        relationShips = new HashMap<>();
        Set<String> childs = new HashSet<>();
        for(KeyValue key : keyValues){
            String[] parts = key.getKey().split(",");
            addToMap(parts[0],parts[1] + "," + parts[2]);
            if(!childs.contains(parts[0])) {
                parent.add(parts[0]);
            }
            childs.add(parts[1]);
            parent.remove(parts[1]);
        }
        parentNode = parent.iterator().next();
    }

    private void addToMap(String key, String value){
        relationShips.computeIfAbsent(key, k -> new HashSet<>());
        relationShips.get(key).add(value);
    }

    public void export() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), StorageUtil.PICK_DOCUMENT_FOLDER_FOR_EXPORT);
    }

    protected void doExport(DocumentFile mediaStorageDir) {

        // save view to file
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";

        String selectedOutputPath = mediaStorageDir.getUri().getPath() + File.separator + imageName;

        customDrawableView.setDrawingCacheEnabled(true);
        customDrawableView.buildDrawingCache();
        Bitmap bitmap = customDrawableView.getDrawingCache();
        //Bitmap bitmap = Bitmap.createBitmap(customDrawableView.getDrawingCache());
        customDrawableView.setDrawingCacheEnabled(false);
        customDrawableView.destroyDrawingCache();

        int maxSize = 1080;

        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        if (bWidth > bHeight) {
            int imageHeight = (int) Math.abs(maxSize * ((float)bitmap.getWidth() / (float) bitmap.getHeight()));
            bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, imageHeight, true);
        } else {
            int imageWidth = (int) Math.abs(maxSize * ((float)bitmap.getWidth() / (float) bitmap.getHeight()));
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, maxSize, true);
        }

        OutputStream fOut = null;
        try {
            File file = new File(selectedOutputPath);
            fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        menu.removeItem(R.id.menu_add);
        menu.removeItem(R.id.menu_share);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_settings);
        menu.removeItem(R.id.menu_sell);
        menu.removeItem(R.id.menu_pay);
        menu.removeItem(R.id.menu_save);
        menu.removeItem(R.id.menu_copy);
        menu.removeItem(R.id.menu_edit);
        menu.removeItem(R.id.menu_remove);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_import);
        menu.removeItem(R.id.menu_view);
        menu.removeItem(R.id.menu_design_screen);
        menu.removeItem(R.id.menu_add_to_shadba_kosh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_export:
                export();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
