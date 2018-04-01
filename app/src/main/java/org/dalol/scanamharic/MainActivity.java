package org.dalol.scanamharic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;

public class MainActivity extends AppCompatActivity {

    private OcrHelper ocrHelper;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView mCameraView;
    private MaskView viewMask;

    private int RECT_WIDTH;
    private int RECT_HEIGHT;

    private float cameraRatio;
    private float ratio = 1f;

    private Point rectPictureSize;
    private int mCameraWidth;
    private int mCameraHeight;

    private int leftRight;
    private int topBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = findViewById(R.id.camera_view);
        findViewById(R.id.ibt_capture)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCameraView.takePicture();
                    }
                });

        mCameraView.addCallback(mCallback);


        ocrHelper = new OcrHelper();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ocrHelper.init(getApplicationContext());

                ocrHelper.recognize(getApplicationContext(), BitmapFactory.decodeResource(getResources(), R.drawable.test));
                ocrHelper.recognize(getApplicationContext(), BitmapFactory.decodeResource(getResources(), R.drawable.test2));
            }
        }).start();


        viewMask = (MaskView) findViewById(R.id.view_mask);
        AspectRatio currentRatio = mCameraView.getAspectRatio();
        cameraRatio = currentRatio.toFloat();
        mCameraWidth = (int) DisplayUtils.getScreenWidth(this);
        mCameraHeight = (int) (mCameraWidth * cameraRatio);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = mCameraWidth;
        layoutParams.height = mCameraHeight;
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        viewMask.setLayoutParams(layoutParams);

        if (ratio > cameraRatio) {
            RECT_HEIGHT = mCameraHeight - topBottom;
            RECT_WIDTH = (int) (RECT_HEIGHT / ratio);
        } else {
            RECT_WIDTH = mCameraWidth - leftRight;
            RECT_HEIGHT = (int) (RECT_WIDTH * ratio);
        }
        if (viewMask != null) {
            Rect screenCenterRect = DisplayUtils.createCenterScreenRect(mCameraWidth, mCameraHeight, RECT_WIDTH, RECT_HEIGHT);
            viewMask.setCenterRect(screenCenterRect);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    private void showLog(String tag, String message) {
        Log.d(tag, message);
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            showToast("Show explanation");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
//                if (permissions.length != 1 || grantResults.length != 1) {
//                    throw new RuntimeException(getString(R.string.error_camera_permission));
//                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Camera permission not granted");
                }
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
            showLog(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
            showLog(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            showLog(TAG, "onPictureTaken " + data.length);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成类图


            ocrHelper.recognize(getApplicationContext(), bitmap);

            //            //保存图片到sdcard
//            if (bitmap != null) {
//                if (rectPictureSize == null) {
//                    rectPictureSize = DisplayUtils.createCenterPictureRect(ratio, cameraRatio, bitmap.getWidth(), bitmap.getHeight());
//                }
//                int x = bitmap.getWidth() / 2 - rectPictureSize.x / 2;
//                int y = bitmap.getHeight() / 2 - rectPictureSize.y / 2;
//                Bitmap rectBitmap = Bitmap.createBitmap(bitmap, x, y, rectPictureSize.x, rectPictureSize.y);
//                int imageWidth = rectBitmap.getWidth();
//                int imageHeight = rectBitmap.getHeight();
//                FileUtils.saveBitmap(rectBitmap,imagePath);
//                setResultUri(imageUri,imageWidth,imageHeight);
//
//                if (bitmap.isRecycled()) {
//                    bitmap.recycle();
//                }
//                if (rectBitmap.isRecycled()) {
//                    rectBitmap.recycle();
//                }
//
//                finish();
//
//            }
        }
    };
}
