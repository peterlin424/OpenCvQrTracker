package helloopencv.peter.com.opencvqrtracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by linweijie on 7/20/16.
 */
public class SubSurfaceView extends SurfaceView implements SurfaceHolder.Callback  {

    //呼叫getHolder()方法來取得 SurfaceHolder,並指給 holder
    SurfaceHolder holder = getHolder();
    Bitmap bitmap;
    Canvas canvas;
    int x=0,y=0;  //貼圖在螢幕上的 x,y 座標
    DrawThread drawThread;

    public SubSurfaceView(Context context) {
        super(context);

        //把這個 class 本身(extends SurfaceView)
        //透過 holder 的 Callback()方法連結起來
        //下面這行也可寫成 getHolder().addCallback(this);
        holder.addCallback(this);
    }
    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        //在 canvas 畫布上貼圖的三個步驟

        if (bitmap!=null){
            //1. 鎖住畫布
            canvas = holder.lockCanvas();
            //2. 在畫布上貼圖
            canvas.drawBitmap(bitmap,x,y,null);
            //3. 解鎖並po出畫布
            holder.unlockCanvasAndPost(canvas);
        }

        //
        drawThread = new DrawThread(this, holder);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

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
        if (bitmap!=null)
            c.drawBitmap(bitmap,x,y,null);
        c.restore();
    }
}
