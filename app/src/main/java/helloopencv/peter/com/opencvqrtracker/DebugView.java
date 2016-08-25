package helloopencv.peter.com.opencvqrtracker;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by linweijie on 7/26/16.
 */
public class DebugView extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    interface ViewListener {
        void OnChangeThresholdView(boolean state);
//        void OnChangeBalanceWhite(boolean state);
        void OnChangeMinThreshold(int min);
//        void OnChangeWhiteBalance(int wb);
    }

    private ViewListener listener;

//    private TextView tvTh, tvWb;
    private TextView tvTh;
//    private Button btShow, btTh, btBw;
    private Button btShow, btTh;
//    private SeekBar sbTh, sbWb;
    private SeekBar sbTh;
    private LinearLayout llPanel;

    private int minThreshold = 0;
//    private int whiteBalance = 0;
    private boolean isThreshold = false;
//    private boolean isBalanceWhite = false;

    public DebugView(Context context, ViewListener listener) {

        super(context);

        this.listener = listener;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.debug_view, this, true);

        initViews(context);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public DebugView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context){

        llPanel = (LinearLayout) findViewById(R.id.ll_panel);
        btShow = (Button) findViewById(R.id.bt_show);

        btTh = (Button) findViewById(R.id.bt_th);
//        btBw = (Button) findViewById(R.id.bt_bw);

        tvTh = (TextView) findViewById(R.id.tv_th);
        sbTh = (SeekBar) findViewById(R.id.sb_th);
//        tvWb = (TextView) findViewById(R.id.tv_wb);
//        sbWb = (SeekBar) findViewById(R.id.sb_wb);

        btShow.setOnClickListener(this);
        btTh.setOnClickListener(this);
//        btBw.setOnClickListener(this);

        sbTh.setOnSeekBarChangeListener(this);
//        sbWb.setOnSeekBarChangeListener(this);

        llPanel.setVisibility(INVISIBLE);
    }

    public void setPanelColor(String color){
        llPanel.setBackgroundColor(Color.parseColor(color));
    }
    public void setTextColor(String color){
        tvTh.setTextColor(Color.parseColor(color));
//        tvWb.setTextColor(Color.parseColor(color));
    }
    public void setMinThreshold(int min){
        minThreshold = min;
        sbTh.setProgress(min);
    }
//    public void setWhiteBalance(int wb){
//        whiteBalance = wb;
//        sbWb.setProgress(wb);
//    }
    public void setThreshold(boolean state){
        isThreshold = state;
    }
//    public void setBalanceWhite(boolean state){
//        isBalanceWhite = state;
//    }

    /**
     * OnClickListener
     * */
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.bt_show:

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llPanel.getLayoutParams();
                if (llPanel.getVisibility() == VISIBLE){
                    params.height = 0;
                    llPanel.setLayoutParams(params);
                    llPanel.setVisibility(INVISIBLE);
                } else {
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    llPanel.setLayoutParams(params);
                    llPanel.setVisibility(VISIBLE);
                }
                break;

            case R.id.bt_th:

                if (isThreshold)
                    isThreshold = false;
                else
                    isThreshold = true;

                listener.OnChangeThresholdView(isThreshold);

                break;

//            case R.id.bt_bw:
//
//                if (isBalanceWhite)
//                    isBalanceWhite = false;
//                else
//                    isBalanceWhite = true;
//
//                listener.OnChangeBalanceWhite(isBalanceWhite);
//
//                break;
        }
    }

    /**
     * OnSeekBarChangeListener
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()){
            case R.id.sb_th:
                minThreshold = i;
                tvTh.setText(String.valueOf(i));
                listener.OnChangeMinThreshold(i);
                break;
//            case R.id.sb_wb:
//                whiteBalance = i;
//                tvWb.setText(String.valueOf(i));
//                listener.OnChangeWhiteBalance(i);
//                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
