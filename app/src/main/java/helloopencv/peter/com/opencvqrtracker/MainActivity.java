package helloopencv.peter.com.opencvqrtracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ImageView root = new ImageView(this);
        setContentView(root);

        Bitmap bp = BitmapFactory.decodeResource(getResources(), R.drawable.opencv_logo_white);

        // 初始 NDK Library
        myNDK ndk = new myNDK();

        // Mat 宣告
        Mat orgMat = new Mat(bp.getWidth(), bp.getHeight(), CvType.CV_8UC1);

        // 取得 Bitmap，並且轉換為 Mat
        Utils.bitmapToMat(bp, orgMat);

        // 取得特徵點範例
//        Mat gryMat = new Mat(bp.getWidth(), bp.getHeight(), CvType.CV_8UC1);
//        Imgproc.cvtColor(orgMat, gryMat, Imgproc.COLOR_BGR2GRAY, 1);
//        Mat dstMat = new Mat(bp.getWidth(), bp.getHeight(), CvType.CV_8UC1);
//        ndk.jni_FeatureDetector(gryMat.getNativeObjAddr(), orgMat.getNativeObjAddr(), dstMat.getNativeObjAddr());
//        gryMat.release();

        // 抓取輪廓
        ndk.jni_GrayDenoisingThresholdContour(orgMat.getNativeObjAddr());

        // 轉換 Bitmap
        Bitmap result = Bitmap.createBitmap(orgMat.cols(), orgMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(orgMat, result, true);
        orgMat.release();

        root.setImageBitmap(result);
    }
}
