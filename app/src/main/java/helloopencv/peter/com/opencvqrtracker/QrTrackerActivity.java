package helloopencv.peter.com.opencvqrtracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

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
    private PatternMatchItem[][] matcher = new PatternMatchItem[8][4];
    private int maxSize = 10;
    private QrItem[] qrItems = new QrItem[maxSize];
    private int minThreshold = 131;
    private int maxThreshold = 255;
//    private int whiteBalance = 5;
    private boolean isThreshold = false;
//    private boolean isBalanceWhite = false;

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
//            double ratio = (double)whiteBalance / 100.f;
//            Log.d(TAG, "whiteBalance ratio : " + String.valueOf(ratio));
//            int c = ndk.jni_QrTracking(mRgba.getNativeObjAddr(), qrsAddr, minThreshold, maxThreshold, isThreshold, isBalanceWhite, ratio);
            int c = ndk.jni_QrTracking(mRgba.getNativeObjAddr(), qrsAddr, minThreshold, maxThreshold, isThreshold);
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

                    //1
                    if (matcher[0][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0][0].addr)))
                        code = "TOOL-001";
                    else if (matcher[0][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0][1].addr)))
                        code = "TOOL-001";
                    else if (matcher[0][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0][2].addr)))
                        code = "TOOL-001";
                    else if (matcher[0][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[0][3].addr)))
                        code = "TOOL-001";
                    //2
                    else if (matcher[1][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[1][0].addr)))
                        code = "TOOL-002";
                    else if (matcher[1][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[1][1].addr)))
                        code = "TOOL-002";
                    else if (matcher[1][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[1][2].addr)))
                        code = "TOOL-002";
                    else if (matcher[1][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[1][3].addr)))
                        code = "TOOL-002";
                    //3
                    else if (matcher[2][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[2][0].addr)))
                        code = "TOOL-003";
                    else if (matcher[2][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[2][1].addr)))
                        code = "TOOL-003";
                    else if (matcher[2][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[2][2].addr)))
                        code = "TOOL-003";
                    else if (matcher[2][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[2][3].addr)))
                        code = "TOOL-003";
                    //4
                    else if (matcher[3][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[3][0].addr)))
                        code = "TOOL-004";
                    else if (matcher[3][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[3][1].addr)))
                        code = "TOOL-004";
                    else if (matcher[3][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[3][2].addr)))
                        code = "TOOL-004";
                    else if (matcher[3][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[3][3].addr)))
                        code = "TOOL-004";
                    //5
                    else if (matcher[4][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[4][0].addr)))
                        code = "3L FCCL";
                    else if (matcher[4][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[4][1].addr)))
                        code = "3L FCCL";
                    else if (matcher[4][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[4][2].addr)))
                        code = "3L FCCL";
                    else if (matcher[4][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[4][3].addr)))
                        code = "3L FCCL";
                        //6
                    else if (matcher[5][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[5][0].addr)))
                        code = "2L FCCL";
                    else if (matcher[5][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[5][1].addr)))
                        code = "2L FCCL";
                    else if (matcher[5][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[5][2].addr)))
                        code = "2L FCCL";
                    else if (matcher[5][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[5][3].addr)))
                        code = "2L FCCL";
                        //7
                    else if (matcher[6][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[6][0].addr)))
                        code = "Coverlay";
                    else if (matcher[6][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[6][1].addr)))
                        code = "Coverlay";
                    else if (matcher[6][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[6][2].addr)))
                        code = "Coverlay";
                    else if (matcher[6][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[6][3].addr)))
                        code = "Coverlay";
                        //8
                    else if (matcher[7][0].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[7][0].addr)))
                        code = "Composited PI Sheet";
                    else if (matcher[7][1].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[7][1].addr)))
                        code = "Composited PI Sheet";
                    else if (matcher[7][2].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[7][2].addr)))
                        code = "Composited PI Sheet";
                    else if (matcher[7][3].matching(ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[7][3].addr)))
                        code = "Composited PI Sheet";

