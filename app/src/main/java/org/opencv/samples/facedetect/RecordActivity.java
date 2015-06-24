package org.opencv.samples.facedetect;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;

public class RecordActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setListAdapter(new MyAdapter(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("debug", "running record");
    }
}