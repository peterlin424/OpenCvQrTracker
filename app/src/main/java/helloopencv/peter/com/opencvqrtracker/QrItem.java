package helloopencv.peter.com.opencvqrtracker;

import android.graphics.Bitmap;

/**
 * Created by linweijie on 7/21/16.
 */
public class QrItem {

    public Bitmap bitmap;
    public String info = "";
    public float[] imgPos = new float[2];
    public float[] infoPos = new float[2];

    public QrItem(Bitmap bitmap, String info, float x, float y){

        this.bitmap = bitmap;
        this.info = info;

        imgPos[0] = x;
        imgPos[1] = y;

        infoPos[0] = x+90;
        infoPos[1] = y+45;

    }
}
