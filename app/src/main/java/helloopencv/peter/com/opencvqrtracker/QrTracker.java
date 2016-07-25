package helloopencv.peter.com.opencvqrtracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class QrTracker extends Activity {

    private static final String TAG = "Peter";
    private CameraBridgeViewBase mOpenCvCameraView;
    private myNDK ndk = new myNDK();
    private SubSurfaceView surfaceView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // TODO Load native library after(!) OpenCV initialization


                    // when openCV init finished do camera view enable
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

    private Mat mRgba;
    private int maxSize = 10;
    private QrItem[] qrItems = new QrItem[maxSize];

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

            // 原始影像
            mRgba = inputFrame.rgba(); // RGB 彩色影像

            // QR影像
            Mat[] qrsMat = new Mat[maxSize];
            long[] qrsAddr = new long[qrsMat.length];
            for (int i = 0; i < qrsMat.length; i++) {
                qrsMat[i] = new Mat();
                qrsAddr[i] = qrsMat[i].getNativeObjAddr();
            }

            // 追蹤 jni api
            int c = ndk.jni_QrTracking(mRgba.getNativeObjAddr(), qrsAddr);
            Log.d(TAG, "count : " + String.valueOf(c));

            // 結果 QR 影像轉換
            int i = 0;
            for (Mat tempMat : qrsMat){
                try {

                    Bitmap bitmap = Bitmap.createBitmap(tempMat.cols(), tempMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(tempMat, bitmap);
                    Log.d(TAG, "have marker bitmap, Bitmap Width : " + bitmap.getWidth() + ", Height : " + bitmap.getHeight());

                    String code = QrHelper.getReult(bitmap);
                    Log.d(TAG, "QR code : " + code);

                    qrItems[i] = new QrItem(bitmap, code, 10, i*85);

                    ndk.jni_QrDrawing(mRgba.getNativeObjAddr(), i, code);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
            }

            // 清除 qrItems
            if (c == 0){
                qrItems = new QrItem[maxSize];
            }

            return mRgba;
        }
    };

    private Paint paint = new Paint();
    private Paint paintClear = new Paint();
    private SubSurfaceView.SurfaceListener surfaceListener = new SubSurfaceView.SurfaceListener() {
        @Override
        public void drawing(Canvas canvas) {

            // 清畫布
            paintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(paintClear);
            paintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            paintClear.setColor(Color.parseColor("#ffffff"));
            paintClear.setStyle(Paint.Style.FILL);

            canvas.drawPaint(paintClear);

            // 繪製 QR 資訊
            int i = 0;
            for (QrItem temp : qrItems) {
                if (temp != null) {
                    canvas.drawBitmap(temp.bitmap, temp.imgPos[0], temp.imgPos[1], paint);
                    canvas.drawText(temp.info, temp.infoPos[0], temp.infoPos[1], paint);
                }
                i++;
            }
        }
    };

    static {
        if (!OpenCVLoader.initDebug()) {
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

        LinearLayout layout = (LinearLayout) findViewById(R.id.ll_subview);

        qrItems[0] = new QrItem(BitmapFactory.decodeResource(getResources(),
                R.drawable.opencv_logo_white), "hello qr tracker", 0, 0);

        surfaceView = new SubSurfaceView(this, surfaceListener);

        layout.addView(surfaceView);

        paint.setTextSize(30);         //設定字體大小
        paint.setColor(Color.BLACK);  //設定字體顏色

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
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
