//package helloopencv.peter.com.opencvqrtracker.Testing;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import org.opencv.android.Utils;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//
//import helloopencv.peter.com.opencvqrtracker.R;
//import helloopencv.peter.com.opencvqrtracker.myNDK;
//
//public class FeatureMatchingActivity extends Activity {
//
//    private String LOGTAG = "TestActivity";
//
//    private myNDK ndk = new myNDK();
//
//    private ImageView ivOrg, ivRes;
//    private Bitmap orgBp, resBp;
//
//    private Bitmap tmpBp;
//
//    private int flnnMin = 200;
//    private int flnnMax = 0;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_test);
//
//        initLayouts();
//        setBitmap();
//        featureMatch();
//
//    }
//
//    private void featureMatch() {
//        Mat objMat = new Mat(orgBp.getWidth(), orgBp.getHeight(), CvType.CV_8UC1);
//        Utils.bitmapToMat(orgBp, objMat);
//        Mat sceneMat = new Mat (resBp.getWidth(), resBp.getHeight(), CvType.CV_8UC1);
//        Utils.bitmapToMat(resBp, sceneMat);
//        Mat matchMat = new Mat(tmpBp.getWidth(), tmpBp.getHeight(), CvType.CV_8UC1);
//        Utils.bitmapToMat(tmpBp, matchMat);
//
//        boolean isMatch = ndk.jni_FeatureMatching_test(objMat.getNativeObjAddr(), sceneMat.getNativeObjAddr(), matchMat.getNativeObjAddr(), flnnMin, flnnMax, 5);
//        String matchRes = "False";
//        if (isMatch) matchRes = "True";
//        Log.d(LOGTAG, "MatchResult : " + matchRes);
//
//        Utils.matToBitmap(objMat, orgBp);
//        Utils.matToBitmap(sceneMat, resBp);
//        Utils.matToBitmap(matchMat, tmpBp);
//
//        ivOrg.setImageBitmap(orgBp);
//        ivRes.setImageBitmap(resBp);
//    }
//
//    private void setBitmap() {
//        orgBp = BitmapFactory.decodeResource(getResources(), R.drawable.input1_2);
//        resBp = BitmapFactory.decodeResource(getResources(), R.drawable.input1_1);
//        tmpBp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//
//        ivOrg.setImageBitmap(orgBp);
//    }
//
//    private void initLayouts() {
//
//        //
//        LinearLayout ll = new LinearLayout(this);
//        ll.setOrientation(LinearLayout.HORIZONTAL);
//        ll.setLayoutParams(
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        ivOrg = new ImageView(this);
//        ivOrg.setLayoutParams(
//                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
//        ll.addView(ivOrg);
//
//        ivRes = new ImageView(this);
//        ivRes.setLayoutParams(
//                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
//        ll.addView(ivRes);
//
//        //
//        FrameLayout fl = new FrameLayout(this);
//        fl.setLayoutParams(
//                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        RelativeLayout rl = new RelativeLayout(this);
//        rl.setLayoutParams(
//                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        fl.addView(rl);
//
//        //
//        RelativeLayout root = new RelativeLayout(this);
//        root.addView(ll);
//        root.addView(fl);
//
//        setContentView(root);
//    }
//
//}
