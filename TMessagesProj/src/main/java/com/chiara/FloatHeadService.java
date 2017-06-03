package com.chiara;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.MediaActivity;

import static java.security.AccessController.getContext;

public class FloatHeadService extends Service {
    public FloatHeadService() {
    }

    private WindowManager mWindowManager;
    private ImageView mImageFloatingView;
    private ImageView mCancelImageFloatingView;
    private LinearLayout mchatDisplayLinear;
    private boolean mIsFloatingViewAttached = false;
    private boolean open = false;
    private int startLoadFromMessageId = 0;
    private boolean scrollToTopOnResume = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();

        //if everything is fine, you should pass the bundle onClick to open the chat :)

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        mImageFloatingView = new ImageView(this);
        mImageFloatingView.setImageResource(R.mipmap.ic_heart);


        mCancelImageFloatingView = new ImageView(this);
        mCancelImageFloatingView.setImageResource(R.drawable.ic_ab_back);

        mchatDisplayLinear = new LinearLayout(this);

        final WindowManager.LayoutParams paramsLinearLayout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        Context context = getApplicationContext();
        mWindowManager.addView(mImageFloatingView,params);
        mWindowManager.addView(mCancelImageFloatingView,params);
        mWindowManager.addView(mchatDisplayLinear,paramsLinearLayout);

        mCancelImageFloatingView.setVisibility(View.GONE);
        mchatDisplayLinear.setVisibility(View.GONE);

        mIsFloatingViewAttached = true;

        /**
         * TOUCH LISTENER FOR MOTION*/
        mImageFloatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        //mCancelImageFloatingView.setVisibility(View.VISIBLE);
                        return false;
                    case MotionEvent.ACTION_UP:
                        //mCancelImageFloatingView.setVisibility(View.INVISIBLE);
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mImageFloatingView, params);
                        mCancelImageFloatingView.setVisibility(View.VISIBLE);
                        return false;
                }
                return false;
            }
        });


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //get extras and see with a toast if that is right :))
        Bundle extras = intent.getExtras();
        if (extras != null){
            final int chatId = extras.getInt("chat_id", 0);
            final int userId = extras.getInt("user_id", 0);
            final int encId = extras.getInt("enc_id", 0);
            startLoadFromMessageId = extras.getInt("message_id", 0);
            scrollToTopOnResume = extras.getBoolean("scrollToTopOnResume", false);



            mImageFloatingView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    if(!open){
                        //create Bundle
                        Bundle args = new Bundle();
                        args.putInt("chat_id",chatId);
                        args.putInt("user_id",userId);
                        args.putInt("enc_id",encId);
                        args.putInt("message_id",startLoadFromMessageId);
                        args.putBoolean("scrollToTopOnResume",scrollToTopOnResume);
                        args.putBoolean("isChatOpen",true);

                        //create Intent to class ChatChatHead.java
                        Intent intent = new Intent(getApplicationContext(),LaunchActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        intent.putExtras(args);
                        startActivity(intent);

                        open = true;

                    }else{

                        Bundle args = new Bundle();
                        args.putInt("chat_id",chatId);
                        args.putInt("user_id",userId);
                        args.putInt("enc_id",encId);
                        args.putInt("message_id",startLoadFromMessageId);
                        args.putBoolean("scrollToTopOnResume",scrollToTopOnResume);
                        args.putBoolean("isChatOpen",false);


                        Intent intent = new Intent(getApplicationContext(),LaunchActivity.class);
                        intent.putExtras(args);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        startActivity(intent);
                        open = false;
                    }
                }
            });
        }

        if(!mIsFloatingViewAttached){
            mWindowManager.addView(mImageFloatingView,mImageFloatingView.getLayoutParams());
            mWindowManager.addView(mCancelImageFloatingView,mCancelImageFloatingView.getLayoutParams());
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        removeView();
    }

    public void removeView(){
        if (mImageFloatingView != null){
            mWindowManager.removeView(mImageFloatingView);
            mWindowManager.removeView(mCancelImageFloatingView);
            mIsFloatingViewAttached = false;
        }
    }
}
