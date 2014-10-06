package com.aniblitz;

import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationManager {

    public static void Shake(View view)
    {
        if(view != null)
        {
            Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake);
            view.startAnimation(shake);
        }
    }

    public static void FragmentSlideTransition(FragmentTransaction ft)
    {
        if(ft != null)
            ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
    }
}