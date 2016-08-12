package helloopencv.peter.com.opencvqrtracker;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by linweijie on 8/12/16.
 */
public class PatternMatchItem {

    public double minValve = 0;
    public Bitmap tempBitmap = null;

    public Mat mat;
    public long addr = 0;

    public PatternMatchItem(Bitmap bitmap, double valve) {
        tempBitmap = bitmap;
        minValve = valve;

        mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1, new Scalar(4));
        Utils.bitmapToMat(bitmap, mat);
        addr = mat.getNativeObjAddr();
    }

    public boolean matching(double in){
        if (in<=minValve)
            return true;
        return false;
    }
}
