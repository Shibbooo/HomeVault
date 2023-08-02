package s.javaproject.homevault;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
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

import static java.lang.Integer.parseInt;

public class ItemInfo extends AppCompatActivity {
    Uri image_uri;
    Button saveBtn;
    Button cancelBtn;
    EditText eanField;
    EditText descriptionField;
    EditText amountField;
    ImageView itemPhoto;
    Spinner dropdown;
    String base64Img;

    public static final int REQUEST_CODE_PHOTO = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);

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
        String description = "";
        String amount = "";
        String type = "";
        String image = "";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ean = extras.getString("EAN");
            description = extras.getString("DESC");
            amount = extras.getString("AMOUNT");
            type = extras.getString("TYPE");
            image = extras.getString("IMAGE");
        }
        eanField.setText(ean);
        descriptionField.setText(description);
        amountField.setText(amount);
        dropdown.setSelection(parseInt(type));

        if(!image.isEmpty())
        {
            image_uri = Uri.parse(image);
            MainFunctions.CreatePhoto(image_uri, getContentResolver(), itemPhoto);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
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
