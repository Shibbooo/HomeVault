/*

  TODO:
  - Codes zusammenfassen (Spaghetticode beseitigen)
  - Detailsatz Aufruf nicht anhand der alten Listendaten machen, sondern neue Webanfrage senden!!!
    (nötig für Scan von Hauptseite aus)

 */

package s.javaproject.homevault;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ITEM = 1000;
    public static final int REQUEST_CODE_SCAN = 1001;
    public Integer CAMERA_REQUEST_CODE = 101;

    public SwipeRefreshLayout swipeLayout;

    Button addBtn;
    Button scanBtn;

    Context Maincontext;

    ListView itemList;
    List<VaultItem> items;
    ItemAdapter adapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPermission();

        Maincontext = this;
        swipeLayout = findViewById(R.id.swiperefresh);
        addBtn = findViewById(R.id.addButton);
        scanBtn = findViewById(R.id.scanBtn);
        itemList = findViewById(R.id.itemList);
        items = new ArrayList<>();

        swipeLayout.setRefreshing(true);

        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        LoadItemListFromFile();
                    }
                }
        );

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAddItemView();
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenScanner();
            }
        });

        LoadItemListFromFile();
    }

    private void OpenAddItemView() {
        Intent intent = new Intent(this, ItemDetail.class);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
    }

    private void LoadItemListFromFile() {
        items.clear();
        try {
            JSONArray itemArray = MainFunctions.GetAllJSON(getApplicationContext());

            if(itemArray != null) {
                for (int i = 0; i < itemArray.length(); i++) {
                    JSONObject itemObject = itemArray.getJSONObject(i);

                    String ean = "";
                    try{  ean = itemObject.getString("ean"); } catch(Exception e) {}

                    String description = "";
                    try{  description = itemObject.getString("description"); } catch(Exception e) {}

                    int amount = 0;
                    try{  amount = itemObject.getInt("amount"); } catch(Exception e) {}

                    int type = 0;
                    try{  type = itemObject.getInt("type"); } catch(Exception e) {}

                    String image = "";
                    try{  image = itemObject.getString("image"); } catch(Exception e) {}


                    VaultItem item = new VaultItem(ean, description, amount, type);
                    item._image = image;
                    items.add(item);
                }
                adapter = new ItemAdapter(items, getApplicationContext(), Maincontext);
                itemList.setAdapter(adapter);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "JSON ist irgendwie leer", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        swipeLayout.setRefreshing(false);
    }
    public void GetSingleItemFromFile(String ean) {
        try
        {
            JSONArray itemArray = MainFunctions.GetEanInJSON(getApplicationContext(), ean);

            if(itemArray != null) {
                JSONObject itemObject = itemArray.getJSONObject(0);

                String _ean = "";
                try{  _ean = itemObject.getString("ean"); } catch(Exception e) {}

                String description = "";
                try{  description = itemObject.getString("description"); } catch(Exception e) {}

                String amount = "";
                try{  amount = itemObject.getString("amount"); } catch(Exception e) {}

                String type = "";
                try{  type = itemObject.getString("type"); } catch(Exception e) {}

                String image = "";
                try{  image = itemObject.getString("image"); } catch(Exception e) {}


                OpenItemInfo(_ean, description, amount, type, image);
            } else {
                NewItemInfo(ean);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void NewItemInfo(String ean) {
        Intent intent = new Intent(this, ItemDetail.class);
        intent.putExtra("EAN", ean);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
    }

    private void OpenItemInfo(String ean, String description, String count, String type, String image)
    {
        Intent intent = new Intent(this, ItemInfo.class);
        intent.putExtra("EAN", ean);
        intent.putExtra("DESC", description);
        intent.putExtra("AMOUNT", count);
        intent.putExtra("TYPE", type);
        intent.putExtra("IMAGE", image);
        startActivityForResult(intent, REQUEST_CODE_ITEM);
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
                    GetSingleItemFromFile(ean);
                }
                break;
            case REQUEST_CODE_ITEM:
                if(resultCode == Activity.RESULT_OK)
                {
                    String ean = data.getStringExtra("EAN");
                    String description = data.getStringExtra("DESC");
                    String amount = data.getStringExtra("AMOUNT");
                    int type = data.getIntExtra("TYPE", 0);
                    String image = data.getStringExtra("IMAGE");
                    SaveDataToFile(ean, description, amount, type, image, false);
                }
                break;
            default:
                if(resultCode != Activity.RESULT_CANCELED)
                    LoadItemListFromFile();
                break;
        }
    }

    public void SaveDataToFile(String ean, String description, String amount, int type, String image, boolean fromHere)
    {
        int count = 0;
        for(VaultItem item : items)
        {
            if(item.getEAN().equals(ean))
            {
                item._description = description;
                item._type = type;
                item._image = image;
                try {
                    item._amount = Integer.parseInt(amount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
                break;
            }
        }

        if(count == 0)
        {
            VaultItem item = new VaultItem(ean, description, 0, type);
            item._image = image;
            try {
                item._amount = Integer.parseInt(amount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            items.add(item);
        }

        if(MainFunctions.SaveAllJSON(items, getApplicationContext())) {
            if (!fromHere) {
                LoadItemListFromFile();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Fehler beim Speichern!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPermission()
    {
        Integer permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if(permission != PackageManager.PERMISSION_GRANTED)
        {
            makeRequest();
        }
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if(requestCode == CAMERA_REQUEST_CODE)
        {
            if(grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Du musst Berechtigungen zur Nutzung der Kamera erteilen, um Barcodes zu scannen!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // success
            }
        }
    }
}