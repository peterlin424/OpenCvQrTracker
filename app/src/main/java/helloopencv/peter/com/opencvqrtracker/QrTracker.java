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
import android.widget.RelativeLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;

public class QrTracker extends Activity {

    private static final String TAG = "Peter";
    private CameraBridgeViewBase mOpenCvCameraView;
    private myNDK ndk = new myNDK();
    private ThresholdDBA dba;
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
    private Mat[] matcher = new Mat[2];
    private int maxSize = 10;
    private QrItem[] qrItems = new QrItem[maxSize];
    private int minThreshold = 131;
    private int maxThreshold = 255;
    private boolean isThreshold = false;
    private boolean isBalanceWhite = false;

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
            int c = ndk.jni_QrTracking(mRgba.getNativeObjAddr(), qrsAddr, minThreshold, maxThreshold, isThreshold, isBalanceWhite);
            Log.d(TAG, "count : " + String.valueOf(c));

            // 結果 QR 影像轉換
            ArrayList<QrItem> temp = new ArrayList<>();
            for (int i=0; i<qrsMat.length; i++){

                // step1 : tranBp
                if (qrsMat[i].rows()<=0 || qrsMat[i].cols()<=0)
                    continue;

                Bitmap bitmap = Bitmap.createBitmap(qrsMat[i].cols(), qrsMat[i].rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(qrsMat[i], bitmap);

                Log.d(TAG, "have marker bitmap, Bitmap Width : " + bitmap.getWidth() + ", Height : " + bitmap.getHeight());

                // TODO step2 : check qr code or image target
                String code = "";
                try {
                    code = QrHelper.getReult(bitmap);
                } catch (Exception e){
                    e.printStackTrace();
                }

                Mat bpMat = new Mat();
                Utils.bitmapToMat(bitmap, bpMat);
                boolean isMatch = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0].getNativeObjAddr());
                if (isMatch){
                    code = "matcher";
                }

                if (code.equals(""))
                    continue;

                // 檢查是否重複
                boolean repeat = false;
                for (QrItem item : temp){
                    if (item.info.equals(code)){
                        repeat = true;
                    }
                }
                if (repeat) continue;

                Log.d(TAG, "QR code : " + code);

                // step3 : new and draw
                temp.add(new QrItem(bitmap, code, 10, i*85));
                ndk.jni_QrDrawing(mRgba.getNativeObjAddr(), i, code);
            }

            for (int i=0; i<temp.size(); ++i){
                qrItems[i] = temp.get(i);
            }
            temp.clear();

            // 擴增 陣列長度
            if (c > maxSize) maxSize = c;
            // 清除 qrItems
            if (c == 0) qrItems = new QrItem[maxSize];

            // 釋放
            for (int i=0; i<qrsMat.length; ++i){
                qrsMat[i].release();
            }

//            if (!isThreshold && mRgba!=null && matcher[0]!=null)
//                ndk.jni_ImageMatching_test(mRgba.getNativeObjAddr(), matcher[0].getNativeObjAddr());

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

    private DebugView debugView;
    private DebugView.ViewListener viewListener = new DebugView.ViewListener() {
        @Override
        public void OnChangeThresholdView(boolean state) {
            isThreshold = state;
        }

        @Override
        public void OnChangeBalanceWhite(boolean state) {
            isBalanceWhite = state;
        }

        @Override
        public void OnChangeMinThreshold(int min) {
            minThreshold = min;
            dba.update(String.valueOf(min));
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

        // set OpenCV View
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(cameraViewListener);

        // set Surface View
        LinearLayout layout = (LinearLayout) findViewById(R.id.ll_subview);
        qrItems[0] = new QrItem(BitmapFactory.decodeResource(getResources(),
                R.drawable.opencv_logo_white), "hello qr tracker", 0, 0);
        surfaceView = new SubSurfaceView(this, surfaceListener);
        layout.addView(surfaceView);

        // set DB data
        dba = new ThresholdDBA(this);
        String dbValue = dba.query();
        if (dbValue != null){
            minThreshold = Integer.valueOf(dbValue);
        } else {
            dba.insert(String.valueOf(minThreshold));
        }

        // set Debug View
        debugView = new DebugView(this, viewListener);
        RelativeLayout llDebug = (RelativeLayout) findViewById(R.id.debug_view);
        llDebug.addView(debugView);
        debugView.setMinThreshold(minThreshold);
        debugView.setThreshold(isThreshold);
        debugView.setBalanceWhite(isBalanceWhite);

        //
        paint.setTextSize(30);         //設定字體大小
        paint.setColor(Color.BLACK);  //設定字體顏色

        //
        Bitmap mbp = BitmapFactory.decodeResource(getResources(), R.drawable.match);
        matcher[0] = new Mat(mbp.getHeight(), mbp.getWidth(), CvType.CV_8UC1, new Scalar(4));
        Utils.bitmapToMat(mbp, matcher[0]);

        Bitmap mbp2 = BitmapFactory.decodeResource(getResources(), R.drawable.match2);
        matcher[1] = new Mat(mbp2.getHeight(), mbp2.getWidth(), CvType.CV_8UC1, new Scalar(4));
        Utils.bitmapToMat(mbp2, matcher[1]);
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
        dba.closeDB();
    }
}
