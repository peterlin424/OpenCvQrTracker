package helloopencv.peter.com.opencvqrtracker.Testing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import helloopencv.peter.com.opencvqrtracker.R;

public class PatternMatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_match);
    }

//    private void patternMatch(){
//
//        Mat resMat = new Mat (resBp.getWidth(), resBp.getHeight(), CvType.CV_8UC1);
//        Utils.bitmapToMat(resBp, resMat);
//
//        Mat tmpMat = new Mat (tmpBp.getWidth(), tmpBp.getHeight(), CvType.CV_8UC1);
//        Utils.bitmapToMat(tmpBp, tmpMat);
//
//        min = ndk.jni_ImageMatching_test(resMat.getNativeObjAddr(), tmpMat.getNativeObjAddr());
//        Utils.matToBitmap(resMat, resBp);
//
//        ivRes.setImageBitmap(resBp);
//        tvMin.setText(String.valueOf(min));
//    }
}
