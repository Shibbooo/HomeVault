package s.javaproject.homevault;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

public class ItemDetail extends AppCompatActivity {

    Uri image_uri;

    Button saveBtn;
    Button cancelBtn;
    EditText eanField;
    EditText descriptionField;
    EditText amountField;
    ImageView itemPhoto;
    Spinner dropdown;
    String base64Img;

    public static final int REQUEST_CODE_SCAN = 1000;
    public static final int REQUEST_CODE_PHOTO = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        base64Img = "";

        eanField = findViewById(R.id.eanField);
        descriptionField = findViewById(R.id.descriptionField);
        amountField = findViewById(R.id.amountField);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        itemPhoto = findViewById(R.id.itemPhoto);

        dropdown = findViewById(R.id.spinner);
        MainFunctions.SetTypeDropDown(dropdown, this);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        eanField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenScanner();
            }
        });

        itemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCamera();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
            }
        });

        String ean = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ean = extras.getString("EAN");
        }
        eanField.setText(ean);
    }

    private void OpenCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "HomeVault Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Made by HomeVault");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

        if(cameraIntent.resolveActivity(this.getPackageManager()) != null)
            startActivityForResult(cameraIntent, REQUEST_CODE_PHOTO);
        else
            Toast.makeText(this, "Keine Kamera-App gefunden!", Toast.LENGTH_SHORT).show();

    }

    private void SaveData() {
        String ean = eanField.getText().toString();
        String description = descriptionField.getText().toString();
        String amount = amountField.getText().toString();
        int type = dropdown.getSelectedItemPosition();
        String image = "";
        if(image_uri != null)
            image = image_uri.toString();
        returnData(ean, description, amount, type, image);
    }

    private void returnData(String ean, String description, String amount, int type, String image) {
        Intent intent = new Intent();

        intent.putExtra("EAN", ean);
        intent.putExtra("DESC", description);
        intent.putExtra("AMOUNT", amount);
        intent.putExtra("TYPE", type);
        intent.putExtra("IMAGE", image);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void OpenScanner() {
        Intent intent = new Intent(this, ScannerView.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_CODE_SCAN:
                if(resultCode == Activity.RESULT_OK)
                {
                    String ean = data.getStringExtra("EANCODE");
                    eanField.setText(ean);
                }
                break;
            case REQUEST_CODE_PHOTO:
                if(resultCode == Activity.RESULT_OK)
                {
                    MainFunctions.CreatePhoto(image_uri, getContentResolver(), itemPhoto);
                }
                break;
            default:
                break;
        }
    }

}
