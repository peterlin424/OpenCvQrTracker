package helloopencv.peter.com.opencvqrtracker;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by linweijie on 7/20/16.
 */
public class DrawThread extends Thread {

    private SubSurfaceView sf;
    private SurfaceHolder holder;
    private boolean flag = false;
    private int span = 1;
    private Canvas canvas;

    public DrawThread(SubSurfaceView sf, SurfaceHolder holder){
        this.sf = sf;
        this.holder = holder;
    }

    public void setRunning(boolean flag){
        this.flag = flag;
    }

    public void run(){
        while (flag){
            try {
                Thread.sleep(span);
                canvas = holder.lockCanvas();
                synchronized (holder){
                    sf.DoDraw(canvas);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (canvas != null){
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
