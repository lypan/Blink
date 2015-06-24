package org.opencv.samples.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 0, 255, 255);
    private static final Scalar EYE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final Scalar IRIS_CIRCLE_COLOR = new Scalar(255, 0, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    private MenuItem mItemFace50;
    private MenuItem mItemFace40;
    private MenuItem mItemFace30;
    private MenuItem mItemFace20;
    private MenuItem mItemType;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private File mEyeCascadeFile;
    private CascadeClassifier mJavaDetector;
    private CascadeClassifier mEyeJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2F;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Button mStart;
    private TextView mTimer;
    private Handler mHandler = new Handler();
    private Long mStartTime = 0L;
    private Boolean mFlagStart = false;
    private Boolean mDetectedFace = false;
    private Boolean mDetectedEye = false;
    private Long mCount = 0L;
    private Long mScore = 0L;
    private Context mContext;
    private Boolean mShowing;
    private Mat mBlinkPhoto;
    private String ID_str;
//    combined ui


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                        InputStream Eyeis = getResources().openRawResource(R.raw.haarcascade_eye);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalcatface_alt.xml");
                        mEyeCascadeFile = new File(cascadeDir, "haarcascade_eye.xml");

                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        FileOutputStream Eyeos = new FileOutputStream(mEyeCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        byte[] Eyebuffer = new byte[4096];
                        int EyebytesRead;
                        while ((EyebytesRead = Eyeis.read(Eyebuffer)) != -1) {
                            Eyeos.write(Eyebuffer, 0, EyebytesRead);
                        }
                        Eyeis.close();
                        Eyeos.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());


                        mEyeJavaDetector = new CascadeClassifier(mEyeCascadeFile.getAbsolutePath());
                        if (mEyeJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load eye cascade classifier");
                            mEyeJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade eye classifier from " + mEyeCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mContext = this;
        ID_str = this.getIntent().getExtras().getString("ID");

        Bundle bundle = new Bundle();
        bundle.putString("ID", ID_str);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);

        mStart = (Button) findViewById(R.id.start);
        mTimer = (TextView) findViewById(R.id.timer);

        mStartTime = System.currentTimeMillis();
        mHandler.removeCallbacks(updateTimer);
        mHandler.postDelayed(updateTimer, 1000);

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlagStart = true;
            }
        });

        mFlagStart = false;
        mShowing = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        mShowing = false;
        mFlagStart = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

        mShowing = false;
        mFlagStart = false;
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

//        lypan opencv modification
        int flags = Objdetect.CASCADE_FIND_BIGGEST_OBJECT;
        Size minFeatureSize = new Size(50.0, 50.0);
        Size maxFeatureSize = new Size();
        // How detailed should the search be. Must be larger than 1.0.
        float searchScaleFactor = 1.1f;
        // How much the detections should be filtered out. This should depend on how bad false detections are to your system.
        // minNeighbors=2 means lots of good+bad detections, and minNeighbors=6 means only good detections are given but some are missed.
        int minNeighbors = 4;


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        MatOfRect leftEyeRect = new MatOfRect();
        MatOfRect rightEyeRect = new MatOfRect();


//==============face detection==============
        int scaledWidth = 200;
        Mat inputImg = new Mat();
        float scale = mGray.cols() / (float) scaledWidth;
        if (mGray.cols() > scaledWidth) {
            // Shrink the image while keeping the same aspect ratio.
            int scaledHeight = Math.round(mGray.rows() / scale);
            Imgproc.resize(mGray, inputImg, new Size(scaledWidth, scaledHeight));
        } else {
            // Access the input image directly, since it is already small.
            inputImg = mGray;
        }

        Mat equalizedImg = new Mat();
        Imgproc.equalizeHist(inputImg, equalizedImg);

        mJavaDetector.detectMultiScale(equalizedImg, faces, searchScaleFactor, minNeighbors, 2, minFeatureSize, maxFeatureSize);

        Rect[] facesArray = faces.toArray();
        if (mGray.cols() > scaledWidth) {
            for (int i = 0; i < facesArray.length; i++) {
                facesArray[i].x = Math.round(facesArray[i].x * scale);
                facesArray[i].y = Math.round(facesArray[i].y * scale);
                facesArray[i].width = Math.round(facesArray[i].width * scale);
                facesArray[i].height = Math.round(facesArray[i].height * scale);
            }
        }
        Rect biggestFace = new Rect();
        for (Rect rect : facesArray) {
            if (rect.area() > biggestFace.area()) {
                biggestFace = rect;
            }
        }
        Core.rectangle(mRgba, biggestFace.tl(), biggestFace.br(), FACE_RECT_COLOR, 3);


