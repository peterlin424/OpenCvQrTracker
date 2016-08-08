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
import java.util.HashMap;

public class QrTrackerActivity extends Activity {

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
    private Mat[][] matcher = new Mat[5][2];
    private int maxSize = 10;
    private QrItem[] qrItems = new QrItem[maxSize];
    private int minThreshold = 131;
    private int maxThreshold = 255;
    private int whiteBalance = 5;
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
            double ratio = (double)whiteBalance / 100.f;
            Log.d(TAG, "whiteBalance ratio : " + String.valueOf(ratio));
            int c = ndk.jni_QrTracking(mRgba.getNativeObjAddr(), qrsAddr, minThreshold, maxThreshold, isThreshold, isBalanceWhite, ratio);
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

                    Mat bpMat = new Mat();
                    Utils.bitmapToMat(bitmap, bpMat);

                    boolean[] isMatch1 = {false, false};
                    isMatch1[0] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0][0].getNativeObjAddr());
                    isMatch1[1] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0][1].getNativeObjAddr());
                    if (isMatch1[0]||isMatch1[1]) code = "matcher1";

//                    boolean[] isMatch2 = {false, false};
//                    isMatch2[0] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[1][0].getNativeObjAddr());
//                    isMatch2[1] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[1][1].getNativeObjAddr());
//                    if (isMatch2[0]||isMatch2[1]) code = "matcher2";
//
//                    boolean[] isMatch3 = {false, false};
//                    isMatch3[0] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[2][0].getNativeObjAddr());
//                    isMatch3[1] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[2][1].getNativeObjAddr());
//                    if (isMatch3[0]||isMatch3[1]) code = "matcher3";

                    boolean[] isMatch4 = {false, false};
                    isMatch4[0] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[3][0].getNativeObjAddr());
                    isMatch4[1] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[3][1].getNativeObjAddr());
                    if (isMatch4[0]||isMatch4[1]) code = "matcher4";

                    boolean[] isMatch5 = {false, false};
                    isMatch5[0] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[4][0].getNativeObjAddr());
                    isMatch5[1] = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[4][1].getNativeObjAddr());
                    if (isMatch5[0]||isMatch5[1]) code = "matcher5";
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
            dba.update(String.valueOf(minThreshold), String.valueOf(whiteBalance));
        }

        @Override
        public void OnChangeWhiteBalance(int wb) {
            whiteBalance = wb;
            dba.update(String.valueOf(minThreshold), String.valueOf(whiteBalance));
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
        HashMap<String, String> dbValue = dba.query();
        if (dbValue != null){
            minThreshold = Integer.valueOf(dbValue.get(ThresholdDBA.COL_THRESHOLD));
            whiteBalance = Integer.valueOf(dbValue.get(ThresholdDBA.COL_WHITEBALANCE));
        } else {
            dba.insert(String.valueOf(minThreshold), String.valueOf(whiteBalance));
        }

        // set Debug View
        debugView = new DebugView(this, viewListener);
        RelativeLayout llDebug = (RelativeLayout) findViewById(R.id.debug_view);
        llDebug.addView(debugView);
        debugView.setMinThreshold(minThreshold);
        debugView.setWhiteBalance(whiteBalance);
        debugView.setThreshold(isThreshold);
        debugView.setBalanceWhite(isBalanceWhite);

        //
        paint.setTextSize(30);         //設定字體大小
        paint.setColor(Color.BLACK);  //設定字體顏色

        //
        Bitmap mbp1_1 = BitmapFactory.decodeResource(getResources(), R.drawable.input1_1);
        Bitmap mbp1_2 = BitmapFactory.decodeResource(getResources(), R.drawable.input1_2);
        matcher[0][0] = new Mat(mbp1_1.getHeight(), mbp1_1.getWidth(), CvType.CV_8UC1, new Scalar(4));
        matcher[0][1] = new Mat(mbp1_2.getHeight(), mbp1_2.getWidth(), CvType.CV_8UC1, new Scalar(4));
        Utils.bitmapToMat(mbp1_1, matcher[0][0]);
        Utils.bitmapToMat(mbp1_2, matcher[0][1]);

//        Bitmap mbp2_1 = BitmapFactory.decodeResource(getResources(), R.drawable.input2_1);
//        Bitmap mbp2_2 = BitmapFactory.decodeResource(getResources(), R.drawable.input2_2);
//        matcher[1][0] = new Mat(mbp2_1.getHeight(), mbp2_1.getWidth(), CvType.CV_8UC1, new Scalar(4));
//        matcher[1][1] = new Mat(mbp2_2.getHeight(), mbp2_2.getWidth(), CvType.CV_8UC1, new Scalar(4));
//        Utils.bitmapToMat(mbp2_1, matcher[1][0]);
//        Utils.bitmapToMat(mbp2_2, matcher[1][1]);
//
//        Bitmap mbp3_1 = BitmapFactory.decodeResource(getResources(), R.drawable.input3_1);
//        Bitmap mbp3_2 = BitmapFactory.decodeResource(getResources(), R.drawable.input3_2);
//        matcher[2][0] = new Mat(mbp3_1.getHeight(), mbp3_1.getWidth(), CvType.CV_8UC1, new Scalar(4));
//        matcher[2][1] = new Mat(mbp3_2.getHeight(), mbp3_2.getWidth(), CvType.CV_8UC1, new Scalar(4));
//        Utils.bitmapToMat(mbp3_1, matcher[2][0]);
//        Utils.bitmapToMat(mbp3_2, matcher[2][1]);

        Bitmap mbp4_1 = BitmapFactory.decodeResource(getResources(), R.drawable.input4_1);
        Bitmap mbp4_2 = BitmapFactory.decodeResource(getResources(), R.drawable.input4_2);
        matcher[3][0] = new Mat(mbp4_1.getHeight(), mbp4_1.getWidth(), CvType.CV_8UC1, new Scalar(4));
        matcher[3][1] = new Mat(mbp4_2.getHeight(), mbp4_2.getWidth(), CvType.CV_8UC1, new Scalar(4));
        Utils.bitmapToMat(mbp4_1, matcher[3][0]);
        Utils.bitmapToMat(mbp4_2, matcher[3][1]);

        Bitmap mbp5_1 = BitmapFactory.decodeResource(getResources(), R.drawable.input5_1);
        Bitmap mbp5_2 = BitmapFactory.decodeResource(getResources(), R.drawable.input5_2);
        matcher[4][0] = new Mat(mbp5_1.getHeight(), mbp5_1.getWidth(), CvType.CV_8UC1, new Scalar(4));
        matcher[4][1] = new Mat(mbp5_2.getHeight(), mbp5_2.getWidth(), CvType.CV_8UC1, new Scalar(4));
        Utils.bitmapToMat(mbp5_1, matcher[4][0]);
        Utils.bitmapToMat(mbp5_2, matcher[4][1]);
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
