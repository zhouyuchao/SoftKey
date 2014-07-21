package com.zyc.softkey.view;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

public class FloatAnimView implements ValueAnimator.AnimatorUpdateListener {

    public final ArrayList<ShapeHolder> balls = new ArrayList<ShapeHolder>();
    AnimatorSet animation = null;
    
    private View mView;

    public FloatAnimView(Context context, View view) {
        mView = view;
    }

    private void createAnimation() {
        if (animation == null) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(mView, "y", 0f, mView.getHeight() - mView.getHeight()).setDuration(300);
            anim.addUpdateListener(this);

            animation = new AnimatorSet();
            animation.playTogether(anim);
        }
    }

    public void startAnimation() {
        createAnimation();
        animation.start();
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        mView.invalidate();
    }

}
