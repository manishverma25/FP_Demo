package com.telpo.tps900_demo.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.annotation.StringRes;

import com.telpo.tps900_demo.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yemiekai on 2016/12/9 0009.
 */

public class TelpoProgressDialog extends Dialog {
    private TextView tv_title;
    private TextView tv_text;
    private ImageView imageView;
    private Animation operatingAnim;
    private Context mContext;
    private long mTimeOut = 0;//默认timeOut为0即无限大
    private OnTimeOutListener mTimeOutListener = null;
    private Timer mTimer = null;// 定时器

    public interface OnTimeOutListener {
        void onTimeOut();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(mTimeOutListener != null){
                mTimeOutListener.onTimeOut();
                dismiss();
            }
        }
    };

    public TelpoProgressDialog(Context context) {
        this(context, R.style.TelpoProgressDialog,null,null);
    }

    public TelpoProgressDialog(Context context, CharSequence title, CharSequence text) {
        this(context, R.style.TelpoProgressDialog,null,null);
    }

    public TelpoProgressDialog(Context context, long time, OnTimeOutListener listener) {
        this(context, R.style.TelpoProgressDialog,null,null);
        mTimeOut = time;
        if (listener != null) {
            mTimeOutListener = listener;
        }
    }

    private TelpoProgressDialog(Context context, int theme, CharSequence title, CharSequence text) {
        super(context, theme);
        this.setContentView(R.layout.telpo_progress_dialog);
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
        this.setCancelable(false);
        mContext = context;

        //找到组件
        tv_title = (TextView) this.findViewById(R.id.telpoProgress_title);
        tv_text = (TextView) this.findViewById(R.id.telpoProgress_text);
        imageView = (ImageView) this.findViewById(R.id.telpoProgress_image);

        if(title!=null){
            tv_title.setText(title);
        }

        if(text!=null){
            tv_text.setText(text);
        }

        //动画属性
        operatingAnim = AnimationUtils.loadAnimation(context, R.anim.progress);
        LinearInterpolator lin = new LinearInterpolator();//匀速旋转
        operatingAnim.setInterpolator(lin);
    }


    @Override
    public void onStart() {
//        Log.d("kaiye","---TelpoProgressDialog--- onStart");
        super.onStart();
        //开始动画
        imageView.startAnimation(operatingAnim);

        //开始计时
        if (mTimeOut != 0) {
            mTimer = new Timer();
            TimerTask timerTast = new TimerTask() {
                @Override
                public void run() {
                    Message msg = mHandler.obtainMessage();
                    mHandler.sendMessage(msg);
                }
            };
            mTimer.schedule(timerTast, mTimeOut);
        }
    }

    @Override
    protected void onStop() {
//        Log.d("kaiye","---TelpoProgressDialog--- onStop");
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if(title == null){
            title = "";
        }
        tv_title.setText(title);
    }
    @Override
    public void setTitle(@StringRes int titleId) {
        tv_title.setText(mContext.getText(titleId));
    }

    public void setMessage(CharSequence text) {
        if(text == null){
            text = "";
        }
        tv_text.setText(text);
    }

    public void setMessage(@StringRes int titleId) {
        tv_text.setText(mContext.getText(titleId));
    }

    public void setTimeOut(long t, OnTimeOutListener timeOutListener) {
        mTimeOut = t;
        if (timeOutListener != null) {
            this.mTimeOutListener = timeOutListener;
        }
    }
}
