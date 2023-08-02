package s.javaproject.homevault;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;


public class MainFunctions {

    public static String _FILE_PATH_ = "homevault_db.json";
    public static String[] _TYPES_ = new String[]{"Unbekannt", "Mahlzeit", "Brot", "Brotbelag", "Getränk", "Beilage"};

    public static Bitmap uriToBitmap(Uri selectedFileUri, ContentResolver contentResolver) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    contentResolver.openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static String bitmapToBase64(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    public static Bitmap base64ToBitmap(String base64)
    {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    public static void SetTypeDropDown(Spinner dropdown, Context activity)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, _TYPES_);
        dropdown.setAdapter(adapter);
    }

    public static JSONArray GetAllJSON(Context ctx)
    {
        File file = new File(ctx.getExternalFilesDir(null).getAbsolutePath(), _FILE_PATH_);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        try {
            JSONObject obj = new JSONObject(text.toString());
            JSONArray itemArray = obj.getJSONArray("items");

            return itemArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray GetEanInJSON(Context ctx, String ean)
    {
        JSONArray itemArray = GetAllJSON(ctx);
        //System.out.println(itemArray);
        JSONArray retArray = null;
        try {
            if (itemArray != null) {
                for (int i = 0; i < itemArray.length(); i++) {
                    JSONObject itemObject = itemArray.getJSONObject(i);
                    String currentEAN = itemObject.getString("ean");
                    if(currentEAN.equals(ean))
                    {
                        retArray = new JSONArray();
                        JSONObject innerObj = new JSONObject();
                        try {
                            innerObj.put("ean", itemObject.getString("ean"));
                            innerObj.put("description", itemObject.getString("description"));
                            innerObj.put("amount", itemObject.getString("amount"));
                            innerObj.put("type", itemObject.getInt("type"));
                            innerObj.put("image", itemObject.getString("image"));
                            retArray.put(innerObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(retArray);
        return retArray;
    }

    public static boolean SaveAllJSON(List<VaultItem> items, Context ctx)
    {
        JSONObject jsonObject = CreateJSON(items);
        try {
            Writer output = null;
            File file = new File(ctx.getExternalFilesDir(null).getAbsolutePath(), _FILE_PATH_);
            output = new BufferedWriter(new FileWriter(file));
            output.write(jsonObject.toString());
            output.close();
            return true;

        } catch (Exception e) {
            Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static JSONObject CreateJSON (List<VaultItem> items) {

        JSONObject obj = new JSONObject() ;
        JSONArray arr = new JSONArray();
        int count = 0;
        for(VaultItem item : items)
        {
            try {
                JSONObject innerObj = new JSONObject();
                try {
                    innerObj.put("ean", item.getEAN());
                    innerObj.put("description", item.getDescription());
                    innerObj.put("amount", item.count());
                    innerObj.put("type", item.getType());
                    innerObj.put("image", item._image);
                    arr.put(innerObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                obj.put("items", arr);
                count++;
            } catch (JSONException f) {
                f.printStackTrace();
            }
        }
        try {
            obj.put("total", count);
        } catch (JSONException f) {
            f.printStackTrace();
        }

        return obj;
    }

    public static Bitmap RotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static Bitmap getRotateImage(String photoPath, Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = RotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = RotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = RotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;
    }

    public static void CreatePhoto(Uri image_uri, ContentResolver contentResolver, ImageView itemPhoto){
        Bitmap bitmap = MainFunctions.uriToBitmap(image_uri, contentResolver);

        try {
            //itemPhoto.setImageBitmap(getRotateImage(image_uri.toString(), bitmap));
            itemPhoto.setImageBitmap(bitmap);
        }
        catch(Exception e)
        {

        }
    }
}
