package helloopencv.peter.com.opencvqrtracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class QrTracker extends AppCompatActivity {

    private static final String TAG = "Peter";
    private CameraBridgeViewBase mOpenCvCameraView;
    private myNDK ndk = new myNDK();
    private SubSurfaceView surfaceView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // TODO Load native library after(!) OpenCV initialization


                    // when openCV init finished do camera view enable
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private Mat mRgba;
    private Bitmap resultBitmap;

    private CameraBridgeViewBase.CvCameraViewListener2 cameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {
            mRgba = new Mat();
        }

        @Override
        public void onCameraViewStopped() {
            mRgba.release();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

            // TODO something
            Mat mRgba = inputFrame.rgba(); // RGB 彩色影像
            Mat result = new Mat();

            int c = ndk.jni_QrTracking_2(mRgba.getNativeObjAddr(), result.getNativeObjAddr());
            Log.d(TAG, "count : " + String.valueOf(c));

            try {
                resultBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
                org.opencv.android.Utils.matToBitmap(result, resultBitmap);
                Log.d(TAG, "have marker bitmap, Bitmap Width : " + resultBitmap.getWidth() + ", Height : " + resultBitmap.getHeight());

                surfaceView.setBitmap(resultBitmap);

            } catch (Exception e){
                e.printStackTrace();
            }

            return mRgba;
        }
    };

    static {
        if (!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV initialization failed");
        } else {
            Log.d(TAG, "OpenCV initialization successful");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_tracker);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(cameraViewListener);

        LinearLayout layout = (LinearLayout)findViewById(R.id.ll_subview);
        surfaceView = new SubSurfaceView(this);

        surfaceView.setBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.opencv_logo_white));

        layout.addView(surfaceView);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // TODO 告知系統檢查是否支援 OpenCV 已經成功，OpenCVLoader.initAsync 會檢查是否下載 OpenCV Manager
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
}
