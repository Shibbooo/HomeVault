package s.javaproject.homevault;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import android.view.ViewTreeObserver;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ItemAdapter extends ArrayAdapter<VaultItem> {

    public List<VaultItem> itemList;
    private Context mCtx;
    public Context Mainact;

    public ItemAdapter(List<VaultItem> itemList, Context mCtx, Context Mainact) {
        super(mCtx, R.layout.items, itemList);
        this.itemList = itemList;
        this.mCtx = mCtx;
        this.Mainact = Mainact;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);

        View listViewItem = inflater.inflate(R.layout.items, null, true);

        TextView description = listViewItem.findViewById(R.id.description);
        TextView ean = listViewItem.findViewById(R.id.ean);

        final ImageButton minus = listViewItem.findViewById(R.id.minus);
        final TextView amount = listViewItem.findViewById(R.id.amount);
        final ImageView image = listViewItem.findViewById(R.id.imageView);
        final VaultItem item = itemList.get(position);

        String img = item.getImage();

        if(!img.isEmpty())
        {
            //Uri uri = Uri.parse(img);
            //CreatePhoto(uri, image);
        }

        description.setText(item.getDescription());
        ean.setText(item.getEAN());

        SetListItemState(item.GetAmount(), amount, minus);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer count = item.GetAmount();
                Integer newCount = count - 1;
                if(newCount < 0)
                    newCount = 0;

                item.SetAmount(newCount);
                count = item.GetAmount();
                SetListItemState(count, amount, minus);

                ((MainActivity)Mainact).SaveDataToFile(item.getEAN(), item.getDescription(), item.count(), item._type, item.getImage(), true);

                //SendSetAmount(item.getEAN(), count);
            }
        });

        listViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)Mainact).GetSingleItemFromFile(item.getEAN());
            }
        });

        return listViewItem;
    }

    public void CreatePhoto(Uri uri, ImageView view)
    {
        Bitmap bitmap = MainFunctions.uriToBitmap(uri, Mainact.getContentResolver());
        view.setImageBitmap(bitmap);
    }

    private void SetListItemState(int count, TextView amount, ImageButton minus){
        if(count == 0) {
            amount.setText("Leer");
            minus.setEnabled(false);
            minus.setClickable(false);
            minus.setImageResource(R.drawable.consume_disabled);
        }
        else {
            amount.setText("Noch " + count + " Stück");
            minus.setEnabled(true);
            minus.setClickable(true);
            minus.setImageResource(R.drawable.consume);
        }
    }
}