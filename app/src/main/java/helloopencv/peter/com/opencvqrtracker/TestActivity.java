package helloopencv.peter.com.opencvqrtracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class TestActivity extends Activity {

    private myNDK ndk = new myNDK();
    private ImageView ivOrg, ivRes, ivTmp;
    private TextView tvMin;
    private Bitmap orgBp, resBp, tmpBp;
    double min = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        initLayouts();
        setBitmap();

        Mat resMat = new Mat (resBp.getWidth(), resBp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(resBp, resMat);
        Mat tmpMat = new Mat (tmpBp.getWidth(), tmpBp.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(tmpBp, tmpMat);
        min = ndk.jni_ImageMatching_test(resMat.getNativeObjAddr(), tmpMat.getNativeObjAddr());
        Utils.matToBitmap(resMat, resBp);

        ivRes.setImageBitmap(resBp);
        tvMin.setText(String.valueOf(min));
    }

    private void setBitmap() {
        int id = R.drawable.simulate_input;
        orgBp = BitmapFactory.decodeResource(getResources(), id);
        resBp = BitmapFactory.decodeResource(getResources(), id);
        tmpBp = BitmapFactory.decodeResource(getResources(), R.drawable.input1_1);

        ivOrg.setImageBitmap(orgBp);
        ivTmp.setImageBitmap(tmpBp);
    }

    private void initLayouts() {

        //
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ivOrg = new ImageView(this);
        ivOrg.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        ll.addView(ivOrg);

        ivRes = new ImageView(this);
        ivRes.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        ll.addView(ivRes);

        //
        FrameLayout fl = new FrameLayout(this);
        fl.setLayoutParams(
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ivTmp = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        ivTmp.setLayoutParams(params);
        tvMin = new TextView(this);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        tvMin.setLayoutParams(params2);
        tvMin.setTextColor(Color.parseColor("#ff0000"));

        rl.addView(ivTmp);
        rl.addView(tvMin);
        fl.addView(rl);

        //
        RelativeLayout root = new RelativeLayout(this);
        root.addView(ll);
        root.addView(fl);

        setContentView(root);
    }

}
