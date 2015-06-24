package org.opencv.samples.facedetect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.io.File;

public class SharePhotoToFB extends Activity {
    private ImageView mBlinkPhoto;
    private TextView mBlinkPhotoScore;
    private String mScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_photo);
        FacebookSdk.sdkInitialize(getApplicationContext());

        mScore = this.getIntent().getStringExtra("SCORE");

        mBlinkPhotoScore = (TextView) findViewById(R.id.blinkPhotoTitle);
        mBlinkPhoto = (ImageView) findViewById(R.id.blinkPhoto);

        mBlinkPhotoScore.setText(mScore);
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = "blinkblink.png";
        File file = new File(path, filename);

        if(file.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            mBlinkPhoto.setImageBitmap(myBitmap);

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(myBitmap)
                    .setCaption("Give me my codez or I will ... you know, do that thing you don't like!")
                    .build();

            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            ShareApi.share(content, null);
        }
    }
}