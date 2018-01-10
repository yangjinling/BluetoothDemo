package cw.com.animator;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView contact;
    private ImageView nocontact;
    private ImageView finger;
    private ImageView magnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contact = ((ImageView) findViewById(R.id.iv_contact));
        nocontact = ((ImageView) findViewById(R.id.iv_nocontact));
        finger = ((ImageView) findViewById(R.id.iv_finger));
        magnetic = ((ImageView) findViewById(R.id.iv_magnetic));
        displayWholeAnimation(0); //非接触
        displayWholeAnimation(1);//接触
        displayWholeAnimation(2);//指纹
        displayWholeAnimation(3);

       /* animation.setDuration(2000);//设置动画持续时间
        animation.setRepeatCount(Integer.MAX_VALUE);//设置重复次数
        contact.setAnimation(animation);
        animation.startNow();*/

    }

    private void startAppearanceAnimation(int type) {
        /**
         * 核心类 AnimationSet 顾名思义，可以简单理解为将多种动画放在一个set集合里面
         *    产生渐渐显示+位移动画，将加速小火箭渐渐显示出来;
         *
         */
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        TranslateAnimation translateAnimation = null;
        if (type == 0) {
            translateAnimation = new TranslateAnimation(0, 200, 0, 0);

        } else if (type == 1) {
            translateAnimation = new TranslateAnimation(0, 0, 0, -200);
        } else if (type == 2) {
            translateAnimation = new TranslateAnimation(0, -300, 0, 0);
        } else if (type == 3) {
            translateAnimation = new TranslateAnimation(0, 0, 0, 500);
        }
        translateAnimation.setRepeatCount(Integer.MAX_VALUE);
        alphaAnimation.setRepeatCount(Integer.MAX_VALUE);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillAfter(true);
        animationSet.setDuration(2000);
        animationSet.setRepeatMode(Integer.MAX_VALUE);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        if (type == 0) {
            contact.startAnimation(animationSet);
        } else if (type == 1) {
            nocontact.startAnimation(animationSet);
        } else if (type == 2) {
            finger.startAnimation(animationSet);
        } else if (type == 3) {
            magnetic.startAnimation(animationSet);
        }
    }

    private void startDisappearanceAnimation(int type) {
        TranslateAnimation translateAnimation = null;
        if (type == 0) {
            translateAnimation = new TranslateAnimation(0, 200, 0, 0);
        } else if (type == 1) {
            translateAnimation = new TranslateAnimation(0, 0, 0, -200);
        } else if (type == 2) {
            translateAnimation = new TranslateAnimation(0, -200, 0, 0);
        } else if (type == 3) {
            translateAnimation = new TranslateAnimation(0, 0, 0, 500);
        }
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillAfter(true);
        translateAnimation.setRepeatCount(Integer.MAX_VALUE);
        alphaAnimation.setRepeatCount(Integer.MAX_VALUE);
        animationSet.setDuration(2000);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        if (type == 0) {
            contact.startAnimation(animationSet);
        } else if (type == 1) {
            nocontact.startAnimation(animationSet);
        } else if (type == 2) {
            finger.startAnimation(animationSet);
        } else if (type == 3) {
            magnetic.startAnimation(animationSet);
        }
    }

    private void displayWholeAnimation(final int type) {
        startAppearanceAnimation(type);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startDisappearanceAnimation(type);
            }
        }, 1000);
    }
}
