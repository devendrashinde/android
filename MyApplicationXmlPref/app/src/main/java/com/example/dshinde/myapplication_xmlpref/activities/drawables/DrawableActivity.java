package com.example.dshinde.myapplication_xmlpref.activities.drawables;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import androidx.documentfile.provider.DocumentFile;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.activities.BaseActivity;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.Converter;
import com.example.dshinde.myapplication_xmlpref.helper.DynamicControls;
import com.example.dshinde.myapplication_xmlpref.helper.Factory;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;
import com.example.dshinde.myapplication_xmlpref.listners.DataStorageListener;
import com.example.dshinde.myapplication_xmlpref.model.KeyValue;
import com.example.dshinde.myapplication_xmlpref.services.ReadWriteOnceDataStorage;
import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
    ReadWriteOnceDataStorage readWriteOnceDataStorage;
    Set<String> parent = new HashSet<>();
    Map<String, Set<String>> relationShips;
    String parentNode;
    DocumentFile selectedDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle bundle = getIntent().getExtras();
        collectionName = bundle.getString(Constants.PARAM_FILENAME);
        userId = bundle.getString(Constants.USERID);

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
        photoView = DynamicControls.getPhotoView(this, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        customDrawableView = new RelationshipView(this, parentNode, relationShips, 0);
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

        readWriteOnceDataStorage = Factory.getReadOnceFireDataStorageInstance(
            collectionName,
            new DataStorageListener() {
                @Override
                public void dataChanged(String key, String value) {
                }

                @Override
                public void dataLoaded(List<KeyValue> data) {
                    String jsonString = Converter.getValuesJsonString(data);
                    readWriteOnceDataStorage.removeDataStorageListeners();
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

    public static Bitmap loadBitmapFromView(View view) {

        // width measure spec
        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                view.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
        // height measure spec
        int heightSpec = View.MeasureSpec.makeMeasureSpec(
                view.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
        // measure the view
        view.measure(widthSpec, heightSpec);
        // set the layout sizes
        view.layout(view.getLeft(), view.getTop(), view.getMeasuredWidth() + view.getLeft(), view.getMeasuredHeight() + view.getTop());
        // create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // create a canvas used to get the view's image and draw it on the bitmap
        Canvas c = new Canvas(bitmap);
        // position the image inside the canvas
        c.translate(-view.getScrollX(), -view.getScrollY());
        // get the canvas
        view.draw(c);

        return bitmap;
    }

    protected void doExport(DocumentFile mediaStorageDir) {

        // save view to file
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";

        DocumentFile file = mediaStorageDir.createFile(StorageUtil.JPEG_FILE, imageName);

        Bitmap bitmap = loadBitmapFromView(customDrawableView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream out = getApplicationContext().getContentResolver().openOutputStream(file.getUri());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void cropPhoto(Uri photoUri) {
        CropImage.activity(photoUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        menu.removeItem(R.id.menu_add);
        menu.removeItem(R.id.menu_share);
        menu.removeItem(R.id.menu_clear);
        menu.removeItem(R.id.menu_test);
        menu.removeItem(R.id.menu_daylight);
        menu.removeItem(R.id.menu_nightlight);
        menu.removeItem(R.id.menu_save);
        menu.removeItem(R.id.menu_copy);
        menu.removeItem(R.id.menu_edit);
        menu.removeItem(R.id.menu_remove);
        menu.removeItem(R.id.menu_backup);
        menu.removeItem(R.id.menu_import);
        menu.removeItem(R.id.menu_view);
        menu.removeItem(R.id.menu_design_screen);
        menu.removeItem(R.id.menu_add_to_dictionary);
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