//==============eye detection==============
        // For default eye.xml or eyeglasses.xml: Finds both eyes in roughly 40% of detected faces, but does not detect closed eyes.
        if (biggestFace.width > 0) {
            mDetectedFace = true;

            Mat faceImg = mGray.submat(biggestFace);

            float EYE_SX = 0.16f;
            float EYE_SY = 0.26f;
            float EYE_SW = 0.30f;
            float EYE_SH = 0.28f;
            Point leftEye, rightEye;


            int leftX = Math.round(faceImg.cols() * EYE_SX);
            int topY = Math.round(faceImg.rows() * EYE_SY);
            int widthX = Math.round(faceImg.cols() * EYE_SW);
            int heightY = Math.round(faceImg.rows() * EYE_SH);
            int rightX = (int) Math.round(faceImg.cols() * (1.0 - EYE_SX - EYE_SW));  // Start of right-eye corner

            Mat topLeftOfFace = faceImg.submat(new Rect(leftX, topY, widthX, heightY));
            Core.rectangle(mRgba, new Rect(biggestFace.x + leftX, biggestFace.y + topY, widthX, heightY).tl(), new Rect(biggestFace.x + leftX, biggestFace.y + topY, widthX, heightY).br(), EYE_RECT_COLOR, 3);

            Mat topRightOfFace = faceImg.submat(new Rect(rightX, topY, widthX, heightY));
            Core.rectangle(mRgba, new Rect(biggestFace.x + rightX, biggestFace.y + topY, widthX, heightY).tl(), new Rect(biggestFace.x + rightX, biggestFace.y + topY, widthX, heightY).br(), EYE_RECT_COLOR, 3);

//==============left eye detection==============
            int LEscaledWidth = topLeftOfFace.cols();
            Mat LEImg = new Mat();
            float LEscale = topLeftOfFace.cols() / (float) LEscaledWidth;
            if (topLeftOfFace.cols() > LEscaledWidth) {
                // Shrink the image while keeping the same aspect ratio.
                int LEscaledHeight = Math.round(topLeftOfFace.rows() / LEscale);
                Imgproc.resize(topLeftOfFace, LEImg, new Size(LEscaledWidth, LEscaledHeight));
            } else {
//                // Access the input image directly, since it is already small.
                LEImg = topLeftOfFace;
            }
            Mat LEequalizedImg = new Mat();
            Imgproc.equalizeHist(LEImg, LEequalizedImg);

            mEyeJavaDetector.detectMultiScale(LEImg, leftEyeRect, searchScaleFactor, minNeighbors, 2, minFeatureSize, maxFeatureSize);

            Rect[] LEArray = leftEyeRect.toArray();
            if (topLeftOfFace.cols() > LEscaledWidth) {
                for (int i = 0; i < LEArray.length; i++) {
                    LEArray[i].x = Math.round(LEArray[i].x * scale);
                    LEArray[i].y = Math.round(LEArray[i].y * scale);
                    LEArray[i].width = Math.round(LEArray[i].width * scale);
                    LEArray[i].height = Math.round(LEArray[i].height * scale);
                }
            }
            // Make sure the object is completely within the image, in case it was on a border.
            for (int i = 0; i < LEArray.length; i++) {
                if (LEArray[i].x < 0)
                    LEArray[i].x = 0;
                if (LEArray[i].y < 0)
                    LEArray[i].y = 0;
                if (LEArray[i].x + LEArray[i].width > topLeftOfFace.cols())
                    LEArray[i].x = topLeftOfFace.cols() - LEArray[i].width;
                if (LEArray[i].y + LEArray[i].height > topLeftOfFace.rows())
                    LEArray[i].y = topLeftOfFace.rows() - LEArray[i].height;
            }

            Rect biggestLE = new Rect();
            for (Rect rect : LEArray) {
                if (rect.area() > biggestLE.area()) {
                    biggestLE = rect;
                }
            }
            if (biggestLE.width > 0) {   // Check if the eye was detected.
                mDetectedEye = true;
                biggestLE.x += leftX;    // Adjust the left-eye rectangle because the face border was removed.
                biggestLE.x += biggestFace.x;
                biggestLE.y += topY;
                biggestLE.y += biggestFace.y;
                leftEye = new Point(biggestLE.x + biggestLE.width / 2, biggestLE.y + biggestLE.height / 2);
                Core.circle(mRgba, leftEye, 20, IRIS_CIRCLE_COLOR, 10);
            } else {
                mDetectedEye = false;
            }
//==============right eye detection==============
            int REscaledWidth = topRightOfFace.cols();
            Mat REImg = new Mat();
            float REscale = topRightOfFace.cols() / (float) REscaledWidth;
            if (topRightOfFace.cols() > REscaledWidth) {
                // Shrink the image while keeping the same aspect ratio.
                int REscaledHeight = Math.round(topRightOfFace.rows() / REscale);
                Imgproc.resize(topRightOfFace, REImg, new Size(REscaledWidth, REscaledHeight));
            } else {
//                // Access the input image directly, since it is already small.
                REImg = topRightOfFace;
            }
            Mat REequalizedImg = new Mat();
            Imgproc.equalizeHist(REImg, REequalizedImg);

            mEyeJavaDetector.detectMultiScale(REImg, rightEyeRect, searchScaleFactor, minNeighbors, 2, minFeatureSize, maxFeatureSize);


            Rect[] REArray = rightEyeRect.toArray();
            if (topRightOfFace.cols() > REscaledWidth) {
                for (int i = 0; i < REArray.length; i++) {
                    REArray[i].x = Math.round(REArray[i].x * scale);
                    REArray[i].y = Math.round(REArray[i].y * scale);
                    REArray[i].width = Math.round(REArray[i].width * scale);
                    REArray[i].height = Math.round(REArray[i].height * scale);
                }
            }
            // Make sure the object is completely within the image, in case it was on a border.
            for (int i = 0; i < REArray.length; i++) {
                if (REArray[i].x < 0)
                    REArray[i].x = 0;
                if (REArray[i].y < 0)
                    REArray[i].y = 0;
                if (REArray[i].x + REArray[i].width > topRightOfFace.cols())
                    REArray[i].x = topRightOfFace.cols() - REArray[i].width;
                if (REArray[i].y + REArray[i].height > topRightOfFace.rows())
                    REArray[i].y = topRightOfFace.rows() - REArray[i].height;
            }
            Rect biggestRE = new Rect();
            for (Rect rect : REArray) {
                if (rect.area() > biggestRE.area()) {
                    biggestRE = rect;
                }
            }
            if (biggestRE.width > 0) {   // Check if the eye was detected.
                mDetectedEye = true;
                biggestRE.x += rightX;    // Adjust the left-eye rectangle because the face border was removed.
                biggestRE.x += biggestFace.x;
                biggestRE.y += topY;
                biggestRE.y += biggestFace.y;
                rightEye = new Point(biggestRE.x + biggestRE.width / 2, biggestRE.y + biggestRE.height / 2);
                Core.circle(mRgba, rightEye, 20, IRIS_CIRCLE_COLOR, 10);
            } else {
                mDetectedEye = false;
            }

        } else {
            mDetectedFace = false;
        }

        if (mFlagStart && mDetectedFace && !mDetectedEye) {
            Log.d("debug", "blink blink");

            Log.d("debug", ID_str);
            DBfunction.update_record(ID_str, String.format("%02d", (mScore / 1000) / 60) + ":" + String.format("%02d", (mScore / 1000) % 60) + ":" + String.format("%02d", (mScore / 10) % 100));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mShowing) {

                        Mat mIntermediateMat = new Mat();
                        Imgproc.cvtColor(mRgba, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        String filename = "blinkblink.png";
                        File file = new File(path, filename);
                        Boolean bool = null;
                        filename = file.toString();
                        bool = Highgui.imwrite(filename, mIntermediateMat);
//                        if (bool == true)
//                            Toast.makeText(getApplicationContext(), "SUCCESS writing image to external storage", Toast.LENGTH_SHORT).show();
//                        else
//                        Toast.makeText(getApplicationContext(), "Fail writing image to external storage", Toast.LENGTH_SHORT).show();

                        mShowing = true;
                        new AlertDialog.Builder(mContext)
                                .setTitle("Result")
                                .setCancelable(true)
                                .setMessage("Your Score: " + String.format("%02d", (mScore / 1000) / 60) + ":" + String.format("%02d", (mScore / 1000) % 60) + ":" + String.format("%02d", (mScore / 10) % 100))
                                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Bundle bundle = new Bundle();
                                        bundle.putString("SCORE", String.format("%02d", (mScore / 1000) / 60) + ":" + String.format("%02d", (mScore / 1000) % 60) + ":" + String.format("%02d", (mScore / 10) % 100));
                                        startActivity(new Intent().setClass(mContext, HelloFacebookSampleActivity.class).putExtras(bundle));
//                                        mShowing = false;
//                                        mFlagStart = false;
                                    }
                                }).setNegativeButton("Record", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle = new Bundle();
                                bundle.putString("ID", ID_str);
                                startActivity(new Intent().setClass(mContext, RecordActivity.class));
//                                mShowing = false;
//                                mFlagStart = false;
                            }
                        }).create().show();
                    }
                }
            });
        }
        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }

    private Runnable updateTimer = new Runnable() {
        public void run() {
            final TextView time = (TextView) findViewById(R.id.timer);
            if (mFlagStart == false) {
                mStartTime = System.currentTimeMillis();
            }
            long spentTime = System.currentTimeMillis() - mStartTime;

            long minutes = (spentTime / 1000) / 60;
            long seconds = (spentTime / 1000) % 60;
            long mseconds = (spentTime / 10) % 100;

            mScore = spentTime;

            mTimer.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ":" + String.format("%02d", mseconds));
            mHandler.postDelayed(this, 10);
        }
    };

}
