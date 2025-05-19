package com.example.dshinde.myapplication_xmlpref.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.canhub.cropper.CropImageView;
import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.helper.StorageUtil;

public class ImageCropperActivity extends AppCompatActivity {

    CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_cropper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.linear_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cropImageView = findViewById(R.id.cropImageView);
        setCropImageCompleteListener();
        Bundle bundle = getIntent().getExtras();
        String imagePath = null;
        if(bundle != null) {
            imagePath = bundle.getString(Constants.PARAM_URL);
        }
        if(imagePath == null) {
            selectImageToCrop();
        } else {
            cropImageView.setImageUriAsync(Uri.parse(imagePath));
        }
    }

    private void selectImageToCrop() {
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        cropImageView.setImageUriAsync(uri);
                    }
                });
        mGetContent.launch(Constants.IMAGE_FILE);
    }

    private void setCropImageCompleteListener() {
        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(@NonNull CropImageView cropImageView, @NonNull CropImageView.CropResult cropResult) {
                if (cropResult.getError() == null) {
                    Intent intent = new Intent();
                    intent.setData(cropResult.getUriContent());
                    setResult(Constants.RESULT_CODE_OK, intent);
                    finish();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cropper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.main_action_crop:
                cropImageView.croppedImageAsync(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        0,
                        0,
                        CropImageView.RequestSizeOptions.RESIZE_INSIDE,
                        getOutputUri());
                return true;
            case R.id.main_action_rotate:
                cropImageView.rotateImage(90);
                return true;
            case R.id.main_action_flip_horizontally:
                cropImageView.flipImageHorizontally();
                return true;
            case R.id.main_action_flip_vertically:
                cropImageView.flipImageVertically();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Uri getOutputUri() {
        return StorageUtil.createImageFileUri(this);
    }

}