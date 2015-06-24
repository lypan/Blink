package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomActivity extends Activity {
    /** Called when the activity is first created. */
    private GridView gridView;
    private String ID_str;
    Button bye;
    private int[] image = {
            R.drawable.fight, R.drawable.record,
    };
    private String[] imgText = {
            "Fight", "Record"
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        ID_str = this.getIntent().getExtras().getString("ID");

        final Bundle bundle = new Bundle();
        bundle.putString("ID", ID_str);

        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < image.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", image[i]);
            item.put("text", imgText[i]);
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,
                items, R.layout.grid_item, new String[]{"image", "text"},
                new int[]{R.id.image, R.id.text});
        gridView = (GridView)findViewById(R.id.mygridview);
        gridView.setNumColumns(2);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (imgText[position].equals("Record"))
                {
                    startActivity(new Intent().setClass(RoomActivity.this, RecordActivity.class).putExtras(bundle));
                }
                else if (imgText[position].equals("Fight"))
                {
                    startActivity(new Intent().setClass(RoomActivity.this, FdActivity.class).putExtras(bundle));

                }
                Toast.makeText(getApplicationContext(), "Your choice is " + imgText[position], Toast.LENGTH_SHORT).show();
            }

        });
        bye=(Button)findViewById(R.id.bye);
        bye.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent().setClass(RoomActivity.this, MainActivity.class));
            }
        });
    }
}