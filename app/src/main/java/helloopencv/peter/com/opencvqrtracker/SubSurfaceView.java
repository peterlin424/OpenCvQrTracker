package helloopencv.peter.com.opencvqrtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by linweijie on 7/20/16.
 */
public class SubSurfaceView extends SurfaceView implements SurfaceHolder.Callback  {

    //呼叫getHolder()方法來取得 SurfaceHolder,並指給 holder
    private SurfaceHolder holder = getHolder();
    private DrawThread drawThread;
    private SurfaceListener listener;

    interface SurfaceListener{
        void drawing(Canvas canvas);
    }
    public SubSurfaceView(Context context, SurfaceListener listener) {
        super(context);

        //把這個 class 本身(extends SurfaceView)
        //透過 holder 的 Callback()方法連結起來
        //下面這行也可寫成 getHolder().addCallback(this);
        holder.addCallback(this);
        this.listener = listener;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        drawThread = new DrawThread(this, holder);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        boolean retry = true;
        drawThread.setRunning(false);
        while (retry){
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void DoDraw(Canvas c){
        c.save();
        listener.drawing(c);
        c.restore();
    }
}