//                    double min = ndk.jni_ImageMatching(bpMat.getNativeObjAddr(), matcher[7][0].addr);
//                    code = String.valueOf(min);
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
                if (temp != null && temp.info != "") {
                    canvas.drawBitmap(temp.bitmap, temp.imgPos[0], temp.imgPos[1], paint);
                    canvas.drawText(temp.info, temp.infoPos[0], temp.infoPos[1], paint);
                }
                i++;
            }
        }

        @Override
        public void touch(int x, int y) {
            // TODO show info dialog
            Log.d(TAG, "touch pos x : " + String.valueOf(x) + ", " + "pos y : " + String.valueOf(y));
            int i = 0;
            for (QrItem temp : qrItems) {
                if (temp != null && temp.info != "") {

                    int areaYmin = (int)temp.imgPos[1];
                    int areaYmax = (int)temp.imgPos[1] + 85;

                    if (y<areaYmin || y>=areaYmax)
                        continue;

                    switch (temp.info){
                        case "3L FCCL":
                            showInfoDialog(R.drawable.info_01);
                            break;
                        case "2L FCCL":
                            showInfoDialog(R.drawable.info_02);
                            break;
                        case "Coverlay":
                            showInfoDialog(R.drawable.info_04);
                            break;
                        case "Composited PI Sheet":
                            showInfoDialog(R.drawable.info_03);
                            break;
                    }
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

//        @Override
//        public void OnChangeBalanceWhite(boolean state) {
//            isBalanceWhite = state;
//        }

        @Override
        public void OnChangeMinThreshold(int min) {
            minThreshold = min;
//            dba.update(String.valueOf(minThreshold), String.valueOf(whiteBalance));
            dba.update(String.valueOf(minThreshold), String.valueOf(0));
        }

//        @Override
//        public void OnChangeWhiteBalance(int wb) {
//            whiteBalance = wb;
//            dba.update(String.valueOf(minThreshold), String.valueOf(whiteBalance));
//        }
    };

    private RelativeLayout rlInfoDialog;
    private ImageButton btClose;
    private ImageView ivInfoImage;

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
                R.mipmap.ic_launcher), "hello qr tracker", 0, 0);
        surfaceView = new SubSurfaceView(this, surfaceListener);
        layout.addView(surfaceView);

        // set DB data
        dba = new ThresholdDBA(this);
        HashMap<String, String> dbValue = dba.query();
        if (dbValue != null){
            minThreshold = Integer.valueOf(dbValue.get(ThresholdDBA.COL_THRESHOLD));
//            whiteBalance = Integer.valueOf(dbValue.get(ThresholdDBA.COL_WHITEBALANCE));
        } else {
//            dba.insert(String.valueOf(minThreshold), String.valueOf(whiteBalance));
            dba.insert(String.valueOf(minThreshold), String.valueOf(0));
        }

        // set Debug View
        debugView = new DebugView(this, viewListener);
        RelativeLayout llDebug = (RelativeLayout) findViewById(R.id.debug_view);
        llDebug.addView(debugView);
        debugView.setMinThreshold(minThreshold);
//        debugView.setWhiteBalance(whiteBalance);
        debugView.setThreshold(isThreshold);
//        debugView.setBalanceWhite(isBalanceWhite);

        //
        paint.setTextSize(30);         //設定字體大小
        paint.setColor(Color.BLACK);  //設定字體顏色

        // set match array
        matcher[0][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo1_1),
                0.1f);
        matcher[0][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo1_2),
                0.1f);
        matcher[0][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo1_3),
                0.1f);
        matcher[0][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo1_4),
                0.1f);
        //
        matcher[1][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo2_1),
                0.1f);
        matcher[1][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo2_2),
                0.1f);
        matcher[1][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo2_3),
                0.1f);
        matcher[1][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo2_4),
                0.1f);
        //
        matcher[2][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo3_1),
                0.1f);
        matcher[2][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo3_2),
                0.1f);
        matcher[2][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo3_3),
                0.1f);
        matcher[2][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo3_4),
                0.1f);
        //
        matcher[3][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo4_1),
                0.1f);
        matcher[3][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo4_2),
                0.1f);
        matcher[3][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo4_3),
                0.1f);
        matcher[3][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo4_4),
                0.1f);
        //
        matcher[4][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo5_1),
                0.1f);
        matcher[4][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo5_2),
                0.1f);
        matcher[4][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo5_3),
                0.1f);
        matcher[4][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo5_4),
                0.1f);

        //
        matcher[5][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo6_1),
                0.1f);
        matcher[5][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo6_2),
                0.1f);
        matcher[5][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo6_3),
                0.1f);
        matcher[5][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo6_4),
                0.1f);

        //
        matcher[6][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo7_1),
                0.1f);
        matcher[6][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo7_2),
                0.1f);
        matcher[6][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo7_3),
                0.1f);
        matcher[6][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo7_4),
                0.1f);

        //
        matcher[7][0] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo8_1),
                0.1f);
        matcher[7][1] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo8_2),
                0.1f);
        matcher[7][2] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo8_3),
                0.1f);
        matcher[7][3] = new PatternMatchItem(
                BitmapFactory.decodeResource(getResources(), R.drawable.demo8_4),
                0.1f);

        //
        rlInfoDialog = (RelativeLayout)findViewById(R.id.rl_info_dialog);
        btClose = (ImageButton)findViewById(R.id.bt_close);
        ivInfoImage = (ImageView)findViewById(R.id.iv_info_image);
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlInfoDialog.setVisibility(View.GONE);
                ivInfoImage.setImageBitmap(null);
            }
        });
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


    private void showInfoDialog(int id){
        rlInfoDialog.setVisibility(View.VISIBLE);
        ivInfoImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), id));
    }
}
